#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/../.." && pwd)"
baseline_dir="${repo_root}/gif-baselines"

sync_baselines() {
  local docs_dir="$1"
  local target_dir="${docs_dir}/content/snapshot/wiki/assets"

  if [[ ! -d "${baseline_dir}" ]]; then
    echo "Missing GIF baseline directory: ${baseline_dir}" >&2
    return 1
  fi
  if [[ ! -d "${docs_dir}/content/snapshot/wiki" ]]; then
    echo "Missing charts-docs snapshot wiki directory: ${docs_dir}/content/snapshot/wiki" >&2
    return 1
  fi

  mkdir -p "${target_dir}"
  shopt -s nullglob
  local baseline_files=("${baseline_dir}"/*.gif)
  if [[ "${#baseline_files[@]}" -eq 0 ]]; then
    echo "No GIF baselines found in ${baseline_dir}." >&2
    return 1
  fi

  local baseline
  for baseline in "${baseline_files[@]}"; do
    cp -f "${baseline}" "${target_dir}/$(basename "${baseline}")"
  done

  local published
  for published in "${target_dir}"/*.gif; do
    [[ -e "${published}" ]] || continue
    if [[ ! -f "${baseline_dir}/$(basename "${published}")" ]]; then
      rm -f "${published}"
    fi
  done
}

run_self_test() {
  local temp_dir
  temp_dir="$(mktemp -d /tmp/charts-gif-sync.XXXXXX)"
  trap "rm -rf -- '${temp_dir}'" RETURN

  mkdir -p "${temp_dir}/content/snapshot/wiki/assets"
  printf 'stale' > "${temp_dir}/content/snapshot/wiki/assets/stale.gif"
  sync_baselines "${temp_dir}"

  [[ ! -e "${temp_dir}/content/snapshot/wiki/assets/stale.gif" ]]
  local baseline
  for baseline in "${baseline_dir}"/*.gif; do
    cmp "${baseline}" "${temp_dir}/content/snapshot/wiki/assets/$(basename "${baseline}")"
  done
}

main() {
  if [[ "${1:-}" == "--self-test" ]]; then
    run_self_test
    return
  fi

  sync_baselines "${1:?Usage: $0 <charts-docs-dir>}"
}

main "$@"
