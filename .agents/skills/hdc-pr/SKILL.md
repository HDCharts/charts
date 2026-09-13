---
name: hdc-pr
description: Create or update a pull request for this repository when the user explicitly asks to create, open, publish, or ship a PR.
---

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
   [hdc-changeset](../hdc-changeset/SKILL.md).
2. When the gate requires a changeset, ask the user for confirmation before
   creating it.
3. After confirmation, invoke `hdc-changeset` to create the changeset.
4. Confirm the validation scope with the Validation questionnaire and run the
   selected checks.
5. Commit and push the intended changes after the user asks to ship them.
6. Find or create the pull request for the current repository, targeting `main`,
   using `.github/PULL_REQUEST_TEMPLATE.md` for the body.
7. Report the pull request URL and changeset status.
