---
name: hdc-review
description: Review HDCharts diffs, commits, branches, pull requests, and implementations for bugs, regressions, API risks, release issues, and missing tests.
---

# HDCharts Review

Review the requested scope first, then read the context needed to prove a
finding.

## Guardrails

- Report findings before any fix. Review only the selected scope and related
  context. Release skills remain direct-user workflows.

## Review Priorities

- Public API compatibility, accidental API exposure, and migration completeness.
- Chart validation, rendering geometry, selection, formatting, and edge cases.
- Compose state, recomposition, cancellation, stale callbacks, and interaction
  regressions.
- Multiplatform behavior across JVM, Android, Wasm, and iOS where shared code is
  affected.
- Release-note accuracy, migration topics, CI behavior, and deploy safety.
- Missing tests when the omission creates a concrete regression risk.

## Findings

- Order findings by severity before the summary.
- Include exact file and line references.
- Explain the failure condition and user or release impact.
- Suggest the smallest safe fix and a focused missing test when relevant.
- Report high-confidence, behavior-related findings only.
