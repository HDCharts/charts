---
name: hdc-pr
description: Prepare or create an HDCharts pull request only after the user explicitly asks to create, open, publish, or ship it.
---

# Create Pull Requests

## Guardrails

Follow [AGENTS.md](../../AGENTS.md). Git actions require explicit approval
through the git-actions questionnaire.

## Branch and Commit Naming

- Create feature branches from `main` with `<type>/<short-kebab-summary>`.
- Use `feat`, `fix`, `refactor`, `docs`, `test`, `ci`, or `chore` as branch types.
- Use commit subjects in the format `<type>(<scope>): <imperative summary>`.
- Keep the type and scope lowercase and the summary concise.

Examples:

```text
feat/pie-v3-numeric-hardening
feat(pie): align PieSlice value with v3 Double contract
```

Release notes and API migration topics use separate user-invoked workflows.

## Validation Questionnaire

Ask which checks to run using the canonical matrix in
`docs/workflows/ci-test-matrix.md`.

Run selected tasks and list the exact commands in the PR body. CI owns
`validateDocsGifBaselines` and instrumented Android tests unless the user asks
to run them locally.

Run `./gradlew apiCompatibilityCheck` locally for public API or library-module
changes. If it reports an unacknowledged incompatible API change, ask whether
the change is intentional. If it is intentional, run
`./gradlew apiCompatibilityAcknowledgeBreaks` locally, review
`git diff API-COMPATIBILITY-BREAKS.txt`, and include the regenerated file in
the same pull request. Then ask whether to create a release-note or migration
fragment.

## Workflow

1. Inspect the diff and determine the affected modules.
2. Run the selected checks from the canonical matrix.
3. Read `.github/PULL_REQUEST_TEMPLATE.md` and use its sections and headings.
4. Show the proposed branch, commit, push, and PR commands in one batched
   `question` prompt and wait for an explicit yes before executing them.
5. Report the PR URL and validation.
