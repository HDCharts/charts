# Create Docs PR (changeset)

Create a PR in `HDCharts/charts-docs` for a generated changeset file.

## Required input

- `pr_number`
- `changeset_path` (expected: `../charts-docs/content/snapshot/changes/<pr-number>-<short-kebab>.md`)

## Rules

- Use **CLI only**.
- Use sibling repo `../charts-docs`.
- Commit only the generated changeset file for this PR.
- Do **not** check for `gh` auth/token up front. Just run the PR command.
- If **any step fails**, stop immediately and report the error.
- Never push directly to the base branch (usually `main`).
- Do **not** force-push or rewrite history unless explicitly asked.
- Do not assume local `main` is up to date; branch from `<base-remote>/main`.

## Conventions (short)

- **Base remote (PR target)**: use `upstream` if it exists; otherwise `origin`
- **Head remote (push)**: use `origin` if it exists; otherwise `upstream`
- **Base branch**: `main`
- **PR head format**:
  - If the pushed branch is on a fork (owner is not `HDCharts`), use `--head <headOwner>:<branch>`.
  - Otherwise use `--head <branch>`.
- **PR target repo**: `HDCharts/charts-docs`

## Workflow

1. Validate `changeset_path` exists.
2. `cd ../charts-docs`
3. Decide remotes (base vs head) using the conventions above.
4. Fetch base branch head: `git fetch "<base-remote>" main`
5. Set `branch=docs/changeset-pr-<pr-number>`.
6. Create branch from remote main: `git checkout -b "$branch" "<base-remote>/main"`
7. Stage the validated changeset path: `git add "content/snapshot/changes/$(basename "$changeset_path")"`
8. Commit only the generated changeset file: `git commit -m "docs(changeset): add changeset for charts pr #<pr-number>" -- "content/snapshot/changes/$(basename "$changeset_path")"`
9. Push: `git push -u "<head-remote>" "$branch"`
10. Resolve `headOwner` from `<head-remote>` URL (for example from `git@github.com:<owner>/charts-docs.git` or `https://github.com/<owner>/charts-docs.git`).
11. Create PR:
   - If `headOwner` is not `HDCharts`:
     - `gh pr create --repo HDCharts/charts-docs --base main --head "$headOwner:$branch" --title "docs(changeset): add changeset for charts pr #<pr-number>" --body "Adds snapshot changeset for HDCharts/charts PR #<pr-number>."`
   - Otherwise:
     - `gh pr create --repo HDCharts/charts-docs --base main --head "$branch" --title "docs(changeset): add changeset for charts pr #<pr-number>" --body "Adds snapshot changeset for HDCharts/charts PR #<pr-number>."`
12. Output docs PR URL.
