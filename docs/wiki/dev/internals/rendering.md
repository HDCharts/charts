---
title: Rendering and Animation
order: 6
---

# Rendering and Animation

The Cartesian charts described here draw on a `Canvas` and share animation progress across their
items. Density handling and visible-range drawing determine how much work a frame costs. Drawing
code lives in each chart module; shared helpers live in `charts-core`.

## Dense Data

| Chart | Dense when | Compact view | Expanded view |
| --- | --- | --- | --- |
| Line, stacked area | At the compact-density threshold | A bounded set of points, each merging a bucket of neighbors | Every point at the dense step times the zoom, scrolling |
| Bar, stacked bar, histogram | More bars than fit at the minimum bar width | As many bars as fit, each merging a bucket of neighbors | Every bar at the minimum width times the zoom, scrolling |
| Live line | Never | None: the whole window is drawn | None |

Line, bar, and histogram charts average each bucket. A chart without interaction cannot be expanded, so it
stays in the compact view.

Current thresholds and step sizes live in the
[density helpers](https://github.com/HDCharts/charts/tree/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/density).

## Canvas

In the fit and compact views the canvas is the plot. In the expanded view it is as wide as every
item and scrolls inside the plot.

`chartCanvasFits` in [ChartCanvasLimits.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/layout/ChartCanvasLimits.kt) checks the canvas against Compose's constraint limits.
A chart past the limit shows "Chart exceeds layout limits". The item count that fits depends on
spacing, density, and zoom.

`placedHorizontalScrollPx` in [ChartScroll.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/layout/ChartScroll.kt) clamps the scroll offset the same way
`horizontalScroll` places the canvas, so the frame after a zoom-out draws the bars and labels that
are on screen.

## Animation State

| Chart | State | On a data change |
| --- | --- | --- |
| Bar, histogram | One `Animatable` progress for the whole chart, from `ChartMorphState` in `rememberBarChartMorph` | A morph tweens the progress and blends the value sets. `BarChartCascade` gives bars delayed, eased progress when the dataset fits its cascade limit |
| Stacked bar | One `Animatable` progress for the whole chart, from `ChartMorphState` | A morph tweens the single progress and the draw blends the two value sets |
| Line, live line | One `Animatable` progress for the whole chart, from `ChartMorphState`, plus one slide progress | A morph tweens the single progress and the draw blends the two value sets. A live shift animates the slide progress over one full step |
| Stacked area | One `Animatable` progress for the whole chart, from `ChartMorphState` | A morph tweens the single progress and the draw blends the two value sets |

## Drawing

Every draw reads the current animation values, so it runs on every frame of the reveal, a morph,
or a live shift.

- Line and live line: each draw blends or copies one series at a time into a reused `FloatArray`
  and builds one `Path` through the points of that series with `drawChartPath` in
  [LineChartDrawing.kt](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonMain/kotlin/io/github/hdcharts/line/internal/LineChartDrawing.kt). The path, the canvas heights, and the bezier control points are held in a
  `LineChartDrawScratch` the chart remembers, so a frame allocates nothing per point or per segment.
  A bezier line maps each value to its canvas height once and reads the shared heights per segment.
  An expanded chart draws only the points on screen, plus the overscan of the widest marker, so a
  chart of a million points does not build a path through the whole series.
- Stacked area: each draw copies the values of every series, allocates a zero baseline, and builds
  an `Offset` for every point of each series before it keeps the visible range for the paths.
- Bar and stacked bar: each draw walks only the bars in `visibleRange`.

## Tests

| Behavior | Tests |
| --- | --- |
| Canvas limit | [ChartCanvasLimitsTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/layout/ChartCanvasLimitsTest.kt) in `charts-core/src/commonTest` |
| Error instead of a crash past the limit | `*_expandedPastLayoutLimits_displaysErrorInsteadOfCrashing` in `charts-line`, `charts-stacked-area`, and `charts-stacked-bar` |
| Scroll clamp after a zoom-out | [ChartScrollTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/layout/ChartScrollTest.kt) in `charts-core/src/commonTest`, [BarChartScrollFrameTest](https://github.com/HDCharts/charts/blob/main/charts-bar/src/jvmTest/kotlin/io/github/hdcharts/bar/internal/BarChartScrollFrameTest.kt) in `charts-bar/src/jvmTest` |
| An expanded chart draws the same line as the whole series | [LineChartVisiblePointsTest](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonTest/kotlin/io/github/hdcharts/line/internal/LineChartVisiblePointsTest.kt) in `charts-line/src/commonTest` |
| Control points without an object per segment | [CubicControlPointsIntoTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/bezier/CubicControlPointsIntoTest.kt) in `charts-core/src/commonTest` |
| An interrupted live shift runs a full step | `interruptedShift_startsAFullStepInsteadOfResumingTheInterruptedWindow` in `charts-line/src/commonTest` |

## Known Issues

See [Rendering and Animation Issues](https://charts.hdcode.dev/dev/known-issues/rendering) for known limits.
