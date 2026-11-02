---
title: "Scatter and Bubble Chart"
---

# Scatter and Bubble Chart

## Overview

Scatter charts place each point at its numeric X and Y coordinates to show how two measures relate. Add a size to each point to encode a third measure as a bubble whose area scales with that value. Each series uses a palette color; overlapping bubbles use transparency.

Add the scatter chart module to your dependencies:

```kotlin
implementation("io.github.hdcharts:scatter:{{artifactVersion}}")
```

## Basic Usage

![Scatter and bubble chart showing two regional series](/content/{{version}}/wiki/assets/wiki-scatter-basic.png)

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/ScatterBasicExample.kt#scatter-basic-usage -->
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.scatter.ScatterChart
import io.github.hdcharts.scatter.ScatterData
import io.github.hdcharts.scatter.ScatterPoint
import io.github.hdcharts.scatter.ScatterSeries

@Composable
fun ScatterBasicExample(modifier: Modifier = Modifier) {
    val north =
        ScatterSeries(
            name = "North",
            points =
                listOf(
                    ScatterPoint(x = 1.0, y = 2.4, size = 12.0),
                    ScatterPoint(x = 2.5, y = 3.8, size = 24.0),
                    ScatterPoint(x = 4.0, y = 3.1, size = 18.0),
                ),
        )
    val south =
        ScatterSeries(
            name = "South",
            points =
                listOf(
                    ScatterPoint(x = 1.5, y = 1.8, size = 20.0),
                    ScatterPoint(x = 3.0, y = 2.7, size = 10.0),
                    ScatterPoint(x = 4.5, y = 4.2, size = 28.0),
                ),
        )
    val data = ScatterData(series = listOf(north, south))
    ScatterChart(data = data, modifier = modifier, title = "Regional growth")
}
```
<!-- END WIKI EXAMPLE: scatter-basic-usage -->

## Data Mapping

Use `ScatterPoint(x, y)` for a scatter marker. Supply `size` when the third measure should control bubble area; points without a size use the configured fixed marker size.

## Capabilities

### Fixed Numeric Ranges

Set either axis bound independently. Unset bounds use the data extent.

```kotlin
val style = ScatterChartDefaults.style(
    range = ScatterRangeStyle(xMin = 0.0, xMax = 5.0, yMin = 0.0),
)
```

### Selection

Tapping or dragging selects the nearest point. The chart heading shows its X, Y, and optional size values. Pass a `ChartSelection` to control selection from the application.

## API Reference

- [ScatterChart](/static/api/{{version}}/charts/io.github.hdcharts.scatter/-scatter-chart.html)
- [ScatterChartDefaults](/static/api/{{version}}/charts/io.github.hdcharts.scatter/-scatter-chart-defaults/index.html)
- [ScatterData](/static/api/{{version}}/charts/io.github.hdcharts.scatter/-scatter-data/index.html)
- [ScatterPoint](/static/api/{{version}}/charts/io.github.hdcharts.scatter/-scatter-point/index.html)
- [ScatterRangeStyle](/static/api/{{version}}/charts/io.github.hdcharts.scatter/-scatter-range-style/index.html)
