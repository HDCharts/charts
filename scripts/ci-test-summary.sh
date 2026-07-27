#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATES_DIR="${SCRIPT_DIR}/templates"

SUMMARY_FILE="${CI_TEST_SUMMARY_FILE:-ci-test-summary.md}"
SUMMARY_JSON_FILE="${CI_TEST_SUMMARY_JSON_FILE:-ci-test-summary.json}"
SUMMARY_TEMPLATE_MD="${CI_TEST_SUMMARY_TEMPLATE_MD:-${TEMPLATES_DIR}/ci-test-summary.md.tpl}"
SUMMARY_TEMPLATE_JSON="${CI_TEST_SUMMARY_TEMPLATE_JSON:-${TEMPLATES_DIR}/ci-test-summary.json.tpl}"

FORCE_ZERO="${CI_TEST_SUMMARY_FORCE_ZERO:-false}"
SKIP_STEP_SUMMARY="${CI_TEST_SUMMARY_SKIP_STEP_SUMMARY:-false}"
GRADLE_TEST_OUTCOME="${CI_GRADLE_TEST_OUTCOME:-}"
SHOULD_RUN_TESTS="${CI_SHOULD_RUN:-true}"

CHARTS_RESULT_DIRS=(
  charts/build/test-results/jvmTest
  charts-core/build/test-results/jvmTest
  charts-line/build/test-results/jvmTest
  charts-pie/build/test-results/jvmTest
  charts-bar/build/test-results/jvmTest
  charts-stacked-bar/build/test-results/jvmTest
  charts-stacked-area/build/test-results/jvmTest
  charts-radar/build/test-results/jvmTest
)
ANDROID_SCREENSHOT_RESULT_DIRS=(androidApp/build/test-results/validateDebugScreenshotTest)

# Parse JUnit XML and return: tests failures errors skipped
# If an aggregate <testsuites ...> node is present, use it to avoid double-counting nested suites.
sum_junit_xml_file() {
  local file="$1"
  awk '
    function attr(name, s,    r, has) {
      r = name "=\"[0-9]+\""
      has = match(s, r)
      if (has) {
        sub(".*" name "=\"", "", s)
        sub("\".*", "", s)
        return s + 0
      }
      return 0
    }
    function skipped_like(s,    skipped_val, disabled_val) {
      skipped_val = attr("skipped", s)
      disabled_val = attr("disabled", s)
      return skipped_val + disabled_val
    }

    /<testsuites([[:space:]]|>)/ && !aggregate_seen {
      aggregate_seen = 1
      aggregate_tests = attr("tests", $0)
      aggregate_failures = attr("failures", $0)
      aggregate_errors = attr("errors", $0)
      aggregate_skipped = skipped_like($0)
    }

    /<testsuite([[:space:]]|>)/ {
      suite_tests += attr("tests", $0)
      suite_failures += attr("failures", $0)
      suite_errors += attr("errors", $0)
      suite_skipped += skipped_like($0)
    }

    END {
      if (aggregate_seen) {
        printf "%d %d %d %d\n", aggregate_tests + 0, aggregate_failures + 0, aggregate_errors + 0, aggregate_skipped + 0
      } else {
        printf "%d %d %d %d\n", suite_tests + 0, suite_failures + 0, suite_errors + 0, suite_skipped + 0
      }
    }
  ' "$file"
}

collect_counts_for_dirs() {
  local tests=0 failures=0 errors=0 skipped=0
  local dir file
  local t f e s

  for dir in "$@"; do
    [[ -d "$dir" ]] || continue

    while IFS= read -r -d '' file; do
      if ! read -r t f e s < <(sum_junit_xml_file "$file" 2>/dev/null); then
        continue
      fi
      tests=$((tests + t))
      failures=$((failures + f))
      errors=$((errors + e))
      skipped=$((skipped + s))
    done < <(find "$dir" -type f -name 'TEST-*.xml' -print0)
  done

  printf '%s %s %s %s\n' "$tests" "$failures" "$errors" "$skipped"
}

collect_suite_counts() {
  if [[ "$FORCE_ZERO" == "true" ]]; then
    printf '0 0 0 0\n'
    return
  fi
  collect_counts_for_dirs "$@"
}

status_text() {
  local tests="$1" failures="$2" errors="$3" skipped="$4"
  local broken=$((failures + errors))

  if [[ "$SHOULD_RUN_TESTS" != "true" ]]; then
    printf '⚪ Skipped'
  elif ((broken > 0)); then
    printf '❌ Failed'
  elif ((tests == 0)); then
    printf '⚪ No results'
  elif ((skipped > 0)); then
    printf '⚠️ Passed'
  else
    printf '✅ Passed'
  fi
}

