#!/usr/bin/env bash
# Owns the path rules for changes that need no build or test checks.
# .github/scripts/ci-impact.py applies the core profile to pull requests, and
# snapshot-release.yml applies the snapshot profile to commits since the last snapshot.
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
    # Pull requests that change this script must run the checks it selects.
    scripts/ci-has-code-changes.sh)
      return 1
      ;;
    gif-baselines/*|scripts/*|.github/CODEOWNERS|.github/FUNDING.yml|LICENSE|LICENSE.*|readme-assets/*)
      if [[ "$profile" == "core" ]]; then
        return 0
      fi
      return 1
      ;;
  esac

  return 1
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

assert_filter() {
  local name="$1"
  local profile="$2"
  local changed_files="$3"
  local expected="$4"

  assert_equal "$expected" "$(filter_changes "$profile" "$changed_files")" "$name"
}

run_self_test() {
  local failures=0

  assert_filter "docs-only changes" core \
    $'README.md\ndocs/README.md\n.agent/docs-update.md' \
    "" || failures=$((failures + 1))

  assert_filter "chart and sample sources" core \
    $'README.md\ncharts/src/commonMain/kotlin/Foo.kt\nsample/shared/src/commonMain/kotlin/Foo.kt' \
    $'charts/src/commonMain/kotlin/Foo.kt\nsample/shared/src/commonMain/kotlin/Foo.kt' || failures=$((failures + 1))

  assert_filter "build files, workflows and new directories" core \
    $'build.gradle.kts\n.github/workflows/test.yml\n.github/actions/example/action.yml\ncharts2/src/Main.kt' \
    $'build.gradle.kts\n.github/workflows/test.yml\n.github/actions/example/action.yml\ncharts2/src/Main.kt' \
    || failures=$((failures + 1))

  assert_filter "release and repository metadata" core \
    $'gif-baselines/bar.png\nscripts/build-release-body.sh\n.github/CODEOWNERS\n.github/FUNDING.yml\nLICENSE\nreadme-assets/hero.png\nrelease-notes/3.0.0/changes/example.md' \
    "" || failures=$((failures + 1))

  assert_filter "agent guidance and gitignore" core \
    $'.agents/skills/hdc-review/SKILL.md\n.agents/skills/hdc-pr/agents/openai.yaml\n.kilo/skills/hdc-review\n.claude/skills/hdc-review\n.gitignore' \
    "" || failures=$((failures + 1))

  assert_filter "inputs of lint, API compatibility and this script" core \
    $'.editorconfig\nAPI-COMPATIBILITY-BREAKS.txt\nscripts/ci-has-code-changes.sh' \
    $'.editorconfig\nAPI-COMPATIBILITY-BREAKS.txt\nscripts/ci-has-code-changes.sh' || failures=$((failures + 1))

  assert_filter "snapshot change filter" snapshot \
    $'.agents/skills/hdc-review/SKILL.md\n.agents/skills/hdc-pr/agents/openai.yaml\n.kilo/skills/hdc-review\n.claude/skills/hdc-review\n.gitignore\ndocs/workflows/ci.md\nREADME.md\nrelease-notes/3.0.0/changes/example.md\ngif-baselines/example.gif\nscripts/build-release-body.sh' \
    $'release-notes/3.0.0/changes/example.md\ngif-baselines/example.gif\nscripts/build-release-body.sh' \
    || failures=$((failures + 1))

  if [[ "$failures" -gt 0 ]]; then
    echo "Self-test failed: $failures case(s)." >&2
    return 1
  fi

  echo "All self-tests passed."
}

main() {
  case "${1:-}" in
    --self-test)
      run_self_test
      ;;
    --filter)
      local profile="${2:?filter profile is required}"
      local changed_files
      changed_files="$(cat)"
      filter_changes "$profile" "$changed_files"
      ;;
    *)
      echo "Usage: $0 --filter <core|snapshot> < changed-files" >&2
      echo "       $0 --self-test" >&2
      return 2
      ;;
  esac
}

main "$@"
