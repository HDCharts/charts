---
name: hdc-pr
description: Create or update a pull request for this repository only after the user explicitly asks to create, open, publish, or ship a PR. Do NOT commit, push, or open a PR on your own.
---

## Guardrails

- Never commit, push, force-push, amend, create branches, or open a pull
  request on your own. Each action requires an explicit user request.
- Loading or invoking this skill does **not** grant permission. The skill
  describes the workflow to follow **once** the user has asked to ship.
- Before every destructive or external git action, use the `question` tool to
  show the proposed command (and the diff summary for commits) and wait for
  an explicit "yes". Never assume consent.
- Preserve unrelated working-tree changes; isolate only the intended work.
- Reuse an existing pull request and avoid duplicates.

### Git-actions questionnaire

Use the `question` tool with the proposed command summary and wait for an
explicit yes before each of these actions:

- **create branch** — `git checkout -b <branch> from <base>`
- **commit** — `git commit` (show the staged diff summary and the proposed
  commit subject)
- **push** — `git push` (show the branch name and remote)
- **force-push / amend** — `git push --force*`, `git push -f`, or
  `git commit --amend` (always requires explicit yes; never default)
- **open PR** — `gh pr create` (show the proposed title and body)

When several actions are queued, batch them into a single `question` prompt
so the user can approve the whole sequence at once.

## Branch and commit naming

- Create feature branches from `main` with the format
  `<type>/<short-kebab-summary>`.
- Use branch types such as `feat`, `fix`, `refactor`, `docs`, `test`, `ci`, and
  `chore`.
- Use commit subjects in the format `<type>(<scope>): <imperative summary>`.
- Keep the type and scope lowercase and the summary concise.
- Examples: `feat/pie-v3-numeric-hardening` and
  `feat(pie): align PieSlice value with v3 Double contract`.

## Validation questionnaire

Before committing, use the `question` tool to confirm which validation to run
based on the scope of the changes:

- Change confined to one chart module → scoped tests:
  `./gradlew :charts-<module>:jvmTest`
- Cross-module or `charts-core` change → full JVM tests:
  `./gradlew chartsTestJvm`
- Compose UI / visual change → screenshot baselines:
  `./gradlew :androidApp:validateDebugScreenshotTest` (update via
  `./gradlew updateScreenshots`)
- Public API change → `./gradlew apiCompatibilityCheck`
- Any change → `./gradlew ktlintCheck`

Run the selected tasks and list the executed commands in the PR body
Validation section.

Do not run `./gradlew validateDocsGifBaselines` or instrumented Android tests
locally: they are machine-dependent, slow, and run on CI when required.

## Workflow

1. Determine the changeset status with the user-impact gate in
   [hdc-changeset](../hdc-changeset/SKILL.md). If a changeset is required,
   ask the user for confirmation before creating it.
2. Run the Validation questionnaire and execute the selected checks.
3. Show the proposed branch + commit + push + PR commands as one batched
   `question` prompt (per the Git-actions questionnaire in Guardrails) and
   wait for an explicit "yes" before executing any of them.
4. After all actions succeed, report the pull request URL and changeset
   status.
