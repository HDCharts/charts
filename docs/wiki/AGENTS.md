# Chart Wiki Page Guidelines

Use these conventions for pages in `docs/wiki/` that document a chart. This file is contributor guidance and must stay out of the public wiki sync.

## Page structure

Use this order and heading levels for each chart page:

1. Optional YAML frontmatter used by the docs site.
2. One `#` title naming the chart.
3. `## Overview` — briefly explain what the chart represents, when to use it, and meaningful limitations. Keep the description developer-focused.
4. `## Basic Usage` — required. Show one practical, complete Kotlin example using the public API. Include imports and a composable context so a reader can understand how to render the chart. Use small deterministic data, sensible defaults, and sizing appropriate to the surrounding layout. Favor realistic application usage over minimizing lines: a meaningful domain model can clarify data mapping or show how chart styling can come from application data. Simple input data is equally appropriate when it communicates the intended use clearly.
5. Optional `## Data Mapping` — show how useful application data maps to chart inputs when this teaches something. Meaningful domain models are encouraged when they make the example clearer or demonstrate an API design choice. Avoid introducing models, wrappers, or transformations when they add complexity without educational value. Do not require a domain model in every example.
6. `## Capabilities` — include when there are useful chart capabilities to explain. Organize them as focused `###` subsections with descriptive names such as `### Selection` or `### Value Formatting`. The examples are possibilities, not a required checklist. Give each example one primary capability. A snippet may reuse Basic Usage data when it says so explicitly.
7. `## API Reference` — required. Link to the generated API documentation for the public composable and relevant defaults or styles. Follow the versioned path convention below and confirm each target exists in generated output. Do not copy the complete API signature into the page.

Omit optional sections that add no useful information. Link to common documentation instead of repeating lengthy explanations. Use consistent names for common capabilities across chart pages, while keeping chart-specific API names and behavior clear.

### Table of contents

The `charts-docs` renderer automatically shows an **On this page** list generated from page headings.
H2 headings appear as top-level entries, and relevant H3 capability headings appear nested under
their parent section. Use consistent, descriptive heading levels so the generated navigation reflects
the page structure. Avoid manually maintained TOC lists that duplicate this navigation.

### API links

Dokka output is published under `/static/api/{{version}}/`; `{{version}}` follows the wiki's existing version placeholder convention. The generated chart pages are under `charts/<package>/`, with the composable page named for its symbol and defaults/style pages nested under their generated symbol directory. For each page, copy the exact relative paths from generated output (snapshot output is under `docs/static/api/snapshot/`) and verify the linked symbol exists. Do not construct an anchor or page name from memory.

## Code examples

Use these terms precisely:

- **Complete example:** self-contained Kotlin usage a reader can copy, including required imports, data, and composable context.
- **Focused excerpt:** a short example that relies on earlier context and says what it reuses.
- **Compiled example:** Kotlin source that has actually passed the relevant compilation check.
- **Verified documentation snippet:** displayed Markdown whose contents have been checked against its compiled Kotlin source.

Do not call a Markdown example compile-verified just because a related sample, GIF source, or application compiles. State verification only when both compilation and Markdown/source consistency have actually been checked. If verification is not established, do not imply it; the page does not need a status label for every snippet.

Use `kotlin` fenced code blocks. Give complete examples a short descriptive composable name, such as `ShowMonthlyRevenue`; keep names consistent if the page links the example to a source file. Include only imports needed by the example, and keep each code block focused on the capability being taught. Preserve comments that explain a meaningful choice, remove comments that merely narrate syntax, and prefer readable formatting over compressed lines. Avoid unnecessary state, selection setup, animation, styling, or application configuration in Basic Usage. Add these only when they explain the chart behavior under discussion. Use Compose conventions and public HDCharts APIs.

### Compile-verified wiki examples

Canonical examples live in `sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/`. Mark each managed snippet with paired `// BEGIN WIKI EXAMPLE: <id>` and `// END WIKI EXAMPLE: <id>` comments; the Markdown destination uses matching HTML comments with a repository-relative source path and identifier. The synchronizer discovers managed regions by scanning `docs/wiki/`; do not maintain a separate example inventory in this guide. Keep each Kotlin file valid, including its package and imports. Run `python3 scripts/sync-wiki-examples.py` to update Markdown and `python3 scripts/sync-wiki-examples.py --check` to verify it without writing. The sources are compiled separately by `./gradlew ciCompile`.

## Visual references

Every chart page has at least one representative static chart image unless the page explains why a static image would mislead or add no value. Use an approved screenshot-test baseline as the source. The canonical Kotlin documentation composable must render the screenshot; do not duplicate chart rendering in screenshot tests or maintain an image approximation separately.

Use deterministic data, explicit selection, and settled animation state. Light landscape is the default. Keep labels, titles, and legends readable and inside the canvas. Circular charts may use a centered, chart-appropriate size instead of filling the landscape canvas. Keep layout sizing in the screenshot preview and pass a modifier to reusable example composables where appropriate.

Add capability screenshots only when they explain a meaningful visual state or option that the Basic Usage image, an existing GIF, and the prose do not already communicate. Keep the Kotlin example before its screenshot and use concise alt text that describes the visible chart and state. Use version-aware asset paths such as `/content/{{version}}/wiki/assets/<name>.png`.

