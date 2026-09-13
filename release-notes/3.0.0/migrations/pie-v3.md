# Pie chart v3 migration

Use finite, nonnegative `Double` values when constructing `PieSlice` instances.

## Before

```kotlin
PieChart(
    dataSet = listOf(80f, 20f).toChartDataSet(
        title = "Progress",
        labels = listOf("Completed", "Remaining"),
    ),
    selectedSliceIndex = 0,
)
```

## After

```kotlin
val selection = rememberChartSelection(initialIndex = 0)

PieChart(
    data = listOf(
        PieSlice(label = "Completed", value = 80.0),
        PieSlice(label = "Remaining", value = 20.0),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Progress",
    style = PieChartDefaults.style(
        donut = PieChartDefaults.donut(holePercentage = 50f),
        selection = selection,
    ),
)
```

Convert source values at the application boundary:

```kotlin
val slices = sourceSlices.map { slice ->
    PieSlice(label = slice.label, value = slice.value.toDouble())
}
```

The v3 API replaces the old `ChartDataSet` input with a list of `PieSlice`
values. `PieSlice.value` uses `Double`; invalid or negative values are rejected
by the chart.

## Selection and interaction

Pass a `ChartSelection` through `PieChartDefaults.style` when selection must be
controlled by the application:

```kotlin
val selection = rememberChartSelection()
val style = PieChartDefaults.style(selection = selection)
PieChart(data = slices, style = style)
```

Programmatic selection remains visible when `interactionEnabled` is `false`; in
that mode, user taps and automatic deselection are disabled.
