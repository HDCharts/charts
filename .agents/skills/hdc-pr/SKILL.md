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

## Workflow

1. Determine the changeset status with the user-impact gate in
   [hdc-changeset](../hdc-changeset/SKILL.md).
2. When the gate requires a changeset, ask the user for confirmation before
   creating it.
3. After confirmation, invoke `hdc-changeset` to create the changeset.
4. Commit and push the intended changes after the user asks to ship them.
5. Find or create the pull request for the current repository, targeting `main`,
   using `.github/PULL_REQUEST_TEMPLATE.md` for the body.
6. Report the pull request URL and changeset status.
