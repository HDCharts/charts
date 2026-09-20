---
title: Stacked Bar
---

# Stacked Bar Chart

`StackedBarChart` renders multiple series as stacked segments within each category's bar.

![Stacked Bar Demo](/content/{{version}}/wiki/assets/stacked_bar_default.gif)

```kotlin
@Composable
private fun ShowStackedBar() {
    val items = listOf(
        "North America" to listOf(320.0, 340.0, 360.0, 390.0),
        "Europe" to listOf(210.0, 230.0, 245.0, 260.0),
        "Asia Pacific" to listOf(180.0, 205.0, 225.0, 250.0),
    )

    val data = items.toChartData(categories = listOf("Q1", "Q2", "Q3", "Q4"))

    StackedBarChart(
        data = data,
        title = "Quarterly Revenue by Region",
    )
}
```
