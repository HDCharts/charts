---
title: Pie
---

# Pie Chart

`PieChart` renders proportional data as labeled slices.

![Pie Demo](/content/{{version}}/wiki/assets/pie_default.gif)

A pie is one series whose values are the slices. Each category is a slice: its label names the slice
in the legend and while it is selected. Slice colors come from the style, either as an explicit
palette or as shades generated from its base color.

Categories are optional, as they are on every chart. A pie without them draws its slices with no
legend, the way a line chart draws with no X-axis labels.

The example below is minimal and runs as written. The full source behind the GIF is in
[`PieExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/PieExample.kt).

```kotlin
@Composable
fun ShowPie() {
    val data = listOf(32.0, 21.0, 24.0, 14.0, 9.0).toChartData(
        categories = listOf("Heating", "Cooling", "Appliances", "Water Heating", "Lighting"),
    )

    PieChart(data = data, title = "Household Energy")
}
```

To give the slices your own colors, set a palette with one color per slice, in slice order:

```kotlin
PieChart(
    data = data,
    style = PieChartDefaults.style(
        slices = PieChartDefaults.slices(
            colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta),
        ),
    ),
)
```

A palette whose count does not match the slice count is reported as an error rather than drawn.

## Selected Share

A selected slice shows its share of the total next to its category, such as `42.5%`.
`selectedValueFormatter` formats that share, given in percent from 0 to 100. This one shows whole
percents:

```kotlin
PieChart(
    data = data,
    selectedValueFormatter = ChartValueFormatter { share -> "${share.roundToInt()}%" },
)
```
