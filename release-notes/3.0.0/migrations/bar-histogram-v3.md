# Bar and Histogram v3 migration

Use `ChartData` with one `Double` series for both `BarChart` and
`HistogramChart`. Convert older dataset values before creating the chart data.

## Before

```kotlin
BarChart(
    dataSet = listOf(18f, 32f, 26f).toChartDataSet(
        title = "Daily sales",
        labels = listOf("Mon", "Tue", "Wed"),
    ),
    selectedBarIndex = 1,
)
```

## After

```kotlin
val selection = rememberChartSelection(initialIndex = 1)

BarChart(
    data = listOf(18.0, 32.0, 26.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed"),
        seriesName = "Daily sales",
    ),
    title = "Daily sales",
    selection = selection,
)
```

The equivalent HistogramChart migration is:

```kotlin
// Before
HistogramChart(
    dataSet = listOf(3f, 6f, 11f, 16f, 14f, 9f, 5f).toChartDataSet(
        title = "Request Duration Distribution",
        labels = listOf("0-50ms", "50-100ms", "100-150ms", "150-200ms", "200-250ms", "250-300ms", "300ms+"),
    ),
)

// After
HistogramChart(
    data = listOf(3.0, 6.0, 11.0, 16.0, 14.0, 9.0, 5.0).toChartData(
        categories = listOf("0-50ms", "50-100ms", "100-150ms", "150-200ms", "200-250ms", "250-300ms", "300ms+"),
        seriesName = "Request Duration Distribution",
    ),
    title = "Request Duration Distribution",
)
```

Both charts accept one series. Histogram values must be nonnegative. The old
`dataSet` overloads and `selectedBarIndex` parameter are removed; use the
top-level `selection` parameter instead.

## Styles

Styles are grouped. Migrate customizations to the corresponding blocks on
`BarChartStyle` or `HistogramChartStyle`, such as `bars`, `range`, `grid`,
`axis`, and `selectionLine`. Create them with `BarChartDefaults` or
`HistogramChartDefaults` rather than the removed flat style parameters.

## Behavior

- `title` is separate from `seriesName` and category labels.
- An empty category list hides X-axis labels; supplied categories must match the
  value count.
- `valueFormatter` formats selected values and `axisValueFormatter` formats
  Y-axis ticks independently.
- Selection identifies a source bar or bin. Replacing the data clears it;
  resizing and density changes preserve it.
- Set `interactionEnabled = false` to disable user controls while retaining
  programmatic selection.
