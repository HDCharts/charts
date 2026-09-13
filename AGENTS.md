# Agent Skills

| Skill | Use when | Definition |
| --- | --- | --- |
| `hdc-pr` | The user explicitly asks to create, open, publish, or ship a pull request. | `.agents/skills/hdc-pr/SKILL.md` |
| `hdc-changeset` | The user asks to create or update a release changeset for the current repository. | `.agents/skills/hdc-changeset/SKILL.md` |
| `hdc-rc` | The user asks to inspect breaking API changes, check snapshot or release compatibility, or update release migration notes. | `.agents/skills/hdc-rc/SKILL.md` |

## Commit, push, and pull-request policy

The single source of truth is the **Guardrails** section of
[`.agents/skills/hdc-pr/SKILL.md`](.agents/skills/hdc-pr/SKILL.md)
(including the git-actions questionnaire).

Summary: never commit, push, force-push, amend, create branches, or open a
pull request without an explicit user request. Loading a skill does not grant
permission. Use the `question` tool before every destructive or external git
action.
