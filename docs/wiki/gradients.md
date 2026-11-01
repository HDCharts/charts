---
title: Gradients
---

# Gradients

Gradients color chart shapes along a line between stops. Supported styles accept a `ChartGradient`:
bar, histogram, stacked bar, stacked area, and line. Other chart families use solid colors.

| Chart | Style factory | Guide |
|---|---|---|
| Bar | `BarChartDefaults.bars(gradient = ...)` | [Bar chart](/{{version}}/wiki/bar-chart) |
| Histogram | `HistogramChartDefaults.bars(gradient = ...)` | [Histogram chart](/{{version}}/wiki/histogram-chart) |
| Stacked bar | `StackedBarChartDefaults.segments(gradient = ...)` | [Stacked bar chart](/{{version}}/wiki/stacked-bar-chart) |
| Stacked area | `StackedAreaChartDefaults.fill(gradient = ...)` | [Stacked area chart](/{{version}}/wiki/stacked-area-chart) |
| Line | `LineChartDefaults.line(gradient = ...)` | [Line chart](/{{version}}/wiki/line-chart) |

## Span and Direction

`GradientSpan.Shape` uses each shape's bounds, so every shape shows the full gradient. `GradientSpan.Plot`
uses the plot bounds, so a shape shows only the part that crosses it. For bars below zero, a shape-spanning
vertical gradient mirrors at the baseline: it still runs from the value edge toward zero.

The following compiled example compares a repeated vertical gradient with a horizontal gradient spanning
the plot. The negative values also show the mirrored shape behavior.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/GradientGuideShapePlotExample.kt#gradients-shape-and-plot -->
```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan

@Composable
fun CompareShapeAndPlotGradients() {
    val data =
        listOf(45.0, -12.0, 38.0, 27.0, -19.0).toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Net cash flow",
        )
    val colors = listOf(Color(0xFF1E89E6), Color(0xFF9B4EDD), Color(0xFFEA7A2E))

    Column {
        BarChart(
            data = data,
            title = "Every bar repeats the gradient",
            style =
                BarChartDefaults.style(
                    bars = BarChartDefaults.bars(gradient = ChartGradients.vertical(colors)),
                ),
        )
        BarChart(
            data = data,
            title = "One gradient spans the plot",
            style =
                BarChartDefaults.style(
                    bars =
                        BarChartDefaults.bars(
                            gradient = ChartGradients.horizontal(colors, span = GradientSpan.Plot),
                        ),
                ),
        )
    }
}
```
<!-- END WIKI EXAMPLE: gradients-shape-and-plot -->

