---
title: Rendering and Animation
order: 3
---

# Rendering and Animation

Bar, histogram, stacked bar, line, live line, and stacked area charts each draw on one `Canvas`,
keep one animation value per drawn item, and show a compact view when they have more items than
fit. These three choices decide how much work a frame costs. The drawing code lives in each chart
module; the shared helpers are in `charts-core`.

## Dense Data

| Chart | Dense when | Compact view | Expanded view |
| --- | --- | --- | --- |
| Line, stacked area | 50 points or more | Up to 50 points, each merging a bucket of neighbors | Every point, at least 12 px apart times the zoom, scrolling |
| Bar, stacked bar | More bars than fit at the minimum bar width | As many bars as fit, each merging a bucket of neighbors | Every bar at the minimum width times the zoom, scrolling |
| Histogram | More bins than fit at the minimum bar width | None: every bin is squeezed into the plot | Every bin, scrolling, as for bar charts |
| Live line | Never | None: the whole window is drawn | None |

Line and bar charts average each bucket. A chart without interaction cannot be expanded, so it
stays in the compact view.

## Canvas

In the fit and compact views the canvas is the plot. In the expanded view it is as wide as every
item and scrolls inside the plot.

`chartCanvasFits` in `ChartCanvasLimits.kt` checks the canvas against the largest size Compose
constraints can hold, about 262,000 px wide. A chart past it shows "Chart exceeds layout limits"
instead of drawing. At 12 px per point, an expanded line or stacked area chart reaches the limit at
21,847 points, or 5,463 at 4× zoom. With the default 10dp bars and 10dp spacing on a 2.625 density
screen, a bar or stacked bar chart reaches it at 4,994 bars.

`placedHorizontalScrollPx` in `ChartScroll.kt` clamps the scroll offset the same way
`horizontalScroll` places the canvas, so the frame after a zoom-out draws the bars and labels that
are on screen.

## Animation State

| Chart | State | On a data change |
| --- | --- | --- |
| Bar, histogram | One `Animatable` per drawn bar, from `rememberBarChartAnimatedValues` | One coroutine per bar whose value changed; charts of up to 200 bars animate in a cascade |
| Stacked bar | One `Animatable` per drawn bar | One coroutine per bar |
| Line, live line | One `Animatable` progress for the whole chart, from `LineChartMorphState`, plus one slide progress | A morph tweens the single progress and the draw blends the two value sets. A live shift animates the slide progress over one full step |
| Stacked area | One `Animatable` per point of every series | One coroutine per point |

## Drawing

Every draw reads the current animation values, so it runs on every frame of the reveal, a morph,
or a live shift.

- Line and live line: each draw blends or copies one series at a time into a reused `FloatArray`
  and builds one `Path` through the points of that series with `drawChartPath` in
  `LineChartDrawing.kt`. The path, the canvas heights, and the bezier control points are held in a
  `LineChartDrawScratch` the chart remembers, so a frame allocates nothing per series or per segment.
  A bezier line maps each value to its canvas height once and reads the shared heights per segment.
  An expanded chart draws only the points on screen, plus the overscan of the widest marker, so a
  chart of a million points does not build a path through the whole series.
- Stacked area: each draw copies the values of every series, allocates a zero baseline, and builds
  an `Offset` for every point of each series before it keeps the visible range for the paths.
- Bar and stacked bar: each draw walks only the bars in `visibleRange`.

## Tests

| Behavior | Tests |
| --- | --- |
| Canvas limit | `ChartCanvasLimitsTest` in `charts-core/src/commonTest` |
| Error instead of a crash past the limit | `*_expandedPastLayoutLimits_displaysErrorInsteadOfCrashing` in `charts-line`, `charts-stacked-area`, and `charts-stacked-bar` |
| Scroll clamp after a zoom-out | `ChartScrollTest` in `charts-core/src/commonTest`, `BarChartScrollFrameTest` in `charts-bar/src/jvmTest` |
| An expanded chart draws the same line as the whole series | `LineChartVisiblePointsTest` in `charts-line/src/commonTest` |
| Control points without an object per segment | `CubicControlPointsIntoTest` in `charts-core/src/commonTest` |
| An interrupted live shift runs a full step | `interruptedShift_startsAFullStepInsteadOfResumingTheInterruptedWindow` in `charts-line/src/commonTest` |

## Known Issues

Known issues and limits are listed in Rendering and Animation Issues, in the Known Issues section.
