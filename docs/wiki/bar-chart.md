---
title: Bar
---

# Bar Chart

## Overview

`BarChart` compares values across named categories. It supports positive and negative values and
draws them from a zero baseline. Use it for discrete comparisons such as daily totals or category
counts.

![Bar Demo](/content/{{version}}/wiki/assets/bar_default.gif)

Recording source: [BarExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/BarExample.kt).

## Basic Usage

This complete example maps application data with `toChartData()` and renders signed daily cash
flow with the default bar style.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/BarBasicExample.kt#bar-basic-usage -->
```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.toChartData

internal data class DailyCashFlow(
    val day: String,
    val amount: Double,
) {
    companion object {
        const val TITLE = "Daily Net Cash Flow"
        const val SERIES_NAME = "Net cash flow"

        val sample =
            listOf(
                DailyCashFlow("Mon", 45.0),
                DailyCashFlow("Tue", -12.0),
                DailyCashFlow("Wed", 38.0),
                DailyCashFlow("Thu", 27.0),
                DailyCashFlow("Fri", -19.0),
            )
    }
}

@Composable
fun ShowDailyCashFlow() {
    val data =
        DailyCashFlow.sample.toChartData(
            value = DailyCashFlow::amount,
            category = DailyCashFlow::day,
            seriesName = DailyCashFlow.SERIES_NAME,
        )

    BarChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(280.dp),
        title = DailyCashFlow.TITLE,
    )
}
```
<!-- END WIKI EXAMPLE: bar-basic-usage -->

## Capabilities

### Fixed Value Range

The automatic range includes zero, so it provides a meaningful baseline for positive, negative, or
mixed values. Set explicit bounds when related charts or changing datasets need a consistent scale.
This excerpt reuses `data` from Basic Usage.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/BarCapabilities.kt#bar-fixed-range -->
```kotlin
@Composable
fun CompareCashFlow(data: ChartData) {
    BarChart(
        data = data,
        style =
            BarChartDefaults.style(
                range = BarChartDefaults.range(min = -50.0, max = 50.0),
            ),
    )
}
```
<!-- END WIKI EXAMPLE: bar-fixed-range -->

### Gradients

Each bar repeats the vertical blue-to-purple-to-pink gradient. The excerpt reuses `data` from Basic
Usage.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/BarCapabilities.kt#bar-gradient -->
```kotlin
@Composable
fun DailyCashFlowGradient(data: ChartData) {
    val colors =
        listOf(
            Color(0xFF2563EB),
            Color(0xFF8B5CF6),
            Color(0xFFEC4899),
        )
    val gradient = ChartGradients.vertical(colors = colors)

    BarChart(
        data = data,
        style =
            BarChartDefaults.style(
                bars = BarChartDefaults.bars(gradient = gradient),
            ),
    )
}
```
<!-- END WIKI EXAMPLE: bar-gradient -->

![Bar chart with a vertical blue, purple, and pink gradient](/content/{{version}}/wiki/assets/wiki-bar-gradient.png)

For `GradientSpan.Shape` and `GradientSpan.Plot`, custom stops, and gradient direction, see the
shared [Gradients guide](/{{version}}/wiki/gradients).

## API Reference

- [BarChart](/static/api/{{version}}/charts/io.github.hdcharts.bar/-bar-chart.html)
- [BarChartDefaults](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-bar-chart-defaults/index.html)
