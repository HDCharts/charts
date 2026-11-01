---
title: Pie
---

# Pie Chart

## Overview

`PieChart` compares parts of a whole as proportional slices. Use one series with at least two
nonnegative finite values. Categories are optional; when supplied, each names a slice in the legend
and while it is selected.

```kotlin
implementation("io.github.hdcharts:pie:{{artifactVersion}}")
```

![Pie Demo](/content/{{version}}/wiki/assets/pie_default.gif)

Slice colors come from the style, either as an explicit palette or as shades generated from its base
color. Without categories the chart has no legend. With no category and no title, a selected slice
shows only its share.

Recording source: [PieExample.kt](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/PieExample.kt).

## Basic Usage

This complete example maps application data with `toChartData()` and renders it with the public
`PieChart` composable.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/PieBasicExample.kt#pie-basic-usage -->
```kotlin
import androidx.compose.runtime.Composable
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.pie.PieChart

internal data class EnergyUsage(
    val category: String,
    val consumption: Double,
) {
    companion object {
        val sample =
            listOf(
                EnergyUsage("Heating", 32.0),
                EnergyUsage("Cooling", 21.0),
                EnergyUsage("Appliances", 24.0),
                EnergyUsage("Water Heating", 14.0),
                EnergyUsage("Lighting", 9.0),
            )
    }
}

@Composable
fun ShowPie() {
    val energyUsage = EnergyUsage.sample
    val data =
        energyUsage.toChartData(
            value = { it.consumption },
            category = { it.category },
        )

    PieChart(data = data, title = "Household Energy")
}
```
<!-- END WIKI EXAMPLE: pie-basic-usage -->

![Pie chart showing household energy usage by category](/content/{{version}}/wiki/assets/wiki-pie-basic.png)

## Capabilities

### Slice Colors

Set one color per slice, in slice order. The chart reports a validation error when the palette size
does not match the slice count. This excerpt reuses `data` from Basic Usage.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/PieCapabilities.kt#pie-slice-colors -->
```kotlin
@Composable
fun ShowPieWithSliceColors(data: ChartData) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)

    PieChart(
        data = data,
        style =
            PieChartDefaults.style(
                slices = PieChartDefaults.slices(colors = colors),
            ),
    )
}
```
<!-- END WIKI EXAMPLE: pie-slice-colors -->

### Selected Share

A selected slice shows its category and share of the total in the title, such as `Lighting: 9%` for
the Basic Usage data.
`selectedValueFormatter` formats that share, given in percent from 0 to 100. This one shows whole
percents and reuses `data` from Basic Usage:

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/PieCapabilities.kt#pie-selected-share -->
```kotlin
@Composable
fun ShowPieWithRoundedShare(
    data: ChartData,
    selection: ChartSelection = rememberChartSelection(),
) {
    PieChart(
        data = data,
        selection = selection,
        selectedValueFormatter = ChartValueFormatter { share -> "${share.roundToInt()}%" },
    )
}
```
<!-- END WIKI EXAMPLE: pie-selected-share -->

![Pie chart with Lighting selected and its 9% share shown](/content/{{version}}/wiki/assets/wiki-pie-selected-share.png)

## API Reference

- [PieChart](/static/api/{{version}}/charts/io.github.hdcharts.pie/-pie-chart.html)
- [PieChartDefaults](/static/api/{{version}}/charts/io.github.hdcharts.pie/-pie-chart-defaults/index.html)
