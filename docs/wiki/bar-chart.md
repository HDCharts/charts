---
title: Bar
---

# Bar Chart

`BarChart` renders categorical values as vertical bars, positive or negative.

![Bar Demo](/content/{{version}}/wiki/assets/bar_default.gif)

The example below is minimal and runs as written. The GIF above uses a longer generated
series; its full source is in
[`BarExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/BarExample.kt).

```kotlin
data class DailyCashFlow(val day: String, val amount: Double)

@Composable
fun ShowBar() {
    val cashFlow = listOf(
        DailyCashFlow("Mon", 45.0), DailyCashFlow("Tue", -12.0),
        DailyCashFlow("Wed", 38.0), DailyCashFlow("Thu", 27.0),
        DailyCashFlow("Fri", -19.0), DailyCashFlow("Sat", 42.0),
        DailyCashFlow("Sun", 31.0),
    )
    val data = cashFlow.toChartData(
        value = { it.amount },
        category = { it.day },
        seriesName = "Net cash flow",
    )

    BarChart(data = data, title = "Daily Net Cash Flow")
}
```

## Fixed Y-Axis Range

By default the Y-axis is derived from the data, which can make small fluctuations look
exaggerated when the axis doesn't start at zero. Set `range` on `BarChartDefaults.style()`
to pin `min`, `max`, or both, independently of one another; whichever bound you leave `null`
keeps deriving from the data.

```kotlin
data class DailyCashFlow(val day: String, val amount: Double)

@Composable
fun ShowBarWithRange() {
    val cashFlow = listOf(
        DailyCashFlow("Mon", 45.0), DailyCashFlow("Tue", -12.0),
        DailyCashFlow("Wed", 38.0), DailyCashFlow("Thu", 27.0),
        DailyCashFlow("Fri", -19.0), DailyCashFlow("Sat", 42.0),
        DailyCashFlow("Sun", 31.0),
    )
    val data = cashFlow.toChartData(
        value = { it.amount },
        category = { it.day },
        seriesName = "Net cash flow",
    )

    BarChart(
        data = data,
        title = "Daily Net Cash Flow",
        style = BarChartDefaults.style(
            range = BarChartDefaults.range(min = -50.0, max = 50.0),
        ),
    )
}
```

## Gradients

Bars take a linear gradient through `BarChartDefaults.bars(gradient = ...)`,
covered in [Gradients](/{{version}}/wiki/gradients).
