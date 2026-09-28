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
| Line, live line | One `Animatable` per point of every series, plus one slide progress | A morph launches one coroutine per point. A live shift calls `snapTo` on every point, then animates the slide progress |
| Stacked area | One `Animatable` per point of every series | One coroutine per point |

## Drawing

Every draw reads the current animation values, so it runs on every frame of the reveal, a morph,
or a live shift.

- Line and live line: each draw copies the values of every series into a new list and builds one
  `Path` through every point with `drawChartPath` in `LineChartDrawing.kt`. Bezier lines also
  allocate an `Offset` for every point, and visible point markers draw one circle per point. The
  expanded view draws every point, including the ones scrolled off screen.
- Stacked area: each draw copies the values of every series, allocates a zero baseline, and builds
  an `Offset` for every point of each series before it keeps the visible range for the paths.
- Bar and stacked bar: each draw walks only the bars in `visibleRange`.

## Tests

| Behavior | Tests |
| --- | --- |
| Canvas limit | `ChartCanvasLimitsTest` in `charts/src/commonTest` |
| Error instead of a crash past the limit | `*_expandedPastLayoutLimits_displaysErrorInsteadOfCrashing` in `charts-line`, `charts-stacked-area`, and `charts-stacked-bar` |
| Scroll clamp after a zoom-out | `ChartScrollTest` in `charts/src/commonTest`, `BarChartScrollFrameTest` in `charts-bar/src/jvmTest` |

## Known Issues

Known issues and limits are listed in Rendering and Animation Issues, in the Known Issues section.
