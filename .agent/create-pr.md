# Create PR (branch → commit → push → PR)

Use this file as the **single source of truth** for an agent to ship the current work using Git + GitHub CLI (`gh`).

> **Important:** Treat this file as instructions. **Do not modify it unless the user explicitly asks you to.**

## Inputs

- `skip_ci_workflows` (optional): `true|false` (default: `false`)

## Rules

- Use **CLI only**. Never open web pages.
- Do **not** check for `gh` auth/token up front. Just run the PR command.
- If **any step fails**, stop immediately and report the error.
- Never push directly to the base branch (usually `main`).
- Do **not** force-push or rewrite history unless explicitly asked.
- Always run `./gradlew ktlintFormat` before staging/commit.
- If `ktlintFormat` fails, fix the reported issues and re-run `./gradlew ktlintFormat` until it passes.

## Conventions (short)

- **Base remote (PR target)**: use `upstream` if it exists; otherwise `origin`
- **Head remote (push)**: use `origin` if it exists; otherwise `upstream`
- **Base branch**: `main`
- **PR head format**:
  - If base is `upstream` and your branch is on `origin` (a fork), use `--head <forkOwner>:<branch_name>`.
  - Otherwise use `--head <branch_name>`.
- **PR changeset template**: `.agent/templates/pr-changeset.md.tpl`
- **PR body template**: `.agent/templates/pr-body.md.tpl`
- **PR Git metadata template**: `.agent/templates/pr-git-metadata.md.tpl`
- **PR changeset destination**: `../charts-docs/content/snapshot/changes/`
- **PR changeset filename**: `<pr-number>-<short-kebab>.md`
- **PR changeset workflow**: `.agent/create-changeset.md`
- **Docs PR workflow**: `.agent/create-docs-pr.md`

## Workflow (simple)

1. Infer the summary from context (do not ask the user).
2. Resolve inputs:
   - `skip_ci_workflows=false` if not provided.
3. Decide remotes (base vs head) using the conventions above.
4. Build Git metadata using `.agent/templates/pr-git-metadata.md.tpl`:
   - Provide required inputs per the template contract.
   - Use resolved outputs: `branch_name`, `commit_header`, `pr_title`.
5. Create a new branch: `git checkout -b <branch_name>`.
6. Run formatting/lint flow:
   - `./gradlew ktlintFormat`
   - If it fails, fix the reported issues and run `./gradlew ktlintFormat` again until it succeeds
7. Stage changes: `git add -A`
8. Commit: `git commit -m "<commit_header>"`
9. Push: `git push -u <head-remote> <branch_name>`
10. Create a temp PR body file (auto-cleaned at the end of this flow):
   - `PR_BODY_FILE="$(mktemp -t pr-body.XXXXXX.md)"`
11. Write PR body to `$PR_BODY_FILE` using `.agent/templates/pr-body.md.tpl`:
   - Fill `Release/Changeset` as `Pending (resolved after PR creation)`.
12. Create PR (GitHub CLI):
   - If head is on the fork (`origin`), include the fork owner:
     - `gh pr create --repo <base-owner>/<base-repo> --base main --head <forkOwner>:<branch_name> --title "<pr_title>" --body-file "$PR_BODY_FILE"`
   - If head is on upstream, use:
     - `gh pr create --repo <base-owner>/<base-repo> --base main --head <branch_name> --title "<pr_title>" --body-file "$PR_BODY_FILE"`
13. Capture PR URL and PR number from the `gh pr create` result.
14. Execute `.agent/create-changeset.md` using the captured PR URL/PR number context.
15. If step 14 outputs `No changeset needed (technical/internal-only PR).`, skip docs PR creation.
16. If a changeset path is returned from step 14, execute `.agent/create-docs-pr.md` with:
    - `pr_number`
    - `changeset_path` (returned by step 14)
17. Update PR body to final release status:
    - Reuse `PR_BODY_FILE` and update it using `.agent/templates/pr-body.md.tpl`
    - Set `Release/Changeset` to:
      - `Not required (technical/internal-only)` when step 15 path is taken, or
      - `Created: ../charts-docs/content/snapshot/changes/<pr-number>-<short-kebab>.md` when step 16 succeeded
    - Optionally include docs PR URL in the same section when created
    - Update PR body: `gh pr edit <pr-number> --repo <base-owner>/<base-repo> --body-file "$PR_BODY_FILE"`
18. Output:
    - code PR URL
    - docs PR URL (when created)
    - explicit skip reason from `.agent/create-changeset.md` (when skipped)
19. Cleanup:
    - `rm -f "$PR_BODY_FILE"`
