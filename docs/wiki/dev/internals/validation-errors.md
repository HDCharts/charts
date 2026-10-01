---
title: Validation Errors
order: 6
---

# Validation Errors

A chart shows a validation error instead of drawing when its input has no correct fallback. The
chart draws the errors as text in its container with `ChartErrors`, one error per line.

## What is an error

- **Data problems:** too few values, series of different lengths, values that are not finite, and
  negative values in charts that need positive ones.
- **Counts that must match:** categories that do not match the value count, and colors that do not
  match what they color.
- **Settings with no fallback:** non-finite range bounds and invalid axis label settings.

Style values such as alphas, sizes, and grid steps are never errors. They are clamped instead, as
Style Clamping describes.

## Messages

Every message comes from `ValidationErrors` in `DataValidation.kt` in `charts-core`, so the same
rule reads the same way in every chart.

| Rule | Message | Charts |
| --- | --- | --- |
| No series | At least one series is required. | Line, radar, stacked bar, stacked area |
| Wrong series count | Exactly one series is required; got 2. | Bar, histogram |
| Too few values | At least 2 values are required. (3 on radar) | All |
| Category count | Category count (3) must match value count (4). | Bar, histogram, line, radar, stacked bar, stacked area |
| Color count | Color count (2) must match series count (3). | All with a color list |
| Series length | Series 1 is not aligned with the first series. | Line, radar, stacked bar, stacked area |
| Not finite | Series 0 contains a non-finite value. | Line, radar, stacked bar, stacked area |
| Not finite | Value at index 2 is not finite. | Bar, histogram, pie |
| Negative | Series 0 contains a negative value. | Stacked bar, stacked area |
| Negative | Value at index 2 is negative. | Histogram, pie |

The color message names what the colors are matched to: `value` on bar and histogram, and `series` on
line, radar, and the stacked charts.

Charts that draw several series report a problem once per series. Charts with one series report
it per value, with its index.

## Where it happens

All checks live in `DataValidation.kt` in `charts-core`. Charts have no validation code of their
own; each chart combines these functions where it validates its input:

| Function | Checks | Charts |
| --- | --- | --- |
| `validateSeries` | No series, too few values, category count, series length, non-finite values, negative values with `allowNegative = false`, and the series color count | Line, radar, stacked bar, stacked area |
| `validateSingleSeries` | One series, too few values, color count, category count, and each bad value | Bar, histogram |
| `validateValues` | Each non-finite value, and each negative value with `allowNegative = false` | Pie, and inside `validateSingleSeries` |
| `validateColorCount` | A color list that does not match what it colors | Inside the two series checks |
| `validateRange` | Non-finite range bounds | Bar, histogram, line |

Axis label settings are checked by `validateAxisLabels` in `AxisLabelValidation.kt`.

For example, stacked bar validates its input with:

```kotlin
validateSeries(
    data = data,
    minValues = ValidationErrors.MIN_VALUES,
    allowNegative = false,
    colorCount = style.segments.colors.size,
) + validateAxisLabels(style.axis.xLabels, style.axis.yLabels, density)
```

Line keeps this combination in `validateLineInput` in `LineChartEntry.kt`, because `LineChart` and
`LiveLineChart` share it.

The minimums are `ValidationErrors.MIN_VALUES` (2) and `ValidationErrors.MIN_RADAR_VALUES` (3).
With no series, `validateSeries` reports only that error, and `validateSingleSeries` stops at the
first shape problem.

## Adding a rule

- Add the message to `ValidationErrors` and the check to `DataValidation.kt`. Do not write message
  text or checks in a chart.
- If the rule applies to more than one chart, add it to the function those charts already call.
- Keep messages short, name the value or series, and say what is expected.

## Tests

`DataValidationTest` in `charts-core` covers every function and the message text. Each chart's
tests check that its errors are shown for invalid input.
