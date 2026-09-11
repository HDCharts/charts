# Bar and Histogram v3 migration

This PR migrates BarChart and HistogramChart to the shared v3 contracts from merged PR #541. It is the first Cartesian chart migration.

## What changed

### Public composables

Both composables expose the same top-level parameters, with chart-specific style defaults:

```kotlin
@Composable
fun BarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: BarChartStyle = BarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    valueFormatter: ChartValueFormatter = BarChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = BarChartDefaults.axisValueFormatter,
)

@Composable
fun HistogramChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: HistogramChartStyle = HistogramChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    valueFormatter: ChartValueFormatter = BarChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = BarChartDefaults.axisValueFormatter,
)
```

The old `dataSet: ChartDataSet` overloads and `selectedBarIndex` parameter are removed, with no public v2 forwarding overloads. Use `selection` instead. `animateOnStart` controls only the initial reveal, not later update animations.

Before:

```kotlin
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
```

After (inside a composable):

```kotlin
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.BarChartDefaults

val selection = rememberChartSelection(initialIndex = 1)
BarChart(
    data = listOf(18.0, 32.0, 26.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed"),
        seriesName = "Daily sales",
    ),
    title = "Daily sales",
    style = BarChartDefaults.style(
        range = BarChartDefaults.range(min = 0.0, max = 100.0),
        axis = BarChartDefaults.axis(
            xLabels = BarChartDefaults.xLabels(visible = false),
            yLabels = BarChartDefaults.yLabels(visible = false),
        ),
    ),
    selection = selection,
)
```

### Grouped styles

`BarChartStyle` and the concrete `HistogramChartStyle` are grouped classes in `io.github.dautovicharis.charts.style` (`charts-core`). Both contain:

- `chartContainerStyle: ChartContainerStyle`, built with `ChartContainerDefaults.style(...)`.
- `bars: BarBarsStyle`: color, colors, alpha, space, minBarWidth.
- `range: BarRangeStyle`: optional `min`/`max` of type `Double?` only.
- `grid: BarGridStyle`: visible, steps, color, lineWidth.
- `axis: BarAxisStyle`: visible, color, lineWidth, plus `xLabels`/`yLabels: AxisLabelStyle` (visible, color, size, count).
- `selectionLine: BarSelectionLineStyle`: visible, color, width.
- `zoomControlsVisible: Boolean`.

Use `BarChartDefaults.style(...)` and its block factories, or `HistogramChartDefaults.style(...)`. When customizing histogram colors or alpha, use `HistogramChartDefaults.bars(...)` to retain `space = 0.dp` and `minBarWidth = 10.dp`; histogram range defaults to `min = 0.0`. Spacing, minimum width, and bounds remain overridable.

`BarBarsStyle.colors` stores an `ImmutableList<Color>`. Its `List<Color>` convenience constructor defensively copies the palette, so later list mutations do not change the style. Factories accept ordinary lists; `copy(colors = ...)` requires an immutable list. A nonempty palette must match the source value count.

Public grid/axis `lineWidth` and selection-line `width` are now `Dp`. Defaults convert one physical pixel with `with(LocalDensity.current) { 1f.toDp() }`, preserving the old default appearance. An explicit `2.dp` is density-independent and intentionally renders differently from an old two-pixel stroke at non-unit densities. Review those custom-width differences deliberately; do not blindly refresh screenshot baselines.

### Data and precision

- `ChartData.series` contains `ChartSeries`; `ChartSeries.values` is `ImmutableList<Double>`, with a defensive `List<Double>` constructor. Build single-series data with `List<Double>.toChartData(categories = ..., seriesName = ...)`. Convert Float/Int/String inputs at the application boundary; the new public API has no such overloads.
- `title` is independent of categories and `seriesName`. Empty categories hide X labels. A selected readout shows `category: formattedValue` when labeled, otherwise only the formatted raw source value. No title-prefix/index labels are generated.
- `valueFormatter` formats selected raw values and defaults to `BarChartDefaults.valueFormatter`, which is `ChartValueFormatters.Default` (rounds to two decimals, retaining `.0` for whole values). `axisValueFormatter` formats Y-axis ticks independently; `BarChartDefaults.axisValueFormatter` uses Default and removes only a terminal `.0`. Changing one formatter does not change the other or the category labels.
- Automatic Y ranges include zero and use the original source values, including in compact mode. All-zero data uses `[0.0, 1.0]`. Bounds accept only finite `Double?` values; `null` derives that bound automatically. If explicit bounds produce an equal or reversed range, both bounds fall back to the zero-inclusive source domain.
- Each public entry point validates data and style once before renderer-data conversion. Data requires exactly one series with at least two values, all finite; categories and palettes must be empty or match the value count. Histograms additionally require nonnegative heights, including fractional values. Invalid data, nonfinite bounds, or invalid style dimensions use the existing error UI with the caller's `modifier` preserved, rather than entering the renderer.
- Spacing, minimum width, and strokes must resolve to `0..16384` physical pixels. Label sizes must be positive `sp` and resolve to at most 16384 pixels; label counts are `2..1000`, and grid steps are `0..1000`. These limits prevent invalid or excessive drawing work. The measured canvas must also fit Compose's combined width/height constraints; an oversized expanded canvas displays an error while leaving Fit and zoom controls available for recovery. Long Y labels are constrained to 40% of the available width so they cannot consume the plot.

