#!/usr/bin/env bash
set -euo pipefail

is_gif_validation_path() {
  local changed_file="$1"

  case "$changed_file" in
    androidApp/*|app/*|charts*/src/*|charts*/build.gradle.kts|gif-baselines/*.gif|gradle/*|build.gradle.kts|settings.gradle.kts|gradle.properties|gradlew|gradlew.bat|buildSrc/*|*.gradle.kts|*.gradle|*gradle.lockfile|.github/workflows/pull-request.yml|.github/workflows/validate-gifs.yml|.github/scripts/sync-gif-baselines.sh|scripts/ci-should-run-gif-validation.sh)
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

is_gif_validation_change() {
  local changed_files="$1"
  local changed_file

  while IFS= read -r changed_file; do
    [[ -n "$changed_file" ]] || continue
    if is_gif_validation_path "$changed_file"; then
      echo "true"
      return
    fi
  done <<<"$changed_files"

  echo "false"
}

assert_equal() {
  local expected="$1"
  local actual="$2"
  local name="$3"

  if [[ "$expected" != "$actual" ]]; then
    echo "FAIL: $name (expected '$expected', got '$actual')" >&2
    return 1
  fi
}

run_self_test() {
  local failures=0
  local result

  result="$(is_gif_validation_change $'README.md\ndocs/release/charts-snapshot.md\nrelease-notes/2.4.0/summary.md')"
  if ! assert_equal "false" "$result" "docs and release-note changes"; then
    failures=$((failures + 1))
  fi

  result="$(is_gif_validation_change "charts-line/src/commonMain/kotlin/LineChart.kt")"
  if ! assert_equal "true" "$result" "chart source change"; then
    failures=$((failures + 1))
  fi

  result="$(is_gif_validation_change "androidApp/src/main/kotlin/io/github/dautovicharis/charts/app/gif/DocsGifScenarios.kt")"
  if ! assert_equal "true" "$result" "GIF scenario change"; then
    failures=$((failures + 1))
  fi

  result="$(is_gif_validation_change "gif-baselines/pie_default.gif")"
  if ! assert_equal "true" "$result" "GIF baseline change"; then
    failures=$((failures + 1))
  fi

  result="$(is_gif_validation_change "gradle/libs.versions.toml")"
  if ! assert_equal "true" "$result" "dependency catalog change"; then
    failures=$((failures + 1))
  fi

  result="$(is_gif_validation_change "iosApp/iosApp/ContentView.swift")"
  if ! assert_equal "false" "$result" "unrelated iOS-only change"; then
    failures=$((failures + 1))
  fi

  result="$(is_gif_validation_change ".github/workflows/validate-gifs.yml")"
  if ! assert_equal "true" "$result" "GIF workflow change"; then
    failures=$((failures + 1))
  fi

  if [[ "$failures" -gt 0 ]]; then
    echo "Self-test failed: $failures case(s)." >&2
    return 1
  fi
}

main() {
  if [[ "${1:-}" == "--self-test" ]]; then
    run_self_test
    return
  fi

  local base_sha="${1:?base sha is required}"
  local head_sha="${2:?head sha is required}"
  local changed_files

  changed_files="$(git diff --name-only "${base_sha}...${head_sha}")"
  is_gif_validation_change "$changed_files"
}

main "$@"
