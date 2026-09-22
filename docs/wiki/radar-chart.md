---
title: Radar
---

# Radar Chart

`RadarChart` renders one or more series as a polygon across shared categories (spokes).

![Radar Demo](/content/{{version}}/wiki/assets/radar_default.gif)

The example below is minimal and runs as written. The full source behind the GIF is in
[`RadarExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/RadarExample.kt).

```kotlin
@Composable
fun ShowRadar() {
    val data = listOf(84.0, 79.0, 76.0, 88.0, 82.0, 74.0).toChartData(
        categories = listOf(
            "Performance", "Reliability", "Usability",
            "Security", "Scalability", "Observability",
        ),
        seriesName = "Platform Readiness Score",
    )

    RadarChart(data = data, title = "Platform Readiness Score")
}
```
