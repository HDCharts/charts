#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/../.." && pwd)"
reference_dir="${repo_root}/sample/androidApp/src/screenshotTestDebug/reference/io/github/hdcharts/app/screenshot/HeroChartScreenshotTestKt"

# The screenshot plugin adds a hash of the preview settings to the file name, so match it with a glob.
find_hero_reference() {
  shopt -s nullglob
  local matches=("${reference_dir}"/HeroChartPreview_*.png)
  if [[ "${#matches[@]}" -ne 1 ]]; then
    echo "Expected exactly one hero chart reference in ${reference_dir}, found ${#matches[@]}." >&2
    return 1
  fi
  printf '%s\n' "${matches[0]}"
}

sync_hero_image() {
  local docs_dir="$1"
  local target_dir="${docs_dir}/docs-app/public"

  if [[ ! -d "${target_dir}" ]]; then
    echo "Missing charts-docs public directory: ${target_dir}" >&2
    return 1
  fi

  local reference
  reference="$(find_hero_reference)"
  cp -f "${reference}" "${target_dir}/charts-hero-chart.png"
}

run_self_test() {
  local temp_dir
  temp_dir="$(mktemp -d /tmp/charts-hero-sync.XXXXXX)"
  trap "rm -rf -- '${temp_dir}'" RETURN

  mkdir -p "${temp_dir}/docs-app/public"
  printf 'stale' > "${temp_dir}/docs-app/public/charts-hero-chart.png"
  sync_hero_image "${temp_dir}"

  cmp "$(find_hero_reference)" "${temp_dir}/docs-app/public/charts-hero-chart.png"
}

main() {
  if [[ "${1:-}" == "--self-test" ]]; then
    run_self_test
    return
  fi

  sync_hero_image "${1:?Usage: $0 <charts-docs-dir>}"
}

main "$@"
