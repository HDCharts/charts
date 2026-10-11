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

### Data with a new point count draws without animation

Line, stacked area, stacked bar, bar, and histogram charts animate an update only when the number
of series and points stays the same. Data that goes from 5 points to 6, or adds a series, draws
straight away. Live line charts keep the point count fixed and slide the window, so new points still
animate there.

Confirmed by the code: `LineChartTransitionState.update` snaps on a changed series structure,
`StackedAreaChartImpl` keys its `ChartMorphState` on the series and point counts, and
`StackedBarChartImpl` and `rememberBarChartMorph` key theirs on the bar count.

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

### Gradients show steps in docs GIFs

The `*_gradient*.gif` docs GIFs show visible steps (banding) in bar, histogram, and stacked
gradients. The charts draw the gradients smoothly: screenshot tests of the same charts show no
steps. A GIF holds at most 256 colors, and a gradient through several colors across the plot needs
more shades than that, next to the text, grid, and background colors in the same palette.

Confirmed by measuring one column of the tallest bar in `bar_gradient_plot_vertical.gif` at each
recorder step: the emulator frame has 557 shades with flat runs of at most 6 px, and after the
256-color palette it has 80 shades with flat runs of up to 56 px. `gifsicleLossy` does not change
the result.

Options:

- Set `gifDither` to `floyd_steinberg`: the steps disappear, but GIFs grow about 47% and the
  setting re-records every docs GIF.
- Add a per-scenario dither option to `compose-gif-recorder`, so only the gradient GIFs use
  `floyd_steinberg`.
