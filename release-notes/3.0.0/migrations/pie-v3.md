# Pie chart v3 migration

Use finite, nonnegative `Double` values when building the chart data.

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
    data = listOf(80.0, 20.0).toChartData(categories = listOf("Completed", "Remaining")),
    modifier = Modifier.fillMaxWidth(),
    title = "Progress",
    style = PieChartDefaults.style(
        donut = PieChartDefaults.donut(holePercentage = 50f),
    ),
    selection = selection,
)
```

Convert source values at the application boundary:

```kotlin
val data = sourceSlices.map { slice -> slice.value.toDouble() }
    .toChartData(categories = sourceSlices.map { slice -> slice.label })
```

The v3 API replaces the old `ChartDataSet` input with `ChartData`, the model every
chart takes. A pie is one series whose values are the slices, so each category is a
slice: its label names the slice in the legend and while it is selected. Categories
are optional, as they are on every chart; a pie without them draws its slices with
no legend.

## Styles

Move customizations from the flat 2.x parameters to the grouped `PieChartStyle`
sections. Slice, border, and legend values each get their own block:

```kotlin
// Before
PieChartDefaults.style(
    pieColor = Color.Blue,
    pieColors = listOf(Color.Red, Color.Green),
    pieAlpha = 0.8f,
    borderColor = Color.White,
    borderWidth = 2f,
    donutPercentage = 50f,
    legendVisible = false,
)

// After
PieChartDefaults.style(
    slices = PieChartDefaults.slices(baseColor = Color.Blue, alpha = 0.8f, colors = listOf(Color.Red, Color.Green)),
    border = PieChartDefaults.border(color = Color.White, width = 2.dp),
    donut = PieChartDefaults.donut(holePercentage = 50f),
    legend = PieChartDefaults.legend(visible = false),
)
```

`pieColors` becomes `slices(colors = …)`, still one color per slice in slice
order. Leave it empty and the chart generates a shade per slice from `baseColor`,
the way it did in 2.x. A palette whose count does not match the slice count is
reported as an error rather than drawn.

`pieAlpha` becomes `slices(alpha = …)`. Sizes are `Dp` rather than the 2.x
`Float` pixels, so divide pixel sizes by the screen density when migrating:
`borderWidth = 2f` was `2.dp` at 1x and `0.67.dp` on a 3x screen.

The interim v3 snapshots replaced this with a `PieSlice` type that carried a
`color` per slice. That type is gone; slice color is style, like every other
chart.

## Selection and interaction

Pass a `ChartSelection` directly to `PieChart` when selection must be
controlled by the application:

```kotlin
val selection = rememberChartSelection()
PieChart(data = data, selection = selection)
```

Programmatic selection remains visible when `interactionEnabled` is `false`; in
that mode, user taps and automatic deselection are disabled.
