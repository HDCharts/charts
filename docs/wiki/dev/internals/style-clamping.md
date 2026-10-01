---
title: Style Clamping
order: 5
---

# Style Clamping

Charts never reject a style value. A value out of range is drawn as the closest value that makes
sense, so a slider, an animation, or float math that lands on `1.0000001f` never replaces a chart
with an error.

## Rules

| Value | Out of range | `NaN` |
| --- | --- | --- |
| Alpha | Clamped to `0..1` | Drawn as `1f`, so the alpha has no effect |
| Size (`Dp`) | Clamped to `0.dp` up to 16384 pixels (`MAX_SIZE_PX`) | Falls back to that size's `StyleDefaults` default |
| Text size (`sp`) | Clamped to a positive size up to 16384 pixels (`MAX_SIZE_PX`) | Falls back to that size's `StyleDefaults` default |
| Grid steps | Clamped to `0..1000` (`MAX_GRID_STEPS`) | — |

A `NaN` size is usually `Dp.Unspecified`, which Compose uses to mean "use the default", so it falls
back to the default of that size. No single size fits every element, and `0.dp` would hide it. A
`NaN` alpha has no such meaning, so `1f` is enough. A text size is not a length, so `clampTextSize`
also falls back for a non-`sp` unit and for zero, which would draw nothing.

The 16384 pixel cap keeps a size inside Compose layout limits. It depends on the screen density,
which is why `clampSize` and `clampTextSize` take a `Density`.

## Where it happens

- The helpers are `clampAlpha()`, `clampSize(fallback, density)`,
  `clampTextSize(fallback, density)`, and `clampGridSteps()` in `StyleClamping.kt` in `charts-core`.
- Each chart's style has a `clamped(density)` extension next to its style class, for example
  `StackedAreaChartStyle.clamped` in `StackedAreaChartStyle.kt`. It returns a copy with every
  number clamped.
- Each chart calls `clamped()` once, at its public entry point, as
  `remember(style, density) { style.clamped(density) }`, and passes the copy to its drawing code.
  Drawing code never clamps on its own.
- Factories and style constructors never clamp. A style keeps exactly what the user passed, so
  `copy()` and equality behave as users expect.

## Adding a style value

- Give it a default in `StyleDefaults`.
- Clamp it in the chart's `clamped()`: alphas with `clampAlpha()`, sizes with `clampSize()` or
  `clampTextSize()`, and the same `StyleDefaults` value the factory uses as the fallback.
- Do not add a validation error for it.

## Tests

- `StyleClampingTest` in `charts-core` covers each helper, including `NaN`, `Dp.Unspecified`, and an
  unspelled text unit.
- Line, radar, stacked bar, and stacked area each have a
  `*_withInvalidNumericStyleValues_drawsClampedChart` test that draws the chart with out-of-range
  values and checks that no error appears.
- `BarStyleDefaultsTest` checks the clamped bar values, which histogram shares. Pie has no clamping
  test.
