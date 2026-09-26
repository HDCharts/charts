#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/../.." && pwd)"
source_dir="${repo_root}/docs/wiki/dev"

# Dev docs are unversioned and track main, so only the snapshot sync runs this, never a release.
sync_dev_docs() {
  local docs_dir="$1"
  local target_dir="${docs_dir}/content/dev"

  # The target must exist so the workflow's git add of content/dev never fails.
  mkdir -p "${target_dir}"
  if [[ ! -d "${source_dir}" ]]; then
    echo "No dev docs at ${source_dir}; leaving ${target_dir} unchanged."
    return 0
  fi
  if find "${source_dir}" -type l -print -quit | grep -q .; then
    echo "Dev doc sources must not contain symbolic links." >&2
    return 1
  fi

  rsync --archive --delete \
    --exclude='README.md' \
    "${source_dir}/" "${target_dir}/"
}

run_self_test() {
  local temp_dir
  temp_dir="$(mktemp -d /tmp/charts-dev-docs-sync.XXXXXX)"
  trap "rm -rf -- '${temp_dir}'" RETURN

  mkdir -p "${temp_dir}/content/dev"
  printf 'stale' > "${temp_dir}/content/dev/stale.md"

  sync_dev_docs "${temp_dir}"

  [[ ! -e "${temp_dir}/content/dev/stale.md" ]]

  local source_file
  while IFS= read -r source_file; do
    [[ "$(basename "${source_file}")" == "README.md" ]] && continue
    cmp "${source_file}" "${temp_dir}/content/dev/${source_file#"${source_dir}/"}"
  done < <(find "${source_dir}" -name '*.md')
}

main() {
  if [[ "${1:-}" == "--self-test" ]]; then
    run_self_test
    return
  fi

  sync_dev_docs "${1:?Usage: $0 <charts-docs-dir>}"
}

main "$@"
