# Agent Skills

| Skill | Use when | Definition |
| --- | --- | --- |
| `hdc-pr` | The user explicitly asks to create, open, publish, or ship a pull request. | `.agents/skills/hdc-pr/SKILL.md` |
| `hdc-changeset` | The user asks to create or update a release changeset for the current repository. | `.agents/skills/hdc-changeset/SKILL.md` |
| `hdc-rc` | The user asks to inspect breaking API changes, check snapshot or release compatibility, or update release migration notes. | `.agents/skills/hdc-rc/SKILL.md` |

## Commit, push, and pull-request policy

- Never commit, push, force-push, amend, or open a pull request on your own.
  These actions require an explicit request from the user.
- Loading or invoking a skill (including `hdc-pr` and `hdc-changeset`) does
  **not** grant permission to commit, push, or open a PR. Skills describe the
  workflow to follow **once** the user has asked to ship the change.
- Before any commit, push, or PR action, confirm with the user that the
  current working-tree changes are the intended ones to ship.
