# Stacked Bar v3 migration

Use shared `ChartData` with one `ChartSeries` per stack segment. Each category
is one bar, and each series supplies one contribution for every bar.

## Before

```kotlin
@Composable
private fun ShowStackedBar() {
    val items = listOf(
        "North America" to listOf(320f, 340f, 360f, 390f),
        "Europe" to listOf(210f, 230f, 245f, 260f),
        "Asia Pacific" to listOf(180f, 205f, 225f, 250f),
    )

    val dataSet = items.toMultiChartDataSet(
        title = "Quarterly Revenue by Region",
        prefix = "$",
        categories = listOf("Q1", "Q2", "Q3", "Q4"),
    )

    StackedBarChart(
        dataSet = dataSet,
        selectedBarIndex = 1,
    )
}
```

## After

```kotlin
StackedBarChart(
    data = chartDataOf(
        categories = listOf("Q1", "Q2", "Q3", "Q4"),
        ChartSeries("North America", listOf(320.0, 340.0, 360.0, 390.0)),
        ChartSeries("Europe", listOf(210.0, 230.0, 245.0, 260.0)),
        ChartSeries("Asia Pacific", listOf(180.0, 205.0, 225.0, 250.0)),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Quarterly Revenue by Region",
    selection = rememberChartSelection(initialIndex = 1),
)
```

The old row-oriented `MultiChartDataSet` input is removed. Transpose legacy
rows when migrating: each row becomes one bar, its label becomes the category
label, and each new series contains one segment column.

## Styles and selection

Move customizations to the grouped `StackedBarChartStyle` sections such as
`segments`, `layout`, `axis`, and `selection`. Segment colors correspond to
series, not bars.

Use `selection` instead of `selectedBarIndex`. Selection applies to a whole bar,
not an individual segment. Use `staticChartSelection(index)` for a preset
preview or screenshot.

The selection line width is `Dp` and scales with screen density. The default is
`1.dp`; in 2.x widths were pixels.

The old dataset `prefix` is not a parameter on the v3 stacked-bar API; selected
values use the chart's default formatting.

## Behavior

- Series must be aligned, contain at least two bars, and use finite,
  nonnegative values.
- Categories are optional; when supplied, they must match every series.
- Stacks show absolute totals rather than percentages.
- Replacing data clears selection. Resizing and density changes preserve it.
- `interactionEnabled = false` disables user controls while programmatic
  selection remains visible.
