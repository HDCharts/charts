#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/../.." && pwd)"
baseline_dir="${repo_root}/sample/androidApp/src/screenshotTestDebug/reference/io/github/hdcharts/app/screenshot/WikiChartScreenshotTestKt"
declare -a screenshot_mappings=(
  'BarGradientWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-bar-gradient.png'
  'PieWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-pie-basic.png'
  'LineWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-line-basic.png'
  'HistogramWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-histogram-basic.png'
  'StackedBarWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-stacked-bar-basic.png'
  'StackedAreaWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-stacked-area-basic.png'
  'RadarWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-radar-basic.png'
  'RingGaugeWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-ring-gauge-basic.png'
  'SegmentedRingGaugeWikiPreview_Light Tablet Landscape_5e637cf0_0.png|wiki-segmented-ring-gauge-basic.png'
)

sync_screenshots() {
  local docs_dir="$1"
  local source_dir="${2:-${baseline_dir}}"
  local target_dir="${docs_dir}/content/snapshot/wiki/assets"
  if [[ ! -d "${docs_dir}/content/snapshot/wiki" ]]; then
    echo "Missing charts-docs snapshot wiki directory: ${docs_dir}/content/snapshot/wiki" >&2
    return 1
  fi

  local mapping source_name published_name
  for mapping in "${screenshot_mappings[@]}"; do
    IFS='|' read -r source_name published_name <<<"${mapping}"
    if [[ ! -f "${source_dir}/${source_name}" ]]; then
      echo "Missing approved wiki screenshot baseline: ${source_dir}/${source_name}" >&2
      return 1
    fi
  done

  mkdir -p "${target_dir}"
  for mapping in "${screenshot_mappings[@]}"; do
    IFS='|' read -r source_name published_name <<<"${mapping}"
    cp -f "${source_dir}/${source_name}" "${target_dir}/${published_name}"
  done
}

run_self_test() {
  local temp_dir
  temp_dir="$(mktemp -d /tmp/charts-wiki-screenshot-sync.XXXXXX)"
  trap 'rm -rf -- "${temp_dir}"' RETURN
  mkdir -p "${temp_dir}/content/snapshot/wiki/assets" "${temp_dir}/baselines"
  printf 'keep gif' > "${temp_dir}/content/snapshot/wiki/assets/existing.gif"
  printf 'keep unrelated png' > "${temp_dir}/content/snapshot/wiki/assets/hero.png"
  local source
  for source in "${baseline_dir}"/*.png; do
    [[ "$(basename "${source}")" == *WikiPreview* ]] || continue
    cp "${source}" "${temp_dir}/baselines/"
  done
  sync_screenshots "${temp_dir}" "${temp_dir}/baselines"
  local first_hashes second_hashes mapping source_name published_name
  first_hashes="$(cd "${temp_dir}/content/snapshot/wiki/assets" && shasum -a 256 wiki-*.png)"
  sync_screenshots "${temp_dir}" "${temp_dir}/baselines"
  second_hashes="$(cd "${temp_dir}/content/snapshot/wiki/assets" && shasum -a 256 wiki-*.png)"
  [[ "${first_hashes}" == "${second_hashes}" ]]
  [[ "$(cat "${temp_dir}/content/snapshot/wiki/assets/existing.gif")" == 'keep gif' ]]
  [[ "$(cat "${temp_dir}/content/snapshot/wiki/assets/hero.png")" == 'keep unrelated png' ]]
  [[ "$(find "${temp_dir}/content/snapshot/wiki/assets" -maxdepth 1 -type f -name 'wiki-*.png' | wc -l | tr -d ' ')" == '9' ]]
  for mapping in "${screenshot_mappings[@]}"; do
    IFS='|' read -r source_name published_name <<<"${mapping}"
    cmp "${temp_dir}/baselines/${source_name}" "${temp_dir}/content/snapshot/wiki/assets/${published_name}"
  done
  local page
  for page in \
    'bar-chart.md|wiki-bar-gradient.png' \
    'pie-chart.md|wiki-pie-basic.png' \
    'line-chart.md|wiki-line-basic.png' \
    'histogram-chart.md|wiki-histogram-basic.png' \
    'stacked-bar-chart.md|wiki-stacked-bar-basic.png' \
    'stacked-area-chart.md|wiki-stacked-area-basic.png' \
    'radar-chart.md|wiki-radar-basic.png' \
    'ring-gauge-chart.md|wiki-ring-gauge-basic.png' \
    'segmented-ring-gauge-chart.md|wiki-segmented-ring-gauge-basic.png'; do
    IFS='|' read -r page_name published_name <<<"${page}"
    grep -Fq "/content/{{version}}/wiki/assets/${published_name}" "${repo_root}/docs/wiki/${page_name}"
  done
  if sync_screenshots "${temp_dir}" "${temp_dir}/missing" 2>"${temp_dir}/error"; then
    echo 'Expected missing baseline synchronization to fail.' >&2
    return 1
  fi
  grep -q 'Missing approved wiki screenshot baseline:' "${temp_dir}/error"
}

if [[ "${1:-}" == '--self-test' ]]; then
  run_self_test
else
  sync_screenshots "${1:?Usage: $0 <charts-docs-dir>}"
fi
