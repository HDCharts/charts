---
title: Style Defaults
order: 5
---

# Style Defaults

Build chart styles with their `*ChartDefaults` factories. Shared default values live in
[`StyleDefaults`](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/style/StyleDefaults.kt)
in `charts-core`. Read the current values there when changing a default.

## Factories

Chart-specific blocks live on `*ChartDefaults`. Cartesian axes use the shared `AxesDefaults`,
`AxisLinesDefaults`, `GridDefaults`, and `AxisLabelDefaults` factories.

```kotlin
BarChartDefaults.style(
    axes = AxesDefaults.style(xLabels = AxisLabelDefaults.style(maxCount = 4)),
)
```

- Keep factory nesting shallow: style, axes, then the axis blocks.
- Read numeric and theme defaults from `StyleDefaults`. Forwarding factories use the same defaults
  as the factories they call.
- The master axes `visible` switch gates drawing and label layout. Each child keeps its own
  visibility setting.
- Use `Dp` for lengths so they scale with screen density. Point and marker sizes are radii.

## Colors and selection

Use `MaterialTheme.colorScheme` roles so defaults follow the app's light or dark theme. Keep theme
colors at their supplied opacity and express chart transparency through the style's alpha fields.

Choose a contrasting role for a selection mark drawn over a series. A mark in the series' own
color can disappear. Bars and stacked areas also distinguish selection by dimming the unselected
region; selection lines stop where they would overlap the selected fill.

Keep opacity exceptions with their reason. Radar fills are translucent so overlapping polygons
remain visible. Scatter points are translucent so overlapping observations remain visible.
While a selection is active, dimming multiplies the alpha of unselected marks.

## Formatters

Use the shared selected-value and axis formatters as defaults where their output fits the chart.
Pie formats a slice's share with a percent suffix. Chart-specific factories own any other formatter
defaults.

[Legend and Selection](legend.md) describes readouts. [Y-Axis Labels](y-axis-labels.md) describes
numeric tick formatting.

## Adding or changing a default

1. Define the value in `StyleDefaults` and update [StyleDefaultsTest](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonTest/kotlin/io/github/hdcharts/core/style/StyleDefaultsTest.kt).
2. Read it from the style factory and use the same value as the clamping fallback where needed.
3. Cover chart-specific behavior in the chart's style tests.
4. For a visual change, update and inspect the affected screenshots using
   [Screenshot baselines](https://charts.hdcode.dev/dev/releases/validation-matrix#screenshot-baselines).

[Style Clamping](style-clamping.md) owns drawable-value corrections. [Validation
Errors](validation-errors.md) owns input and style settings that need an error message.
