---
title: Rendering and Animation Issues
order: 3
---

# Rendering and Animation Issues

Known issues and limits of the Rendering and Animation page in Chart Internals.

## Need Verification

### Frame cost of large charts is not measured

The issues under Confirmed follow from the code, but none of them has been timed on a device.
Before choosing which to fix first, measure frame times for an expanded line chart, a live line
window, an expanded stacked area chart, and a collapsed histogram at 10,000, 100,000, and
1,000,000 items.

## Confirmed

### Line charts rebuild every point on every frame

During the reveal, a morph, or a live shift, every frame copies every value of every series,
builds a new path through every point, and, for bezier lines, allocates an `Offset` per point. The
expanded view also draws points scrolled off screen. Live line windows and expanded line charts
are not compacted, so the cost grows with the item count.

Confirmed by the code: `drawChartPath` in `LineChartDrawing.kt`, and the canvas in
`LineChartContent`, which maps every `Animatable` to a new list per series on each draw.

Options:

- Keep the drawn values in a reused `FloatArray`.
- Draw only the points in the visible range, plus one on each side.

### One animation value per point or bar

Line, live line, and stacked area charts keep one `Animatable` per point of every series; bar,
histogram, and stacked bar charts keep one per bar. A morph launches one coroutine per point, and
a live shift calls `snapTo` on every point in turn: a two-series window of 100,000 points makes
200,000 calls per update. A histogram, which never compacts, holds one `Animatable` per bin, so
1,000,000 bins make 1,000,000 of them and a coroutine for each bin that changes.

Confirmed by the code: `animatedValues` in `LineChartContent`, `StackedAreaChart`, and
`StackedBarChart`, and `rememberBarChartAnimatedValues` in `BarChartAnimation.kt`.

Options:

- Animate one progress value per chart, and blend the old and new values in the draw.

### Stacked area builds every point before culling

Each draw copies every `Animatable` of every series, allocates a zero baseline as long as the
data, and builds an `Offset` for every point in `buildSeriesPoints`, then keeps only the visible
range with `subList`. Culling saves path segments, not the work over every point of every series.

Confirmed by the code: the canvas in `StackedAreaChart` and `buildSeriesPoints`.

Options:

- Build points only for the visible range, plus one on each side.

### Wide expanded charts show an error instead of drawing

The expanded view sizes its canvas to every item, and Compose constraints cannot hold more than
about 262,000 px. Past that, the chart shows "Chart exceeds layout limits" instead of drawing: an
expanded line or stacked area chart at 21,847 points, or 5,463 at 4× zoom, and a bar or stacked bar
chart with default bar sizes at 4,994 bars on a 2.625 density screen.

Confirmed by `ChartCanvasLimitsTest` and by the
`*_expandedPastLayoutLimits_displaysErrorInsteadOfCrashing` tests in `charts-line`,
`charts-stacked-area`, and `charts-stacked-bar`.

Options:

- Draw on a viewport-sized canvas and offset the drawing by the scroll position, so the canvas no
  longer grows with the item count. Positions then need to count from the first visible item:
  past about 8.4 million px, a `Float` position can no longer hold fractions of a pixel, and past
  about 16.8 million px it skips every other pixel.
