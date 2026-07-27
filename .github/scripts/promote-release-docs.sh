#!/usr/bin/env bash
set -euo pipefail

docs_dir="${1:?Usage: $0 <charts-docs-dir> <release-version> <charts-sha>}"
release_version="${2:?Usage: $0 <charts-docs-dir> <release-version> <charts-sha>}"
charts_sha="${3:?Usage: $0 <charts-docs-dir> <release-version> <charts-sha>}"

if [[ ! "${release_version}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "release version must be SemVer, got: ${release_version}" >&2
  exit 1
fi
if [[ ! "${charts_sha}" =~ ^[0-9a-fA-F]{40}$ ]]; then
  echo "charts SHA must be a full commit SHA, got: ${charts_sha}" >&2
  exit 1
fi

# Release content is immutable once present, so an existing registry entry may
# only be reused when the committed manifest proves it came from this charts SHA.
if jq -e --arg version "${release_version}" \
  'any(.versions[]?; .id == $version)' "${docs_dir}/registry/versions.json" >/dev/null; then
  manifest_path="${docs_dir}/docs-app/public/release-manifest.json"
  if [[ ! -f "${manifest_path}" ]] || ! jq -e \
      --arg version "${release_version}" \
      --arg sha "${charts_sha}" \
      '.charts_version == $version and .source_sha == $sha' \
      "${manifest_path}" >/dev/null; then
    echo "Existing docs release ${release_version} has no matching charts provenance marker." >&2
    echo "Refusing to reuse immutable release content for charts SHA ${charts_sha}." >&2
    exit 1
  fi
else
  bash "${docs_dir}/scripts/promote-snapshot-to-release.sh" "${release_version}"
fi

bash .github/scripts/sync-release-notes.sh "${docs_dir}" "${release_version}"

mkdir -p "${docs_dir}/docs-app/public"
published_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
printf '{"source_sha":"%s","charts_version":"%s","published_at":"%s"}\n' \
  "${charts_sha}" "${release_version}" "${published_at}" \
  > "${docs_dir}/docs-app/public/release-manifest.json"
