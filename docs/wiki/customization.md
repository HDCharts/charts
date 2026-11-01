# Customization

Customize charts through the style factories that accompany each chart. Start with the defaults and
override the properties that support the result your screen needs. Style APIs vary by chart family;
use the chart guides for chart-specific capabilities.

## Style, Labels, and Formatting

This complete example styles a cash-flow chart with a teal bar color, quiet axes, a subtitle, and
currency in selected-value readouts. The selected-value formatter does not change axis tick labels;
each formatter has a separate role.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/CustomizationGuideExample.kt#customization-styled-cash-flow -->
```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxisLinesDefaults
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.GridDefaults

@Composable
fun StyledCashFlowChart() {
    val data =
        listOf(45.0, -12.0, 38.0, 27.0, -19.0).toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Net cash flow",
        )
    val axes =
        AxesDefaults.style(
            lines = AxisLinesDefaults.style(color = Color(0xFF64748B)),
            grid = GridDefaults.style(color = Color(0xFFCBD5E1)),
            xLabels = BarChartDefaults.xLabels(color = Color(0xFF475569), size = 12.sp),
            yLabels = BarChartDefaults.yLabels(color = Color(0xFF475569), size = 12.sp),
        )

    BarChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(280.dp),
        title = "Daily net cash flow",
        subtitle = "Revenue minus costs · 14–18 October",
        style =
            BarChartDefaults.style(
                bars = BarChartDefaults.bars(color = Color(0xFF0F766E)),
                axes = axes,
                chartContainerStyle =
                    ChartContainerDefaults.style(
                        styleSubtitle = ChartContainerDefaults.style().styleSubtitle.copy(fontSize = 16.sp),
                        contentPadding = 12.dp,
                    ),
            ),
        selectedValueFormatter = ChartValueFormatters.prefix("$"),
    )
}
```
<!-- END WIKI EXAMPLE: customization-styled-cash-flow -->

## Titles and Typography

Every chart accepts a title and subtitle. A subtitle appears below the title and can also appear by
itself. Set `styleTitle` and `styleSubtitle` through `ChartContainerDefaults.style(...)` to customize
their `TextStyle`s. The container style also controls internal `contentPadding`, which reserves room
for chart content such as labels and legends.

## Size and Surrounding Layout

Use the chart composable's `modifier` to set its size. Standard Compose layout modifiers such as
`fillMaxWidth()` and `height(...)` work with charts. Bounded width and height define the chart size;
when one axis is unbounded the chart uses a square fallback based on the bounded axis, and when both
are unbounded the default plot size is 200 dp. Put a chart in a `Card`, `Column`, or other container
to add a background, shape, shadow, or outer spacing; those belong to the caller's layout.

## Axes, Colors, and Palettes

Bar, histogram, line, stacked bar, and stacked area charts provide `AxesStyle` for axis lines, grid lines, and X and Y labels. `AxesDefaults.style(visible = false)` hides those elements together; set an individual block's visibility when only one element needs to change. The pie and ring chart families expose different labels and style options.

Axis labels are evenly spaced and avoid overlap. By default the chart chooses how many X labels fit; Y axes show up to five values and short charts can show fewer. Set a label block's `maxCount` to cap the count (valid values are 2 through 1000); the chart can still show fewer when labels do not fit. When Y labels are visible, grid lines align with them. When they are hidden, `GridStyle.steps` controls the horizontal intervals; the default four steps produce five lines.

Use each chart family's style factory to choose shape colors, line widths, label text styles, spacing,
or palettes. Default colors follow the Material theme. Multi-series chart families can use a palette in series order; palette support and
legend behavior vary by chart. A gradient can replace a shape's solid color on supported chart families; see the
[Gradients guide](/{{version}}/wiki/gradients). For chart-specific palette and legend behavior, see
[pie](/{{version}}/wiki/pie-chart), [radar](/{{version}}/wiki/radar-chart), [stacked bar](/{{version}}/wiki/stacked-bar-chart), and [stacked area](/{{version}}/wiki/stacked-area-chart).

## Formatting and Selection

Axis tick formatters and selected-value formatters are separate. Use `axisValueFormatter` for numeric
axis labels and `selectedValueFormatter` for selection readouts; the supported parameters vary by
chart. `ChartValueFormatters` provides common prefix and suffix formatters. See [bar formatting](/{{version}}/wiki/bar-chart), [stacked bar formatting](/{{version}}/wiki/stacked-bar-chart), and [ring gauge formatting](/{{version}}/wiki/ring-gauge-chart) for examples matched to those chart APIs.

Charts that support selection can expose it to surrounding Compose UI through
`rememberChartSelection()`. Legend contents and selection behavior differ between chart families;
the [legend guide](/{{version}}/wiki/dev/internals/legend) describes the shared behavior, and individual
chart guides show supported interactions.

## Animation and Interaction

Where offered, `animateOnStart` controls the initial reveal and `interactionEnabled` controls chart
gestures. Their availability and effects depend on the chart family. Check that chart's guide before
using either parameter.

## API Reference

- [ChartContainerDefaults](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-chart-container-defaults/index.html)
- [AxesStyle](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-axes-style/index.html)
- [BarChartDefaults](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-bar-chart-defaults/index.html)
- [ChartValueFormatters](/static/api/{{version}}/charts/io.github.hdcharts.core.model/-chart-value-formatters/index.html)
