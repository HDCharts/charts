# Pie Slice Numeric Migration

`PieSlice.value` is now `Double`. The public `PieChart` call shape and Pie styles remain unchanged.

Before:

```kotlin
PieChart(
    data = listOf(
        PieSlice(label = "Completed", value = 80f),
        PieSlice(label = "Remaining", value = 20f),
    ),
)
```

After:

```kotlin
PieChart(
    data = listOf(
        PieSlice(label = "Completed", value = 80.0),
        PieSlice(label = "Remaining", value = 20.0),
    ),
)
```

There are no Float convenience overloads. Convert or parse values at the application boundary with
`toDouble()` when the source data is still Float or another numeric representation.
