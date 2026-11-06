#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/../.." && pwd)"
source_dir="${repo_root}/docs/wiki"

sync_wiki_docs() {
  local docs_dir="$1"
  local target_dir="${docs_dir}/content/snapshot/wiki"

  if [[ ! -d "${source_dir}" ]]; then
    echo "Missing charts wiki source directory: ${source_dir}" >&2
    return 1
  fi
  if find "${source_dir}" -type l -print -quit | grep -q .; then
    echo "Wiki doc sources must not contain symbolic links." >&2
    return 1
  fi

  mkdir -p "${target_dir}"
  # README.md documents the source directory itself and is not a wiki page.
  # assets/ is owned by sync-gif-baselines.sh and must survive this sync untouched.
  # dev/ holds unversioned developer docs, synced by sync-dev-docs.sh and never frozen into a release.
  rsync --archive --delete \
    --exclude='README.md' \
    --exclude='AGENTS.md' \
    --exclude='assets/' \
    --exclude='/dev/' \
    "${source_dir}/" "${target_dir}/"
}

run_self_test() {
  local temp_dir
  temp_dir="$(mktemp -d /tmp/charts-wiki-sync.XXXXXX)"
  trap "rm -rf -- '${temp_dir}'" RETURN

  mkdir -p "${temp_dir}/content/snapshot/wiki/assets"
  printf 'stale' > "${temp_dir}/content/snapshot/wiki/stale.md"
  printf 'keep' > "${temp_dir}/content/snapshot/wiki/assets/keep.gif"

  sync_wiki_docs "${temp_dir}"

  [[ ! -e "${temp_dir}/content/snapshot/wiki/stale.md" ]]
  [[ ! -e "${temp_dir}/content/snapshot/wiki/README.md" ]]
  [[ ! -e "${temp_dir}/content/snapshot/wiki/AGENTS.md" ]]
  [[ -f "${temp_dir}/content/snapshot/wiki/assets/keep.gif" ]]
  [[ ! -e "${temp_dir}/content/snapshot/wiki/dev" ]]

  local source_file
  for source_file in "${source_dir}"/*.md; do
    [[ "$(basename "${source_file}")" == "README.md" || "$(basename "${source_file}")" == "AGENTS.md" ]] && continue
    cmp "${source_file}" "${temp_dir}/content/snapshot/wiki/$(basename "${source_file}")"
  done

  # Guard the public chart-page convention as part of the existing sync
  # self-test. This checks structure, not how examples or capabilities read.
  local chart_file
  for chart_file in "${source_dir}"/*-chart.md; do
    awk '
      /^## / {
        if ($0 == "## Setup") {
          setup_count++
          setup_line = NR
          in_setup = 1
        } else {
          if (in_setup && $0 != "## API Reference") invalid = 1
          in_setup = 0
          if ($0 == "## API Reference") {
            api_count++
            api_line = NR
          }
        }
      }
      /^implementation\("io.github.hdcharts:[a-z0-9-]+:\{\{artifactVersion\}\}"\)$/ {
        dependency_count++
        if (!in_setup) invalid = 1
      }
      END {
        if (setup_count != 1 || api_count != 1 || dependency_count != 1 ||
            setup_line >= api_line || invalid) exit 1
      }
    ' "${chart_file}" || {
      echo "Chart wiki Setup convention failed: ${chart_file}" >&2
      return 1
    }
  done
}

main() {
  if [[ "${1:-}" == "--self-test" ]]; then
    run_self_test
    return
  fi

  sync_wiki_docs "${1:?Usage: $0 <charts-docs-dir>}"
}

main "$@"
