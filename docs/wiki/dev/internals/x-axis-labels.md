---
title: X-Axis Labels
order: 7
---

# X-Axis Labels

Indexed Cartesian charts share an X-axis label planner in `charts-core`. Labels sit on every
N-th item, centered on their ticks. Scrolling keeps that grid anchored to the data.

## Pipeline

| Responsibility | Helpers |
|---|---|
| Row height and edge insets | `estimateXAxisLabelExtent`, `xAxisLabelRowHeightPx`, `xAxisLabelEdgeInsetPx` |
| Label stride and visible ticks | `rememberXAxisLabelPlan`, `planAxisXLabelStride`, `planAxisXLabels` |
| Tick positions | `buildXAxisLayoutTicks` |
| Measurement and placement | `AxisXLabelsLayout`, `placeXAxisLabel` |

The helpers live in the
[axis package](https://github.com/HDCharts/charts/tree/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/axis).
The stride depends on layout and is remembered separately from scroll position. Scrolling updates
visible ticks while reusing the stride.

## Labeled items

`planAxisXLabels` labels every `step`-th item. Labels come from the data's categories;
`resolveAxisLabel` turns a missing or blank category into the 1-based item number.

The planner chooses the densest grid that respects minimum spacing and `AxisLabelStyle.maxCount`.
A null cap lets spacing determine the count. A set cap can reduce the count, and spacing can reduce
it further.

Without scrolling, a grid ending on the last item wins when it loses at most one label. For
example, with 10 items, `maxCount = 6`, and enough room, the planner picks 0, 3, 6, 9 over
0, 2, 4, 6, 8. It finds these endpoint-aligned grids by checking divisors of the last index.

While scrolling, the cap applies per viewport. The planner accounts for pixel rounding and the
placement slack at the edges. The last-item preference does not apply.

### Live windows

`TimelineWindowCounter` counts points dropped from a live window. A window item gets a label when
its index plus the dropped count is a multiple of the stride, so labels stay on their samples.

`AxisXLabelsLayout` reads `tickOffsetPx` during placement to slide labels with the line. The offset
starts at a full step until the update effect starts the slide. This keeps the first composed frame
aligned and moves labels without recomposing them.

## Minimum spacing

`xAxisLabelMinSpacingPx` in [AxisHelpers.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/axis/AxisHelpers.kt) keeps parallel, tilted text lines apart. Their
perpendicular distance depends on line height and tilt:

```text
minSpacing = fontSize × lineHeightFactor × (1 + gapFactor) / sin(tilt)
```

The helper owns the current constants:

| Constant | Purpose |
|---|---|
| `AXIS_LABEL_LINE_HEIGHT_FACTOR` | Estimates line height and label-row height |
| `AXIS_LABEL_GAP_FACTOR` | Adds space between labels; also affects Y-axis ticks |
| `X_AXIS_LABEL_TILT_DEGREES` | Sets tilt for spacing, placement, and extent calculations |

Font size includes the system font scale. Label length affects the row and edge insets; the
perpendicular spacing rule depends on text thickness. Fonts taller than the estimated line height
can leave less space than intended.

The minimum item stride is `ceil(minSpacing / unitWidth)`, bounded by the item count. A set
`maxCount` can widen it. Without scrolling, the endpoint preference can widen it again.

Apps control density through `AxisLabelStyle.maxCount`, text size, chart width, bar spacing, and
zoom. When changing internal spacing constants, check their shared effects on layout and Y ticks.

## Placement

`buildXAxisLayoutTicks` puts item `i` at `firstTickPx + i × unitWidthPx − scrollOffsetPx`.

| Item geometry | First tick | Item step |
|---|---|---|
| `AxisXItems.Bars(barWidthPx)` | Half a bar width | Bar width plus spacing |
| `AxisXItems.Points` | Plot start | Distance between points |

`AxisXLabelsLayout` tilts labels around their centers and puts all centers on the same line. A
label is drawn when its tick is inside the plot, with 1 px of rounding slack.

### Edges

Point-based plots inset their edge points using `xAxisLabelEdgeInsetPx`, accounting for the longest
tilted label, container padding, and Y-axis gutter. The helper caps each inset so long labels leave
room for the plot. Gestures include the insets, so touching just beyond an edge point selects it.

Bars place labels under their centers. A long edge label can reach into the gutter or padding.
While scrolling, labels can also extend beyond the plot before their ticks leave it.

The planner includes an item past `visibleRange` for placement slack. Keep `visibleRange` exact:
bar drawing uses it too.

## Row height

`estimateXAxisLabelExtent` estimates text width and line height, then applies the shared tilt.
It finds the longest label by length and counts fallback-number digits without allocating each
fallback string.

`xAxisLabelRowHeightPx` adds `AXIS_LABEL_CHART_GAP`. Placement uses the same gap to align the row
with the plot.

## Tests

| Behavior | Tests |
|---|---|
| Stride choice, caps, scrolling, and fractional geometry | [AxisXPlannerTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/axis/AxisXPlannerTest.kt) in `charts-core` |
| Tick positions, placement, spacing, row height, and visible ranges | [AxisHelpersTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/axis/AxisHelpersTest.kt) in `charts-core` |
| Labels align with rendered marks and respect chart edges | Chart interaction and screenshot tests |
| Live labels stay on their samples while sliding | `slidingWindow_*` in [LiveLineChartTest](https://github.com/HDCharts/charts/blob/main/charts-line/src/commonTest/kotlin/io/github/hdcharts/line/LiveLineChartTest.kt) |

After an intentional visual change, follow
[Screenshot baselines](https://charts.hdcode.dev/dev/releases/validation-matrix#screenshot-baselines). Historical exploratory results are preserved in the
[earlier source revision](https://github.com/HDCharts/charts/blob/bf27d8869609f641745098bc508f9617782db2fb/docs/wiki/dev/internals/x-axis-labels.md#brute-force-checks).

## Known Issues

See [X-Axis Label Issues](https://charts.hdcode.dev/dev/known-issues/x-axis-labels) for known limits.
