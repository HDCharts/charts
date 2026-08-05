#!/usr/bin/env bash
set -euo pipefail

version="${1:?Usage: $0 <release-version>}"
source_dir="release-notes/${version}"

if [[ ! -d "${source_dir}" ]]; then
  echo "::error::Release notes directory not found: ${source_dir}" >&2
  exit 1
fi

shopt -s nullglob
changes=( "${source_dir}/changes/"*.md )
migrations=( "${source_dir}/migrations/"*.md )

if (( ${#changes[@]} == 0 && ${#migrations[@]} == 0 )); then
  echo "::error::No release notes found in ${source_dir} (expected changes/*.md or migrations/*.md)" >&2
  exit 1
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
  echo "**Breaking changes:** this release includes API migrations. Review the migration guide before upgrading:"
  echo
  grep -hE '^## ' "${migrations[@]}" | sed -E 's/^##[[:space:]]+//' | sort -u | while IFS= read -r m; do
    printf -- '- `%s`\n' "${m}"
  done
  echo
  echo "📖 Migration guide: https://charts.hdcode.dev/${version}/wiki/migration"
  echo
fi
