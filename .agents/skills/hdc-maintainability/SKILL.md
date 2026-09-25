---
name: hdc-maintainability
description: Review HDCharts changes, plans, or proposals for long-term maintenance cost — new API surface, cross-repo fan-out, configuration growth, drift-prone duplication, and fragile coupling — for a solo maintainer.
---

# HDCharts Maintainability Review

HDCharts is maintained by one person across several repositories. Every change
is a long-term obligation. This review asks whether a change is worth owning,
not whether it is correct; use `hdc-review` for correctness.

## Guardrails

- Report findings before any fix. Do not modify files until the user asks.
- Review the requested scope: a diff, branch, pull request, or local plan under
  `plans/`. Read outward from it only to measure what it drags along.
- Judge proportionality: weigh the ongoing cost against how often the problem
  or need actually occurs.

## What a Change Can Drag Along

- `charts-*` public API, `API-COMPATIBILITY-BREAKS.txt`, and `release-notes/`.
- `docs/wiki/`, synced into `../charts-docs`, plus `README.md` and `readme-assets/`.
- `sample/` screens, previews, screenshot tests, and `gif-baselines/` scenarios.
- `../charts-playground`, which exposes chart options and generates chart code.
- `../charts-gif-recorder`, the published `compose-gif-recorder` library.
- `.github/workflows/`, `scripts/`, and `buildSrc/` tooling.

## Review Priorities

- **Public surface.** Each public type, parameter, default, or style property is
  a compatibility promise. Prefer `internal`, fewer knobs, or no addition.
- **Fan-out.** Count the places above that must now stay in sync. Flag anything
  a human must remember to update by hand.
- **Configuration growth.** New flags, modes, and options multiply the states to
  test, document, and expose in the playground.
- **Duplication.** Copied logic, constants, snippets, or docs that will drift.
  Prefer one owner and derivation, or a check that catches drift.
- **Fragile coupling.** Correctness that depends on an invariant held elsewhere,
  platform-specific branches, new dependencies, or tooling that only works
  locally.
- **Placement.** Knowledge added to a low layer or threaded through call sites
  when the layer that creates the need could own it.
- **Automation gaps.** Artifacts that go stale silently, such as screenshots,
  GIFs, or hand-copied examples, when a test or CI check could guard them.

## Findings

- Give each finding a verdict: `keep`, `simplify`, or `cut`.
- Name the ongoing obligation, where it lives, and file and line references.
- Propose the cheaper shape: reuse, narrow, make internal, defer, or drop.
- End with the overall maintenance footprint in one or two sentences.
- Skip style nits and correctness bugs; those belong to `hdc-review`.
