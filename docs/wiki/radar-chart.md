---
title: Radar
---

# Radar Chart

`RadarChart` renders one or more series as a polygon across shared categories (spokes).

![Radar Demo](/content/{{version}}/wiki/assets/radar_default.gif)

```kotlin
@Composable
private fun ShowRadar() {
    val categories = listOf(
        "Performance",
        "Reliability",
        "Usability",
        "Security",
        "Scalability",
        "Observability"
    )

    val data = listOf(84.0, 79.0, 76.0, 88.0, 82.0, 74.0).toChartData(
        categories = categories,
        seriesName = "Platform Readiness Score",
    )

    RadarChart(data = data, title = "Platform Readiness Score")
}
```
