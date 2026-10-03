---
title: Naming and File Structure
order: 7
---

# Naming and File Structure

Every chart is one public composable plus the internals behind it. These rules name both, so a
reader can tell what a file holds from its name alone. Follow them when adding a chart, adding a
public composable to a chart module, or moving code between layers.

The rules exist because the naming drifted. Five charts once declared an internal composable with
the same name as their public one, and two of those needed an import alias or a shadowing import to
call it. Neither was visible at the call site.

## Public Composables

One public composable per file. The file is named after it and holds nothing else public.

| File | Composable | Module |
| --- | --- | --- |
| `LineChart.kt` | `LineChart` | `charts-line` |
| `LiveLineChart.kt` | `LiveLineChart` | `charts-line` |
| `BarChart.kt` | `BarChart` | `charts-bar` |
| `HistogramChart.kt` | `HistogramChart` | `charts-histogram` |
| `PieChart.kt` | `PieChart` | `charts-pie` |
| `RadarChart.kt` | `RadarChart` | `charts-radar` |
| `StackedBarChart.kt` | `StackedBarChart` | `charts-stacked-bar` |
| `StackedAreaChart.kt` | `StackedAreaChart` | `charts-stacked-area` |

A second public composable in the same module gets its own file. `charts-line` has two, because
live line and the regular line chart are different charts that share internals.

A style file is named after the chart it styles: `LineChartStyle.kt` holds `LineChartStyle` and its
blocks. See Style Defaults for how the styles are built.

## Internal Composables

An internal composable never repeats the name of the public composable it implements. The public
name belongs to the public composable.

The suffix says what the composable does. There are four roles, and a composable that needs two of
them should be split.

| Role | Suffix | What it holds | Example |
| --- | --- | --- | --- |
| Canvas | `Content` | A canvas and the drawing calls on it | `BarChartContent` |
| State and interaction | `Impl` | Density mode, zoom, animation values, the header; delegates drawing | `BarChartImpl` |
| Layout shell | `Frame` | Chrome slots — header, plot, legend — and no chart state. Not every frame has every slot | `LineChartFrame`, `StackedBarChartFrame` |
| Input preparation | `Entry` | Names the chart's policy, style values, clamping and conversion once, runs them through the shared `ChartEntry` seam, and hands over to the content | `LineChartEntry`, `BarChartEntry`, and one per chart — see the chains below |

A suffix outside this table is the signal that a composable is doing two jobs.

A boundary that a second chart calls is named as a distinct noun rather than with a suffix.
`BarChartInternalPlot` is shared by bar and histogram and is neither bar's entry nor bar's canvas.

### The chains

Every chain ends at the composable that owns the canvas, which is a `Content` everywhere except the
two stacked charts.

| Chart | Chain |
| --- | --- |
| Line | `LineChart` → `LineChartEntry` → `LineChartImpl` → `LineChartFrame` → `LineChartContent` |
| Live line | `LiveLineChart` → `LineChartEntry` → `LiveLineChartImpl` → `LineChartFrame` → `LineChartContent` |
| Bar | `BarChart` → `BarChartEntry` → `BarChartInternalPlot` → `BarChartImpl` → `BarChartContent` |
| Histogram | `HistogramChart` → `HistogramChartEntry` → `BarChartInternalPlot` → `BarChartImpl` → `BarChartContent` |
| Pie | `PieChart` → `PieChartFrame` → `PieChartContent` |
| Radar | `RadarChart` → `RadarChartEntry` → `RadarChartContent` |
| Stacked bar | `StackedBarChart` → `StackedBarChartEntry` → `StackedBarChartFrame` → `StackedBarChartImpl` |
| Stacked area | `StackedAreaChart` → `StackedAreaChartEntry` → `StackedAreaChartFrame` → `StackedAreaChartImpl` |

Radar and pie lay their plot out with `ChartSquarePlotLayout` from `charts-core`, which supplies
the square plot, the header, and the legend.

The stacked charts are the two that do not follow the rest: their `Frame` wraps the `Impl` and hands
it the plot slot, where line and bar put the `Frame` inside the `Impl` and hand it a content slot.
Neither stacked chart has a `Content`, so its `Impl` draws on the canvas itself.

Every chart except pie has an `Entry`: it names the chart's policy, style values, clamping and
conversion once, and runs them through the shared `ChartEntry` seam. An entry with one public
composable calls the content itself; an entry shared by two takes the content as a lambda. Bar and
histogram each have their own entry, because their policies differ, and then share
`BarChartInternalPlot` and `toBarRenderData`.

## Two Public Composables in One Module

Line is the only module with two, and the split is at the input boundary, not the draw boundary.
`LineChartImpl` and `LiveLineChartImpl` are separate, because live line has no selection and a
window that shifts. They share `LineChartContent`, because both draw the same way.

When a module gets a second public composable, copy that split: each gets its own input handling,
and they share the content. State which stage the split falls at in the file's KDoc.

## Files

