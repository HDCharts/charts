#!/usr/bin/env bash
set -euo pipefail

is_code_change() {
  local changed_files="$1"
  local changed_file

  while IFS= read -r changed_file; do
    [[ -n "$changed_file" ]] || continue
    case "$changed_file" in
      docs/*|release-notes/*|*.md|LICENSE|LICENSE.*) ;;
      *)
        echo "true"
        return
        ;;
    esac
  done <<<"$changed_files"

  echo "false"
}

collect_changed_files() {
  local base_sha="$1"
  local head_sha="$2"
  git diff --name-only "${base_sha}...${head_sha}"
}

assert_equal() {
  local expected="$1"
  local actual="$2"
  local name="$3"

  if [[ "$expected" != "$actual" ]]; then
    echo "FAIL: $name (expected '$expected', got '$actual')" >&2
    return 1
  fi

  echo "PASS: $name"
}

run_self_test() {
  local failures=0
  local result

  result="$(is_code_change $'README.md\ndocs/README.md\n.agent/docs-update.md')"
  if ! assert_equal "false" "$result" "docs-only changes"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'README.md\ncharts/src/commonMain/kotlin/Foo.kt')"
  if ! assert_equal "true" "$result" "charts source change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'README.md\ncharts-demo-shared/src/commonMain/kotlin/Foo.kt')"
  if ! assert_equal "true" "$result" "charts-demo-shared source change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change "charts-core/src/commonMain/kotlin/Foo.kt")"
  if ! assert_equal "true" "$result" "modular chart source change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change "build.gradle.kts")"
  if ! assert_equal "true" "$result" "root gradle file"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change ".github/workflows/test.yml")"
  if ! assert_equal "true" "$result" "workflow change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change ".github/actions/example/action.yml")"
  if ! assert_equal "true" "$result" "local action change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change "scripts/ci-test-summary.sh")"
  if ! assert_equal "true" "$result" "script change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change "charts2/src/Main.kt")"
  if ! assert_equal "true" "$result" "new source directory"; then
    failures=$((failures + 1))
  fi

  if [[ "$failures" -gt 0 ]]; then
    echo "Self-test failed: $failures case(s)." >&2
    return 1
  fi

  echo "All self-tests passed."
}

main() {
  if [[ "${1:-}" == "--self-test" ]]; then
    run_self_test
    return
  fi

  local base_sha="${1:?base sha is required}"
  local head_sha="${2:?head sha is required}"
  local changed_files

  changed_files="$(collect_changed_files "$base_sha" "$head_sha")"
  is_code_change "$changed_files"
}

main "$@"
