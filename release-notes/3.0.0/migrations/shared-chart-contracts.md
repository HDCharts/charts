# Shared v3 chart contracts

This step finalizes the shared data, formatting, and selection foundation before the individual chart migrations. It changes the provisional APIs introduced in v3 snapshots. It does not replace the legacy `ChartDataSet` / `MultiChartDataSet` parameters of existing charts yet.

## One value type: Double

The shared v3 API accepts **Double only**, not separate String, Int, Float, or generic Number inputs. `ChartSeries.values` and `ChartValueFormatter.format` now use Double, and `toChartData()` is an extension on `List<Double>` only.

```kotlin
// Before: provisional v3 snapshot API
val series = ChartSeries(name = "Sales", values = listOf(24f, 18f))
val data = listOf(24f, 18f).toChartData()

// After
val series = ChartSeries(name = "Sales", values = listOf(24.0, 18.0))
val data = listOf(24.0, 18.0).toChartData()
```

Convert or parse external values at the application boundary, before constructing chart data:

```kotlin
val dataFromInts = integerValues.map { it.toDouble() }.toChartData()
val dataFromFloats = floatValues.map { it.toDouble() }.toChartData()
val dataFromStrings = stringValues.map { text ->
    requireNotNull(text.toDoubleOrNull()?.takeIf { it.isFinite() }) {
        "Invalid chart value: $text"
    }
}.toChartData()
```

Choose an appropriate application error policy for invalid strings; the library does not infer parsing, locale, or missing-value behavior. Widening a Float does not recover precision already lost. The immutable models copy supplied lists; changing those original lists does not update a chart. Replace data instead. Models do not enforce chart-specific lengths or value ranges; each chart's validation remains responsible for those rules.

Legacy v2 numeric/string dataset conversions remain while their charts still consume them. They are not new v3 input alternatives. Pie still uses its existing `PieSlice` entity with a Float value in this PR; a separate numeric alignment is planned before v3 stabilization. Do not pass the new `ChartData` to a chart that has not migrated its public signature.

## Categories are explicit

`toChartData()` no longer invents index-string labels. Empty categories mean no explicit labels, matching `ChartData()` and `chartDataOf()` defaults. Nonempty categories describe the shared indexed dimension and must match each series' length.

```kotlin
val values = listOf(24.0, 18.0)
val data = values.toChartData(categories = listOf("Mon", "Tue"), seriesName = "Sales")

// Explicitly request index labels if needed.
val indexed = values.toChartData(categories = values.indices.map(Int::toString))
```

Series names are display names, not unique identities. Categories are labels, not numeric X coordinates. Title and formatting are separate from this shared table; legacy labels, prefix/postfix, and title must be migrated deliberately with each chart.

## Selection callbacks

`ChartSelection` notifies after an actual selection change. Selecting the same index twice or clearing an already empty selection no longer emits duplicate events. Construction and callback replacement do not emit events.

```kotlin
val selection = rememberChartSelection(
    initialIndex = null,
    onSelectionChanged = { index -> onPointSelected(index) },
)
```

The holder is remembered; a new `initialIndex` on recomposition does not reset it. The callback above always uses the latest callback from composition without recreating the holder. Non-composable consumers can pass the callback to `ChartSelection(...)`. Explicitly assigning its `onSelectionChanged` property overrides the factory callback.

Selection indices refer to source data. Bounds checking and chart-specific reset/gesture policies belong to the chart. `staticChartSelection(index)` creates ordinary mutable state initialized to that index; it does not lock selection or disable animation/interaction.

Pie continues to take selection through `PieChartDefaults.style(selection = selection)`. It now redirects taps to a replacement holder and cancels the previous holder's pending deselection timer. Repeated taps still drive the interaction timeout independently of deduplicated selection notifications. No pie geometry or public parameter redesign is included.

## Value formatting

Custom formatters receive Double:

```kotlin
val formatter = ChartValueFormatter { value: Double -> "$value units" }
val fixed = ChartValueFormatters.fixed(2)
fixed.format(3.0) // "3.00"
ChartValueFormatters.fixed(0).format(3.7) // "4", previously "4.0"
ChartValueFormatters.Default.format(3.0) // "3.0"
```

- `fixed(precision)` supports `0..15` fractional digits and pads exactly that many. Values outside this precision range throw `IllegalArgumentException`.
- Default formatting rounds to two fractional digits, trims trailing zeros, and keeps `.0` for integers. Prefix/suffix factories use the same default formatter.
- Decimal ties round half away from zero. Negative values near zero display unsigned zero; negative ties no longer follow the old integer-rounding asymmetry.
- Formatting manipulates the Double's decimal string rather than scaling into an Int/Long, so large finite values do not saturate or overflow. Scientific notation is expanded into decimal output.
- NaN and infinities display as `NaN`, `Infinity`, and `-Infinity`; these strings are not permission for a chart to render invalid numeric data.
- Rounding follows the platform's `Double.toString()` decimal representation, not its exact binary fraction. Last-digit representation differences can affect boundary rounding across platforms. Applications needing a stricter domain-specific formatting policy can supply their own formatter.

Legacy chart-axis formatting is not switched to this formatter until each chart migrates. Review these intentional shared API breaks through the normal v3 `breaking-change` workflow; do not reset API baselines manually.
