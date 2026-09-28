---
title: X-Axis Label Issues
order: 1
---

# X-Axis Label Issues

Known issues and limits of the X-Axis Labels page in Chart Internals.

## Need Verification

### Wide glyphs can make a label taller than the row

`estimateXAxisLabelExtent` sizes labels at 0.58 × font size per character, but CJK and emoji
glyphs are about 1 em wide. A long label of them can be taller than the row and, in line and
stacked area charts, wider than the edge insets. By the same geometry, 10 CJK characters at 11sp
need about 6.6 em of height where the estimate gives 4.2 em, so the label reaches about 1.2 em
(13dp) above its share of the row, more than the 10dp gap, and may touch the plot. No test or
screenshot shows it yet.

Options:

- Measure the few longest labels with a `TextMeasurer` once per data change, and use the measured
  width and line height for the row height, the edge insets, and the minimum spacing.

### Long edge labels can pass the edges of bar charts

Bar, histogram, and stacked bar charts center the first and last labels under their bars, half a
bar in from the plot edges, and do not move the plot in the way line and stacked area charts do.
By the estimate, a tilted 11sp label of eight characters, such as "December", reaches about 25dp to
each side of its tick, so on bars narrower than about 20dp it passes the 15dp chart padding on the
right. No test or screenshot shows it yet.

Options:

- Inset the plot for bar charts too, by the part of the edge label that half a bar and the padding
  cannot hold.

### Live window labels can leave their points

`LineChartContent` counts the points a live window has dropped with `TimelineWindowCounter`, which
it updates while composing. If a composition is thrown away after the count moved, the next
composition sees the same window twice. That is not a shift, so the count resets to 0 and every
label jumps to other points once. This has not been seen on a device.

Options:

- Keep the count with the slide state, set in the same place as the transition.
- Let `LiveLineChart` take the series index of its first point from the caller.

## Confirmed

### Long labels can look busy

The minimum spacing, described under Minimum Spacing on the X-Axis Labels page, depends only on the
line height, not the label length. Long labels at the minimum spacing can look busy. For example,
the live chart preview in tablet landscape shows 30 `14:00:00`-style times, one every 2 seconds,
about 35.5dp apart, just over the 35.4dp minimum.

Confirmed by `LiveLineChartDefaultPreview` in tablet landscape.

Options:

- Raise `AXIS_LABEL_GAP_FACTOR`. This shows fewer labels on every chart and also spaces Y-axis
  ticks further apart; at a full empty line (about 47dp) the phone quarterly stacked bar chart
  drops from 8 labels to 4.
- Also require the spacing to be a share of the longest label's tilted width, using the same
  length estimate as the row height, so only long labels get more room.

### Labels pop in and out at the scroll edges

While scrolling, a label is drawn in full as long as its tick is inside the plot, then disappears
at once when the tick leaves. The label row is not clipped, so near the edges a label also hangs
past the plot.

Confirmed by the code: `placeXAxisLabel` drops a label once its tick is more than 1 px outside the
row, and `AxisXLabelsLayout` does not clip. `AxisHelpersTest` covers both.

Options:

- Clip the label row to the plot plus the Y-axis gutter while scrolling, and plan labels one
  stride past each visible edge so they slide in and out. Keep `visibleRange` exact, because bar
  charts use it to draw bars.

### The scrolling plan can miss a label at the left edge

While scrolling, the layout draws a label whose tick is up to 1 px left of the plot, but
`planAxisXLabels` can leave that item out when items are about 1 px wide. The missing label would
hang almost entirely outside the plot, so nothing visible is lost.

Confirmed by a random check of 200,000 scrolling plans: 29 labels the layout would draw were not
planned, all with a tick 0.5 to 1 px left of the plot.

Options:

- Plan one item before `visibleRange` as well as one after it.

### Labels are always tilted

Labels are tilted 34°, even when they would fit flat.

Confirmed by `AxisXLabelsLayout`, which tilts every label by `X_AXIS_LABEL_TILT_DEGREES`, and by
`HeroChartPreview`, whose six "Week" labels would fit flat.

### A flat live window does not slide its labels

A live window is seen as a shift only when its values move by one point. A window whose values do
not change, such as a line of zeros, looks the same after every update: the line does not slide,
the count of dropped points stays put, and the labels keep their places while their text changes.

Confirmed by the code: `rawSeries` keys both the counter in `LineChartContent` and the effect that
starts the slide, and an equal list does not change either key.

Options:

- Detect a shift from the X-axis labels as well as the values, or let the caller say that the
  window moved.

### A live window resizes its plot when its labels change width

A live window can change the width of its plot on any update:

- The label row height and the edge insets come from the longest X label in the window. When it
  gains or loses a character, such as `9:59:59` becoming `10:00:00`, the plot changes width and
  height by a few dp.
- The Y axis rescales to each window, so its labels change, such as `36.75` becoming `37`. A
  narrower Y column gives the plot more width.

Every point and label then jumps once instead of sliding. When the new width changes the label
stride, every X label also moves to other points, such as from every 4th point to every 3rd.

Confirmed by `LiveLineChartTest.slidingWindow_keepsXLabelsOnTheirSamples` on an API 35 Nexus 6
emulator, with a window whose Y range went from 0–49 to 1–49. The test now keeps the Y range
fixed.

Options:

- Let a live window keep its stride while it slides: grow it when labels would overlap, and shrink
  it only when the data is replaced or the chart is resized.
- Let a live window only grow its label extent and Y column, so the plot resizes once and then
  stays.

### Each chart computes its own X geometry

Bar, stacked bar, line, and stacked area charts each work out the item width, the first tick, the
viewport, the content width, the scroll offset, the visible range, and the tap and drag mapping in
their own content composable. They share helpers such as `denseStepForViewport`,
`chartCanvasFits`, `placedHorizontalScrollPx`, and `InteractionMath`, but each wires them up
itself. A change has to be made four times, and a copy that drifts puts labels, drawing, and hit
testing out of step. The crash past the canvas limit, taps selecting the neighboring point, and the
stale frame after a zoom-out each needed the same fix in several of these composables.

Confirmed by the code: `BarChartContent`, `StackedBarChartContent`, `LineChartContent`, and
`StackedAreaChartContent`.

Options:

- Compute one X frame per chart from the layout alone: item count, item width, first tick,
  viewport, and content width, with the position of each item, the nearest point or bar at a
  given x, and the visible range for a scroll offset. Read the scroll offset only where labels are
  placed and the plot is drawn. This also fixes Scrolling recomposes the chart content.

### Scrolling recomposes the chart content

Bar, stacked bar, line, and stacked area charts read the scroll offset in composition to plan their
labels, so every scrolled pixel recomposes the chart content and hands `AxisXLabelsLayout` a new
tick list. Its `Text` children have no keys, so once the first label leaves the screen, every label
gets new text. How much this costs per frame is not measured yet.

Confirmed by the code: each content composable reads `scrollState.value` for `scrollOffsetPx`,
which keys the `remember` in `rememberXAxisLabelPlan`.

Options:

- Read the scroll offset only where labels are placed and the plot is drawn, as live windows
  already do with `tickOffsetPx`, and plan labels with `derivedStateOf` so they change only when
  the labeled items change.
- Key each label by its item index.
