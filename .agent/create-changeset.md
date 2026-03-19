# Create Changeset

Create a PR changeset file when the PR is user-impacting.

## Required input

- `pr_number` (only when run standalone)

## Release note rule

- `release_note` must be one short human-friendly sentence in plain language (no internal/technical jargon), maximum 20 words.

## Inference

- When called from `.agent/create-pr.md`, use current PR context.
- Infer `short_kebab` from PR title or branch name.
- Infer `type` from PR/commit semantics.
- Infer `module` from touched files and project modules.
- Use `charts_docs_root=../charts-docs` (charts-docs is always a sibling of the `charts` repo).
- Draft `release_note` from user-visible impact and follow the release note rule.
- Explicit user-provided values are optional overrides.

## User-impact gate

- Skip changeset creation for technical/internal-only PRs (CI/workflows, dependency-only bumps with no behavior change, internal refactors/build cleanup, lint/format/repo maintenance).
- If skipped, output exactly: `No changeset needed (technical/internal-only PR).`

## Steps

1. Apply the user-impact gate.
2. If skipped, stop.
3. Use `charts_docs_root=../charts-docs`.
4. Ensure `<charts_docs_root>/content/snapshot/changes/` exists.
5. Create `<charts_docs_root>/content/snapshot/changes/<pr-number>-<short-kebab>.md` from `.agent/templates/pr-changeset.md.tpl`.
6. Fill inferred values: `type`, `module`, `pr` (`https://github.com/HDCharts/charts/pull/<number>`), `release_note`.
7. Validate: no placeholders remain and `release_note` is non-empty and follows the release note rule.
8. Output the created file path.
