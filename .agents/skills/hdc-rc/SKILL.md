---
name: hdc-rc
description: Inspect HDCharts binary API compatibility and maintain concise, stable migration topics for the current release.
---

# Check HDCharts Release Compatibility

Generate user migration guidance from API compatibility reports. Keep one
coherent public API topic per migration document.

## Guardrails

Follow [AGENTS.md](../../AGENTS.md). The user invokes this skill directly.
Inspect every generated report and distinguish API incompatibilities from
infrastructure failures.

Write direct migration prose with the supported API and required user action.

## Migration Topic Policy

- Inspect existing migration files before creating one. Merge overlapping
  symbols, examples, and behavior into the canonical topic.
- Document an API removal with its replacement in the same topic.
- Use concise topic filenames such as `pie-v3.md`, `line-v3.md`, or
  `shared-chart-contracts.md`.
- Keep unrelated release history stable.

## Workflow

1. Resolve `release_version` with the repository helper:

   ```bash
   bash ./.github/scripts/resolve-release-version.sh
   ```

   Use the helper output as the version directory name:
   `release-notes/<release_version>/migrations/`.

2. Run from the repository root, which compares against the latest release
   tag:

   ```bash
   ./gradlew apiCompatibilityCheck --no-daemon --continue
   ```

   Only if the user explicitly names an older release to investigate, run
   `apiCompatibilityCompare -PapiCompatibilityBaselineRef=<the ref the user
   named>` instead. Never choose a ref on your own.
3. Capture the command status.
4. Verify that every configured module produced a current Markdown report.
5. For each breaking module, determine the affected call sites, user-visible
   API change, supported replacement, and minimal migration example.
6. For breaking modules, update the canonical topic file using this structure:

   ```markdown
   # <Topic> migration

   <Direct description of the supported API and user outcome.>

   ## Use

   <Current API example and required application-boundary conversion.>

   ## Behavior

   <Important validation, selection, rendering, or compatibility behavior.>

   ## Validation

   <Focused tests and remaining platform or CI gates.>
   ```

7. Validate examples against the reports and check for placeholders or
   overlapping topics. Leave migration files unchanged when the reports name
   no breaking module.
8. Report the release version, baseline ref, command result, breaking modules,
   canonical topic paths, and any duplicate files removed.
