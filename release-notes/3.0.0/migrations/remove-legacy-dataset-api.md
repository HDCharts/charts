# Legacy Dataset API Removal

The v3 migration removes the public `ChartDataSet` and `MultiChartDataSet` wrappers and their
numeric/string discriminator API. Chart entry points already use the v3 `ChartData` contract.

Before:

```kotlin
val data = listOf(10f, 20f, 30f).toChartDataSet(title = "Revenue")
LineChart(data = data)
```

After:

```kotlin
val data = listOf(10.0, 20.0, 30.0).toChartData(seriesName = "Revenue")
LineChart(data = data)
```

For multiple aligned series, the concise v3 builder is recommended:

```kotlin
val data =
    listOf(
        "Revenue" to listOf(10.0, 20.0, 30.0),
        "Costs" to listOf(4.0, 8.0, 12.0),
    ).toChartData(categories = listOf("Jan", "Feb", "Mar"))
```

For an explicit construction, use `ChartData` and `ChartSeries` directly:

```kotlin
val data =
    ChartData(
        categories = listOf("Jan", "Feb", "Mar"),
        series =
            listOf(
                ChartSeries(name = "Revenue", values = listOf(10.0, 20.0, 30.0)),
                ChartSeries(name = "Costs", values = listOf(4.0, 8.0, 12.0)),
            ),
    )
```

Convert or parse source values at the application boundary before constructing `ChartData`; the
v3 public numeric contract is `Double` only. The remaining internal prepared-data adapters are not
public API and are retained until their renderer consumers can be migrated independently.
