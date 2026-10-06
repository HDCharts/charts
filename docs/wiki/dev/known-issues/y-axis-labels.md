---
title: Y-Axis Label Issues
order: 8
---

# Y-Axis Label Issues

Known issues and limits of the Y-Axis Labels page in Chart Internals.

## Need Verification

### Wide glyphs can be cut off in the Y column

`estimateYAxisLabelWidthPx` sizes the column at 0.58 × font size per character, but CJK and emoji
glyphs are about 1 em wide. A custom `axisValueFormatter` that prints them, such as a `万` suffix,
can get a column narrower than its labels, which are then cut off. Tick spacing also assumes a line
height of 1.2 × font size, so fonts with taller lines get less than the intended gap. No test or
screenshot shows either yet.

Options:

- Measure the longest labels with a `TextMeasurer` once per data change, as for X-axis labels.

## Confirmed

### Stacked charts have no public formatters

`BarChart`, `HistogramChart`, `LineChart`, and `LiveLineChart` take an `axisValueFormatter` for Y
labels, and all but `LiveLineChart` take a `valueFormatter` for the selected-value readout.
`StackedBarChart` and `StackedAreaChart` take neither: their Y labels always use
`defaultAxisValueFormatter`, and their readout always uses `ChartValueFormatters.Default`. Apps
cannot add a currency prefix or change the precision on a stacked chart.

The bar and line defaults and `defaultAxisValueFormatter` are also three copies of one formatter,
so a change to one must be made to all three.

Confirmed by the public signatures of `StackedBarChart` and `StackedAreaChart`.

Plan, before 3.0 ships:

- Add `valueFormatter` and `axisValueFormatter` to `StackedBarChart` and `StackedAreaChart`, with
  defaults on `StackedBarChartDefaults` and `StackedAreaChartDefaults` that mirror bar and line.
- Delete `defaultAxisValueFormatter`.
- Add a line to the stacked bar and stacked area migration notes, a release note, and a docs
  example.
- Test that a custom formatter reaches both the Y labels and the readout.

After 3.0 ships, adding parameters to a public composable changes its JVM signature. Compiled
callers then keep working only through a hidden, deprecated overload with the old signature, so
landing this before the release is cheaper. Pie and radar charts have no value axis; whether they
take a readout-only `valueFormatter` can be decided later.

### Overflowing stacked totals print Infinity and NaN

Stacked totals of finite values can overflow to infinity. The Y labels then read `Infinity`, and
the bottom label reads `NaN`, because infinity × 0 is NaN.

Confirmed by `buildNumericYAxisTicks` for `0.0..Double.POSITIVE_INFINITY`: the first four of five
ticks are infinity and the last is NaN. Validation checks each stacked value, not the totals.

Options:

- Return `max` and `min` directly for the first and last tick, so the bottom label reads the
  minimum.

### Line charts cannot show ranges wider than Double.MAX_VALUE

For a range such as `-1e308..1e308`, `normalizeByMinMax` and `baselineYForRange` compute
`max − min`, which overflows, so the line and the zero line are drawn in the wrong place. The Y
tick values stay right, because `buildNumericYAxisTicks` never subtracts the ends. Bar charts map
values with their own overflow-safe `barValueYFraction`.

Confirmed by `baselineYForRange(-1e308, 1e308, 200f)`, which returns 200, the bottom of the plot.
The misplaced line follows from the same overflow in `normalizeByMinMax`; no rendered chart shows
it yet.

### Near-flat ranges can put a tick outside the range

A range whose ends are one double apart can still put a tick one double outside it. This changes a
label only next to a rounding tie.

Confirmed by evaluating `max × (1 − p) + min × p` in double arithmetic: about 1% of ticks fall
outside ranges one double wide.

### Long labels are cut off

The Y label column is never wider than 40% of the chart width, and a longer label shows only as
much as fits on one line. Very large values in the default formatter can hit this, such as stacked
totals in the millions on a narrow chart, as can a long custom `axisValueFormatter`. The cut-off
label gives no sign that text is missing.

Confirmed by the code: `yAxisLabelColumnWidthPx` caps the column at 40% of the available width,
and `AxisYLabelsLayout` measures each label to that width on one line. Every chart uses it through
`rememberNumericYAxisLayout`.

Options:

- End cut-off labels with an ellipsis, so the missing text is visible.
- Shorten large values, such as `12.5M` for 12,500,000, before the cap is reached.

### Bar charts count the bars that fit with an estimated Y column

Bar and stacked bar charts decide how many bars fit, and so whether they scroll or compact, before
they know the plot height. They estimate the Y-axis column at the full chart height, where the tick
count can be higher than on the plot, and the column then drawn can be a character wider or
narrower than the estimate. Near the threshold, a chart can scroll when its bars would just fit, or
fit bars slightly narrower than `minBarWidth`.

Confirmed by the code: `BarChart` and `StackedBarChart` pass `constraints.maxHeight` as the plot
height to `rememberNumericYAxisLayout`, and their content builds the drawn column again from the
real plot height. Building the Y layout twice also formats every tick twice per data change, which
costs little.

Options:

- Estimate the plot height from the chart height less the X-axis row, which the outer layout can
  compute from the same label estimate.
