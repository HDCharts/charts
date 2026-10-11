---
title: Gradients
---

# Gradients

Bar and histogram charts paint their bars with a gradient through the `gradient` parameter
of their `bars()` style. A gradient runs along a line through any number of colors, and draws
either per bar or across the whole plot.

## Supported Charts

| Chart | Style parameter |
|---|---|
| Bar | `BarChartDefaults.bars(gradient = ...)` |
| Histogram | `HistogramChartDefaults.bars(gradient = ...)` |

Line, pie, radar, ring gauge, stacked bar, and stacked area charts draw solid colors.

## Use

Pass a `ChartGradient` to `bars()`. `ChartGradients` builds the common ones from a list of
colors.

```kotlin
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan

@Composable
fun ShowGradientBar() {
    val data = listOf(45.0, -12.0, 38.0, 27.0, -19.0, 42.0, 31.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        seriesName = "Net cash flow",
    )
    val gradient = ChartGradients.vertical(
        colors = listOf(Color(0xFF1E89E6), Color(0xFFEA7A2E)),
    )
    val bars = BarChartDefaults.bars(gradient = gradient)

    BarChart(
        data = data,
        title = "Daily Net Cash Flow",
        style = BarChartDefaults.style(bars = bars),
    )
}
```

A style without a `gradient` draws solid bars.

## Per Bar or Across the Plot

A gradient draws in one of two ways, set by its `span`:

| `span` | Drawing |
|---|---|
| `GradientSpan.Shape` | Per bar: every bar shows the whole gradient. This is the default. |
| `GradientSpan.Plot` | Across the plot: one gradient covers the whole plot, and each bar shows the part it sits on. |

### Per Bar

Each bar draws the whole gradient within its own bounds, so every bar looks the same apart
from its height. A vertical gradient runs from the bar's end to the baseline. A bar below
zero mirrors it, so it still runs from its end, at the bottom, up to the baseline.

```kotlin
// Every bar goes from blue at its end to orange at the baseline.
BarChartDefaults.bars(
    gradient = ChartGradients.vertical(
        colors = listOf(Color(0xFF1E89E6), Color(0xFFEA7A2E)),
        span = GradientSpan.Shape,
    ),
)
```

### Across the Plot

One gradient covers the whole plot, and each bar shows the part of it the bar sits on. A
horizontal gradient moves from the first bar's color to the last bar's color. A vertical one
gives taller bars more of the top color, so the color follows the value.

```kotlin
// The first bar is blue, the last bar is orange, and the bars between blend from one to the other.
BarChartDefaults.bars(
    gradient = ChartGradients.horizontal(
        colors = listOf(Color(0xFF1E89E6), Color(0xFFEA7A2E)),
        span = GradientSpan.Plot,
    ),
)
```

```kotlin
// Tall bars reach into red; short bars stay green.
BarChartDefaults.bars(
    gradient = ChartGradients.vertical(
        colors = listOf(Color(0xFFE5484D), Color(0xFFF5D90A), Color(0xFF30A46C)),
        span = GradientSpan.Plot,
    ),
)
```

When dense data is expanded to scroll, the plot gradient covers every bar and scrolls with
them.

## Ready-Made Gradients

`ChartGradients` spaces the colors evenly along the gradient.

| Function | Gradient |
|---|---|
| `vertical(colors, span)` | Top to bottom. With `GradientSpan.Shape`, from each bar's end to the baseline. |
| `horizontal(colors, span)` | Left to right. |
| `linear(angleDegrees, colors, span)` | At an angle, clockwise from left to right. `45` runs from the top-left corner to the bottom-right corner whatever the bounds' shape. At other angles on non-square bounds, the end colors hold near the corners. |
| `fade(endAlpha, span)` | Top to bottom, from each bar's own color to that color at `endAlpha`. |

## Custom Gradients

Build a `ChartGradient.Linear` directly for full control over stops and geometry. It takes
`start` and `end` points, a `tileMode` for beyond them, `stops`, and a `span`. Points are
`GradientPoint`s, as fractions of the gradient's bounds: `(0, 0)` is the top-left corner and
`(1, 1)` the bottom-right. Stops are `GradientStop`s with an `offset` from `0` at the start to
`1` at the end.

```kotlin
val highlight = ChartGradient.Linear(
    stops = listOf(
        GradientStop.Series(offset = 0f, alpha = 1f),
        GradientStop.Fixed(offset = 0.6f, color = Color.White),
        GradientStop.Series(offset = 1f, alpha = 0.2f),
    ),
    start = GradientPoint(x = 0f, y = 0f),
    end = GradientPoint(x = 1f, y = 1f),
    span = GradientSpan.Shape,
    tileMode = TileMode.Clamp,
)
```

## Colors

A stop is one of two kinds:

- `GradientStop.Fixed(offset, color)` draws that color as given.
- `GradientStop.Series(offset, alpha)` draws the bar's own color at `alpha`. The bar's color comes
  from `color` or `colors` in `bars()`, so one gradient follows per-bar colors.

`ChartGradients.fade()` uses series colors, so each bar fades from its own color:

```kotlin
BarChartDefaults.bars(
    color = Color(0xFF22B59A),
    gradient = ChartGradients.fade(endAlpha = 0.15f),
)
```

## Alpha and Selection

The `alpha` of `bars()` multiplies the whole gradient. While a bar is selected, the other bars
draw their gradient at `selection.unselectedAlpha`, as solid bars do.

## Clamping

A gradient never replaces the chart with an error. The chart draws the closest gradient it can:

- An offset outside `0..1` is clamped to that range, and stops out of order are sorted. Equal
  offsets make a hard edge between two colors.
- A stop with a non-finite offset is dropped.
- A single stop paints its color solid.
- With no stops, non-finite positions, or the same start and end, the bars draw solid, without a
  gradient.
- A `GradientStop.Series` alpha outside `0..1` is clamped to that range.
