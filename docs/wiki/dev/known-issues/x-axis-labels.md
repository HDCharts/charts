---
title: X-Axis Label Issues
order: 7
---

# X-Axis Label Issues

Known issues and limits of the X-Axis Labels page in Chart Internals.

## Need Verification

None yet.

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

### Labels are always tilted

Labels are tilted 34°, even when they would fit flat.

Confirmed by `AxisXLabelsLayout`, which tilts every label by `X_AXIS_LABEL_TILT_DEGREES`, and by
`HeroChartPreview`, whose six "Week" labels would fit flat.

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