table_row() {
  local label="$1" tests="$2" failures="$3" errors="$4" skipped="$5"
  local status
  status="$(status_text "$tests" "$failures" "$errors" "$skipped")"

  printf '| %s | %s | %s | %s | %s | %s |' \
    "$label" "$status" "$tests" "$failures" "$errors" "$skipped"
}

gradle_step_broken() {
  if [[ "$SHOULD_RUN_TESTS" != "true" ]]; then
    printf '0\n'
    return
  fi

  case "$GRADLE_TEST_OUTCOME" in
    failure|cancelled|timed_out|action_required)
      printf '1\n'
      ;;
    *)
      printf '0\n'
      ;;
  esac
}

total_status_text() {
  local total_tests="$1" total_failures="$2" total_errors="$3" total_skipped="$4" gradle_broken="$5"
  local total_broken=$((total_failures + total_errors))

  if [[ "$SHOULD_RUN_TESTS" != "true" ]]; then
    printf '⚪ Skipped'
  elif ((gradle_broken == 1)); then
    printf '❌ Incomplete'
  elif ((total_broken > 0)); then
    printf '❌ Failed'
  elif ((total_tests == 0)); then
    printf '⚪ No results'
  elif ((total_skipped > 0)); then
    printf '⚠️ Passed'
  else
    printf '✅ Passed'
  fi
}

total_note_text() {
  local gradle_broken="$1"

  if [[ "$SHOULD_RUN_TESTS" != "true" ]]; then
    printf '> Tests were skipped for docs-only changes.'
  elif ((gradle_broken == 1)); then
    printf '> Test workflow failed before complete results were produced.'
  fi
}

render_template() {
  local template_path="$1" output_path="$2"
  shift 2

  [[ -f "$template_path" ]] || {
    echo "Missing template file: $template_path" >&2
    return 1
  }

  local content
  content="$(cat "$template_path")"

  while (($# > 0)); do
    local key="$1" value="$2"
    shift 2

    if [[ ! "$key" =~ ^[a-zA-Z0-9_]+$ ]]; then
      echo "Invalid template key: $key" >&2
      return 1
    fi
    if [[ "$value" == *$'\n'* ]]; then
      echo "Template value for ${key} must be single-line." >&2
      return 1
    fi
    if [[ "$value" == *'{{'* || "$value" == *'}}'* ]]; then
      echo "Template value for ${key} contains template delimiters." >&2
      return 1
    fi

    content="${content//\{\{${key}\}\}/${value}}"
  done

  printf '%s\n' "$content" > "$output_path"
}

main() {
  local charts_tests charts_failures charts_errors charts_skipped
  local android_tests android_failures android_errors android_skipped

  read -r charts_tests charts_failures charts_errors charts_skipped < <(collect_suite_counts "${CHARTS_RESULT_DIRS[@]}")
  read -r android_tests android_failures android_errors android_skipped < <(collect_suite_counts "${ANDROID_SCREENSHOT_RESULT_DIRS[@]}")

  local gradle_broken total_tests total_failures total_errors total_skipped total_status total_note
  total_tests=$((charts_tests + android_tests))
  total_failures=$((charts_failures + android_failures))
  total_errors=$((charts_errors + android_errors))
  total_skipped=$((charts_skipped + android_skipped))
  gradle_broken="$(gradle_step_broken)"
  total_status="$(total_status_text "$total_tests" "$total_failures" "$total_errors" "$total_skipped" "$gradle_broken")"
  total_note="$(total_note_text "$gradle_broken")"

  local charts_row android_row
  charts_row="$(table_row "Charts JVM" "$charts_tests" "$charts_failures" "$charts_errors" "$charts_skipped")"
  android_row="$(table_row "Android screenshots" "$android_tests" "$android_failures" "$android_errors" "$android_skipped")"

  render_template \
    "$SUMMARY_TEMPLATE_MD" \
    "$SUMMARY_FILE" \
    summary_status "$total_status" \
    charts_row "$charts_row" \
    android_screenshot_row "$android_row" \
    total_tests "$total_tests" \
    total_failures "$((total_failures + gradle_broken))" \
    total_errors "$total_errors" \
    total_skipped "$total_skipped" \
    total_status "$total_status" \
    total_note "$total_note"

  render_template \
    "$SUMMARY_TEMPLATE_JSON" \
    "$SUMMARY_JSON_FILE" \
    charts_tests "$charts_tests" \
    charts_failures "$charts_failures" \
    charts_errors "$charts_errors" \
    android_screenshot_tests "$android_tests" \
    android_screenshot_failures "$android_failures" \
    android_screenshot_errors "$android_errors" \
    total_tests "$total_tests" \
    total_failures "$((total_failures + gradle_broken))" \
    total_errors "$total_errors"

  if [[ "$SKIP_STEP_SUMMARY" != "true" && -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
    cat "$SUMMARY_FILE" >> "$GITHUB_STEP_SUMMARY"
  fi

  cat "$SUMMARY_FILE"
}

main "$@"
