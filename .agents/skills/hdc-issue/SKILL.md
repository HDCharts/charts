---
name: hdc-issue
description: Draft an HDCharts GitHub issue from the repository issue template and create it with `gh api` only after the user explicitly approves the title, labels, and body. Pass a story issue number to create the issue as its sub-issue.
---

# Create GitHub Issues

## Input

An optional story issue, as a number (`#690`) or URL. With a story, the new
issue is created as its sub-issue. Without one, it is a standalone issue.
A story URL must point to `HDCharts/charts`; stop and tell the user when it
points to another repository.

## Guardrails

Follow [AGENTS.md](../../../AGENTS.md). Creating an issue publishes it, so show
the final title, labels, and body in one `question` prompt and wait for an
explicit yes before running `gh api`. Loading this skill does not grant
permission to post.

## Workflow

1. Read `.github/ISSUE_TEMPLATE/` fully, including the front matter. Use the
   template's `title:` prefix and `labels:` as defaults. An empty `labels:`
   means no default label.
2. Gather the facts: the user's description, any bug report or plan the user
   points to, and the code involved. Reproduce or trace the problem when it
   is cheap; say which steps you ran and which come from reading code.
   With a story, read it and its existing sub-issues so the new issue fits
   the story's scope and repeats none of its siblings:

   ```bash
   gh api repos/HDCharts/charts/issues/<story> --jq '"\(.state) \(.title)\n\(.body)"'
   gh api repos/HDCharts/charts/issues/<story>/sub_issues --paginate \
     --jq '.[] | "#\(.number) [\(.state)] \(.title)"'
   ```

   If the story is closed, ask the user before continuing.
3. Search for duplicates by the visible symptom and by the feature name:

   ```bash
   gh api -X GET search/issues \
     -f q='repo:HDCharts/charts is:issue <keywords>' \
     --jq '.items[] | "#\(.number) [\(.state)] \(.title)"'
   ```

   Report close matches to the user before drafting.
4. Write the title to `temp/issues/<short-kebab-summary>.title` and the body
   to `temp/issues/<short-kebab-summary>.md` (both gitignored). Keep every
   template heading in its order and fill each one. Write `N/A` for sections
   that do not apply, such as Steps To Reproduce for a feature request. For
   the version, use the current commit (`git rev-parse --short HEAD`).
5. Pick labels only from the existing set; never create labels:

   ```bash
   gh api repos/HDCharts/charts/labels --paginate --jq '.[].name'
   ```

   Use `bug` for broken behavior and `feature` for requests.
6. Show the proposed title, labels, body, story (or "standalone"), and the
   exact commands in one `question` prompt. Apply the user's edits to the
   draft and ask again until they approve.
7. Create the issue. Reading the title from its file keeps quotes in it
   intact. Pass one `labels[]` field per label, and none when there is no
   label:

   ```bash
   gh api repos/HDCharts/charts/issues \
     -f title="$(cat temp/issues/<short-kebab-summary>.title)" \
     -F body=@temp/issues/<short-kebab-summary>.md \
     -f 'labels[]=<label>' \
     --jq '"\(.id) \(.html_url)"'
   ```

8. With a story, attach the new issue using the `id` from step 7. The `id` is
   GitHub's internal id, which differs from the `#number`; `-F` sends it as
   an integer:

   ```bash
   gh api repos/HDCharts/charts/issues/<story>/sub_issues \
     -F sub_issue_id=<id>
   ```

   If attaching fails, report the created issue URL and the error. Leave the
   issue in place; creating it again makes a duplicate.
9. Report the issue URL, and the story it belongs to. Leave the draft in
   `temp/issues/`.

## Wording

Write in the `hdc-docs` voice: plain, direct, concrete. Lead with what the
user notices; keep code references to a short pointer. Every sentence must
make sense to a reader who never saw this session. Leave out AI attribution
and "Generated with" footers.

Write every repository file path, of any kind, as a full link to `main`, so it
is clickable: `[<path>](https://github.com/HDCharts/charts/blob/main/<path>)`.
Code symbols such as `PieChart` stay in backticks.
