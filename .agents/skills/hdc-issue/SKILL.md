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
4. Draft the title and body in the conversation. Do not write draft files in
   or outside the repository. Keep every template heading in its order and
   fill each one. Write `N/A` for sections that do not apply, such as Steps To
   Reproduce for a feature request. For the version, use the current commit
   (`git rev-parse --short HEAD`).
5. Pick labels only from the existing set; never create labels:

   ```bash
   gh api repos/HDCharts/charts/labels --paginate --jq '.[].name'
   ```

   Use `bug` for broken behavior and `feature` for requests.
6. Set a `Type`, `Priority`, and `Effort` for every issue. `Type` is `Task`
   for a specific piece of work, `Bug` for unexpected behavior, or `Feature`
   for a request, idea, or new functionality. `Priority` accepts `Urgent`,
   `High`, `Medium`, or `Low`; `Effort` accepts `High`, `Medium`, or `Low`.
   Pass an option name, not its numeric option id. Look up the available
   types, field ids, and allowed values before drafting the commands, because
   the repository configuration can change:

   ```bash
   gh api repos/HDCharts/charts/issue-types --jq \
     '.[].name'

   gh api orgs/HDCharts/issue-fields --jq \
     '.[] | select(.name == "Priority" or .name == "Effort") | "\(.name) (field_id=\(.id)): \([.options[].name] | join(", "))"'
   ```

   Show the proposed title, labels, `Type`, `Priority`, and `Effort` values,
   body, story (or "standalone"), and the exact create, field-update, and
   verification commands in one `question` prompt. Apply the user's edits to
   the draft and ask again until they approve.
7. Create the issue with `gh issue create`. Pipe the exact approved Markdown
   body through standard input, capture the returned URL, and derive the issue
   number for step 8. Omit `--label` when there are no labels and `--parent`
   for a standalone issue. For a story supplied as `#690`, pass `690` to
   `--parent`; a full story URL also works:

   ```bash
   issue_url="$(gh issue create \
     --repo HDCharts/charts \
     --title '<title>' \
     --body-file - \
     --label '<label>' \
     --type '<type>' \
     --parent <story-number-or-url> <<'ISSUE_BODY'
   <body>
   ISSUE_BODY
   )"
   issue_number="${issue_url##*/}"
   ```

8. Set the approved field values using the `number` from step 7 and the field
   ids from step 6. Verify the `Type` and field values before continuing:

   ```bash
   gh api -X POST repos/HDCharts/charts/issues/<number>/issue-field-values \
     --input - <<< '{"issue_field_values":[{"field_id":<priority-field-id>,"value":"<priority>"},{"field_id":<effort-field-id>,"value":"<effort>"}]}'

   gh api repos/HDCharts/charts/issues/<number> \
     --jq '"Type=\(.type.name)"'

   gh api repos/HDCharts/charts/issues/<number>/issue-field-values \
     --jq '.[] | "\(.issue_field_name)=\(.single_select_option.name)"'
   ```

9. Report the issue URL, its `Type`, `Priority`, and `Effort` values, and
   the story it belongs to.

## Wording

Write in the `hdc-docs` voice: plain, direct, concrete. Lead with what the
user notices; keep code references to a short pointer. Every sentence must
make sense to a reader who never saw this session. Leave out AI attribution
and "Generated with" footers.

Write every repository file path, of any kind, as a full link to `main`, so it
is clickable: `[<path>](https://github.com/HDCharts/charts/blob/main/<path>)`.
Code symbols such as `PieChart` stay in backticks.
