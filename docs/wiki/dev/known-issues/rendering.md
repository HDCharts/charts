---
title: Rendering and Animation Issues
order: 6
---

# Rendering and Animation Issues

Known issues and limits of the Rendering and Animation page in Chart Internals.

## Need Verification

### Frame cost of large charts is not measured

The issues under Confirmed follow from the code, but none of them has been timed on a device.
Before choosing which to fix first, measure frame times for an expanded line chart, a live line
window, an expanded stacked area chart, and an expanded histogram at 10,000, 100,000, and
1,000,000 items.

## Confirmed

### One animation value per bar

Bar, histogram, and stacked bar charts keep one `Animatable` per bar, and a morph launches one
coroutine per bar. Bar and histogram compact dense data, but expanding brings back one `Animatable`
per source bar, so 1,000,000 bins make 1,000,000 of them and a coroutine for each bin that changes.

Confirmed by the code: `animatedValues` in `StackedBarChart`, and `rememberBarChartAnimatedValues`
in `BarChartAnimation.kt`. Line, live line, and stacked area charts no longer appear here:
`ChartMorphState` animates one progress value and the draw blends the two value sets.

Options:

- Animate one progress value per chart, and blend the old and new values in the draw.

### Data with a new point count draws without animation

Line and stacked area charts animate an update only when the number of series and points stays the
same. Data that goes from 5 points to 6, or adds a series, draws straight away. Live line charts
keep the point count fixed and slide the window, so new points still animate there.

Confirmed by the code: `LineChartTransitionState.update` snaps on a changed series structure, and
`StackedAreaChartImpl` keys its `ChartMorphState` on the series and point counts.

Options:

- Morph between the two counts: `ChartMorphState` already blends lists of different lengths, and
  new points could start from the last drawn value.

### Stacked area builds every point before culling

Each draw blends every point of every series from `ChartMorphState`, allocates a zero baseline as
long as the data, and builds an `Offset` for every point in `buildSeriesPoints`, then keeps only the
visible range with `subList`. Culling saves path segments, not the work over every point of every
series.

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
