#!/usr/bin/env bash
set -euo pipefail

mode="${1:-}"
asset="${2:-}"
if [[ -z "${mode}" || -z "${asset}" ]]; then
  echo "Usage: $0 <release|snapshot> <api|demo|shared|metadata>" >&2
  exit 1
fi

if [[ -z "${DOCS_STATIC_BUCKET:-}" ]]; then
  echo "Missing required environment variable: DOCS_STATIC_BUCKET" >&2
  exit 1
fi

bucket_uri="s3://${DOCS_STATIC_BUCKET}/static"

sync_subdir() {
  local rel_path="$1"
  local cache_control="$2"
  local include_only_show_errors="${3:-false}"
  local src="docs/static/${rel_path}"
  local dst="${bucket_uri}/${rel_path}"

  if [[ ! -d "${src}" ]]; then
    echo "Missing generated static asset directory: ${src}" >&2
    return 1
  fi

  local args=(
    aws s3 sync "${src}/" "${dst}/"
    --delete
    --cache-control "${cache_control}"
  )
  if [[ "${include_only_show_errors}" == "true" ]]; then
    args+=(--only-show-errors)
  fi

  "${args[@]}"
}

s3_prefix_has_objects() {
  local rel_path="$1"
  local key_count
  if ! key_count="$(aws s3api list-objects-v2 \
    --bucket "${DOCS_STATIC_BUCKET}" \
    --prefix "static/${rel_path}/" \
    --max-keys 1 \
    --query 'KeyCount' \
    --output text)"; then
    echo "Failed to list S3 prefix: ${bucket_uri}/${rel_path}/" >&2
    exit 1
  fi
  (( key_count > 0 ))
}

claim_release_asset() {
  local rel_path="$1"
  local source_sha="${SOURCE_SHA:-$(git rev-parse HEAD)}"
  local marker_key="static/_meta/release-assets/${CURRENT_VERSION}/${asset}.json"
  local marker_uri="s3://${DOCS_STATIC_BUCKET}/${marker_key}"

  if [[ ! "${source_sha}" =~ ^[0-9a-fA-F]{40}$ ]]; then
    echo "Release asset source SHA must be a full commit SHA, got: ${source_sha}" >&2
    exit 1
  fi

  if aws s3 cp "${marker_uri}" "${metadata_file}" --only-show-errors 2>/dev/null; then
    if jq -e \
      --arg version "${CURRENT_VERSION}" \
      --arg asset "${asset}" \
      --arg sha "${source_sha}" \
      '.charts_version == $version and .asset == $asset and .source_sha == $sha' \
      "${metadata_file}" >/dev/null; then
      echo "Reusing release asset claim for ${rel_path} from ${source_sha}."
      return 0
    fi

    if [[ "${REPLACE_STATIC_ASSETS:-false}" != "true" ]]; then
      echo "Refusing to overwrite release asset claimed by a different source: ${rel_path}." >&2
      exit 1
    fi
  elif s3_prefix_has_objects "${rel_path}" && [[ "${REPLACE_STATIC_ASSETS:-false}" != "true" ]]; then
    echo "Refusing to overwrite existing release asset without a provenance marker: ${rel_path}." >&2
    echo "Run Release with replace_static_assets enabled to replace it." >&2
    exit 1
  fi

  printf '{"source_sha":"%s","charts_version":"%s","asset":"%s"}\n' \
    "${source_sha}" "${CURRENT_VERSION}" "${asset}" > "${metadata_file}"
  aws s3 cp "${metadata_file}" "${marker_uri}" \
    --content-type "application/json" \
    --cache-control "no-store, max-age=0" \
    --only-show-errors
}

validate_asset() {
  case "${asset}" in
    api|demo|shared|metadata) ;;
    *)
      echo "Unsupported static asset: ${asset}" >&2
      exit 1
      ;;
  esac
}

validate_asset

published_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
metadata_file="$(mktemp)"
trap 'rm -f "${metadata_file}"' EXIT

case "${mode}" in
  release)
    if [[ -z "${CURRENT_VERSION:-}" ]]; then
      echo "Missing required environment variable for release mode: CURRENT_VERSION" >&2
      exit 1
    fi

    case "${asset}" in
      api|demo)
        rel_path="${asset}/${CURRENT_VERSION}"
        claim_release_asset "${rel_path}"
        sync_subdir "${rel_path}" "public, max-age=31536000, immutable"
        ;;
      shared)
        if [[ -d docs/static ]]; then
          aws s3 sync docs/static/ "${bucket_uri}/" --exclude "api/*" --exclude "demo/*" --exclude "playground/*"
        fi
        ;;
      metadata)
        source_sha="${SOURCE_SHA:-$(git rev-parse HEAD)}"
        printf '{"source_sha":"%s","charts_version":"%s","published_at":"%s"}\n' \
          "${source_sha}" "${CURRENT_VERSION}" "${published_at}" > "${metadata_file}"
        aws s3 cp "${metadata_file}" "${bucket_uri}/_meta/charts-release-publish.json" \
          --content-type "application/json" --cache-control "no-store, max-age=0"
        ;;
    esac
    ;;
  snapshot)
    if [[ -z "${CHARTS_VERSION:-}" ]]; then
      echo "Missing required environment variable for snapshot mode: CHARTS_VERSION" >&2
      exit 1
    fi

    cache_control_snapshot="no-store, max-age=0"
    case "${asset}" in
      api|demo)
        sync_subdir "${asset}/snapshot" "${cache_control_snapshot}" "true"
        ;;
      shared)
        if [[ -d docs/static ]]; then
          aws s3 sync docs/static/ "${bucket_uri}/" --exclude "api/*" --exclude "demo/*" --exclude "playground/*" --only-show-errors
        fi
        ;;
      metadata)
        source_sha="${SOURCE_SHA:-${GITHUB_SHA:-$(git rev-parse HEAD)}}"
        printf '{"source_sha":"%s","charts_version":"%s","published_at":"%s"}\n' \
          "${source_sha}" "${CHARTS_VERSION}" "${published_at}" > "${metadata_file}"
        aws s3 cp "${metadata_file}" "${bucket_uri}/_meta/charts-snapshot-publish.json" \
          --content-type "application/json" --cache-control "${cache_control_snapshot}" --only-show-errors
        ;;
    esac
    ;;
  *)
    echo "Unsupported mode: ${mode}" >&2
    exit 1
    ;;
esac
