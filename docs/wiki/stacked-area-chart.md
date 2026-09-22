---
title: Stacked Area
---

# Stacked Area Chart

`StackedAreaChart` renders multiple series as cumulative, stacked areas over a shared category axis.

![Stacked Area Demo](/content/{{version}}/wiki/assets/stacked_area_default.gif)

The example below is minimal and runs as written. The GIF above uses a longer generated
series per plan; its full source is in
[`StackedAreaExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/StackedAreaExample.kt).

```kotlin
@Composable
fun ShowStackedArea() {
    val data = listOf(
        "Free Plan" to listOf(240.0, 380.0, 520.0, 610.0, 740.0, 880.0),
        "Standard Plan" to listOf(100.0, 210.0, 330.0, 450.0, 580.0, 700.0),
        "Premium Plan" to listOf(60.0, 140.0, 260.0, 380.0, 520.0, 660.0),
    ).toChartData(
        categories = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
    )

    StackedAreaChart(data = data, title = "Monthly Active Subscribers by Plan")
}
```
