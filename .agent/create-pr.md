# Create PR

Use this as the single instruction for shipping PRs with Git + GitHub CLI (`gh`).

## Inputs

- `skip_ci_workflows` (optional): `true|false` (default: `false`)

## Terminology

- **Primary PR**: PR for `charts` (this repo).
- **Companion PR**: optional PR in a sibling repo (`../charts-docs` or `../charts-playground`) when needed.

## Rules

- Use CLI only.
- Stop on any error and report it.
- Never push directly to `main`.
- Never force-push or rewrite history unless explicitly asked.
- Do not pre-check `gh` auth; just run the command.
- Remotes:
  - base remote: `upstream` if exists, else `origin`
  - head remote: `origin` if exists, else `upstream`
- PR head format:
  - use `<forkOwner>:<branch_name>` when pushing from fork to upstream
  - otherwise use `<branch_name>`
- For any companion repo (`../charts-docs`, `../charts-playground`), create the companion branch from `<base-remote>/main` before staging files.

## Workflow

1. Infer summary from current work (do not ask the user).
2. Resolve metadata from `.agent/templates/pr-git-metadata.md.tpl` and get `branch_name`, `commit_header`, `pr_title`.
3. Branch from remote main (`git checkout -b "<branch_name>" "<base-remote>/main"`), run `./gradlew ktlintFormat` until it passes, stage, commit, push.
4. Create the primary PR in `main` using `.agent/templates/pr-body.md.tpl` with release status set to `Pending`.
5. Capture primary PR URL and PR number.
6. Run `.agent/create-changeset.md`.
7. If changeset is required, create a companion docs PR in `HDCharts/charts-docs`:
   - repo root: `../charts-docs`
   - branch from remote main: `git checkout -b "docs/changeset-pr-<pr-number>" "<base-remote>/main"`
   - branch: `docs/changeset-pr-<pr-number>`
   - commit only `content/snapshot/changes/<pr-number>-<short-kebab>.md`
   - push branch and create PR using the same remote/head rules as the primary PR
8. If playground updates are needed, create a companion playground PR in `HDCharts/charts-playground`:
   - repo root: `../charts-playground`
   - branch from remote main: `git checkout -b "playground/charts-pr-<pr-number>" "<base-remote>/main"`
   - branch: `playground/charts-pr-<pr-number>`
   - commit only playground files related to this charts PR
   - push branch and create PR using the same remote/head rules as the primary PR
9. Update primary PR body using `.agent/templates/pr-body.md.tpl`:
   - `Not required (technical/internal-only)` if no changeset was needed
   - or `Created: ../charts-docs/content/snapshot/changes/<pr-number>-<short-kebab>.md` (+ docs PR URL if created)
   - include companion playground PR URL when created
   - apply the updated body to the primary PR using `gh pr edit <primary-pr-number> --body-file <file>`

## Output

- Primary PR URL
- Companion docs PR URL (when created)
- Companion playground PR URL (when created)
- Exact skip reason from `.agent/create-changeset.md` (when docs companion PR is skipped)
