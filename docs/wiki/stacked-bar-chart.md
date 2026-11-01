---
title: Stacked Bar
---

# Stacked Bar Chart

## Overview

`StackedBarChart` compares how multiple nonnegative series contribute to each category total. Use it
when both the overall total and the series contributions matter. Each series must provide one value
for every category, in the same order.

![Stacked Bar Demo](/content/{{version}}/wiki/assets/stacked_bar_default.gif)

Recording source: [StackedBarExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/StackedBarExample.kt).

## Basic Usage

Each channel has one quarterly revenue value per category. Values at the same index stack into that
quarter's total bar; segment colors follow the series order.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/StackedBarBasicExample.kt#stacked-bar-basic-usage -->
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.stackedbar.StackedBarChart

@Composable
fun ShowQuarterlyRevenue(modifier: Modifier = Modifier) {
    val data =
        listOf(
            "Online" to listOf(280.0, 520.0, 760.0, 1_080.0),
            "Retail" to listOf(480.0, 760.0, 1_050.0, 1_400.0),
            "Wholesale" to listOf(360.0, 540.0, 620.0, 720.0),
        ).toChartData(categories = listOf("Q1", "Q2", "Q3", "Q4"))

    StackedBarChart(
        data = data,
        modifier = modifier,
        title = "Quarterly Revenue by Channel",
    )
}
```
<!-- END WIKI EXAMPLE: stacked-bar-basic-usage -->

![Stacked bar chart comparing quarterly revenue by channel](/content/{{version}}/wiki/assets/wiki-stacked-bar-basic.png)

## Capabilities

### Value Formatting

When a category is selected, `selectedValueFormatter` formats each channel's value in the legend.
It does not replace the category with an aggregate total. `axisValueFormatter` formats Y-axis ticks,
which represent cumulative stack heights. This excerpt reuses `data` from Basic Usage.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/StackedBarCapabilities.kt#stacked-bar-value-formatting -->
```kotlin
@Composable
fun FormattedQuarterlyRevenue(data: ChartData) {
    StackedBarChart(
        data = data,
        selectedValueFormatter = ChartValueFormatters.prefix("$"),
        axisValueFormatter =
            ChartValueFormatter { value ->
                "$" + StackedBarChartDefaults.axisValueFormatter.format(value)
            },
    )
}
```
<!-- END WIKI EXAMPLE: stacked-bar-value-formatting -->

### Fade Gradient

The fade follows each series color, keeping the channel identity visible through each segment. This
excerpt reuses `data` from Basic Usage. See the shared [Gradients guide](/{{version}}/wiki/gradients) for
gradient spans and custom stops.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/StackedBarCapabilities.kt#stacked-bar-fade-gradient -->
```kotlin
@Composable
fun FadedQuarterlyRevenue(data: ChartData) {
    StackedBarChart(
        data = data,
        style =
            StackedBarChartDefaults.style(
                segments =
                    StackedBarChartDefaults.segments(
                        gradient = ChartGradients.fade(endAlpha = 0.35f),
                    ),
            ),
    )
}
```
<!-- END WIKI EXAMPLE: stacked-bar-fade-gradient -->

## API Reference

- [StackedBarChart](/static/api/{{version}}/charts/io.github.hdcharts.stackedbar/-stacked-bar-chart.html)
- [StackedBarChartDefaults](/static/api/{{version}}/charts/io.github.hdcharts.stackedbar/-stacked-bar-chart-defaults/index.html)
- [StackedBarChartDefaults.segments](/static/api/{{version}}/charts/io.github.hdcharts.stackedbar/-stacked-bar-chart-defaults/segments.html)
