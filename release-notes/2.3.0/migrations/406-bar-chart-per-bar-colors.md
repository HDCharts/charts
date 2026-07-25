## charts-bar

### Reported API symbols

- `io.github.dautovicharis.charts.style.BarChartDefaults.style(...)`
- `io.github.dautovicharis.charts.internal.BarValidationKt.validateBarData(...)`

### Do I need to update call sites?

- No, if you call `BarChartDefaults.style(...)` with named arguments only.
- Yes, if you pass `BarChartDefaults.style(...)` arguments positionally after `barColor`, or if you call internal `validateBarData(...)` directly.

### What changed

- `BarChartDefaults.style(...)` adds `barColors: List<Color> = emptyList()` immediately after `barColor`.
- `validateBarData(...)` now accepts `colorsSize` to validate `barColors` length against data points.

### Migration (only if required)

```kotlin
// Before
val barStyle = BarChartDefaults.style(
    MaterialTheme.colorScheme.primary,
    0.4f,
    10.dp,
)

// After
val barStyle = BarChartDefaults.style(
    barColor = MaterialTheme.colorScheme.primary,
    barAlpha = 0.4f,
    space = 10.dp,
)
```

- Recommended: Use named arguments for `BarChartDefaults.style(...)` to stay resilient to future parameter additions.
