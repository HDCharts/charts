# Radar v3 migration

Use shared `ChartData` with one `ChartSeries` per radar polygon. Categories are
the shared axis labels.

## Before

```kotlin
@Composable
private fun ShowRadar() {
    val categories = listOf(
        "Performance",
        "Reliability",
        "Usability",
        "Security",
        "Scalability",
        "Observability",
    )

    val dataSet = listOf(84f, 79f, 76f, 88f, 82f, 74f).toChartDataSet(
        title = "Platform Readiness Score",
        labels = categories,
    )

    RadarChart(
        dataSet = dataSet,
        selectedAxisIndex = 1,
    )
}
```

## After

```kotlin
RadarChart(
    data = chartDataOf(
        categories = listOf("Performance", "Reliability", "Usability", "Security", "Scalability", "Observability"),
        ChartSeries("Platform Readiness Score", listOf(84.0, 79.0, 76.0, 88.0, 82.0, 74.0)),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Platform Readiness Score",
    selection = rememberChartSelection(initialIndex = 1),
)
```

The old `ChartDataSet`, `MultiChartDataSet`, and `selectedAxisIndex` inputs are
removed. Use the `title` and `selection` parameters instead. Use
`staticChartSelection(index)` for a preset preview or screenshot.

## Styles

Move customizations to the grouped `RadarChartStyle` sections such as `grid`,
`axes`, `polygon`, `points`, and `categories`.

Sizes are `Dp` and scale with screen density. The 2.x `Float` sizes were
pixels, so divide them by the screen density when migrating. Point and pin
sizes are radii. The defaults are `1.dp` grid and axis lines, `3.dp` label
padding, a `2.dp` polygon line, `4.dp` points, and `2.dp` category pins.

## Behavior

- Use at least one series and three aligned axes. Values must be finite.
- Categories are optional; when supplied, they must match every series.
- When categories are provided, selecting an axis uses its category as the
  selected title and exposes the raw value for each series.
- `interactionEnabled = false` disables drag gestures while programmatic
  selection remains visible.
