# Line v3 migration

Use one `LineChart` for both single- and multi-series line charts. The chart
accepts shared `ChartData` with aligned `Double` values.

## Before

```kotlin
@Composable
private fun ShowMultiLine() {
    val items = listOf(
        "Web Store" to listOf(420f, 510f, 480f, 530f, 560f, 590f),
        "Mobile App" to listOf(360f, 420f, 410f, 460f, 500f, 540f),
        "Partner Sales" to listOf(280f, 320f, 340f, 360f, 390f, 420f),
    )

    val dataSet = items.toMultiChartDataSet(
        title = "Weekly Revenue by Channel",
        prefix = "$",
        categories = listOf("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6"),
    )

    LineChart(
        dataSet = dataSet,
        selectedPointIndex = 1,
    )
}
```

## After

```kotlin
val selection = rememberChartSelection(initialIndex = 1)

LineChart(
    data = listOf(
        "Web Store" to listOf(420.0, 510.0, 480.0, 530.0, 560.0, 590.0),
        "Mobile App" to listOf(360.0, 420.0, 410.0, 460.0, 500.0, 540.0),
        "Partner Sales" to listOf(280.0, 320.0, 340.0, 360.0, 390.0, 420.0),
    ).toChartData(
        categories = listOf("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6"),
    ),
    title = "Weekly Revenue by Channel",
    selection = selection,
    valueFormatter = ChartValueFormatters.prefix("$"),
)
```

The old `ChartDataSet` and `MultiChartDataSet` entry points are removed. There
is no separate multi-line composable. Use `chartDataOf` when explicit
`ChartSeries` construction is more convenient.

## Styles and selection

Move customizations to the grouped `LineChartStyle` sections such as `line`,
`points`, `selection`, and `axis`. Use `selection` instead of
`selectedPointIndex`; it identifies one source X index shared by all series.
Use `staticChartSelection(index)` for a preset preview or screenshot.

Sizes are `Dp` and scale with screen density. The 2.x `Float` sizes were
pixels, so divide them by the screen density when migrating. For example,
`pointSize = 9f` looked like `points(size = 3.dp)` on a 3x screen. Point and
selection sizes are radii. The defaults are a `2.dp` line, `4.dp` points,
`3.dp` and `5.dp` selection markers, and a `1.dp` axis.

Categories are explicit labels. An empty list hides X-axis labels, and supplied
categories must match every series. `valueFormatter` and `axisValueFormatter`
are independent and both receive `Double` values.
