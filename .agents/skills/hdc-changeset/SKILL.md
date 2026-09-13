---
name: hdc-changeset
description: Create or update a concise HDCharts release note on direct user request.
---

# Create Release Notes

## Guardrails

Follow [AGENTS.md](../../AGENTS.md). This skill edits release-note files.

Only a direct user request loads this skill; other workflows leave release-note
work unchanged.

## Scope

Create notes for observable features, fixes, behavior changes, public API
changes, and public documentation changes. For maintenance-only work, report:

```text
No release note needed.
```

## Workflow

1. Confirm that the user directly requested release-note work.
2. Resolve `release_version` with the repository helper:

   ```bash
   bash ./.github/scripts/resolve-release-version.sh
   ```

   Use the helper output as the version directory name.
3. Ensure these directories exist:

   ```text
   release-notes/<release_version>/changes/
   release-notes/<release_version>/migrations/
   ```

4. Create or update a stable release note at:

   ```text
   release-notes/<release_version>/changes/<short-kebab-summary>.md
   ```

   Use a short, stable topic summary for the filename and release information
   for the content.
5. Use direct, concise wording and this template:

   ```markdown
   # Release Changeset

   - type: `<feature|feat|fix|refactor|docs|chore>`
   - module: `<published-module>`
   - release_note: `<plain-language sentence, maximum 20 words>`
   ```

   Populate the change type, published module, and one concise public outcome.
6. Validate the fields and word limit.
7. Report the release version and created or updated path.

## Release-Note Check

Before finalizing a release note, confirm:

- The change is public and user-visible.
- The note describes one coherent outcome.
- The wording is direct and positive.
- The note contains the public outcome and release information only.
- The note fits the public release highlights.
