---
title: Radar
---

# Radar Chart

`RadarChart` renders one or more series as a polygon across shared categories (spokes).

![Radar Demo](/content/{{version}}/wiki/assets/radar_default.gif)

The example below is minimal and runs as written. The full source behind the GIF is in
[`RadarExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/RadarExample.kt).

```kotlin
data class ReadinessMetric(val name: String, val score: Double)

@Composable
fun ShowRadar() {
    val metrics = listOf(
        ReadinessMetric("Performance", 84.0),
        ReadinessMetric("Reliability", 79.0),
        ReadinessMetric("Usability", 76.0),
        ReadinessMetric("Security", 88.0),
        ReadinessMetric("Scalability", 82.0),
        ReadinessMetric("Observability", 74.0),
    )
    val data = metrics.toChartData(
        value = { it.score },
        category = { it.name },
        seriesName = "Platform Readiness Score",
    )

    RadarChart(data = data, title = "Platform Readiness Score")
}
```

With more than one series, tap a series outline to focus it, and the other series fade. Where series overlap, tap again to move to the next one. Tap empty space to clear the
focus. Drag around the chart to select a category and read each series' value; the selection stays so
you can read it, and a tap clears it. Pass `seriesSelection = rememberChartSelection()` to read or set
the focused series. A single series has no legend to read, so the title carries the selected value as
`Category: value` instead.

Each category name sits at the end of its axis, and the web shrinks to leave room for them. The
chart keeps its full width in a wider box, so a small chart shows a small web and readable labels.
Pass `RadarChartDefaults.style(axes = RadarChartDefaults.axes(labelVisible = false))` for a web that
fills the chart instead.
