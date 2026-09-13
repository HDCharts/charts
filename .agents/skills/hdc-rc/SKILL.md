---
name: hdc-rc
description: Run the configured HDCharts binary API compatibility check and generate concise per-PR migration fragments in charts. Use when the user asks to inspect breaking API changes, check snapshot or release compatibility, or update release migration notes.
---

# Check HDCharts release compatibility

Generate incremental migration guidance from the library's API compatibility
reports. Use the configured compatibility baseline for normal PR checks. Use
the previous release tag for release audits. Migration fragments accumulate for
the target release. The docs sync mirrors these fragments unchanged and the
docs app assembles them at build time. Generate per-fragment migration guidance
for the target release.

## Guardrails

- Follow the commit, push, and pull-request policy — including the
  git-actions questionnaire — in the Guardrails section of
  [hdc-pr](../hdc-pr/SKILL.md).
- Preserve migration fragments belonging to other pull requests.
- Write only when the intended fragment has a clean worktree state.
- Inspect generated reports for every command result and distinguish reported
  incompatibilities from infrastructure failures.


## Workflow

1. Read `charts/.version`, require
   `<major>.<minor>.<patch>-SNAPSHOT`, remove the suffix to obtain
   `release_version`, and use:

   ```text
   charts/release-notes/<release_version>/migrations/
   ```

2. Resolve the current `HDCharts/charts` pull request number. Search the target
   migration directory for `<pr-number>-*.md`:
   - exactly one matching fragment: reuse that path;
   - multiple matching fragments: report the ambiguity before writing;
   - no matching fragment: resolve a concise kebab-case summary and create:

   ```text
   <pr-number>-<short-kebab>.md
   ```

   Use an explicit user value when creating the first fragment. Complete the
   compatibility inspection and omit writing when no stable fragment identity
   exists.
3. Resolve the baseline ref:
   - release audits: use the previous release tag provided by the user or
     inferred from the release history;
   - standard checks: read the configured baseline ref from
     `charts/.github/api-compatibility-baseline.txt`.
4. Remove the generated
   `charts/build/reports/api-compatibility/` directory before running the check.
5. Run from `charts`:

   ```text
   ./gradlew apiCompatibilityCheck --no-daemon --continue
   ```

   For release audits, pass the previous release tag as the baseline:

   ```text
   ./gradlew apiCompatibilityCheck --no-daemon --continue -PapiCompatibilityBaselineRef=<previous-release-tag>
   ```

6. Capture the command's exit status and output. Classify API incompatibilities
   separately from build, dependency, tool, and other infrastructure failures.
   Report infrastructure failures and leave release notes unchanged.
7. Read every Markdown report generated under
   `build/reports/api-compatibility/`. Verify that every configured library
   module expected to run produced a current report. Record modules whose
   baseline artifact is unavailable and report those modules in the results.
   Report failures for missing, stale, or incomplete reports and leave release
   notes unchanged.
8. For each module with a breaking change, determine:
   - source call sites requiring edits;
   - the user-visible API change;
   - a minimal Kotlin before/after migration for required source edits.
9. Update the current pull request's fragment using
   [assets/migration-fragment.md](assets/migration-fragment.md):
   - breaking modules: write one section per module with applicable migration
     content;
   - no breaking modules with an existing current fragment: remove the stale
     fragment;
   - no breaking modules with no current fragment: leave release notes
     unchanged.
10. Validate that no placeholders remain and that examples match the reports.
11. Report the release version, baseline ref, command result, breaking modules,
    and fragment path or no-write reason.
