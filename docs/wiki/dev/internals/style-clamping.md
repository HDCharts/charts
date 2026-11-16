---
title: Style Clamping
order: 4
---

# Style Clamping

Charts never reject a style value they can correct. Range bounds and axis labels are the
exceptions, described below. A value out of range is drawn as the closest value that makes sense, so a slider, an animation, or float math that lands on `1.0000001f` never replaces a chart
with an error.

Styles are clamped; data is reported. The two halves of that rule, and why they differ, are on
[Validation Errors](validation-errors.md).

## Rules

| Value | Out of range | `NaN` |
| --- | --- | --- |
| Alpha | Clamped to `0..1` | Drawn as `1f`, so the alpha has no effect |
| Size (`Dp`) | Clamped to `0.dp` up to 16384 pixels (`MAX_SIZE_PX`) | Falls back to that size's `StyleDefaults` default |
| Text size (`sp`) | Clamped to at most 16384 pixels (`MAX_SIZE_PX`); zero, negative, or infinite falls back to the default | Falls back to that size's `StyleDefaults` default |
| Grid steps | Clamped to `0..1000` (`MAX_GRID_STEPS`) | — |
| Gradient stop offset | Clamped to `0..1`, then the stops are sorted | Stop dropped, as is an infinite offset |

A `NaN` size is usually `Dp.Unspecified`, which Compose uses to mean "use the default", so it falls
back to the default of that size. No single size fits every element, and `0.dp` would hide it. A
`NaN` alpha has no such meaning, so `1f` is enough. A text size is not a length, so `clampTextSize`
also falls back for a non-`sp` unit and for zero, which would draw nothing.

A gradient left with no stops, or with non-finite positions or the same start and end, clamps to
null, so the shape draws solid. A single stop is drawn as its color at both ends. That rule is
`ChartGradient.clamp()` in [ChartGradient.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/style/ChartGradient.kt).

The 16384 pixel cap keeps a size inside Compose layout limits. It depends on the screen density,
which is why `clampSize` and `clampTextSize` take a `Density`.

## Where it happens

Clamping runs from the inside out. Each style block clamps its own fields; the chart style's
`clamp(density)` extension composes those results.

| Owner | Responsibility |
|---|---|
| [StyleClamping.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/StyleClamping.kt) in `charts-core` | Shared alpha, size, text-size, and grid-step helpers |
| Style block's `clamp` | Corrects its fields using the matching `StyleDefaults` fallbacks |
| Chart style's `clamp` extension | Combines the clamped blocks |
| Chart entry | Remembers a clamped copy at the current density and passes it to content |

Indexed-series entries use `ChartEntry`; other data models remember the clamped style in their
own entry. See [Entry Seam and Chart Policy](entry-seam.md).

Style constructors and factories retain the caller's values. Clamping produces a separate copy
for drawing, preserving equality and caller-visible style values.

[`BarChartStyle.kt`](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/style/BarChartStyle.kt)
shows block-level clamping; the chart-style extensions compose the same pattern.

## Construct every field

Construct the block explicitly in `clamp()`, naming every field. Its constructor has no defaults,
so adding a field makes the clamp fail to compile until that field is handled. A `copy()` call
could silently pass a new field through unclamped.

A block with no correctable values needs no clamp. Range bounds and axis-label settings pass to
validation, which reports values that cannot be drawn.

## Adding a style value

- Give it a default in `StyleDefaults`.
- Clamp it in the **block's** `clamp()`: alphas with `clampAlpha()`, sizes with `clampSize()` or
  `clampTextSize()`, and the same `StyleDefaults` value the factory uses as the fallback. The build
  will not compile until you do.
- A field with nothing to clamp still gets named in the block's `clamp()`, passed straight through.
- A field added directly to a chart style, above its blocks, is named in the chart style's
  `clamp()`, which already lists every field.
- Do not add a validation error for a value that clamping handles.

## Tests

- [StyleClampingTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/internal/StyleClampingTest.kt) in `charts-core` covers helper behavior, including non-finite and unspecified
  values.
- Chart style-block clamp tests cover each field without Compose.
- [ChartGradientsTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/style/ChartGradientsTest.kt) covers gradient clamping.
- Chart interaction and style-default tests check that invalid numeric styles draw a corrected
  chart without displaying validation errors.
