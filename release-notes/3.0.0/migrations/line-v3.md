# Single and Multi-Line v3 migration

This part migrates the line family to the shared v3 API. One `LineChart` composable handles one or more aligned `ChartSeries`; there is no separate `MultiLineChart` alias.

## Public API

```kotlin
val selection = rememberChartSelection()

LineChart(
    data = chartData,
    modifier = Modifier.fillMaxWidth(),
    style = LineChartDefaults.style(),
    title = "Temperature",
    selection = selection,
    renderMode = LineChartRenderMode.Morph,
    valueFormatter = LineChartDefaults.valueFormatter,
    axisValueFormatter = LineChartDefaults.axisValueFormatter,
)
```

`chartData` is shared `ChartData` with `List<Double>` values. Use `List<Double>.toChartData(categories = ..., seriesName = ...)` for one series or `chartDataOf(categories = ..., ChartSeries(...), ...)` for multiple series. Categories are explicit labels, not numeric X coordinates; empty categories hide the X-label layer and do not create index-string labels.

The old `LineChart(dataSet: ChartDataSet, ...)` and `LineChart(dataSet: MultiChartDataSet, ...)` entry points are removed. `selectedPointIndex` is removed; use `selection = staticChartSelection(index)` for screenshots and deterministic previews. Selection is a source X index shared by every line. Replacing data clears selection, while resize, scrolling, compact/expanded changes, and external callback replacement preserve the current source selection where it remains valid.

## Grouped style

`LineChartStyle` is now grouped into:

- `line: LineVisualStyle` — color, alpha, per-series colors, stroke width (`Dp`), and Bezier interpolation.
- `points: LinePointStyle` — color, size (`Dp`), and visibility.
- `selection: LineSelectionStyle` — drag color, normal/active sizes (`Dp`), and visibility.
- `axis: LineAxisStyle` — visibility/color/width plus `xLabels` and `yLabels` `AxisLabelStyle` blocks.
- `chartContainerStyle` and `zoomControlsVisible`.

Defaults are available through `LineChartDefaults.style`, `line`, `points`, `selection`, `axis`, `xLabels`, and `yLabels`. Physical widths and marker sizes are density-aware. The line renderer temporarily adapts the grouped style to a flat internal renderer shape; that internal adapter is not a public v2 compatibility API and may be removed in a later renderer cleanup.

## Formatting and selection

`valueFormatter` formats selected raw values. `axisValueFormatter` independently formats Y-axis ticks. Both receive `Double`, default to the shared formatter contract, and avoid JVM-only formatting. Multi-line selection resolves one source index across all series; the selected title uses the category when categories exist, and the selected series values remain separate legend/readout values.

Morph continues to support compact averaging for dense data, but source-index mapping selects a bucket-center source point and readouts are taken from source data. Expanded/scrollable rendering uses the same source mapping. Timeline retains its update animation semantics and uses one domain for animated geometry and ticks.

Malformed data is rejected before internal conversion: empty data, fewer than two points, ragged series, category mismatch, non-finite values, and invalid palette/line dimensions render the existing error UI with the caller modifier. Interaction-disabled charts do not accept user gestures or control changes; programmatic selection still renders.

## Sample and test migration

Line samples, previews, screenshot call sites, GIF scenarios, the smoke consumer, UI tests, dense-data tests, and shared validation fixtures now construct `ChartData`/`ChartSeries` explicitly. Legacy dataset models remain in `charts-core` because Radar, Stacked Bar, and Stacked Area still consume them; they are deleted only after their last consumer migrates.

## Validation

The local line JVM suite, line production/test compilation, Wasm test-source compilation, smoke-line compilation, sample compilation, and lint are the implementation gates for this part. Android screenshot, browser execution, emulator, simulator, and API compatibility checks remain CI gates and are not claimed as locally run by this migration note.
