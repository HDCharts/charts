#!/usr/bin/env bash
set -euo pipefail

docs_dir="${1:?Usage: $0 <charts-docs-dir> <release-version>}"
release_version="${2:?Usage: $0 <charts-docs-dir> <release-version>}"

if [[ ! "${release_version}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "release version must be SemVer, got: ${release_version}" >&2
  exit 1
fi

source_dir="release-notes/${release_version}"
target_dir="${docs_dir}/release-notes/${release_version}"
if [[ ! -d "${source_dir}" ]]; then
  if [[ "${ALLOW_MISSING_RELEASE_NOTES:-false}" == "true" ]]; then
    echo "::warning::Skipping release-note sync because ${source_dir} does not exist."
    exit 0
  fi
  echo "Missing charts release-note directory: ${source_dir}" >&2
  exit 1
fi
if find "${source_dir}" -type l -print -quit | grep -q .; then
  echo "Release-note sources must not contain symbolic links." >&2
  exit 1
fi

mkdir -p "${target_dir}"
rsync --archive --delete "${source_dir}/" "${target_dir}/"
printf '%s\n' "${release_version}" > "${docs_dir}/release-notes/current-version.txt"

bash "${docs_dir}/scripts/test-docs-release-links-contract.sh"
