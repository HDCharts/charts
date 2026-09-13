---
name: hdc-plan
description: Create a local HDCharts implementation plan for complex feature work.
---

# Plan Feature Work

This skill writes local planning documents for complex feature work.

## Required Workflow

1. Define the outcome, behavior, acceptance criteria, constraints, and platforms.
2. Inspect the relevant chart modules, sample code, tests, API compatibility
   configuration, documentation, and release-note conventions.
3. Ask only for decisions that materially change the implementation.
4. Analyze affected modules, API contracts, rendering and interaction risks,
   platform differences, tests, validation, and sequencing.
5. Split the work into the smallest practical sequence of independently
   reviewable implementation pull requests.
6. Write all plan files under the git-ignored directory:

   ```text
   plans/<feature-name>/
   ```

7. Present the created local plan files.

## Main Plan File

Create:

```text
plans/<feature-name>/plan-<feature-name>.md
```

The index contains only this structure:

```markdown
# <Feature name>

| PR | File | Description |
|---|---|---|
| 1 | `pr-1-<short-name>.md` | One-line description of the first implementation PR |
```

Use one row when the feature needs one implementation PR. The table guides
implementation work.

## Implementation Plan Files

Create one file per index row:

```text
plans/<feature-name>/pr-<number>-<short-name>.md
```

Use this structure:

```markdown
# PR <number>: <Short title>

## Purpose
<The single outcome delivered by this implementation PR.>

## Prerequisites
<Required earlier implementation PRs or "None".>

## Affected Modules And Layers
<Gradle modules, chart layers, platforms, tests, and release configuration.>

## Estimate
<Small, medium, or large; approximate changed-line range.>

## Implementation Steps
1. <Concrete implementation step.>
2. <Concrete implementation step, including tests and validation.>

## Acceptance Criteria
- <Observable, testable completion condition.>

## Excluded Follow-up
<Explicitly deferred work or "None".>

## Open Decisions
<Unresolved decision or "None".>
```

Keep plans concrete and repository-specific. Separate public API, chart
implementation, sample or platform work, and release validation when they form
independent pull requests.
