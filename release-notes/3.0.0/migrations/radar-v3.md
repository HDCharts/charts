# Radar v3 migration

`RadarChart` now accepts shared `ChartData`. Categories identify shared axes; each `ChartSeries` is one polygon with one contribution per axis. One and multiple polygons share the same composable and the same observed global min/max normalization.

```kotlin
RadarChart(
    data = chartDataOf(
        categories = listOf("Performance", "Reliability", "Usability", "Security", "Scalability", "Observability"),
        ChartSeries("Android App", listOf(88.0, 81.0, 79.0, 90.0, 83.0, 76.0)),
        ChartSeries("iOS App", listOf(84.0, 86.0, 82.0, 88.0, 80.0, 79.0)),
        ChartSeries("Web App", listOf(78.0, 74.0, 85.0, 83.0, 88.0, 84.0)),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Platform Readiness Score",
    selection = rememberChartSelection(),
)
```

The old `ChartDataSet` and `MultiChartDataSet` overloads are removed. Title now comes from the `title` parameter or, when an axis is selected, from the selected category; the dataset's own title string is no longer a chart input. `selectedAxisIndex` is replaced by top-level `ChartSelection`; use `selection = staticChartSelection(index)` for deterministic previews and screenshots.

## Grouped style

`RadarChartStyle` now groups:

- `grid: RadarGridStyle` with visibility, color, `Dp` line width, and ring step count.
- `axes: RadarAxesStyle` with visibility, line color, `Dp` line width, label color/size, `Dp` label padding, and label visibility.
- `polygon: RadarPolygonStyle` with fill visibility/alpha, fallback color, immutable polygon colors, and `Dp` line width.
- `points: RadarPointStyle` with visibility, color, color-same-as-line, and `Dp` size.
- `categories: RadarCategoryStyle` with legend/pin visibility, immutable colors, and `Dp` pin size.
- `chartContainerStyle`.

Explicit polygon and category colors match the number of series and the number of categories respectively. The internal renderer temporarily receives a flat adapter style; that adapter is not a public compatibility API and can be removed in a later renderer cleanup.

## Data and behavior

- Require at least one polygon series and at least three aligned axes.
- Categories are empty or match every series length.
- Values must be finite. Signed, constant, and all-zero inputs keep observed global min/max normalization; constant-domain behavior is documented in the shared radar notes rather than silently switched to a zero-based domain.
- Series legend rows expose raw axis values when an axis is selected. The category legend remains controlled by `categories.legendVisible` and shows whenever a selection exists.
- `interactionEnabled = false` disables drag gestures while programmatic selection still highlights the corresponding axis point, pin, legend, and title.
- The caller `Modifier` is preserved for both valid and validation-error output.

## Validation

The implementation adds no-categories, axis-selection, and grouped-style coverage; updates the central mock and helper tests; migrates the demo, screenshot test, Docs GIF, and chart gallery preview; and refreshes baselines to reflect density-aware default widths and the new title/legend semantics. Local validation completed for radar JVM tests, central JVM tests, radar Wasm test-source compilation, affected lint, sample compilation, and screenshot validation. Android screenshots in additional variants, browser, simulator, and API compatibility remain CI gates.
