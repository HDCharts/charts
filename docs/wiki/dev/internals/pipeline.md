---
title: Pipeline and Stage Ownership
order: 9
---

# Pipeline and Stage Ownership

Every chart is the same fifteen stages. This page names them, says which stage belongs where, and
records where each chart differs. Entry Seam describes the four stages that run in `ChartEntry` and
how a chart declares them; this page is where a stage goes and why.

The pipeline splits in two. Stages 1 to 4 turn public data and a style into a render model and a
drawable style, and run once before anything is drawn. Stages 8 to 15 work in pixels — a measured
size, the clock, a pointer — and run in the chart's content composable. Stages 5 to 7 and 11 to 12
sit either side of that split and are the interesting ones.

## The stages

| # | Stage | Canonical code | Runs in |
| --- | --- | --- | --- |
| 1 | Selection lifecycle | `rememberSelectionLifecycle` | Public composable |
| 2 | Validation | `ChartPolicy.errorsFor` | Seam |
| 3 | Style clamp | `ChartSpec.clamp` → `style.clamp(density)` | Seam |
| 4 | Model conversion | `ChartSpec.convert` → `ChartRenderData` | Seam |
| 5 | Density decision | `shouldUseScrollableDensity`, `maxBarsThatFit` | Content |
| 6 | Aggregation | `aggregateForCompactDensity` | Content |
| 7 | Palette | `resolvePaletteColors` | See [Palette](#palette) |
| 8 | Chrome layout | `BoxWithConstraints` and the header, plot and legend slots | Content |
| 9 | Axis planning | `planAxisXLabelStride`, `buildNumericYAxisTicks` | Content |
| 10 | Canvas limit guard | `chartCanvasFits` | Content |
| 11 | Domain | `resolveLineRange`, `resolveBarRange`, `minMax`, `resolveStackedTotalsRange` | Content |
| 12 | Normalization | `normalizeByMinMax`, `barValueYFraction`, `normalizeStackedValues` | Content |
| 13 | Animation state | `update()` → `animateTo` | Content |
| 14 | Gesture math | `selectedIndexForTouchX`, `nearestPointIndexForContentX` | Content |
| 15 | Draw | `drawLineChartSeries` → `drawChartPath` | Content |

## Rules

These four are the contract. Everything above is a description of them.

**1. Stages 2 and 3 happen at the entry point, once.** Validation *logic* lives in `charts-core`;
which checks apply is the chart's `ChartPolicy`. The style is clamped at the ambient density before
any content runs.

**2. Stage 4 happens once, between the entry and the content composable.** Public `ChartData`
becomes the render model at exactly one seam. A chart does not convert at the entry and again
below.

**3. Stages 8 to 10 and 13 to 15 happen inside the content composable, because they work in
pixels.** Layout, axis planning, the canvas guard, animation, gesture maths and drawing all need a
measured size, the clock, or a pointer. A stage that needs a measured size is never hoisted to the
entry.

**4. Formatting reads the caller's formatter and is never hardcoded in a content composable.** A
chart without a formatter parameter falls back to its `Defaults`.

Rules 1, 2 and 3 hold for every chart. **Rule 4's exception** is
stacked bar, stacked area and radar, which format their readouts from `ChartValueFormatters.Default`
because their public composables declare the formatter through `ChartValueFormatters.Default`.

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

| Stage | Line | Bar | Histogram | Stacked bar | Stacked area | Radar | Pie |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 Selection lifecycle | yes | yes | shared | yes | yes | yes, twice | yes |
| 2 Validation | yes | yes | shared | yes | yes | yes | own path |
| 3 Clamping | yes | yes | shared | yes | yes | yes | yes |
| 4 Conversion | pass-through | pass-through | pass-through | transposes | pass-through | pass-through | none |
| 5 Density decision | yes | yes | shared | yes | yes | — | — |
| 6 Aggregation | yes | yes | disabled | yes | yes | — | — |
| 7 Palette | frame | own colour | shared | entry | entry | entry | own file |
| 8 Chrome layout | `LineChartFrame` | `BarChartContent` | shared | `StackedBarChartFrame` | `StackedAreaChartFrame` | inline | `PieChartFrame` |
| 9 Axis planning | yes | yes | shared | yes | yes | labels only | — |
| 10 Canvas guard | yes | yes | shared | yes | yes | — | — |
| 11 Domain | `resolveLineRange` | `resolveBarRange` | shared | totals | totals | `minMax` | share-based |
| 12 Normalization | list | scalar per bar | shared | list | list | list | share-based |
| 13 Animation | yes | yes | shared | yes | yes | yes | yes |
| 14 Gesture | yes | yes | shared | yes | yes | yes | yes |
| 15 Draw | yes | yes | shared | yes | yes | yes | yes |

Bar and histogram share one plot, so they share every stage but aggregation: stages 1 to 5 and
7 to 15. Their specs differ in the policy — histogram forbids negative bin heights — and only
histogram passes `aggregate = false`, so histogram runs the density decision but never compacts.

## Recorded divergences

Each of these is a decision, written down so the next change reads it.

**Two pipeline shapes.** Radar and pie run stages 1 to 4, 7, 8 and 11 to 15. Neither compacts, so
they skip the density decision and aggregation, and neither runs the Cartesian canvas guard. Radar
labels its own axes in place of stage 9; pie has no axis and skips it. Every other chart runs all
fifteen. A Cartesian chart with a compact mode and a polar or share-based chart are presented as one
pipeline, and this is the shape the library has. Pie runs the same stages 1 to 4 as every other
chart, through `PieChartEntry`.

**The domain has five shapes, and pie has none of them.** Bar and both stacked charts fold zero into
the domain, so an all-positive series starts at the axis. Line and radar take the data's own range.
Line, bar and histogram accept a caller's fixed range through `style.range`. Stacked area's domain
is a line inside its normalizer, so finding it means opening the normalizer. Pie's axis is the caller's
own share, not a data range, which is why its matrix row reads share-based. A domain is a pair of
doubles and reads no constraint, so by the test above it could sit in the seam; whether it should is
open.

**Flat data draws at a different height per chart.** Line passes `zeroRangeValue = 0f` and draws a
flat series at the bottom. Radar passes `1f` and draws it at full radius. Both domains are the same
shape.

**Normalization has two shapes.** Line's is a list per series, because it animates between two of
them. Bar's is a per-bar scalar computed at draw time. Each is the shape its chart needs.

#### Palette

`resolvePaletteColors` is pure in the clamped style and an item count, so by the test above it belongs
at the seam. Each style exposes a thin wrapper over it — `resolveColors` in line, stacked bar and
stacked area, `resolveLineColors` in radar — and pie's wrapper takes a slice count instead of a series
count. Stacked bar (`StackedBarChartEntry`), stacked area (`StackedAreaChartEntry`), radar
(`RadarChartEntry`) and pie (`PieChartEntry`) resolve inside their entry. Line resolves it in
`rememberLineColors`, which is declared in `LineChartFrame` and called from `LineChartImpl` and
`LiveLineChartImpl`, one frame above where it is declared.

`singleItemUsesBase` is a boolean literal at each of the five call sites. A single-series chart draws
its base colour on line, stacked area and radar, and a generated shade on pie and stacked bar.

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

A new chart runs all fifteen stages in this order. A chart that skips one says so here; radar and pie
skip stages 5, 6 and 10, and stage 9 is radar's own label layout and absent from pie.

A new stage goes where the test puts it, and a stage whose behaviour differs between charts is
declared in the chart's `ChartSpec`, not branched inside a content composable. A policy field
nothing reads is the drift the seam exists to remove, so a field lands with the change that reads it.
