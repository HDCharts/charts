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

### The Y axis line scrolls away in expanded charts

In an expanded bar, histogram, or line chart, the Y axis line scrolls off screen with the data.
The Y axis labels stay in place, so after a scroll the labels have no axis line next to them.

Confirmed by the code: `drawBars` in `BarChartDrawing.kt` and the canvas in `LineChartContent`
draw the Y axis line at `x = 0` of the canvas inside `horizontalScroll`.

Options:

- Draw the Y axis line outside the scrolling canvas, at the left edge of the plot area, next to
  the Y axis labels.

### Stacked area charts stay busy after a swipe on Android and iOS

After a swipe on an expanded stacked area chart, Compose never goes idle on Android and iOS. A test
that waits for idle after the swipe hangs until its timeout, and a tap after the swipe does not
select a new point in time. Line charts settle within about a second. JVM tests settle, so the
cause is likely the platform fling or overscroll.

Confirmed by `stackedAreaChart_scrollThenTap_changesSelectedLabelAtSameViewportX`, which timed out
on the iOS simulator, and a rewrite that waited for idle and hung on an API 35 Nexus 6 emulator.
The test was removed until this is fixed.

Options:

- Find what keeps the chart busy after a swipe, starting from the scroll wiring in
  `StackedAreaChartContent`, and add the scroll-then-tap test back.

### One animation value per point or bar

Stacked area charts keep one `Animatable` per point of every series; bar, histogram, and stacked
bar charts keep one per bar. A morph launches one coroutine per point, so a two-series window of
100,000 points makes 200,000 calls per update. A histogram, which never compacts, holds one
`Animatable` per bin, so 1,000,000 bins make 1,000,000 of them and a coroutine for each bin that
changes.

Confirmed by the code: `animatedValues` in `StackedAreaChart` and `StackedBarChart`, and
`rememberBarChartAnimatedValues` in `BarChartAnimation.kt`. Line and live line charts no longer
appear here: `LineChartMorphState` animates one progress value and the draw blends the two value
sets.

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
