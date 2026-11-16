---
title: Validation Errors
order: 3
---

# Validation Errors

A chart displays errors through `ChartErrors` when its input has no correct fallback. Validation
runs at the entry, before conversion and content.

## What to correct and what to report

Correct drawable style values such as alpha, spacing, and size through [Style
Clamping](style-clamping.md). Report malformed data and settings whose meaning cannot be inferred,
such as non-finite range bounds or invalid axis labels.

A negative spacing can safely become zero. A non-finite data point has no meaningful replacement,
so the entry reports it and leaves the plot undrawn. Invalid chart input produces messages;
programmer arguments such as formatter precision can still have argument checks.

## Validation rules

| Input | Checks |
|---|---|
| Indexed series | Series count, minimum values, category alignment, finite values, and the policy's negative-value rules |
| Explicit palette | Count matches the items it colors when the policy enables matching |
| Stacked series | Intermediate totals stay finite |
| Scatter points | Non-empty series and points, required series names, finite X/Y values, valid bubble sizes, palette count, and axis bounds |
| Heatmap matrix | Labels and matrix dimensions agree, present values are finite, at least one value exists, and the color scale is valid |
| Waterfall entries | Entries exist, changes are finite, and running balances stay finite |
| Range and axis labels | Bounds are finite and label settings can be drawn |
| Chart-specific settings | Additional requirements such as Segmented Ring Gauge's capacity and segment total |

Current minimums and indexed error messages live in
[`DataValidation.kt`](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/DataValidation.kt).
Chart policies select the applicable rules. Model-specific messages live with their chart's entry.
Use [Entry Seam and Chart Policy](entry-seam.md) to choose the validation path.

## Error collection

The shared indexed policy checks data shape, range, then axis labels. It collects enabled errors
where inputs are readable. Shape errors can stop checks that depend on that shape: a missing series
has no values to inspect, and the single-series validator stops at its first shape problem.

Multi-series validation reports bad values by series. Single-series validation identifies values
by index. Scatter and Heatmap collect each violated rule once across their points or cells;
Waterfall stops at the first non-finite change or running total. A missing Heatmap cell is valid
when the rest of the matrix contract holds.

An explicit palette is optional. A zero palette count skips matching; a nullable expected-color
rule can also skip matching for a particular dataset. Keep these decisions in the policy.

## Internal model

Indexed content reads `ChartRenderData`, which holds the validated `ChartData` and the title.
Its default conversion retains the caller's series and categories. A chart can convert their
shape when its renderer needs it, as stacked bar does.

Categories can be empty or aligned with the values. Blank categories are valid. Use
`resolveAxisLabel` for the axis-label fallback; [X-Axis Labels](x-axis-labels.md) describes it.

Validation establishes data invariants so conversion and drawing can rely on them. Keep guards
that handle other inputs: caller-chosen ranges, zero-sized viewports, zoom, and intermediate
animation states. For example, `normalizeValue` clips a line to its caller's fixed range even when
all data values are finite.

Finiteness is checked at the entry. Wrapping every numeric value in a value class inside a list
would add boxing costs, so the render model relies on validation and contract tests.

## Adding a rule

- Put shared indexed checks and messages in [DataValidation.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/DataValidation.kt); keep model-specific checks with
  their chart. Reuse existing range and axis-label validation.
- Declare optional shared checks through `ChartPolicy` and supply their style inputs.
- Keep messages short: identify the value or series and state what is expected.
- Cover the rule in validation tests and verify that invalid input displays errors before content.

## Tests

[DataValidationTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/DataValidationTest.kt) covers indexed checks and message text. [ChartPolicyTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/ChartPolicyTest.kt) covers policy
selection, and [ChartEntryTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/ChartEntryTest.kt) checks the boundary before content. [ChartRenderDataTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/model/ChartRenderDataTest.kt) covers
retained data, conversion, and the validated model's assumptions. Each chart tests its own input
contract and displayed errors.
