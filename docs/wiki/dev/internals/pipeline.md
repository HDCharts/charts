---
title: Pipeline and Stage Ownership
order: 1
---

# Pipeline and Stage Ownership

The fifteen stages below describe the chart pipeline. This page records which stages each chart
uses and where they run. Entry Seam describes the shared `ChartEntry` contract for indexed data and
the chart-owned entries used by scatter and heatmap.

The pipeline splits in two. Stages 1 to 4 turn public data and a style into a render model and a
drawable style, and run once before anything is drawn. Stages 8 to 15 work in pixels — a measured
size, the clock, a pointer — and run in the chart's content composable. Stages 5 to 7 and 11 to 12
sit either side of that split and are the interesting ones.

## The stages

| # | Stage | Canonical code | Runs in |
| --- | --- | --- | --- |
| 1 | Selection lifecycle | `rememberSelectionLifecycle` | Public composable or chart entry |
| 2 | Validation | `ChartPolicy.errorsFor`, `ScatterChartPolicy.errorsFor`, `HeatmapChartPolicy.errorsFor` | Entry |
| 3 | Style clamp | `style.clamp(density)`, through `ChartSpec` for indexed data | Entry |
| 4 | Model conversion | `ChartSpec.convert` → `ChartRenderData` | Seam |
| 5 | Density decision | `shouldUseScrollableDensity`, `maxBarsThatFit` | Content |
| 6 | Aggregation | `aggregateForCompactDensity` | Content |
| 7 | Palette | `resolvePaletteColors` | See [Palette](#palette) |
| 8 | Chrome layout | `BoxWithConstraints` and the header, plot and legend slots | Content |
| 9 | Axis planning | `planAxisXLabelStride`, `buildNumericYAxisTicks` | Content |
| 10 | Canvas limit guard | `chartCanvasFits` | Content |
| 11 | Domain | `resolveLineRange`, `resolveBarRange`, `minMax`, `resolveStackedTotalsRange` | Content |
| 12 | Normalization | `normalizeByMinMax`, `barValueYFraction`, `normalizeStackedValues` | Content |
| 13 | Animation state | `update()` → `animateTo` for data transitions | Content |
| 14 | Gesture math | `selectedIndexForTouchX`, `nearestPointIndexForContentX` | Content |
| 15 | Draw | `drawLineChartSeries` → `drawChartPath` | Content |

## Rules

These four are the contract. Everything above is a description of them.

**1. Stages 2 and 3 happen at the entry point, once.** Indexed-series checks live in `charts-core`,
selected by `ChartPolicy` and `SeriesRule`. Scatter owns its X/Y checks in `ScatterChartPolicy` and
heatmap owns its matrix checks in `HeatmapChartPolicy`; both reuse shared axis label validation.
Every entry validates before clamping the style at the ambient density and calling content.

**2. A required model conversion happens once, between the entry and content.** Indexed `ChartData`
becomes `ChartRenderData` through the shared seam. Scatter and heatmap pass their validated data
directly to their content and need no conversion stage.

**3. Stages 8 to 10 and 13 to 15 happen inside the content composable, because they work in
pixels.** Layout, axis planning, the canvas guard, animation, gesture maths and drawing all need a
measured size, the clock, or a pointer. A stage that needs a measured size is never hoisted to the
entry.

**4. Formatting reads the caller's formatter and is never hardcoded in a content composable.** A
chart without a formatter parameter falls back to its `Defaults`.

All four rules hold for every chart.

## The test for a stage that is not obviously placed

> Is this stage a pure function of the public data, the style and the density — or of a measured
> size, the clock, or a pointer?

Pure in the first: decided once, cannot change while the chart is on screen, belongs in the seam.
Pure in the second: belongs in the content composable, because hoisting it would recompute it on
every size change or read a value that does not exist yet.

Two costs sit on top of purity. A stage that walks the data costs a pass per data change, so a hoist
has to be justified against the passes already there; and `ChartEntry` must not walk the data twice
for the same fact, which is why validation checks finiteness and the conversion does not check it
again.

## Which chart runs which stage

| Stage | Line | Bar | Histogram | Stacked bar | Stacked area | Radar | Pie | Ring gauge | Scatter / bubble | Heatmap | Waterfall |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 Selection lifecycle | yes | yes | shared | yes | yes | yes, twice | yes | yes | entry | entry | entry |
| 2 Validation | yes | yes | shared | yes | yes | yes | own path | yes | `ScatterChartPolicy` | `HeatmapChartPolicy` | `WaterfallChartPolicy` |
| 3 Clamping | yes | yes | shared | yes | yes | yes | yes | yes | entry | entry | entry |
| 4 Conversion | pass-through | pass-through | pass-through | transposes | pass-through | pass-through | none | pass-through | — | — | bar list in entry |
| 5 Density decision | yes | yes | shared | yes | yes | — | — | — | — | — | — |
| 6 Aggregation | yes | yes | shared | yes | yes | — | — | — | — | — | — |
| 7 Palette | frame | own colour | shared | entry | entry | entry | own file | entry | content | color scale in content | content |
| 8 Chrome layout | `LineChartFrame` | `BarChartContent` | shared | `StackedBarChartFrame` | `StackedAreaChartFrame` | inline | `PieChartFrame` | inline | `ChartPlotLayout` | `ChartPlotLayout` | `ChartPlotLayout` |
| 9 Axis planning | yes | yes | shared | yes | yes | labels only | — | range labels only | shared X/Y layouts | category labels on shared layouts | shared X/Y layouts |
| 10 Canvas guard | yes | yes | shared | yes | yes | — | — | — | — | — | — |
| 11 Domain | `resolveLineRange` | `resolveBarRange` | shared | totals | totals | `minMax` | share-based | `style.range` | `scatterDomain` | `heatmapColorMapping` | `waterfallRange` |
| 12 Normalization | list | scalar per bar | shared | list | list | list | share-based | `gaugeFraction` per ring | X/Y positions and bubble radii | color fraction per cell, interpolated strips | rect per bar |
| 13 Animation | yes | yes | shared | yes | yes | yes | yes | yes | initial reveal | initial reveal | initial reveal |
| 14 Gesture | yes | yes | shared | yes | yes | yes | yes | yes | `nearestScatterPointIndex` | `HeatmapPlotLayout.cellIndexAt` | `waterfallHitIndex` |
| 15 Draw | yes | yes | shared | yes | yes | yes | yes | yes | `ScatterChartContent` | `HeatmapChartContent` | `WaterfallChartContent` |

Bar and histogram share one plot, so they share every stage, compact aggregation included. Their
specs differ only in the policy: histogram forbids negative bin heights.

## Recorded divergences

Each of these is a decision, written down so the next change reads it.

**Pipeline shapes.** Radar, pie and ring gauge run stages 1 to 4, 7, 8 and 11 to 15. None of
them compacts, so they skip the density decision and aggregation, and none runs the Cartesian canvas
guard. Radar labels its own axes in place of stage 9, and ring gauge draws its two range labels; pie
has no axis and skips it. Line, bar, histogram and the stacked charts run all
fifteen. A Cartesian chart with a compact mode and a polar or share-based chart are presented as one
pipeline, and this is the shape the library has. Pie runs the same stages 1 to 4 as every other
indexed chart, through `PieChartEntry`.

Scatter owns its input stages in `ScatterChartEntry`. It shares error UI, palette resolution and
Cartesian axis layouts, and skips conversion, density selection, aggregation, the canvas guard and
data-transition animation. `ScatterChartContent` still reveals its points on initial display using
`animateFloatAsState`, controlled by `animateOnStart`. `ScatterMath.kt` owns its domain, positions,
bubble radii and nearest-point lookup.

Heatmap owns its input stages in `HeatmapChartEntry` and has the same shape as scatter. Its value
domain is a color-scale domain rather than an axis: `heatmapColorMapping` resolves it from the present
cells and the optional color-scale bounds. `HeatmapMath.kt` owns the color mapping, the
shape-preserving bicubic field, the interpolated gradient strips, cell geometry and hit testing. The
field uses Fritsch-Butland slopes at cell centers and clamps each patch to its corner values, so it is
smooth without overshoot. The strips depend on the plot
size, so they are remembered in content against the data, color mapping and layout, never per frame.

Waterfall owns its input stages in `WaterfallChartEntry`, in the same shape as scatter and heatmap.
Its entry derives the floating bars from the validated entries once, the one conversion-like step
among the custom-model charts, and content derives its per-entry colors from the clamped style.
`WaterfallMath.kt` owns the bar geometry, pixel bounds and hit testing. There is no compaction and
no canvas guard: every bar fits the viewport by shrinking, capped by `maxBarWidth`.

**Domains.** Bar and both stacked charts fold zero into
the domain, so an all-positive series starts at the axis. Line and radar take the data's own range.
Line, bar, histogram and waterfall accept a caller's fixed range through `style.range`. Stacked area's
domain
is a line inside its normalizer, so finding it means opening the normalizer. Pie's axis is the caller's
own share, not a data range, which is why its matrix row reads share-based. Ring gauge reads only
`style.range` and never the data's range, so a value outside it stops at the nearest end of the arc. A domain is a pair of
doubles and reads no constraint, so by the test above it could sit in the seam; whether it should is
open.

Scatter resolves separate X and Y domains from its points and optional bounds through `scatterDomain`.
Heatmap resolves one value domain for its color scale, and draws a constant domain with the middle
color. Waterfall folds zero and every intermediate balance into `waterfallRange`, so its floating
bars and connectors stay inside the plot; an all-zero dataset draws against a `0..1` fallback, like
bar's.

**Flat data draws at a different height per chart.** Line passes `zeroRangeValue = 0f` and draws a
flat series at the bottom. Radar passes `1f` and draws it at full radius. Both domains are the same
shape.

**Normalization has two shapes.** Line's is a list per series, because it animates between two of
them. Bar's is a per-bar scalar computed at draw time. Each is the shape its chart needs.

#### Palette

`resolvePaletteColors` is pure in the clamped style and an item count, so by the test above it belongs
at the seam. Each style exposes a thin wrapper over it — `resolveColors` in line, stacked bar and
stacked area, `resolveLineColors` in radar — and the pie and ring gauge wrappers take a slice or ring
count instead of a series count. Stacked bar (`StackedBarChartEntry`), stacked area
(`StackedAreaChartEntry`), radar (`RadarChartEntry`), pie (`PieChartEntry`) and ring gauge
(`RingGaugeChartEntry`) resolve inside their entry. Line resolves it in
`rememberLineColors`, which is declared in `LineChartFrame` and called from `LineChartImpl` and
`LiveLineChartImpl`, one frame above where it is declared.

Scatter resolves its series palette in `ScatterChartContent` through `ScatterPointStyle.resolveColors`.
Heatmap has no series palette; its color scale maps values to colors in `HeatmapChartContent`.

`singleItemUsesBase` is a boolean literal at the call sites. A single-series chart draws
its base colour on line, stacked area and radar, and a generated shade on pie, stacked bar and ring
gauge.

Bar does not use the palette helper. It repeats one colour across its bars and keeps two values: the
caller's `colors` when they set any, and `bars.color` as the fallback for every bar when they set
none. Compact mode reads the caller's palette at the bucket centres in source index space, because a
bucket is drawn where its centre was. Both values reach the draw site, which reads
`barColors.getOrNull(index) ?: defaultBarColor`. `BarBarsStyle.resolveColors` is the shape the other
charts use and has no production caller — `BarStyleBlocksTest` is its only one.

## What deliberately stays in the content composable

- **The domain and normalization when a caller's fixed range can clip the data.** Line resolves its
  domain from `style.range`, so the clamp at the end of `normalizeValue` is what keeps a line inside
  a plot whose range excludes its highest value. Validation Errors has the case.
- **Animation buffers.** The morph's two buffers are deliberately different lengths mid-animation, so
  its lookup has to tolerate the shorter one.
- **Axis planning.** `AxisXPlanRequest` is mostly measured input, and the plan is recomputed on
  every resize anyway.

## Adding a stage or a chart

A new chart records its stages in the matrix above, including every skipped stage. Its data model
determines whether it uses the indexed `ChartSpec` seam or owns validation and clamping in its entry.

A new stage goes where the test puts it, and a stage whose behaviour differs between charts is
declared in the chart's `ChartSpec` for indexed data, or in its own entry contract for another data
model. A policy field nothing reads is the drift the seam exists to remove, so a field lands with
the change that reads it.
