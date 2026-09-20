# Customization

General patterns for styling, sizing, and interaction that apply across chart types.

## Style Customization

To customize chart appearance, start from each chart's `*ChartDefaults.style(...)` factory and override only the fields you need.

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.charts.BarChart
import io.github.hdcharts.charts.model.ChartValueFormatters
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.AxisLabelStyle
import io.github.hdcharts.charts.style.BarAxisStyle
import io.github.hdcharts.charts.style.BarBarsStyle
import io.github.hdcharts.charts.style.BarChartDefaults
import io.github.hdcharts.charts.style.BarGridStyle
import io.github.hdcharts.charts.style.BarSelectionLineStyle

@Composable
private fun ShowStyledBar() {
    val data = listOf(45.0, -12.0, 38.0, 27.0, -19.0, 42.0, 31.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        seriesName = "Net cash flow",
    )

    val style = BarChartDefaults.style(
        bars = BarBarsStyle(
            color = Color(0xFF0F766E),
            colors = emptyList(),
            alpha = 0.78f,
            space = 14.dp,
            minBarWidth = 10.dp,
        ),
        grid = BarGridStyle(visible = true, steps = 5, color = Color(0xFF94A3B8), lineWidth = 1.dp),
        selectionLine = BarSelectionLineStyle(visible = true, color = Color(0xFFEA580C), width = 1.dp),
        axis = BarAxisStyle(
            visible = true,
            color = Color.Gray,
            lineWidth = 1.dp,
            yLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 6),
            xLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 6),
        ),
    )

    BarChart(
        data = data,
        modifier = Modifier.background(Color(0xFFF8FAFC), RoundedCornerShape(18.dp)).padding(20.dp),
        title = "Daily Net Cash Flow",
        style = style,
        valueFormatter = ChartValueFormatters.prefix("$"),
    )
}
```

Chart chrome (background, shape, shadow, and outer padding) lives on the caller-supplied `modifier`; sizing is fully modifier-driven. The `chartContainerStyle` block only carries `contentPadding` and the title text style.

Here `valueFormatter` adds currency to selected raw values only; Y ticks keep the independent `axisValueFormatter` default. Titles do not generate labels: omitting `categories` hides X labels and leaves only the formatted value in selected readouts.

## Sizing

The chart composable's `modifier` is the only sizing control. A bounded `modifier` becomes a rectangular plot; a one-axis bounded `modifier` derives a square fallback; a fully unbounded `modifier` falls back to a 200.dp default.

```kotlin
val dataSet = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0).toChartData()

LineChart(
    data = dataSet,
    modifier = Modifier
        .fillMaxWidth()
        .height(260.dp),
)
```

## Surrounding Chrome

Background, shadow, shape, and outer spacing are not applied by the chart. Wrap the chart in any Compose surface to add chrome.

```kotlin
val dataSet = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0).toChartData()

Card(modifier = Modifier.padding(16.dp)) {
    LineChart(
        data = dataSet,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    )
}
```

## Content Spacing

`ChartContainerDefaults.style()` returns a `ChartContainerStyle` with `styleTitle` and `contentPadding`. Set `contentPadding` to change the internal spacing reserved for axes, title, and legend.

```kotlin
val dataSet = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0).toChartData()

LineChart(
    data = dataSet,
    modifier = Modifier.fillMaxWidth().height(260.dp),
    style = LineChartDefaults.style(
        chartContainerStyle = ChartContainerDefaults.style(contentPadding = 8.dp),
    ),
)
```

## Selection

Use `rememberChartSelection()` to hoist selection. The chart calls back into it; you read `selectedIndex` to drive external UI such as legends or summary cards.

```kotlin
val selection = rememberChartSelection()
val values = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0)

LineChart(
    data = values.toChartData(),
    modifier = Modifier.fillMaxWidth().height(260.dp),
    selection = selection,
)

val selected = values.getOrNull(selection.selectedIndex ?: 0)
Text("Selected: $selected")
```

## Animation and Interaction

`animateOnStart` controls the initial reveal animation; `interactionEnabled` disables gestures, selection, and zoom controls together. Disable interaction to embed a chart inside a tappable card without conflicts.

```kotlin
val dataSet = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0).toChartData()

LineChart(
    data = dataSet,
    modifier = Modifier.fillMaxWidth().height(260.dp),
    animateOnStart = true,
    interactionEnabled = false,
)
```

## Responsive Layout

Compose the chart with any layout, including `Row`, `Column`, `LazyColumn`, or `BoxWithConstraints`. A bounded `modifier` is always honored; an unbounded one falls back to the documented defaults.

```kotlin
Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    LineChart(
        data = lineData,
        modifier = Modifier.fillMaxWidth().height(220.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    BarChart(
        data = barData,
        modifier = Modifier.fillMaxWidth().height(220.dp),
    )
}
```