The [bar guide's gradient example](/{{version}}/wiki/bar-chart#gradients) shows a basic chart setup.
The [bar shape](/content/{{version}}/wiki/assets/bar_gradient_shape.gif) and [histogram shape](/content/{{version}}/wiki/assets/histogram_gradient_shape.gif)
recordings show per-shape gradients. The [bar horizontal plot](/content/{{version}}/wiki/assets/bar_gradient_plot_horizontal.gif),
[bar vertical plot](/content/{{version}}/wiki/assets/bar_gradient_plot_vertical.gif),
[histogram horizontal plot](/content/{{version}}/wiki/assets/histogram_gradient_plot_horizontal.gif), and
[histogram vertical plot](/content/{{version}}/wiki/assets/histogram_gradient_plot_vertical.gif) recordings
show gradients spanning the plot. Each uses a longer generated series than the teaching example; the
bar and histogram recording sources are [BarGradientExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/BarGradientExample.kt)
and [HistogramGradientExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/HistogramGradientExample.kt).

For a shape-spanning vertical gradient, color follows each shape's extent. For a plot-spanning vertical
gradient, taller bars reach farther toward the top color. A horizontal plot gradient runs from the left
side of the plot to its right side. In dense scrollable charts, the plot bounds include the full series,
so the gradient moves with the scrolling content.

## Ready-Made Gradients

`ChartGradients` spaces fixed colors evenly between the start and end points.

| Factory | Direction and behavior |
|---|---|
| `vertical(colors, span)` | Top to bottom. Shape-spanning bars mirror below zero. |
| `horizontal(colors, span)` | Left to right. |
| `linear(angleDegrees, colors, span)` | Clockwise from left to right; `45f` runs top-left to bottom-right. Positions use normalized bounds, so at other angles a non-square shape can hold end colors near corners. |
| `fade(endAlpha, span)` | Top to bottom from each shape's resolved series color to that color at `endAlpha`. |

`fade()` uses `GradientStop.Series`, so each series keeps its own color. Fixed-color gradients use
`GradientStop.Fixed` and paint the same colors on each series. A chart style's color supplies the series
color used by series stops, and the style alpha multiplies the complete gradient. This makes a series
gradient the useful choice when a chart legend needs to remain visually matched to its shapes. Stacked
bar and stacked area styles apply one gradient to all segments or layers; they do not accept a separate
gradient per series.

The existing [bar fade](/content/{{version}}/wiki/assets/bar_gradient_fade.gif), [histogram fade](/content/{{version}}/wiki/assets/histogram_gradient_fade.gif), [stacked bar](/content/{{version}}/wiki/assets/stacked_bar_gradient.gif), and [stacked area](/content/{{version}}/wiki/assets/stacked_area_gradient.gif) recordings demonstrate series-color fades. Sources: [BarGradientExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/BarGradientExample.kt), [HistogramGradientExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/HistogramGradientExample.kt), [StackedBarGradientExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/StackedBarGradientExample.kt), [StackedAreaGradientExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/StackedAreaGradientExample.kt).

Line gradients paint the stroke; points and selection markers keep their own colors. `Shape` uses the
line's bounds, while `Plot` follows the full plot. In `LiveLineChart`, a shape-spanning gradient follows
the visible line bounds as they change. See the [line gradient example](/{{version}}/wiki/line-chart#gradients)
and [line gradient recording](/content/{{version}}/wiki/assets/line_gradient.gif), recorded by
[LineGradientExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LineGradientExample.kt).

## Custom Stops and Geometry

Use `ChartGradient.Linear` when a gradient needs an accent stop or custom endpoints. `GradientPoint`
coordinates are fractions of the selected bounds: `(0, 0)` is top-left and `(1, 1)` bottom-right.
Offsets run from `0` at `start` to `1` at `end`. A `Fixed` stop keeps its given color; a `Series` stop
uses the shape's resolved color at the specified alpha.

This compiled example keeps the cash-flow color at each end and adds a white highlight between them.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/GradientGuideStopsExample.kt#gradients-custom-stops -->
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.GradientPoint
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.GradientStop

@Composable
fun HighlightCashFlowExtremes() {
    val data =
        listOf(45.0, -12.0, 38.0, 27.0, -19.0).toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Net cash flow",
        )
    val gradient =
        ChartGradient.Linear(
            stops =
                listOf(
                    GradientStop.Series(offset = 0f, alpha = 1f),
                    GradientStop.Fixed(offset = 0.55f, color = Color.White),
                    GradientStop.Series(offset = 1f, alpha = 0.25f),
                ),
            start = GradientPoint(x = 0f, y = 0f),
            end = GradientPoint(x = 1f, y = 1f),
            span = GradientSpan.Shape,
            tileMode = TileMode.Clamp,
        )

    BarChart(
        data = data,
        title = "Cash flow with a highlight",
        style = BarChartDefaults.style(bars = BarChartDefaults.bars(gradient = gradient)),
    )
}
```
<!-- END WIKI EXAMPLE: gradients-custom-stops -->

## Invalid Values

Charts clamp gradient input when drawing. Offsets outside `0..1` are clamped, stops are sorted by
offset, and non-finite offsets are dropped. Equal offsets create a hard color edge. Series alpha is
clamped to `0..1`. A single remaining stop paints a solid color; no usable stops, non-finite endpoints,
or identical start and end points fall back to the style's solid shape color.

## API Reference

- [ChartGradient](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-chart-gradient/index.html)
- [ChartGradient.Linear](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-chart-gradient/-linear/index.html)
- [ChartGradients](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-chart-gradients/index.html)
- [GradientSpan](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-gradient-span/index.html)
- [GradientStop](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-gradient-stop/index.html)
- [GradientPoint](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-gradient-point/index.html)
