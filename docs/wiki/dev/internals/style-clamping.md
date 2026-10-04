---
title: Style Clamping
order: 5
---

# Style Clamping

Charts never reject a style value. A value out of range is drawn as the closest value that makes
sense, so a slider, an animation, or float math that lands on `1.0000001f` never replaces a chart
with an error.

Styles are clamped; data is reported. The two halves of that rule, and why they differ, are on
Validation Errors.

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

Clamping runs one level at a time, from the inside out: a block clamps its own fields, and the chart
style composes its blocks' clamps and adds nothing of its own.

- The helpers are `clampAlpha()`, `clampSize(fallback, density)`,
  `clampTextSize(fallback, density)`, and `clampGridSteps()` in `StyleClamping.kt` in `charts-core`.
- **A style block clamps itself.** `clamp(density)` is a member of the block, next to the fields it
  owns and beside `resolveColors()` where that exists:

  ```kotlin
  data class BarBarsStyle(
      val color: Color,
      val colors: ImmutableList<Color>,
      val alpha: Float,
      val space: Dp,
      val minBarWidth: Dp,
  ) {
      fun resolveColors(barCount: Int): ImmutableList<Color> = ...

      internal fun clamp(density: Density) =
          BarBarsStyle(
              color = color,
              colors = colors,
              alpha = alpha.clampAlpha(),
              space = space.clampSize(fallback = StyleDefaults.barSpacing, density = density),
              minBarWidth = minBarWidth.clampSize(fallback = StyleDefaults.minBarWidth, density = density),
          )
  }
  ```

- **A chart style composes its blocks.** `clamp(density)` stays an extension on the chart style,
  because the chart owns no rules of its own — it only names which blocks clamp. For example
  `StackedAreaChartStyle.clamp` in `StackedAreaChartStyle.kt`:

  ```kotlin
  internal fun StackedAreaChartStyle.clamp(density: Density) =
      StackedAreaChartStyle(
          chartContainerStyle = chartContainerStyle,
          fill = fill.clamp(),
          axis = axis,
          selection = selection.clamp(density),
          zoomControlsVisible = zoomControlsVisible,
      )
  ```

- Each chart calls `clamp()` once, at its entry point, and passes the copy to its drawing code.
  Drawing code never clamps on its own. Every chart routes its entry through `ChartEntry` in
  `charts-core`, which remembers `style.clamp(density)` and hands the copy to the content.
- A chart's spec delegates to the chart style's extension rather than repeating it, so the rules stay
  next to the style they describe. It is the one member of a spec that no chart varies: clamping is
  the same for every chart, and what differs lives inside each block. Entry Seam and Chart Policy
  says why it is on the spec at all.
- Factories and style constructors never clamp. A style keeps exactly what the user passed, so
  `copy()` and equality behave as users expect.

## Why a block's clamp constructs instead of copying

**A block's `clamp()` must name every field, and must never use `copy()`.** Block constructors have
no default parameter values — the defaults live in the chart's `Defaults` DSL — so a constructor call
that omits a field does not compile. Adding a field to a block therefore breaks the build in exactly
one place, its own clamp, until someone decides whether that field needs clamping:

```
e: BarChartStyle.kt:72:5 No value passed for parameter 'cornerRadius'.
```

`copy()` would compile and silently leave the new field unclamped, which is the failure this
structure exists to prevent. The chart style's own `clamp()` already named every top-level field; it
is the nested `copy()` calls that used to be the hole.

Blocks with nothing to clamp get no `clamp()` — `BarRangeStyle` and `LineRangeStyle` hold optional
bounds that validation reports instead of clamping.

Axis label styles are passed through rather than clamped. A label size that cannot be drawn is a
validation error reported by `validateAxisLabels`, not a value to correct silently.

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

- `StyleClampingTest` in `charts-core` covers each helper, including `NaN`, `Dp.Unspecified`, and an
  unspelled text unit.
- `BarStyleBlocksClampTest`, `LineStyleBlocksClampTest`, `PieStyleBlocksClampTest`,
  `RadarStyleBlocksClampTest`, `StackedBarStyleBlocksClampTest`, and `StackedAreaStyleBlocksClampTest`
  pin what each block clamps. They build the blocks directly, so no Compose is involved.
- Line, radar, stacked bar, and stacked area each have a
  `*_withInvalidNumericStyleValues_drawsClampedChart` test that draws the chart with out-of-range
  values and checks that no error appears.
- `BarStyleDefaultsTest` checks the clamped bar values end to end, which histogram shares.
