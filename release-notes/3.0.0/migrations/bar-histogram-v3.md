# Bar and Histogram v3 migration

This PR migrates BarChart and HistogramChart to the shared v3 chart API and groups their style surface. It depends on the shared v3 chart contracts from PR #541 and is the first Cartesian chart migration.

## What changed

### Public composables

- `BarChart` now takes `data: ChartData`, an optional `Modifier`, an optional `title`, a top-level hoisted `ChartSelection`, and a grouped `BarChartStyle`. Exactly one aligned series is required.
- `HistogramChart` now takes `data: ChartData` and a concrete grouped `HistogramChartStyle`. Histogram defaults enforce contiguous bins (space = 0.dp, minBarWidth = 0.dp) and a zero baseline.
- The old `BarChart(dataSet: ChartDataSet, ...)` and `HistogramChart(dataSet: ChartDataSet, ...)` overloads are removed.
- `selectedBarIndex` is removed. Use `selection = staticChartSelection(index)` from `io.github.dautovicharis.charts.model` for deterministic selection in tests, previews, and screenshots.

```kotlin
// Before
BarChart(
    dataSet = listOf(18f, 32f, 26f).toChartDataSet(
        title = "Daily sales",
        labels = listOf("Mon", "Tue", "Wed"),
    ),
    style = BarChartDefaults.style(
        minValue = 0f,
        maxValue = 100f,
        xAxisLabelsVisible = false,
        yAxisLabelsVisible = false,
    ),
    selectedBarIndex = 1,
)

// After
BarChart(
    data = listOf(18.0, 32.0, 26.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed"),
        seriesName = "Daily sales",
    ),
    title = "Daily sales",
    style = BarChartDefaults.style(
        range = BarChartDefaults.range(min = 0f, max = 100f),
        axis = BarChartDefaults.axis(
            xLabels = BarChartDefaults.xLabels(visible = false),
            yLabels = BarChartDefaults.yLabels(visible = false),
        ),
    ),
    selection = staticChartSelection(1),
)
```

### Grouped styles

`BarChartStyle` and `HistogramChartStyle` are new grouped immutable classes in `io.github.dautovicharis.charts.style` (charts-core module). Both compose the same shared blocks:

- `bars: BarBarsStyle` — color, colors, alpha, space, minBarWidth.
- `range: BarRangeStyle` — optional fixed min/max (`Float?`).
- `grid: BarGridStyle` — visible, steps, color, lineWidth.
- `axis: BarAxisStyle` — visible, color, lineWidth, plus `xLabels: AxisLabelStyle` and `yLabels: AxisLabelStyle` (visible, color, size, count).
- `selectionLine: BarSelectionLineStyle` — visible, color, width.
- `zoomControlsVisible: Boolean`.

Defaults factories:

- `BarChartDefaults.style(...)` for bars.
- `BarChartDefaults.bars(...)`, `range(...)`, `grid(...)`, `axis(...)`, `xLabels(...)`, `yLabels(...)`, `selectionLine(...)` for the individual blocks.
- `HistogramChartDefaults.style(...)` with histogram-friendly defaults (`bars` zero spacing, `range` zero minimum).

The previous flat `BarChartStyle` from `charts-bar` is removed. The internal renderer still consumes a flat shape (`BarChartInternalStyle`, `@InternalChartsApi`) at this PR boundary; a follow-up refactor will move the renderer to grouped blocks and delete the internal type.

### Data and precision

- `ChartData.values` is `List<Double>` (shared with the v3 contracts PR #541). Build chart data via `List<Double>.toChartData(categories = ..., seriesName = ...)`. There is no Float overload for the new shared v3 input.
- Categories describe the shared indexed dimension. When omitted (the new default), the chart falls back to `"$title${index + 1}"` style labels internally; charts that want explicit text labels should pass `categories = ...`.
- Validation requires exactly one series, at least two values, finite values, and categories that match the value count when provided. Histogram additionally requires nonnegative values.

### Selection

`ChartSelection` is a top-level hoisted state input, not part of the style:

```kotlin
val selection = rememberChartSelection()
BarChart(data = ..., selection = selection)
```

`staticChartSelection(index)` remains the way to preset a selection for screenshots. The renderer reports selection via the holder's `onSelectionChanged` callback; programmatic `selection.select(...)` / `selection.clear()` updates visuals and readouts.

### Other removals

- `BarChart(dataSet: ChartDataSet, ...)`, `HistogramChart(dataSet: ChartDataSet, ...)`, and `selectedBarIndex: Int` are removed. Migrate to the v3 signatures above.
- The flat `BarChartStyle` in `charts-bar` and the `HistogramChartStyle = BarChartStyle` typealias are removed. Use the new grouped `BarChartStyle` / `HistogramChartStyle` from `charts-core`.
- `BarSampleUseCase` and `HistogramSampleUseCase` return `ChartData` instead of `ChartDataSet`.

## Sample migration

- `DefaultBarSampleUseCase` and `DefaultHistogramSampleUseCase` produce `ChartData` from `List<Double>` with explicit `seriesName` and `categories`.
- `BarChartViewModel` and `HistogramChartViewModel` expose `StateFlow<ChartData>`.
- `BarDemo`, `HistogramDemo`, `ChartGalleryPreviews`, `DocsGifScenarios`, and the screenshot tests consume the new API.

## Validation

- `./gradlew ciCompile` (JVM, Wasm, Android production) passes.
- `./gradlew :charts:compileTestKotlinJvm :charts-bar:compileTestKotlinJvm :charts-histogram:compileTestKotlinJvm :charts:compileTestKotlinWasmJs :charts-bar:compileTestKotlinWasmJs :charts-histogram:compileTestKotlinWasmJs` pass.
- `./gradlew :charts-core:ktlintCheck :charts-bar:ktlintCheck :charts-histogram:ktlintCheck :sample-shared:ktlintCheck :app:ktlintCheck` pass.
- Tests, screenshots, and the API compatibility gate run on CI per the existing `breaking-change` workflow.

## Follow-ups

- Refactor the bar renderer internals (`BarChart.kt`, `BarChartContent.kt`, `BarChartDrawing.kt`, `BarChartHeader.kt`) to consume grouped blocks directly and delete `BarChartInternalStyle`.
- Subsequent parts migrate Single + Multi line (Part 3), Stacked Bar (Part 4), Stacked Area (Part 5), Radar (Part 6), and Pie numeric alignment + hardening (Part 7).
