---
title: Style Defaults
order: 4
---

# Style Defaults

Every chart takes one `style` object built with its `*ChartDefaults` factories. These rules keep the
defaults the same across charts. Follow them when adding a chart, a style block, or a default.

## Factories

A chart's `*ChartDefaults` object has one factory per style block, plus `style()`. Users write every
call against one object:

```kotlin
BarChartDefaults.style(
    axis = BarChartDefaults.axis(xLabels = BarChartDefaults.xLabels(maxCount = 4)),
)
```

- **Nesting stops at three levels:** `style`, then a block, then axis labels. Axis labels are the
  only third level, and all Cartesian charts share them as `AxisLabelStyle`.
- **Every block gets a factory on the chart's own object**, even when the block type is shared.
  `HistogramChartDefaults` forwards `grid`, `axis`, `xLabels`, `yLabels` and `selectionLine` to
  `BarChartDefaults`, and its `range` keeps a zero minimum.
- **Forwarding factories repeat the default values**, because Kotlin cannot forward them. A test
  compares the two objects so they cannot drift (`BarStyleDefaultsTest`).

## Colors

Default colors come from `MaterialTheme.colorScheme` roles at full opacity, so they follow the app's
theme in light and dark mode. Do not lower the alpha of a theme color, and do not check
`isSystemInDarkTheme()`: the app's theme can differ from the system setting.

| Element | Role |
| --- | --- |
| Series colors (bars, lines, fills, slices) | `primary`, or a palette from it |
| Points (line, radar) | `tertiary` |
| Axis labels | `onSurfaceVariant` |
| Radar axis labels | `onSurface` |
| Grid lines | `outlineVariant` |
| Axis lines | `outline` |
| Selection | `primary` on bar, histogram and stacked bar; `tertiary` on line; `outline` on stacked area |
| Pie slice border | `surface` |

## Alpha

Chart color `alpha` defaults to `1f`, so the colors a user passes are drawn exactly as given. Users
who want a softer look pass `alpha` themselves.

Radar is the one exception: `RadarChartDefaults.polygon` defaults `fillAlpha` to `0.25f`. Radar
polygons overlap each other and the grid, so a solid fill would hide the series behind it.
