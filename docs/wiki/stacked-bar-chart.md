---
title: Stacked Bar
---

# Stacked Bar Chart

`StackedBarChart` renders multiple series as stacked segments within each category's bar.

![Stacked Bar Demo](/content/{{version}}/wiki/assets/stacked_bar_default.gif)

The example below is minimal and runs as written. The GIF above uses a longer generated
series per channel; its full source is in
[`StackedBarExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/StackedBarExample.kt).

```kotlin
@Composable
fun ShowStackedBar() {
    val data = listOf(
        "Online" to listOf(280.0, 520.0, 760.0, 1080.0),
        "Retail" to listOf(480.0, 760.0, 1050.0, 1400.0),
        "Wholesale" to listOf(360.0, 540.0, 620.0, 720.0),
    ).toChartData(
        categories = listOf("Q1", "Q2", "Q3", "Q4"),
    )

    StackedBarChart(data = data, title = "Quarterly Revenue by Channel")
}
```
