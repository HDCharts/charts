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

## Default colors

Series colors draw at full opacity. `alpha` defaults to `1f` on bar, histogram,
line, pie, stacked bar, and stacked area charts. For a softer look, pass `alpha`
yourself. 2.x used `0.7f` in light mode and `0.6f` in dark mode:

```kotlin
BarChartDefaults.style(bars = BarChartDefaults.bars(alpha = 0.7f))
```

Grid, axis, label, and selection defaults use `MaterialTheme.colorScheme`
roles. They follow your app's theme, not the system dark mode setting.

`defaultChartAlpha()` is removed. Pass an explicit `alpha` instead.

## Style values out of range

Charts draw style values as close to what you pass as they can, instead of
showing an error. Alphas are clamped to `0..1`, sizes to at least `0.dp`, and
grid steps to at least `0`. A `NaN` alpha draws at full opacity, and a `NaN`
size uses the chart's default. The style object keeps the values you passed.

Data problems, such as misaligned series or a color count that does not match
the series count, still show an error. Every chart words the same problem the
same way, for example "At least 2 values are required." or "Series 1 is not
aligned with the first series."

## Formatting

`ChartValueFormatter` receives a `Double`. Value and axis formatters are
independent, so a custom selected-value format does not change axis labels.
