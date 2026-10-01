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
`axes`, `polygon`, `points`, and `selection`.

Axis labels are on by default: each category name sits at the end of its axis, and the web shrinks
to leave room for them. Pass `axes(labelVisible = false)` for a web that fills the chart.

Data points are hidden by default, like on line charts; the selected axis still
shows its points. Pass `points(visible = true)` to show every point.

The category legend is gone: the axis labels already name every category, and the legend only
repeated them with a color that meant nothing. 2.x `categoryColors`, `categoryPinsVisible`, and
`categoryPinSize` have no 3.x equivalent. The legend still names the series and shows each value
while an axis is selected.

Selecting an axis dims the data points and labels of the other axes to 70% opacity. Polygons keep
their full shape and color. Pass `selection(unselectedAlpha = 1f)` to keep every point solid.

Sizes are `Dp` and scale with screen density. The 2.x `Float` sizes were
pixels, so divide them by the screen density when migrating. Point sizes are
radii. The defaults are `1.dp` grid and axis lines, 4 grid rings, `10.dp`
label padding, a `2.dp` polygon line, and `4.dp` data points.

## Behavior

- Use at least one series and three aligned axes. Values must be finite.
- Categories are optional; when supplied, they must match every series.
- When categories are provided, selecting an axis uses its category as the
  selected title and exposes the raw value for each series.
- `interactionEnabled = false` disables drag gestures while programmatic
  selection remains visible.
- Dragging selects an axis and the selection stays after the drag, so the
  values can be read. A tap clears it, so touch alone returns the chart to
  rest.
- With more than one series, tapping a series outline focuses it and fades
  the others to 35% opacity. Where series overlap, each
  tap moves to the next one, and tapping empty space clears the focus. The
  focused series is held in the new `seriesSelection` parameter.
  `interactionEnabled = false` also disables these taps.
