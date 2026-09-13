# Stacked Area v3 migration

Use shared `ChartData` with one `ChartSeries` per contribution series. Categories
identify the shared X positions.

## Before

```kotlin
@Composable
private fun ShowStackedArea() {
    val items = listOf(
        "Free Plan" to listOf(620f, 650f, 690f, 720f, 760f, 800f),
        "Standard Plan" to listOf(240f, 260f, 285f, 310f, 340f, 365f),
        "Premium Plan" to listOf(90f, 95f, 105f, 118f, 130f, 142f),
    )

    val dataSet = items.toMultiChartDataSet(
        title = "Monthly Active Subscribers by Plan",
        categories = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
    )

    StackedAreaChart(
        dataSet = dataSet,
        selectedPointIndex = 1,
    )
}
```

## After

```kotlin
StackedAreaChart(
    data = chartDataOf(
        categories = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
        ChartSeries("Free Plan", listOf(620.0, 650.0, 690.0, 720.0, 760.0, 800.0)),
        ChartSeries("Standard Plan", listOf(240.0, 260.0, 285.0, 310.0, 340.0, 365.0)),
        ChartSeries("Premium Plan", listOf(90.0, 95.0, 105.0, 118.0, 130.0, 142.0)),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Monthly Active Subscribers by Plan",
    selection = rememberChartSelection(initialIndex = 1),
)
```

The old `MultiChartDataSet` and `selectedPointIndex` inputs are removed. Use
`selection` instead; it identifies one source X index shared by all series.

## Styles and selection

Move customizations to the grouped `StackedAreaChartStyle` sections such as
`fill`, `boundary`, `axis`, and `selection`. Series colors correspond to
contribution series.

Use `staticChartSelection(index)` for a preset preview or screenshot. Replacing
data clears selection; resizing and density changes preserve it.

## Behavior

- Series must be aligned, contain at least two X positions, and use finite,
  nonnegative values.
- Categories are optional; when supplied, they must match every series.
- Stacking shows absolute totals rather than percentages.
- `interactionEnabled = false` disables user controls while programmatic
  selection remains visible.
