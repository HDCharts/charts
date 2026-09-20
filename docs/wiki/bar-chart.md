---
title: Bar
---

# Bar Chart

`BarChart` renders categorical values as vertical bars, positive or negative.

![Bar Demo](/content/{{version}}/wiki/assets/bar_default.gif)

```kotlin
import androidx.compose.runtime.Composable
import io.github.hdcharts.charts.BarChart
import io.github.hdcharts.charts.model.toChartData

@Composable
private fun ShowBar() {
    val data = listOf(45.0, -12.0, 38.0, 27.0, -19.0, 42.0, 31.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        seriesName = "Net cash flow",
    )

    BarChart(data = data, title = "Daily Net Cash Flow")
}
```
