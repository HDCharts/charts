# Pie chart numeric alignment and hardening

`PieSlice.value` is now `Double` for full v3 numeric alignment. Pie chart hit-testing, validation, percentage readout, selection lifecycle, and the error branch have been hardened.

```kotlin
PieChart(
    data = listOf(
        PieSlice(label = "Completed", value = 80.0),
        PieSlice(label = "Remaining", value = 20.0),
    ),
    modifier = Modifier.fillMaxWidth(),
    title = "Progress",
    style = PieChartDefaults.style(
        donut = PieChartDefaults.donut(holePercentage = 50f),
    ),
)
```

`80f` and other `Float` slice values must be replaced with `80.0`. The composable signature is otherwise unchanged.

## What changed

- **Numeric model**: `PieSlice.value` is `Double`. The renderer keeps raw precision until normalization. Slice alpha replaces source color alpha rather than multiplying it.
- **Validation**: Rejects `NaN`, `+Infinity`, and `-Infinity` separately (two error messages). Negative values still fail. All-zero data is accepted.
- **All-zero readout**: `calculatePercentages` returns `"0"` for every slice when the total is zero or non-finite, so the legend never shows `NaN%`. `createPieSlices` produces finite geometry for all-zero totals.
- **Hit-testing**: Uses the drawn pie radius (the radius after the selected-slice overflow inset), excludes taps inside the donut hole, ignores zero-sweep slices, and resolves boundaries with a half-open `[start, end)` interval so every boundary maps to exactly one slice.
- **Selection lifecycle**: The auto-deselect coroutine keys on `points` and the current selection holder, and the timeout reads the current selection state before clearing — older timeouts never clear a newer unrelated selection. `interactionNonce` restarts the timeout on repeated taps.
- **Modifier forwarding**: The public `modifier` parameter is now forwarded to the `ChartErrors` branch when validation fails.
- **Selection policy**: Programmatic selection is not subject to the auto-deselect timeout.

## Coverage added

- Data validation tests for `+Infinity`, `-Infinity`, and all-zero data.
- Hit-testing tests for drawn radius, donut-hole exclusion, and zero-sweep slice skipping.
- UI tests for `donutTapInsideHole_doesNotSelect`, `allZeroValues_rendersBlankWithoutNaN`, and `modifierForwardedToErrorBranch`.
- Local gates pass: JVM tests, Wasm test-source compilation, ktlint, sample compilation, and screenshot validation (124/124).
