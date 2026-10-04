---
title: Validation Errors
order: 6
---

# Validation Errors

A chart shows a validation error instead of drawing when its input has no correct fallback. The
chart draws the errors as text in its container with `ChartErrors`, one error per line.

## The rule

**Styles are clamped, data is reported. Neither ever throws.**

The two halves are deliberately different, because a wrong style value and a wrong data value are
different mistakes:

| | Out of range | Not finite | Effect |
| --- | --- | --- | --- |
| Style | Clamped to the nearest drawable value | Falls back to that value's `StyleDefaults` default | Drawn |
| Data | Reported, if the chart cannot draw it | Reported | Nothing drawn |

A style has an obvious correct answer — a negative bar spacing means zero spacing — so correcting it
draws the chart the user meant. Data has no such answer: a `NaN` in a series is not a number to
approximate, and drawing the other points as though it were fine would be a lie. So the chart says
what is wrong and draws nothing.

Three consequences worth stating, because each has been got wrong:

- **A malformed value becomes a message, not an exception.** The checks below cover every value that
  reaches them, so malformed input is reported rather than thrown. A `require` in the library is only
  for a programmer's own argument — a formatter precision, a selection delay — never for chart data.
- **A check reports everything it can.** `errorsFor` collects every enabled error and the container
  lists them all. The one exception is a shape error that makes the rest unreadable: with no series
  there is nothing to iterate, and a single-series chart stops at its first shape problem.
- **Nothing downstream re-checks.** Validation runs once, at the entry seam. Conversion and drawing
  take a validated model and may assume it: at least one series, the same number of points in each,
  every value finite, and a label or a blank for every index. That is a precondition of the model,
  not a check each stage repeats — and it is pinned by tests rather than by throwing.

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
| Wrong series count | Exactly one series is required; got 2. | Bar, histogram, pie |
| Too few values | At least 2 values are required. (3 on radar) | All |
| Category count | Category count (3) must match value count (4). | Bar, histogram, line, pie, radar, stacked bar, stacked area |
| Color count | Color count (2) must match series count (3). | All with a color list |
| Series length | Series 1 is not aligned with the first series. | Line, radar, stacked bar, stacked area |
| Not finite | Series 0 contains a non-finite value. | Line, radar, stacked bar, stacked area |
| Not finite | Value at index 2 is not finite. | Bar, histogram, pie |
| Negative | Series 0 contains a negative value. | Stacked bar, stacked area |
| Negative | Value at index 2 is negative. | Histogram, pie |

The color message names what the colors are matched to: `value` on bar, histogram and pie, and
`series` on line, radar, and the stacked charts.

Charts that draw several series report a problem once per series. Charts with one series report
it per value, with its index.

## Where it happens

All checks live in `DataValidation.kt` in `charts-core`. A chart writes no check and no message text
of its own: it declares which checks apply in its `ChartSpec` and lets `ChartEntry` combine them.
Every chart is on the seam. The declaration and the per-chart table are on Entry Seam and
Chart Policy.

| Function | Checks | Charts |
| --- | --- | --- |
| `validateSeries` | No series, too few values, category count, series length, non-finite values, negative values with `allowNegative = false`, and the series color count | Line, radar, stacked bar, stacked area |
| `validateSingleSeries` | One series, too few values, color count, category count, and each bad value | Bar, histogram, pie |
| `validateValues` | Each non-finite value, and each negative value with `allowNegative = false` | Inside `validateSingleSeries` |
| `validateColorCount` | A color list that does not match what it colors. A null expectation skips the check, and so does a style that sets no colors | Inside the two series checks |
| `validateRange` | Non-finite range bounds | Bar, histogram, line |

Axis label settings are checked by `validateAxisLabels` in `AxisLabelValidation.kt`.

The seam composes the enabled checks in one order — data shape, then range, then axis labels — so two
charts with the same policy report the same problems in the same sequence. `ChartPolicy.errorsFor` is
the only caller of the four functions above, and Entry Seam and Chart Policy has which checks each
chart enables.

The minimums are `ValidationErrors.MIN_VALUES` (2) and `ValidationErrors.MIN_RADAR_VALUES` (3).
With no series, `validateSeries` reports only that error, and `validateSingleSeries` stops at the
first shape problem.

`validateSeries` takes the expected color count as `expectedColors`, read from the data by the
policy. A chart whose count depends on the data states the rule rather than a constant, which is
what radar does.

## Adding a rule

- Add the message to `ValidationErrors` and the check to `DataValidation.kt`. Do not write message
  text or checks in a chart, and give no check parameter a default.
- If the rule applies to more than one chart, add it to the function those charts already call.
- Keep messages short, name the value or series, and say what is expected.
- A rule belongs here, in the path that reports, rather than as an assertion further down. An
  assertion in the conversion would fire on the same data and crash instead of drawing the message.

## Internal model types

Everything below the seam reads the render model the checks make possible, and trusts it. The model
is `ChartRenderData`: the caller's own `ChartData` plus the chart's `title`. Four constraints on it:

**A value that is required is not optional, and optionality is explicit.** `ChartRenderData.title` is a
`String`, so an absent title is written as `""`, and `ChartSeries.name` is a `String?` that a chart
converts to `""` when it draws a name. That makes "no label" indistinguishable from a blank one, and
the cost shows up where a stage has to repair it: stacked bar's compaction invents
`"Bucket ${bucketIndex + 1}"` for a blank category, duplicating the fallback `resolveAxisLabel`
already owns. Deciding that in the renderer — one fallback, in one place — is the fix, and it touches
no public API.

**The model holds the caller's data, not a copy.** There is no second leaf type and no per-point copy
of the category list, so nothing below the seam can drift from what the caller passed, and there is
only one type named `ChartData` in the library. A label at an index is `categories[index]`, and
validation has already made the categories either empty or exactly as long as the values, so no read
site needs a fallback for a **missing** label. Blank is a different case and stays legal: that is
`resolveAxisLabel`'s job, and X-Axis Labels owns it.

**Not every guard below the seam is redundant.** `normalizeValue` ends with `.coerceIn(0f, 1f)`
after dividing by the domain width, and the values inside that domain are finite by construction —
yet line resolves its domain from the caller's `style.range`, so a caller who sets a maximum below
their highest value gets a domain that clips the data. The clamp is what stops the line drawing
outside the plot. The same reasoning keeps every zoom, reveal-progress and canvas-bounds clamp: a
zero-size canvas and a mid-animation frame are real states, not bad data. A guard below the seam is
worth classifying before it is removed — viewport, caller-chosen range, or data.

**A numeric payload cannot be given a NaN-free type cheaply.** Kotlin has no NaN-free `Double`, and
the obvious wrapper is wrong for this model: a `@JvmInline value class` element inside a `List` is
boxed on JVM, Native and JS, so a ten-million-point chart would allocate ten million boxes and every
arithmetic site would pay to unbox. Finiteness is therefore a property of construction — the only way
into the model is through this page's checks — and the invariant is pinned by
`ChartRenderDataTest` rather than by throwing.

## Tests

`DataValidationTest` in `charts-core` covers every function and the message text. Each chart's
tests check that its errors are shown for invalid input. `ChartRenderDataTest` covers the model the
checks make possible: that it holds the caller's own series and categories, the stacked-bar transpose,
and that `errorsFor` rejects the data the model assumes.
