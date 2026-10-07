---
title: Y-Axis Labels
order: 8
---

# Y-Axis Labels

Bar, histogram, stacked bar, stacked area, line, and live line charts share one Y-axis label
pipeline in `charts-core`. Ticks are evenly spaced from the maximum at the top to the minimum at
the bottom, each label is centered on its tick, and every label stays inside its column.

## Pipeline

| Step | Function | File |
| --- | --- | --- |
| Tick values and positions | `buildNumericYAxisTicks` | `NumericYAxisTicks.kt` |
| Tick count | `yAxisTickCount`, `yAxisLabelMinSpacingPx` | `NumericYAxisTicks.kt`, `AxisHelpers.kt` |
| Ticks, column width, and gap in one call | `rememberNumericYAxisLayout` | `AxisLabelLayouts.kt` |
| Column width | `yAxisLabelColumnWidthPx`, `estimateYAxisLabelWidthPx` | `AxisLabelLayouts.kt`, `AxisHelpers.kt` |
| Drawing | `AxisYLabelsLayout`, `placeYAxisLabel` | `AxisLabelLayouts.kt`, `AxisHelpers.kt` |
| Line chart zero line | `baselineYForRange` | `AxisHelpers.kt` |

All files are in `charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/axis/`.

## Ticks

`yAxisTickCount` shows `AxisLabelStyle.maxCount` ticks, or five when it is null, but never more
than fit `yAxisLabelMinSpacingPx` apart over the plot height between the insets, and never fewer
than two, the two ends of the range. Validation rejects a set `maxCount` below 2.

`yAxisLabelMinSpacingPx` is one label line plus the gap between X-axis labels:

```text
minSpacing = fontSize × 1.2 × (1 + 0.5) = 1.8 × fontSize
```

Y labels are level, so one line of text plus the gap keeps neighbors apart. At the default 11sp
size, ticks stay at least about 20dp apart. A 5-tick axis needs about 80dp of plot height; shorter
plots show fewer ticks.

Bar and stacked bar charts decide how many bars fit before they know the plot height, so they
estimate the Y-axis column at the full chart height. On a short plot that estimate has more ticks
than the drawn axis, and its column can be a character wider or narrower. Only the number of bars
that fit uses it; the plot is laid out with the drawn column.

`buildNumericYAxisTicks` spaces the ticks evenly over the plot height. Line charts inset the first
and last tick so the line, points, and selection markers fit at the minimum and maximum. The other
charts use no inset.

Each tick value is `max × (1 − p) + min × p`, where `p` runs from 0 at the top to 1 at the bottom.
It stays finite for any finite range, including `-Double.MAX_VALUE..Double.MAX_VALUE`, where
`max − min` overflows, and it returns `max` and `min` exactly at the two ends. When `min == max`,
every tick is that value: blending a value with itself can round to the neighboring double, and a
flat line at 0.375 with 11 labels would then show 0.37 among its 0.38 labels.

## Formatting

All six charts format ticks with their `axisValueFormatter`. Every chart's default points to
`StyleDefaults.axisValueFormatter`:

- It rounds to two decimals with `StyleDefaults.selectedValueFormatter` and drops a trailing
  `.0`: 12.345 prints `12.35` and 12.0 prints `12`.
- It prints plain digits on every platform, so 12,500,000 prints `12500000`, never `1.25E7`.
- It prints values that round to zero as `0`, and `NaN`, `Infinity`, and `-Infinity` as they are.

## Placement

`AxisYLabelsLayout` places each label with `placeYAxisLabel`:

- The label is right-aligned 4dp from the right edge of the label column.
- It is centered on its tick, then moved back inside the column if it would stick out at the top or
  bottom. A label taller than the column starts at the column top.
- A tick more than 1 px above or below the column has no label. The slack covers a fractional plot
  height that is laid out in whole pixels.

Every chart builds its column with `rememberNumericYAxisLayout`. `estimateYAxisLabelWidthPx`
sizes the column from the longest label, at 0.58 × font size per character, and
`yAxisLabelColumnWidthPx` caps it at 40% of the chart width and rounds it to whole pixels. Longer
labels are cut off. `AXIS_LABEL_CHART_GAP` (10dp, rounded to whole pixels) sits between the column
and the plot, shrunk if needed so the plot keeps at least 1 px. Hidden labels take no ticks, width,
or gap.

## Tests

| Behavior | Tests |
| --- | --- |
| Tick count, tick values, formatting, placement, zero line | `AxisHelpersTest` in `charts-core/src/commonTest` |

### Brute-Force Checks

These functions were compared with plain definitions of their output. Every case matched.

| Function | Checked | Inputs |
| --- | --- | --- |
| `buildNumericYAxisTicks` | Top tick exactly `max`, bottom tick exactly `min`, every tick finite, each tick value at most the one before | 100,000 random ranges with ends up to ±`Double.MAX_VALUE` and 2–39 ticks |
| `buildNumericYAxisTicks` | Every tick of a flat range equals its value | 100,000 random flat ranges up to ±10^6 with 2–11 ticks |
| `placeYAxisLabel` | Label top at the tick minus half the label height, rounded and kept in the column; left edge at column width minus label width minus padding, at least 0; no label past the 1 px slack | Columns 0–200 px high, labels 0–250 px high and 0–80 px wide, ticks every 0.05 px from 3 px above to 3 px below the column |

`buildNumericYAxisTicks_equalMinAndMax_keepsExactValueForEveryCount` in `AxisHelpersTest` repeats
the flat-range check for 0.375, which prints a wrong label without the flat-range rule.

## Known Issues

Known issues and limits are listed in Y-Axis Label Issues, in the Known Issues section.
