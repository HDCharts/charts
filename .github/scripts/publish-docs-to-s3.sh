#!/usr/bin/env bash
set -euo pipefail

mode="${1:-}"
if [[ -z "${mode}" ]]; then
  echo "Usage: $0 <release|snapshot>" >&2
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
    return 0
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
  local ls_output
  if ! ls_output="$(aws s3 ls "${bucket_uri}/${rel_path}/" --recursive)"; then
    echo "Failed to list S3 prefix: ${bucket_uri}/${rel_path}/" >&2
    exit 1
  fi
  [[ -n "${ls_output}" ]]
}

published_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
metadata_file="$(mktemp)"
trap 'rm -f "${metadata_file}"' EXIT

case "${mode}" in
  release)
    if [[ -z "${CURRENT_VERSION:-}" ]]; then
      echo "Missing required environment variable for release mode: CURRENT_VERSION" >&2
      exit 1
    fi

    allow_overwrite_release="false"
    if [[ "${ALLOW_OVERWRITE_RELEASE_INPUT:-false}" == "true" ]]; then
      allow_overwrite_release="true"
    fi

    if [[ "${allow_overwrite_release}" != "true" ]]; then
      protected_paths=(
        "api/${CURRENT_VERSION}"
        "demo/${CURRENT_VERSION}"
      )
      existing_paths=()
      for rel_path in "${protected_paths[@]}"; do
        if s3_prefix_has_objects "${rel_path}"; then
          existing_paths+=("${rel_path}")
        fi
      done

      if (( ${#existing_paths[@]} > 0 )); then
        echo "Refusing to overwrite existing release assets for ${CURRENT_VERSION}." >&2
        echo "Existing prefixes:" >&2
        printf '  - %s\n' "${existing_paths[@]}" >&2
        echo "If this is intentional, re-run workflow_dispatch with allow_overwrite_release=true." >&2
        exit 1
      fi
    fi

    sync_subdir "api/${CURRENT_VERSION}" "public, max-age=31536000, immutable"
    sync_subdir "demo/${CURRENT_VERSION}" "public, max-age=31536000, immutable"

    # Keep non-versioned assets in sync without touching version directories.
    aws s3 sync docs/static/ "${bucket_uri}/" \
      --exclude "api/*" \
      --exclude "demo/*" \
      --exclude "playground/*"

    source_sha="${SOURCE_SHA:-$(git rev-parse HEAD)}"
    printf '{"source_sha":"%s","charts_version":"%s","published_at":"%s"}\n' \
      "${source_sha}" "${CURRENT_VERSION}" "${published_at}" > "${metadata_file}"
    aws s3 cp "${metadata_file}" "${bucket_uri}/_meta/charts-release-publish.json" \
      --content-type "application/json" \
      --cache-control "no-store, max-age=0"
    ;;
  snapshot)
    if [[ -z "${CHARTS_VERSION:-}" ]]; then
      echo "Missing required environment variable for snapshot mode: CHARTS_VERSION" >&2
      exit 1
    fi

    cache_control_snapshot="no-store, max-age=0"
    sync_subdir "api/snapshot" "${cache_control_snapshot}" "true"
    sync_subdir "demo/snapshot" "${cache_control_snapshot}" "true"

    # Keep non-versioned assets in sync without touching version directories.
    aws s3 sync docs/static/ "${bucket_uri}/" \
      --exclude "api/*" \
      --exclude "demo/*" \
      --exclude "playground/*" \
      --only-show-errors

    source_sha="${SOURCE_SHA:-${GITHUB_SHA:-$(git rev-parse HEAD)}}"
    printf '{"source_sha":"%s","charts_version":"%s","published_at":"%s"}\n' \
      "${source_sha}" "${CHARTS_VERSION}" "${published_at}" > "${metadata_file}"
    aws s3 cp "${metadata_file}" "${bucket_uri}/_meta/charts-snapshot-publish.json" \
      --content-type "application/json" \
      --cache-control "${cache_control_snapshot}" \
      --only-show-errors
    ;;
  *)
    echo "Unsupported mode: ${mode}" >&2
    exit 1
    ;;
esac
