# HDCharts 3.0.0 agent migration prompt

Paste the prompt below into a coding agent from the root of the project that
consumes HDCharts.

```text
You are migrating a Kotlin Multiplatform and Compose Multiplatform project
from HDCharts 2.x to HDCharts 3.0.0.

You have access to the official HDCharts 3.0.0 Dokka API documentation:
https://charts.hdcode.dev/static/api/3.0.0/index.html

Read and use that Dokka documentation as your API knowledge and source of truth
before editing. It contains the current public classes, functions, parameters,
packages, styles, examples, and supported behavior. Do not guess the new API
from this prompt or preserve removed APIs through compatibility wrappers.

Inspect the consuming project first. Find every HDCharts dependency, import,
chart call, data model, style, selection, test, preview, sample, documentation
snippet, and web target that uses the 2.x API.

Use the Dokka documentation to migrate each usage to its 3.0.0 replacement.
Preserve the project's data meaning, visual behavior, selection behavior, and
supported platforms. Apply only the migrations the project needs, and keep
application-specific data conversion outside the chart library models.

Work in small groups:

1. Inventory the old usages.
2. Apply the documented replacements and compile after each group.
3. Update affected tests, previews, samples, and documentation.
4. Search again for removed APIs, old coordinates, and old packages.
5. Run the smallest relevant compile, unit, platform, and visual checks.

Report the changed files, the Dokka pages or APIs used, every validation
command and result, and any unresolved behavior decision. Do not claim the
migration is complete when a required check was skipped or blocked.
```
