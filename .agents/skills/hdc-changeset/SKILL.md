---
name: hdc-changeset
description: Create or update an HDCharts release note.
---

# Create Release Notes

## Workflow

1. Resolve `release_version` with the helper:

   ```bash
   bash ./.github/scripts/resolve-release-version.sh
   ```

   Use that output as the directory name.
2. Ensure these directories exist:

   ```text
   release-notes/<release_version>/changes/
   release-notes/<release_version>/migrations/
   ```
3. Create or update a release note at:

   ```text
   release-notes/<release_version>/changes/<short-kebab-summary>.md
   ```

   Pick a short, stable topic summary for the filename.
4. Use this template:

   ```markdown
   # Release Changeset

   - type: `<feature|feat|fix|refactor|docs|chore>`
   - module: `<published-module>`
   - release_note: `<plain-language sentence, maximum 12 words>`
   ```
5. Validate the word limit and report the release version and the path you
   created or updated.

## Wording

The release note is the headline shown in "What's New" on the GitHub release
page and the docs site. Write for a developer evaluating whether to upgrade.
Lead with the user-visible outcome in one sentence of 12 words or fewer.
Keep API identifiers, exceptions, and fallback paths in the matching
`migrations/` file. Plain, direct, positive — no emojis, no marketing
language, no hedging.

## Check

Confirm the release_note is one sentence, 12 words or fewer, fits the public
release highlights, and routes rename/removal detail to a matching
`migrations/` file.
