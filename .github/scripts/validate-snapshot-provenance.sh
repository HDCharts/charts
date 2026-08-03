#!/usr/bin/env bash
set -euo pipefail

validate_snapshot_provenance() {
  local manifest_path="${1:?Usage: $0 <snapshot-manifest> <release-version> <source-sha>}"
  local release_version="${2:?Usage: $0 <snapshot-manifest> <release-version> <source-sha>}"
  local source_sha="${3:?Usage: $0 <snapshot-manifest> <release-version> <source-sha>}"
  local expected_snapshot_version="${release_version}-SNAPSHOT"

  if [[ ! -f "${manifest_path}" ]]; then
    echo "Missing snapshot provenance manifest: ${manifest_path}" >&2
    echo "Run Snapshot Release from the exact release source before releasing." >&2
    return 1
  fi

  if ! jq -e \
    --arg expected_version "${expected_snapshot_version}" \
    --arg expected_sha "${source_sha}" \
    '.charts_version == $expected_version and .source_sha == $expected_sha' \
    "${manifest_path}" >/dev/null; then
    echo "Snapshot provenance does not match the release source." >&2
    echo "Expected snapshot version: ${expected_snapshot_version}" >&2
    echo "Expected source SHA: ${source_sha}" >&2
    echo "Manifest: ${manifest_path}" >&2
    jq . "${manifest_path}" >&2 || true
    return 1
  fi
}

run_self_test() {
  local temp_dir
  local manifest_path
  local source_sha="0123456789012345678901234567890123456789"

  temp_dir="$(mktemp -d /tmp/charts-snapshot-provenance.XXXXXX)"
  trap "rm -rf -- '${temp_dir}'" RETURN
  manifest_path="${temp_dir}/snapshot-manifest.json"

  printf '{"source_sha":"%s","charts_version":"2.5.0-SNAPSHOT"}\n' \
    "${source_sha}" > "${manifest_path}"
  validate_snapshot_provenance "${manifest_path}" 2.5.0 "${source_sha}" >/dev/null

  printf '{"source_sha":"%s","charts_version":"2.5.0-SNAPSHOT"}\n' \
    "fedcba9876543210fedcba9876543210fedcba98" > "${manifest_path}"
  if validate_snapshot_provenance "${manifest_path}" 2.5.0 "${source_sha}" >/dev/null 2>&1; then
    echo "FAIL: mismatched source SHA was accepted." >&2
    return 1
  fi

  printf '{"source_sha":"%s","charts_version":"2.4.0-SNAPSHOT"}\n' \
    "${source_sha}" > "${manifest_path}"
  if validate_snapshot_provenance "${manifest_path}" 2.5.0 "${source_sha}" >/dev/null 2>&1; then
    echo "FAIL: mismatched snapshot version was accepted." >&2
    return 1
  fi

  rm -f "${manifest_path}"
  if validate_snapshot_provenance "${manifest_path}" 2.5.0 "${source_sha}" >/dev/null 2>&1; then
    echo "FAIL: missing snapshot manifest was accepted." >&2
    return 1
  fi
}

main() {
  if [[ "${1:-}" == "--self-test" ]]; then
    run_self_test
    return
  fi

  validate_snapshot_provenance "$@"
}

main "$@"
