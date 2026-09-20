---
title: Histogram
---

# Histogram Chart

`HistogramChart` renders binned frequency data as adjacent bars.

![Histogram Demo](/content/{{version}}/wiki/assets/histogram_default.gif)

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.charts.HistogramChart
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.HistogramChartDefaults

@Composable
private fun ShowHistogram() {
    val data = listOf(3.0, 6.0, 11.0, 16.0, 14.0, 9.0, 5.0).toChartData(
        categories = listOf("0-50ms", "50-100ms", "100-150ms", "150-200ms", "200-250ms", "250-300ms", "300ms+"),
        seriesName = "Requests",
    )

    HistogramChart(
        data = data,
        title = "Request Duration Distribution",
        style = HistogramChartDefaults.style(
            bars = HistogramChartDefaults.bars(color = Color(0xFF0F766E)),
        ),
    )
}
```

`HistogramChartDefaults.bars(...)` keeps zero spacing and a `10.dp` minimum width for expanded scrolling. Fit mode preserves every bin, even at subpixel widths, without aggregation.
