# Shared v3 chart contracts

Use `ChartData` with `Double` values for the charts migrated to the v3 API.

## Before

```kotlin
val data = listOf(24f, 18f).toChartData()
```

## After

```kotlin
val sales = listOf(24.0, 18.0).toChartData(
    categories = listOf("Mon", "Tue"),
    seriesName = "Sales",
)

val comparison = listOf(
    "This year" to listOf(24.0, 18.0),
    "Last year" to listOf(20.0, 16.0),
).toChartData(categories = listOf("Mon", "Tue"))
```

Convert `Int`, `Float`, or string values in your application before creating
`ChartData` or `ChartSeries`:

```kotlin
val data = sourceValues.map { it.toDouble() }.toChartData()
```

Handle invalid string values in the application; the library does not choose a
parsing or missing-value policy for you.

## Categories

Categories are explicit labels for the shared data index. An empty list means
that the chart has no category labels. When provided, the category count must
match every series. Index labels are not generated automatically.

## Selection

Hoist selection with `rememberChartSelection()` when the application needs to
observe or control it:

```kotlin
val selection = rememberChartSelection(
    onSelectionChanged = { index -> onPointSelected(index) },
)
```

Callbacks run only when the selected index changes. Use
`staticChartSelection(index)` for an initial selection in a preview or
screenshot.

## Formatting

`ChartValueFormatter` receives a `Double`. Value and axis formatters are
independent, so a custom selected-value format does not change axis labels.
