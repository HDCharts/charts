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

The `charts-docs` renderer automatically shows an **On this page** list for each chart page, generated from its `##` headings. Use that navigation for the standard page sections; do not add a second manual list of those links. The generated list currently omits `###` capability subsections. If a page has important capability subsections that readers should be able to reach directly, add a compact manual **On this page** list with links to those subsections only. Keep links limited to headings present on the page and capability links one level deep. If the renderer later includes the needed subsections, remove the manual list.

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

## Visual references

Add a static chart screenshot when it clarifies chart structure, labels, or a visual option that is difficult to explain in prose. Link a GIF when motion or interaction is itself instructive, such as animation, selection, or live updates. A visual is optional; do not create one for every capability.

Keep the Basic Usage example minimal even when a GIF uses a longer or more interactive scenario. Say when the recording uses different data or setup, and link its complete recording source when available. The source link identifies the recording implementation; it does not verify a separate Markdown snippet.

## Reusable page skeleton

Copy this skeleton and replace every bracketed placeholder before publication. Remove optional sections and their TOC links when they do not apply. Do not publish placeholder text or invent API names or URLs.

````markdown
---
title: "Chart name"
---

# [Chart name]

[Optional manual On this page list linking to important capability subsections not included in the generated navigation.]

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

In the template, the `Data Mapping` and `Capabilities` sections are optional; include them only when useful. Remove optional section placeholders when their sections are omitted. `Overview`, `Basic Usage`, and `API Reference` are required. Use `###` capability headings directly under `## Capabilities`; do not add an `Advanced Usage` section that combines unrelated options. The generated TOC covers the H2 sections; include the optional manual list only for important H3 capability links it omits. Replace `Chart name` in the quoted frontmatter title and replace every other placeholder with final content before publication. Replace API path placeholders with paths verified against generated output.

## Completion checklist

- [ ] Required sections and heading order are present; optional sections add clear value.
- [ ] Overview and capability claims match current public chart behavior.
- [ ] Basic Usage is readable, complete, deterministic, and uses public APIs and sensible defaults.
- [ ] Each focused example teaches one capability and identifies reused context.
- [ ] Visuals clarify the explanation; GIF recording source is linked when available.
- [ ] API references point to the correct generated, versioned documentation.
- [ ] Verification wording reflects checks actually performed; no Markdown snippet is called verified based only on a related source compiling.
