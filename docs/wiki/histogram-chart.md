---
title: Histogram
---

# Histogram Chart

`HistogramChart` renders binned frequency data as adjacent bars.

![Histogram Demo](/content/{{version}}/wiki/assets/histogram_default.gif)

The example below is minimal and runs as written. The GIF above uses a longer generated
distribution; its full source is in
[`HistogramExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/HistogramExample.kt).

```kotlin
data class RequestDurationBucket(val range: String, val requests: Double)

@Composable
fun ShowHistogram() {
    val buckets = listOf(
        RequestDurationBucket("0-25ms", 4.0),
        RequestDurationBucket("25-50ms", 18.0),
        RequestDurationBucket("50-75ms", 47.0),
        RequestDurationBucket("75-100ms", 72.0),
        RequestDurationBucket("100-125ms", 58.0),
        RequestDurationBucket("125-150ms", 31.0),
        RequestDurationBucket("150-175ms", 14.0),
        RequestDurationBucket("175ms+", 6.0),
    )
    val data = buckets.toChartData(
        value = { it.requests },
        category = { it.range },
        seriesName = "Request Duration Distribution",
    )

    HistogramChart(data = data, title = "Request Duration Distribution")
}
```

`HistogramChartDefaults.bars(...)` keeps zero spacing and a `10.dp` minimum width. When the bins do not fit at that width, compact mode merges neighboring bins into wider bars, as `BarChart` does. Each merged bar shows the average height of its bins, and tapping it selects its middle bin. Expanding the chart scrolls through every bin.

## Gradients

Bars take a linear gradient through `HistogramChartDefaults.bars(gradient = ...)`,
covered in [Gradients](/{{version}}/wiki/gradients).
