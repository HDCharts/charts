---
title: Histogram
---

# Histogram Chart

## Overview

`HistogramChart` displays a distribution from precomputed bin heights. Each input value is one
bin's height and each category labels that bin; the chart does not group raw observations or
calculate statistical bin boundaries. Use it when your application has already grouped a numeric
distribution, such as request counts by duration interval.

![Histogram Demo](/content/{{version}}/wiki/assets/histogram_default.gif)

The GIF uses a longer generated distribution to show the chart animation. Its recording source is
[`HistogramExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/HistogramExample.kt).

## Basic Usage

Each `RequestDurationBin` already contains an interval and its counted requests. The chart renders
these equal-width bins as adjacent bars; it does not derive them from individual request durations.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/HistogramBasicExample.kt#histogram-basic-usage -->
```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.histogram.HistogramChart

private data class RequestDurationBin(val interval: String, val requestCount: Double)

@Composable
fun ShowRequestDurationDistribution() {
    // Counts have already been grouped into these equal-width intervals.
    val bins =
        listOf(
            RequestDurationBin("0–25 ms", 4.0),
            RequestDurationBin("25–50 ms", 18.0),
            RequestDurationBin("50–75 ms", 47.0),
            RequestDurationBin("75–100 ms", 72.0),
            RequestDurationBin("100–125 ms", 58.0),
            RequestDurationBin("125–150 ms", 31.0),
        )
    val data =
        bins.toChartData(
            value = { it.requestCount },
            category = { it.interval },
            seriesName = "Requests",
        )

    HistogramChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(280.dp),
        title = "Request Duration",
    )
}
```
<!-- END WIKI EXAMPLE: histogram-basic-usage -->

## Capabilities

### Bin Width and Compact Mode

Histogram bins have zero spacing and a default minimum width of `10.dp`. When the bins no longer
fit at that width, compact mode combines neighboring bins for display. A compact bar shows the
average height of its source bins, while selection still identifies the middle source bin. Expanding
the chart reveals every bin. Increasing the minimum width makes the chart enter compact mode with
fewer visible bins. This excerpt reuses `data` from Basic Usage.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/HistogramBinWidthExample.kt#histogram-bin-width -->
```kotlin
@Composable
fun ShowWiderRequestBins(data: ChartData) {
    HistogramChart(
        data = data,
        style = HistogramChartDefaults.style(
            bars = HistogramChartDefaults.bars(minBarWidth = 16.dp),
        ),
    )
}
```
<!-- END WIKI EXAMPLE: histogram-bin-width -->

### Gradients

Bins can use a linear gradient through `HistogramChartDefaults.bars(gradient = ...)`. See
[Gradients](/{{version}}/wiki/gradients) for gradient setup and color-stop behavior.

## API Reference

- [HistogramChart](/static/api/{{version}}/charts/io.github.hdcharts.histogram/-histogram-chart.html)
- [HistogramChartDefaults](/static/api/{{version}}/charts/io.github.hdcharts.core.style/-histogram-chart-defaults/index.html)
