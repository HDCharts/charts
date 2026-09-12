# Stacked Area v3 migration

`StackedAreaChart` now accepts shared `ChartData`. Categories identify shared X positions; each `ChartSeries` is an ordered contribution series with one value per category.

```kotlin
StackedAreaChart(
    data = chartDataOf(
        categories = listOf("Jan", "Feb", "Mar"),
        ChartSeries("Free Plan", listOf(620.0, 650.0, 690.0)),
        ChartSeries("Standard Plan", listOf(240.0, 260.0, 285.0)),
        ChartSeries("Premium Plan", listOf(90.0, 95.0, 105.0)),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Monthly Active Subscribers by Plan",
    selection = rememberChartSelection(),
)
```

The old `MultiChartDataSet` and `selectedPointIndex` public inputs are removed. Use `selection = staticChartSelection(index)` for deterministic previews and screenshots. Selection is a source X index shared by every series; replacing data clears selection while resize, scrolling, compact/expanded changes, and external callback replacement preserve a valid source selection.

## Grouped style

`StackedAreaChartStyle` now groups:

- `fill: StackedAreaFillStyle` with fallback color, immutable series colors, and alpha.
- `boundary: StackedAreaBoundaryStyle` with visibility, fallback color, immutable series colors, `Dp` width, and Bezier flag.
- `axis: StackedAreaAxisStyle` with X/Y `AxisLabelStyle` blocks.
- `selection: StackedAreaSelectionStyle` with visibility, color, and `Dp` width.
- `chartContainerStyle` and `zoomControlsVisible`.

Explicit colors match the number of contribution series. The internal renderer temporarily receives a flat adapter style; that adapter is not a public compatibility API and can be removed in a later renderer cleanup.

## Data and behavior

- Require at least one aligned series and at least two X positions.
- Categories are empty or match every series length.
- Contributions must be finite and nonnegative. Diverging or signed stacks are not supported in this migration.
- Stacking remains absolute per X position, not percentage-normalized columns. All-zero totals use a finite fallback domain.
- Compact rendering may average source points for density, but source selection maps to a bucket-center source X and selected legend values remain raw contributions.
- Linear and Bezier boundaries share identical geometry. Boundary colors default to the fill palette when not provided.
- `interactionEnabled = false` disables user gestures and header control changes while programmatic selection and theme/style updates still render.
- The caller `Modifier` is preserved for both valid and validation-error output.

## Validation

The implementation adds invalid/ragged/negative/nonfinite data coverage, fill/boundary palette cardinality coverage, selection and dense-mode tests, sample migration, screenshot call-site migration, and release notes. Local validation completed for stacked-area JVM tests, shared JVM tests, stacked-area Wasm test-source compilation, affected lint, and sample compilation. Android screenshots, browser, simulator, and API compatibility remain CI gates.
