## charts-core

### Reported API symbols

- `io.github.dautovicharis.charts.style.ChartViewDefaults.style(...)`

### Do I need to update call sites?

- No, if you already call `ChartViewDefaults.style(...)` and keep the default square chart area.
- Yes, if you want a non-square chart area; pass the new `modifierChart` argument.

### What changed

- `ChartViewDefaults.style(...)` now includes `modifierChart: Modifier` (default `Modifier.aspectRatio(1f)`) and threads it through `ChartViewStyle`.

### Migration (only if required)

```kotlin
// Before
val chartViewStyle = ChartViewDefaults.style()

// After
val chartViewStyle = ChartViewDefaults.style(
    modifierChart = Modifier.aspectRatio(16f / 9f),
)
```

- Recommended: Prefer named arguments when calling style factory functions.

## charts-pie

### Reported API symbols

- `io.github.dautovicharis.charts.style.PieChartDefaults.style(...)`

### Do I need to update call sites?

- No, if you never passed `innerPadding` to `PieChartDefaults.style(...)`.
- Yes, if you previously passed `innerPadding`; move that value into `chartViewStyle`.

### What changed

- `PieChartDefaults.style(...)` removed `innerPadding`.
- Pie content padding now comes from `chartViewStyle.innerPadding`.

### Migration (only if required)

```kotlin
// Before
val pieStyle = PieChartDefaults.style(
    innerPadding = 24.dp,
)

// After
val pieStyle = PieChartDefaults.style(
    chartViewStyle = ChartViewDefaults.style(innerPadding = 24.dp),
)
```

- Recommended: Keep pie container spacing and chart-view spacing aligned through one `ChartViewStyle` instance.
