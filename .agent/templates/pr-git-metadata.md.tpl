<!--
Git metadata template (branch + commit header + PR title)

Inputs:
- skip_ci_workflows: true|false (default false)
- branch-type: feat|fix|chore|refactor|docs|test|build|ci
- short-kebab: concise lowercase kebab summary (example: add-template-gallery)
- base_header: Conventional Commit header (type(scope): subject)

Validation:
- short-kebab must be lowercase kebab (letters, numbers, hyphens).
- base_header should stay single-line and concise.
- base_header must not contain trailing punctuation.

Derivation:
1) branch_name = branch-type + "/" + short-kebab (example: feat/add-template-gallery)
2) title_suffix = " [skip ci]" when skip_ci_workflows=true, else ""
3) If base_header already ends with " [skip ci]", force title_suffix=""
   (prevents duplicate suffixes)
4) commit_header = base_header + title_suffix
5) pr_title = commit_header
-->

branch_name: <branch-type>/<short-kebab>
commit_header: <commit_header>
pr_title: <pr_title>
