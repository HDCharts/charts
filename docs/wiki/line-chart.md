---
title: Line
---

# Line Chart

`LineChart` renders single or multi-series line data. It has one composable with two
render modes, selected via `renderMode`.

![Line Demo](/content/{{version}}/wiki/assets/line_default.gif)

## Basic Usage

```kotlin
@Composable
private fun ShowLine() {
    val values = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    LineChart(
        data = values.toChartData(categories = labels),
        title = "Daily Support Tickets",
    )
}
```

## Multi-Series

Pass more than one named series to plot multiple lines on the same chart.

![MultiLine Demo](/content/{{version}}/wiki/assets/multi_line_default.gif)

```kotlin
@Composable
private fun ShowMultiLine() {
    val categories = listOf("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6")

    val items = listOf(
        "Web Store" to listOf(420.0, 510.0, 480.0, 530.0, 560.0, 590.0),
        "Mobile App" to listOf(360.0, 420.0, 410.0, 460.0, 500.0, 540.0),
        "Partner Sales" to listOf(280.0, 320.0, 340.0, 360.0, 390.0, 420.0),
    )

    val data = items.toChartData(categories = categories)

    LineChart(
        data = data,
        title = "Weekly Revenue by Channel",
        valueFormatter = ChartValueFormatters.prefix("$"),
    )
}
```

## Render Modes

- **`LineChartRenderMode.Morph`** (default) — when `data` changes, the line animates
  smoothly from its old shape to the new one. This is the right choice for most charts,
  including ones with large point counts that support zoom and scroll.
- **`LineChartRenderMode.Timeline`** — built for live, continuously updating data. Instead
  of morphing, new points slide the whole chart horizontally like a rolling window, and the
  Y-axis rescales automatically as old values leave the window. Interaction (selection,
  gestures) is disabled in this mode.

```kotlin
LineChart(
    data = liveData,
    renderMode = LineChartRenderMode.Timeline,
)
```

> This page is intentionally brief — a fuller reference (styling, selection, animation
> tuning) is coming in a dedicated pass.
