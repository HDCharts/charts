---
title: Y-Axis Labels
order: 8
---

# Y-Axis Labels

Numeric Y axes share tick and label-layout helpers in `charts-core`. Ticks run from the maximum at
the top to the minimum at the bottom. Labels are centered on their ticks and kept inside the column.

## Pipeline

| Responsibility | Helpers |
|---|---|
| Tick values and positions | `buildNumericYAxisTicks` |
| Tick count and spacing | `yAxisTickCount`, `yAxisLabelMinSpacingPx` |
| Ticks, column width, and gap | `rememberNumericYAxisLayout` |
| Width estimate | `estimateYAxisLabelWidthPx`, `yAxisLabelColumnWidthPx` |
| Drawing and placement | `AxisYLabelsLayout`, `placeYAxisLabel` |

The helpers live in the
[axis package](https://github.com/HDCharts/charts/tree/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/axis).
`baselineYForRange` also supplies the line chart's zero line.

## Ticks

`yAxisTickCount` uses `AxisLabelStyle.maxCount` or the default from `StyleDefaults`. It reduces the
count to fit the plot height, keeping at least the two range endpoints. Validation rejects an
explicit cap below 2.

`yAxisLabelMinSpacingPx` combines estimated line height with `AXIS_LABEL_GAP_FACTOR`, shared with
X-axis spacing. Taller text leaves room for fewer ticks.

Bar layouts estimate the Y-axis column before they know the final plot height, so their estimate
can differ from the drawn column on a short plot. The estimate only affects how many bars fit.

Line charts inset endpoint ticks to leave room for lines and markers. Tick helpers accept these
insets from the chart layout.

Each tick value is `max × (1 − p) + min × p`, where `p` runs from 0 at the top to 1 at the bottom.
This avoids overflow from `max − min` for a finite range. The endpoints stay exact. For a flat
range, every tick is set directly to that value to avoid rounding differences from blending.

## Formatting

The layout formats numeric ticks with the supplied formatter. Chart defaults use
`StyleDefaults.axisValueFormatter`, which uses the shared selected-value formatting and removes
a trailing `.0` from whole values. See [Style Defaults](style-defaults.md).

## Placement

`AxisYLabelsLayout` delegates to `placeYAxisLabel`:

- Right-align each label with the column's padding.
- Center it on the tick, then keep its top and bottom inside the column.
- Omit ticks beyond the column, allowing 1 px of rounding slack.

`rememberNumericYAxisLayout` estimates the column from the longest label. Width is capped to
preserve plot space, so very long labels can be cut off. `AXIS_LABEL_CHART_GAP` separates the column
and plot and shrinks when space is tight. Hidden labels take no ticks, width, or gap.

## Tests

[AxisHelpersTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/axis/AxisHelpersTest.kt) in `charts-core` covers tick values, formatting, placement, and the zero line.
`buildNumericYAxisTicks_equalMinAndMax_keepsExactValueForEveryCount` pins flat-range behavior.
Historical exploratory results are preserved in the
[earlier source revision](https://github.com/HDCharts/charts/blob/bf27d8869609f641745098bc508f9617782db2fb/docs/wiki/dev/internals/y-axis-labels.md#brute-force-checks).