### Selection and interaction

- `ChartSelection` is top-level state, not style. Its index always identifies a source bar/bin, never a compact bucket. `selection.select(index)` and `selection.clear()` update visuals and raw-value readouts.
- With the same holder, structurally changed `ChartData` clears selection, even when the old index is still valid. A structurally equal replacement does not clear it. Initial presets on a newly supplied holder are preserved when in bounds; out-of-bounds selections clear. Fit/expand, zoom, resizing, and density changes preserve the source selection.
- `rememberChartSelection(initialIndex = ..., onSelectionChanged = ...)` retains the holder and uses the latest callback. Only actual changes notify; clearing a selection reports `null`, while initialization, duplicate selection, and clearing an empty holder do not notify.
- `staticChartSelection(index)` from `io.github.dautovicharis.charts.model` is available for preview/screenshot presets. It creates a new mutable, unremembered holder, not a locked selection, and does not disable interaction or animation.
- `interactionEnabled = false` gates all user controls: selection gestures, horizontal scrolling, double-tap/pinch zoom, and fit/expand and zoom header controls. Those controls are hidden; title/readout text may remain. Programmatic selection still works.

### Dense data

- Bar compact mode renders bucket averages but keeps the original source Y range. Bucket labels/colors and taps use the bucket-center source index; selection readouts format that source value, not the average. Tapping the selected bucket again clears selection. Expanded mode shows individual source bars with scrolling and zoom.
- Histogram renders precomputed, equal-visual-width bins; it never aggregates or merges them. Fit mode preserves every bin and its exact height, allowing subpixel bin widths rather than forcing a one-pixel minimum. Expanded mode uses the default `10.dp` minimum bin width with scrolling and zoom. Labels are explicit text, including open-ended intervals; no automatic binning is performed.

### Other removals

- The flat `BarChartStyle` in `charts-bar` and the `HistogramChartStyle = BarChartStyle` typealias are removed. Use the new grouped `BarChartStyle` / `HistogramChartStyle` from `charts-core`.
- `BarSampleUseCase` and `HistogramSampleUseCase` return `ChartData` instead of `ChartDataSet`.
- Shared `ChartDataSet`, `MultiChartDataSet`, converters, `ChartDataType`, and legacy internal data/helper overloads remain until their last line, radar, or stacked-chart consumer (including samples and tests) migrates. Removing the bar/histogram entry-point overloads does not remove these shared APIs.

## Sample migration

- `DefaultBarSampleUseCase` and `DefaultHistogramSampleUseCase` produce `ChartData` from `List<Double>` with explicit `seriesName` and `categories`.
- `BarChartViewModel` and `HistogramChartViewModel` expose `StateFlow<ChartData>`.
- `BarDemo`, `HistogramDemo`, `ChartGalleryPreviews`, `DocsGifScenarios`, and the screenshot tests consume the new API.

## Validation

- Before publishing, run `ciCompile`, JVM/Wasm test-source compilation for `charts`, `charts-bar`, and `charts-histogram`, and the complete `ktlintCheck buildSrcKtlintCheck` gate.
- Local test execution is limited to the small JVM geometry/helper and data-validation suites. Compose UI, screenshots, browser, emulator, simulator, and API compatibility checks remain CI gates; no passing CI result is claimed here.

## Follow-ups

- Direct grouped-style consumption by the renderer is an approved, explicitly deferred follow-up. The internal flat `BarChartInternalStyle` adapter remains at this boundary; it does not retain any public v2 bar/histogram overloads.
- Subsequent parts migrate Single + Multi line (Part 3), Stacked Bar (Part 4), Stacked Area (Part 5), Radar (Part 6), and Pie numeric alignment + hardening (Part 7).
