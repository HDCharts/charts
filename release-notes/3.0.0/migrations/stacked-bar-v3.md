# Stacked Bar v3 migration

`StackedBarChart` now accepts shared `ChartData`. Categories are bars; each `ChartSeries` is an ordered segment with one contribution per bar.

```kotlin
StackedBarChart(
    data = chartDataOf(
        categories = listOf("North", "Europe", "Asia"),
        ChartSeries("Q1", listOf(320.0, 260.0, 220.0)),
        ChartSeries("Q2", listOf(340.0, 280.0, 210.0)),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Quarterly revenue",
    selection = rememberChartSelection(),
)
```

The old row-oriented `MultiChartDataSet` and `selectedBarIndex` public inputs are removed. Legacy rows must be transposed explicitly: rows are bars, while each new series is one segment column. Use `selection = staticChartSelection(index)` for deterministic previews and screenshots.

## Grouped style

`StackedBarChartStyle` now groups:

- `segments: StackedBarSegmentStyle` with fallback color, immutable segment colors, and alpha.
- `layout: StackedBarLayoutStyle` with spacing and minimum bar width.
- `axis: StackedBarAxisStyle` with X/Y `AxisLabelStyle` blocks.
- `selection: StackedBarSelectionStyle` with visibility, color, and `Dp` width.
- `chartContainerStyle` and `zoomControlsVisible`.

Explicit colors match the number of segment series, not the number of bars. The internal renderer temporarily receives a flat adapter style; that adapter is not a public compatibility API and can be removed in a later renderer cleanup.

## Data and behavior

- Require at least one aligned segment series and at least two bars.
- Categories are empty or match every segment length.
- Contributions must be finite and nonnegative. Negative/diverging stacks are not supported in this migration.
- Stacks remain absolute totals, not percentage-normalized columns. All-zero totals use a finite fallback domain.
- Compact rendering may average source bars for density, but source selection maps to a bucket-center source bar and selected legend values remain raw contributions.
- Selection is whole-bar and hoisted through `ChartSelection`; it is not segment-level.
- Replacing data clears selection. Resize, density changes, and scrolling preserve valid source selection.
- `interactionEnabled = false` disables user controls while programmatic selection remains visible.
- The caller `Modifier` is preserved for both valid and validation-error output.

## Validation

The implementation adds nonsquare transposition fixtures, invalid/ragged/negative/nonfinite data coverage, segment palette cardinality coverage, selection and dense-mode tests, sample migration, screenshot call-site migration, and release notes. Local validation completed for stacked-bar JVM tests, shared JVM tests, stacked-bar Wasm test-source compilation, affected lint, and sample compilation. Android screenshots, browser, simulator, and API compatibility remain CI gates.
