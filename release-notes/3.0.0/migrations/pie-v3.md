# Pie chart v3 migration

`PieChart` uses `Double` slice values and includes stable validation, geometry,
selection, and percentage-readout behavior for v3 applications.

## Public API

Use `Double` values when constructing `PieSlice` instances:

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

Convert source values at the application boundary:

```kotlin
val slices = sourceSlices.map { slice ->
    PieSlice(label = slice.label, value = slice.value.toDouble())
}
```

The public `PieChart` call shape and Pie styles remain available with the v3
numeric contract.

## Validation and rendering

- `PieSlice.value` accepts finite, nonnegative `Double` values.
- `NaN`, positive infinity, and negative infinity produce dedicated validation
  messages.
- All-zero data renders finite geometry and displays `0` for every percentage.
- Slice alpha uses the configured style alpha, and raw value precision remains
  available until percentage normalization.
- Hit testing follows the drawn pie radius, excludes the donut hole, ignores
  zero-sweep slices, and assigns each angular boundary to one slice.
- The caller `Modifier` is preserved when validation renders the error UI.

## Selection and interaction

- Programmatic selection remains visible when `interactionEnabled` is `false`.
- User taps and the auto-deselect timeout are disabled when interaction is
  disabled.
- Repeated taps renew the interaction timeout without producing duplicate
  selection notifications.
- A delayed timeout checks the current selection holder and selected index before
  clearing, so an older timeout cannot clear a newer selection.

## Migration coverage

Update Pie call sites, previews, screenshots, and tests to use `Double` values.
Cover non-finite and all-zero data, drawn-radius and donut-hole hit testing,
zero-sweep slices, selection timeout renewal, selection-holder replacement, and
modifier forwarding on validation errors.
