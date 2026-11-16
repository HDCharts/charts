---
title: Naming and File Structure
order: 10
---

# Naming and File Structure

Name files and composables by their responsibility. Use these rules when adding a chart or moving
code between layers.

## Public composables

Give each public chart composable its own file with the same name: `LineChart` lives in
[LineChart.kt](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonMain/kotlin/io/github/hdcharts/line/LineChart.kt), and `LiveLineChart` lives in [LiveLineChart.kt](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonMain/kotlin/io/github/hdcharts/line/LiveLineChart.kt). A module can expose several charts.
Each chart gets its own entry and style where its input or presentation differs.

Name a style file after the chart it styles. [LineChartStyle.kt](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonMain/kotlin/io/github/hdcharts/line/LineChartStyle.kt), for example, holds
`LineChartStyle` and its blocks. [Style Defaults](style-defaults.md) describes their factories.

## Internal composables

Use a suffix that describes the composable's role:

| Role | Suffix | Example |
|---|---|---|
| Input preparation | `Entry` | `LineChartEntry` validates, converts, and clamps before content |
| State and interaction | `Impl` | `LineChartImpl` owns chart state and delegates layout and drawing |
| Layout shell | `Frame` | `LineChartFrame` places the header, plot, and legend |
| Canvas and drawing | `Content` | `LineChartContent` draws the chart |

Keep internal names distinct from public composables so call sites need no import alias or
shadowing import. Split responsibilities when that makes ownership clearer.

A boundary shared by charts gets a distinct name. `BarChartInternalPlot` is the shared plot used
by bar and histogram; each keeps its own entry because their validation policies differ.

### Sharing internals

Share the layer whose behavior is shared. `LineChart` and `LiveLineChart` have different state
handling but draw through `LineChartContent`. Separate charts in a module can also keep separate
content, as the gauge charts do.

Indexed-series entries delegate to `ChartEntry`. Other data models own input preparation in their
chart entry. An entry can accept a content lambda when callers share preparation and supply
different content. See [Entry Seam and Chart Policy](entry-seam.md).

The stacked chart frames supply the plot slot to their implementation. Follow the ownership rules
in [Pipeline and Stage Ownership](pipeline.md) when changing that layout.

## Files

| File | Holds |
|---|---|
| `<Name>Chart.kt` | Public composable and private helpers used only there |
| `<Name>ChartStyle.kt` | Chart style and its blocks |
| `<Name>ChartPreviews.kt` | Previews for the public chart |
| `internal/<Name>ChartEntry.kt` | Input preparation |
| `internal/<Name>ChartImpl.kt` | Chart state and interaction |
| `internal/<Name>ChartContent.kt` | Canvas and drawing |
| `internal/<Name>ChartFrame.kt` | Reusable layout shell |
| `internal/<Name>ChartHelpers.kt` | Pure helpers |
| `internal/<Name>ChartDrawing.kt` | Drawing helpers |

A file with a narrower concern names it, such as [StackedBarDensity.kt](https://github.com/HDCharts/charts/blob/main/charts-stacked-bar/src/commonMain/kotlin/io/github/hdcharts/stackedbar/internal/StackedBarDensity.kt). Keep a private helper or
layout in its caller's file while that keeps the code easy to follow.

### Test files

- Put shared fixtures in a file with the chart-family prefix, such as [LineTestFixtures.kt](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonTest/kotlin/io/github/hdcharts/line/LineTestFixtures.kt).
- Name test helpers for their job, such as [PixelCapture.kt](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonTest/kotlin/io/github/hdcharts/line/PixelCapture.kt).
- Use `*Test.kt` for files that contain tests.

## Module structure

`charts-core` holds shared code, organized by concern under `internal/`. A chart module owns its
chart-specific code. Keep a small internal package flat; split it by concern when navigation
becomes difficult.

Use Kotlin visibility for implementation details. Cross-module implementation APIs need
`@InternalChartsApi`, the error-level opt-in declared in [InternalChartsApi.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/InternalChartsApi.kt). Check existing
consumers and compatibility before changing the visibility or opt-in of a published declaration.

Expose a shared plot through a boundary such as `BarChartInternalPlot`. Callers use that boundary
to keep dependencies on the other chart's internals contained.

## Adding a chart or public composable

- Give the public composable and its internal layers names that match their roles.
- Choose its input contract using [Entry Seam and Chart Policy](entry-seam.md).
- Cover validation, selection, and rendering behavior in the chart's tests. Indexed-series policy
  expectations live in [ChartPolicyConformanceTest](https://github.com/HDCharts/charts/blob/main/charts/src/commonTest/kotlin/io/github/hdcharts/charts/ChartPolicyConformanceTest.kt) in the umbrella `charts` module.
- Follow [Style Defaults](style-defaults.md) and [Style Clamping](style-clamping.md) for styles.
- Update the guide that owns any new rule or exception.

Check naming and visibility during code review. Rendering tests verify the behavior behind those
boundaries.