Use GIFs when animation, interaction, or state transitions add educational value. Static behavior does not require a GIF. Preserve the recording-source reference, explain the relevant API when the GIF demonstrates a capability, and state when recording data or setup differs from the teaching example. Do not add a GIF that repeats an existing static visual more effectively.

## Reusable page skeleton

Copy this skeleton and replace every bracketed placeholder before publication. Remove optional sections and their TOC links when they do not apply. Do not publish placeholder text or invent API names or URLs.

````markdown
---
title: "Chart name"
---

# [Chart name]

## Overview

[What the chart represents, intended use, and meaningful limitations.]

## Basic Usage

[Optional visual reference that helps explain the basic chart.]

Add the imports needed by this example at the top of the Kotlin block. If useful, define a meaningful application model after the imports and before the composable; it may carry properties used for chart data and styling. Do not add a model just to make the example longer. Replace the body placeholder with deterministic data setup and the real public chart call. Choose a realistic example rather than minimizing line count.

```kotlin
[Required imports]

[Optional application domain model]

@Composable
fun ShowChartExample() {
    [Deterministic data setup and public chart usage]
}
```

## Data Mapping

[Optional: map useful application data to chart inputs. Omit when Basic Usage
already explains the data contract.]

## Capabilities

### [Descriptive capability name]

[Explain one behavior. State when the snippet reuses data from Basic Usage.]

```kotlin
[Focused excerpt demonstrating this capability]
```

[Optional relevant screenshot or GIF. For a GIF, link the complete recording
source when available and explain any difference from the teaching example.]

## API Reference

- [Public composable](/static/api/{{version}}/charts/PACKAGE/SYMBOL_PAGE.html)
- [Relevant defaults or style API](/static/api/{{version}}/charts/PACKAGE/DEFAULTS_PAGE/index.html)
````

In the template, the `Data Mapping` and `Capabilities` sections are optional; include them only when useful. Remove optional section placeholders when their sections are omitted. `Overview`, `Basic Usage`, and `API Reference` are required. Use `###` capability headings directly under `## Capabilities`; do not add an `Advanced Usage` section that combines unrelated options. The generated navigation nests relevant H3 capability headings under their parent. Replace `Chart name` in the quoted frontmatter title and replace every other placeholder with final content before publication. Replace API path placeholders with paths verified against generated output.

## Completion checklist

- [ ] Overview, Basic Usage, relevant capabilities, and API Reference are present in that order; the site generates heading navigation automatically.
- [ ] Overview and capability claims match current public chart behavior.
- [ ] Basic Usage is readable, complete, deterministic, and uses public APIs and sensible defaults.
- [ ] Each focused example teaches one capability and identifies reused context.
- [ ] A representative static screenshot uses an approved baseline and matches its canonical Kotlin example; capability visuals add distinct value.
- [ ] GIFs teach motion, interaction, or transitions, retain their recording-source reference, and explain the demonstrated API.
- [ ] API references point to the correct generated, versioned documentation.
- [ ] Verification wording reflects checks actually performed; no Markdown snippet is called verified based only on a related source compiling.

## Verification commands and ownership

Run the checks that cover the files changed:

| Requirement | Command or workflow | Responsibility |
| --- | --- | --- |
| Compile canonical Kotlin examples | `./gradlew ciCompile` | Contributor locally for Kotlin changes; CI also compiles changed code. |
| Check Kotlin formatting | `./gradlew ktlintCheck` | Contributor locally for Kotlin changes; CI runs the check. |
| Synchronize Kotlin snippets into Markdown | `python3 scripts/sync-wiki-examples.py` | Contributor runs after editing canonical examples. |
| Verify snippet synchronization without writing | `python3 scripts/sync-wiki-examples.py --check` | Contributor and CI (`Prepare PR`). |
| Run synchronizer regression tests | `python3 -B .github/scripts/tests/test_sync_wiki_examples.py` | Contributor and CI (`Prepare PR`). |
| Generate new screenshot baselines | `./gradlew :androidApp:updateDebugScreenshotTest` | Contributor, limited to new previews; inspect generated images and preserve unrelated references. |
| Validate screenshot baselines | `./gradlew :androidApp:validateDebugScreenshotTest` | Contributor locally; CI validates screenshot tests. |
| Validate wiki screenshot asset sync | `bash .github/scripts/sync-wiki-screenshots.sh --self-test` | Contributor and CI (`Prepare PR`). |
| Synchronize wiki pages and approved assets | `bash .github/scripts/sync-wiki-docs.sh --self-test` and the release/snapshot documentation workflow | Contributor checks the sync script; release/snapshot workflow publishes synced documentation and assets. |
| Validate documentation links | Documentation CI link validation | CI owns published-site link checking; verify API targets locally against `docs/static/api/snapshot/`. |
| Check whitespace errors | `git diff --check` | Contributor before handoff. |

Compilation, synchronization, asset byte identity, and link targets are objectively verifiable. Screenshot value, example realism, and whether an existing GIF already teaches a state require author judgment; explain those choices briefly in the change when useful. Do not build a new validation framework for subjective presentation rules.