| File | Holds |
| --- | --- |
| `<Name>Chart.kt` | The public composable, and the private helpers only it uses |
| `<Name>ChartStyle.kt` | The chart's style and its blocks |
| `<Name>ChartPreviews.kt` | Previews for one public composable |
| `internal/<Name>ChartEntry.kt` | Input preparation for one public composable |
| `internal/<Name>ChartImpl.kt` | State and interaction for one public composable |
| `internal/<Name>ChartContent.kt` | The canvas and its drawing |
| `internal/<Name>ChartFrame.kt` | A layout shell, when more than one composable calls it |
| `internal/<Name>ChartHelpers.kt` | Pure functions for one chart |
| `internal/<Name>ChartDrawing.kt` | Drawing helpers kept apart from the canvas composable |

Previews follow the public composable they preview, so a second public composable gets its own
`Previews` file. `LineChartPreviews.kt` currently previews both line composables and should be
split.

A file with one narrow concern names that concern: `StackedBarDensity.kt`,
`StackedBarInteraction.kt`, `StackedAreaDensity.kt`. A file of pure functions for a chart uses
`<Name>ChartHelpers.kt`, which line, bar, pie, and radar all do.

A `Frame` gets its own file when more than one composable calls it, which is why
`LineChartFrame.kt` exists. A frame with one caller stays in the file that owns it, which is why
`PieChartFrame` sits inside `PieChart.kt`.

### Test files

Test source sets hold two kinds of file with no `@Test`, named differently.

- **Shared fixtures** take the chart family prefix: `LineTestFixtures.kt` in `charts-line`,
  `StackedBarTestFixtures.kt` in `charts-stacked-bar`. One file per module, holding the data,
  colours, and builders that module's tests share. Never name one `*Test.kt` — nothing treats that
  suffix as a suite unless the file holds tests.
- **A test helper** is named for what it does, like any other file: `PixelCapture.kt` holds
  `setCapturedContent`.

## Module Structure

`charts-core` holds everything a chart shares, and a chart module holds only what is its own. The
shared code is organised by concern, in `internal/` sub-packages: `axis`, `bezier`, `composable`,
`density`, `drawing`, `interaction`, `layout`, `model`, `palette`, and `theme`, plus the flat files
`InternalChartsApi.kt`, `DataValidation.kt`, `StyleClamping.kt`, `ChartEntry.kt`, `ChartPolicy.kt`,
`ChartSpec.kt`, `Constants.kt`, and `AnimationSpec.kt`.

A chart module's own `internal/` package stays flat. Sub-package it by stage only once it holds more
than about ten files, and match the core naming when you do.

Everything that is not public API should carry `@InternalChartsApi`, a `@RequiresOptIn` at error
level declared in `InternalChartsApi.kt`. `charts-core` applies it across its own `internal/`
packages, and a chart module applies it to anything another chart module calls.

Five composables in `internal/composable/` do not carry it yet, so they are unrestricted public API
of the published `charts-core` artifact: `ChartErrors`, `Legend`, `LegendItems`,
`rememberShowState`, and `rememberAnimationState`. Their siblings in the same directory are
annotated. `rememberAnimationState` has no production caller at all and should be deleted rather
than annotated.

Keep one boundary composable per shared plot, and have every chart that needs that plot call the
boundary rather than the other chart's internals. Histogram calls `BarChartInternalPlot` and
`toBarRenderData` for this reason, and never reaches into `charts-bar` any further. A boundary is
named as a distinct noun, not with a role suffix, because it is none of the four roles on its own.

## Adding a Chart or a Public Composable

- Name the public composable after the file, and give it its own file.
- Give each internal composable the suffix for its role, and give it a file to match.
- Declare the chart's `ChartPolicy` in its entry file, and add its row to
  `ChartPolicyConformanceTest` in the `charts` module. Entry Seam and Chart Policy has the fields
  and the per-chart table.
- Never repeat the public name on an internal composable, and never alias or shadow an import to
  work around a repeat.
- Give the content composable the canvas, and keep state out of it.
- If a second chart will use part of this, put that part behind an `@InternalChartsApi` boundary
  with a distinct name.
- Follow the style rules in Style Defaults and the clamping rules in Style Clamping for anything
  the user can set.

## Tests

| Behavior | Tests |
| --- | --- |
| An internal composable never repeats a public name | `grep -rn "^internal fun [A-Z][A-Za-z]*Chart(" charts-*/src/commonMain` returns nothing |
| Every chart draws through its content composable | The screenshot suite, one directory per test file under `sample/androidApp` |
| Style blocks clamp | `StyleClampingTest` in `charts-core/src/commonTest`, plus a `*_withInvalidNumericStyleValues_drawsClampedChart` test per chart |
| Non-public internals are opted in | `grep -rn "@InternalChartsApi" charts-*/src/commonMain` |

Naming rules are checked by reading, not by a test. The grep in the first row is the closest thing
to one, and it is worth running before opening a pull request.

## Known Issues

Known issues and limits are listed in the Known Issues section.