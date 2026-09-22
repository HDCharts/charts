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
@Composable
fun ShowHistogram() {
    val data = listOf(4.0, 18.0, 47.0, 72.0, 58.0, 31.0, 14.0, 6.0).toChartData(
        categories = listOf(
            "0-25ms", "25-50ms", "50-75ms", "75-100ms",
            "100-125ms", "125-150ms", "150-175ms", "175ms+",
        ),
        seriesName = "Request Duration Distribution",
    )

    HistogramChart(data = data, title = "Request Duration Distribution")
}
```

`HistogramChartDefaults.bars(...)` keeps zero spacing and a `10.dp` minimum width for expanded scrolling. Fit mode preserves every bin, even at subpixel widths, without aggregation.
