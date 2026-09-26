# HDCharts Agent Instructions

HDCharts is a Kotlin Multiplatform and Compose Multiplatform chart library. It
publishes the chart modules and BOM from this repository and maintains sample,
API compatibility, release-note, and documentation workflows alongside them.

## Optional Planning

For a complex or multi-step feature, optionally ask whether the user wants an
implementation plan before coding. Present the choices `Create a plan` and
`Skip planning`. If the user chooses `Create a plan`, use `hdc-plan` to write
the plan locally under `plans/`. Plans are local working documents and must not
be committed or published in a planning pull request.

## Repository Structure

- `charts-core`: shared chart models, selection, formatting, validation, and rendering foundations.
- `charts-line`, `charts-pie`, `charts-bar`, `charts-histogram`, `charts-radar`, `charts-stacked-bar`, and `charts-stacked-area`: published chart modules.
- `charts`: the umbrella artifact exposing the library's combined API.
- `charts-bom`: the published Bill of Materials.
- `sample`: Compose Multiplatform demos, previews, screenshot tests, and platform applications.
- `release-notes`: versioned public release notes and API migration documents.
- `.github`: CI, publishing, release, compatibility, and documentation synchronization workflows.

Read the relevant documentation before substantial changes:

- `CONTRIBUTING.md` for repository layout and test types.
- `docs/wiki/dev/releases/pull-requests.md` for PR CI and required checks.
- `docs/wiki/dev/releases/release-checklist.md` for release readiness.
- `release-notes/README.md` for release-note structure and synchronization.

## Always-Follow Rules

- **Never commit, push, create branches, or open a pull request on your own.**
  Each action requires explicit user approval through the git-actions
  questionnaire. Loading `hdc-pr` does not grant permission to act.
- **Never use history-rewriting commands.** Do not force-push, amend commits,
  rebase published history, or use an equivalent destructive operation.
- **Never touch the git index on your own.** Do not run `git add`, `git rm`,
  `git restore --staged`, `git reset`, or any command that modifies the staged
  set without an explicit user instruction. The user reviews and approves what
  is staged; silently staging follow-up edits hides new changes from their
  review. If you think a follow-up edit should be staged, ask first using the
  `question` tool and list the exact files you intend to stage.
- **Keep release skills user-invoked.** Invoke `hdc-changeset` or `hdc-rc` only
  when the user directly requests release-note or API-compatibility work. Other
  skills must not load or invoke them implicitly.
- **Use stable release information in release notes.** New release-note
  filenames and content remain stable throughout the contribution lifecycle.
- **Keep plans local.** Write plans only under the git-ignored `plans/`
  directory. Do not create a plan PR or include plan files in implementation
  or feature PRs.
- Inspect the relevant existing code, documentation, and workflow before
  editing; preserve unrelated working-tree changes.
- Load only the project skills relevant to the task. Do not load every skill by
  default.
- Prefer the smallest correct change. Do not add speculative abstractions or
  duplicate release metadata.
- Do not start a review unless requested. Reviews are advisory and must not
  modify files until the user explicitly asks to fix findings.
- Run the smallest relevant validation and report exactly what ran and what was
  not run.

### Git-actions Questionnaire

Before each of these actions, use the `question` tool with the proposed command
and wait for an explicit yes:

- **Stage or unstage files**: `git add <paths>`, `git add -p`, `git rm <paths>`,
  `git restore --staged <paths>`, `git reset <paths>`. List the exact paths you
  intend to stage or unstage so the user can approve precisely.
- **Create branch**: `git checkout -b <branch> from <base>`.
- **Commit**: show the staged diff summary and proposed commit subject.
- **Push**: show the branch name and remote.
- **Open PR**: show the proposed title and body.

When several actions are queued, batch them into one question so the user can
approve the complete sequence.

## Available Skills

Project skills use the `hdc-` prefix. The canonical source is
`.agents/skills/`.

| Skill | Use when | Location |
|---|---|---|
| `hdc-review` | Reviewing a diff, commit, branch, pull request, or implementation for bugs, regressions, API risks, and missing tests. | `.agents/skills/hdc-review/SKILL.md` |
| `hdc-maintainability` | Reviewing a diff, branch, pull request, or plan for long-term maintenance cost, API surface growth, and cross-repo fan-out. | `.agents/skills/hdc-maintainability/SKILL.md` |
| `hdc-architecture` | Module boundaries, public chart API layering, validation, rendering, and sample integration. | `.agents/skills/hdc-architecture/SKILL.md` |
| `hdc-kotlin` | Kotlin and Kotlin Multiplatform source sets, models, APIs, visibility, dependencies, and formatting. | `.agents/skills/hdc-kotlin/SKILL.md` |
| `hdc-compose` | Compose Multiplatform chart composables, drawing, interaction, previews, and screenshots. | `.agents/skills/hdc-compose/SKILL.md` |
| `hdc-ux` | Chart usability, interaction design, accessibility, responsive behavior, and data communication. | `.agents/skills/hdc-ux/SKILL.md` |
| `hdc-docs` | Direct, positive, concise documentation and release-note writing. | `.agents/skills/hdc-docs/SKILL.md` |
| `hdc-concurrency` | Coroutine ownership, Compose effects, animations, live previews, delayed selection, and cancellation. | `.agents/skills/hdc-concurrency/SKILL.md` |
| `hdc-testing` | Kotlin, Compose interaction, rendering, API, platform, and screenshot testing. | `.agents/skills/hdc-testing/SKILL.md` |
| `hdc-pr` | The user explicitly asks to create, open, publish, or ship a pull request. | `.agents/skills/hdc-pr/SKILL.md` |
| `hdc-plan` | The user chooses local planning for a complex or multi-step feature. | `.agents/skills/hdc-plan/SKILL.md` |
| `hdc-changeset` | The user directly requests creating or updating a release note. | `.agents/skills/hdc-changeset/SKILL.md` |
| `hdc-rc` | The user directly requests API compatibility or migration work. | `.agents/skills/hdc-rc/SKILL.md` |
| `hdc-gif` | Recording or updating docs GIF scenarios locally against a running emulator. | `.agents/skills/hdc-gif/SKILL.md` |

## Validation

Choose focused checks from `docs/wiki/dev/releases/ci-test-matrix.md` based on the
affected scope.

Do not run `./gradlew validateDocsGifBaselines` or instrumented Android tests
locally unless the user explicitly requests them; CI owns those machine-
dependent checks.

For review tasks, inspect the applicable diff and surrounding code first.
Findings take priority over a summary and should include file and line
references when possible.
