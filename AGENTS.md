# Agent Skills

| Skill | Use when | Definition |
| --- | --- | --- |
| `hdc-pr` | The user explicitly asks to create, open, publish, or ship a pull request. | `.agents/skills/hdc-pr/SKILL.md` |
| `hdc-changeset` | The user asks to create or update a release changeset for the current repository. | `.agents/skills/hdc-changeset/SKILL.md` |
| `hdc-rc` | The user asks to inspect breaking API changes, check snapshot or release compatibility, or update release migration notes. | `.agents/skills/hdc-rc/SKILL.md` |

## Guardrails

These rules apply to every agent session in this repo and override
conflicting instructions in skill files.

- Never commit, push, create branches, or open a pull request on your own.
  Each action requires an explicit user request.
- Never suggest or use history-rewriting commands: `git push --force*`,
  `git push -f`, `git commit --amend`, `git rebase`, or any other command
  that rewrites published history. Always create a new commit instead.
- Loading a skill does **not** grant permission to commit, push, or open a
  PR; skills describe the workflow to follow **once** the user has asked to
  ship the change.
- Before every destructive or external git action, use the `question` tool to
  show the proposed command (and the diff summary for commits) and wait for
  an explicit "yes". Never assume consent.
- Preserve unrelated working-tree changes; isolate only the intended work.
- Reuse an existing pull request and avoid duplicates.

### Git-actions questionnaire

Before each of the following, use the `question` tool with the proposed
command summary and wait for an explicit yes:

- **create branch** — `git checkout -b <branch> from <base>`
- **commit** — `git commit` (show the staged diff summary and the proposed
  commit subject)
- **push** — `git push` (show the branch name and remote)
- **open PR** — `gh pr create` (show the proposed title and body)

When several actions are queued, batch them into a single `question` prompt
so the user can approve the whole sequence at once.
