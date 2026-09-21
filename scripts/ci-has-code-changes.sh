#!/usr/bin/env bash
set -euo pipefail

is_ignored_path() {
  local profile="$1"
  local changed_file="$2"

  case "$changed_file" in
    release-notes/*)
      if [[ "$profile" == "core" ]]; then
        return 0
      fi
      return 1
      ;;
    docs/*|.agents/*|.kilo/*|.claude/*|.gitignore|*.md)
      return 0
      ;;
    gif-baselines/*|scripts/*|API-COMPATIBILITY-BREAKS.txt|.github/CODEOWNERS|.github/FUNDING.yml|.editorconfig|LICENSE|LICENSE.*|readme-assets/*)
      if [[ "$profile" == "core" ]]; then
        return 0
      fi
      return 1
      ;;
  esac

  return 1
}

is_code_change() {
  local changed_files="$1"
  local changed_file

  while IFS= read -r changed_file; do
    [[ -n "$changed_file" ]] || continue
    if ! is_ignored_path core "$changed_file"; then
      echo "true"
      return
    fi
  done <<<"$changed_files"

  echo "false"
}

filter_changes() {
  local profile="$1"
  local changed_files="$2"
  local changed_file

  while IFS= read -r changed_file; do
    [[ -n "$changed_file" ]] || continue
    if ! is_ignored_path "$profile" "$changed_file"; then
      printf '%s\n' "$changed_file"
    fi
  done <<<"$changed_files"
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

  result="$(is_code_change $'README.md\nsample/shared/src/commonMain/kotlin/Foo.kt')"
  if ! assert_equal "true" "$result" "sample-shared source change"; then
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

  result="$(is_code_change "charts2/src/Main.kt")"
  if ! assert_equal "true" "$result" "new source directory"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'README.md\ngif-baselines/bar.png')"
  if ! assert_equal "false" "$result" "gif baseline change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'README.md\nAPI-COMPATIBILITY-BREAKS.txt')"
  if ! assert_equal "false" "$result" "api compatibility acknowledged-breaks change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'README.md\nscripts/build-release-body.sh')"
  if ! assert_equal "false" "$result" "ci/release script change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change "scripts/ci-has-code-changes.sh")"
  if ! assert_equal "false" "$result" "skip script change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'.agents/skills/hdc-review/SKILL.md\n.agents/skills/hdc-pr/agents/openai.yaml\n.kilo/skills/hdc-review\n.claude/skills/hdc-review\n.gitignore')"
  if ! assert_equal "false" "$result" "agent guidance and gitignore changes"; then
    failures=$((failures + 1))
  fi

  result="$(filter_changes snapshot $'.agents/skills/hdc-review/SKILL.md\n.agents/skills/hdc-pr/agents/openai.yaml\n.kilo/skills/hdc-review\n.claude/skills/hdc-review\n.gitignore\ndocs/workflows/ci.md\nREADME.md\nrelease-notes/3.0.0/changes/example.md\ngif-baselines/example.gif\nscripts/build-release-body.sh')"
  if ! assert_equal $'release-notes/3.0.0/changes/example.md\ngif-baselines/example.gif\nscripts/build-release-body.sh' "$result" "snapshot change filter"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change ".editorconfig")"
  if ! assert_equal "false" "$result" "editor config change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'README.md\n.github/CODEOWNERS')"
  if ! assert_equal "false" "$result" "code owners change"; then
    failures=$((failures + 1))
  fi

  result="$(is_code_change $'README.md\n.github/FUNDING.yml')"
  if ! assert_equal "false" "$result" "funding config change"; then
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

  if [[ "${1:-}" == "--filter" ]]; then
    local profile="${2:?filter profile is required}"
    local changed_files
    changed_files="$(cat)"
    filter_changes "$profile" "$changed_files"
    return
  fi

  local base_sha="${1:?base sha is required}"
  local head_sha="${2:?head sha is required}"
  local changed_files

  changed_files="$(collect_changed_files "$base_sha" "$head_sha")"
  is_code_change "$changed_files"
}

main "$@"
