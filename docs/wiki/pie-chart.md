---
title: Pie
---

# Pie Chart

`PieChart` renders proportional data as labeled slices.

![Pie Demo](/content/{{version}}/wiki/assets/pie_default.gif)

```kotlin
@Composable
private fun ShowPie() {
    val slices = listOf(
        PieSlice(label = "Heating", value = 32.0),
        PieSlice(label = "Cooling", value = 21.0),
        PieSlice(label = "Appliances", value = 24.0),
        PieSlice(label = "Water Heating", value = 14.0),
        PieSlice(label = "Lighting", value = 9.0),
    )

    PieChart(
        data = slices,
        title = "Household Energy",
    )
}
```
