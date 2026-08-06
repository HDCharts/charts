#!/usr/bin/env bash
set -euo pipefail

version="${1:?Usage: $0 <release-version> [migration-base-url]}"
source_dir="release-notes/${version}"
migration_base="${2:-${version}}"

if [[ ! -d "${source_dir}" ]]; then
  exit 0
fi

shopt -s nullglob
changes=( "${source_dir}/changes/"*.md )
migrations=( "${source_dir}/migrations/"*.md )

if (( ${#changes[@]} == 0 && ${#migrations[@]} == 0 )); then
  exit 0
fi

sorted() { printf '%s\n' "$@" | sort; }

if (( ${#changes[@]} )); then
  echo "## What's New"
  echo
  while IFS= read -r f; do
    module="$(grep -m1 '^- module:' "${f}" | sed -E "s/^- module:[[:space:]]*\`([^\`]*)\`.*/\1/" || true)"
    pr="$(grep -m1 '^- pr:' "${f}" | sed -E "s#^- pr:[[:space:]]*\`([^\`]*)\`.*#\1#" || true)"
    note="$(grep -m1 '^- release_note:' "${f}" | sed -E "s/^- release_note:[[:space:]]*//; s/^[[:space:]]*\`//; s/\`[[:space:]]*$//" || true)"
    [[ -n "${note}" ]] || continue
    if [[ -n "${pr}" ]]; then
      printf -- '- %s — `%s` ([PR](%s))\n' "${note}" "${module}" "${pr}"
    else
      printf -- '- %s — `%s`\n' "${note}" "${module}"
    fi
  done < <(sorted "${changes[@]}")
  echo
fi

if (( ${#migrations[@]} )); then
  echo "## Migrations"
  echo
  echo "⚠️ Breaking changes: Review the migration guide before upgrading: https://charts.hdcode.dev/${migration_base}/wiki/migration"
  echo
fi
