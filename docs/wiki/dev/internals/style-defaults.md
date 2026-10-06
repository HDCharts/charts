---
title: Style Defaults
order: 4
---

# Style Defaults

Every chart takes one `style` object built with its `*ChartDefaults` factories. These rules keep the
defaults the same across charts. Follow them when adding a chart, a style block, or a default.

Every default value lives in `StyleDefaults` in `charts-core`, including the chart-specific ones.
Factories read their defaults from it and never repeat a literal, so the same element cannot differ
between charts. The tables below list what it holds.

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
  `HistogramChartDefaults` forwards `grid`, `axis`, `xLabels`, `yLabels` and `selection` to
  `BarChartDefaults`, and its `range` keeps a zero minimum.
- **Forwarding factories repeat the default parameters**, because Kotlin cannot forward them. Both
  read the same `StyleDefaults` value, and a test compares the two objects (`BarStyleDefaultsTest`).

## Colors

Default colors come from `MaterialTheme.colorScheme` roles at full opacity, so they follow the app's
theme in light and dark mode. Do not lower the alpha of a theme color, and do not check
`isSystemInDarkTheme()`: the app's theme can differ from the system setting.

| Element | Role | Charts |
| --- | --- | --- |
| Series (bars, lines, fills, slices) | `primary`, or a palette from it | All |
| Points | `tertiary` | Line, radar (with `colorSameAsLine = false`) |
| Axis labels | `onSurfaceVariant` | Bar, histogram, line, stacked bar, stacked area, radar |
| Title | `onSurface` | All |
| Grid lines | `outlineVariant` | Bar, histogram, radar (rings and spokes) |
| Axis lines | `outline` | Bar, histogram, line |
| Selection line | `onSurface` | Bar, histogram, line, stacked bar, stacked area |
| Selection marker | `tertiary` | Line |
| Slice border | `surface` | Pie |

Selection indicators draw on top of series marks. A selection default must never use the series
role (`primary` or its palette), or it disappears over the selected mark.

## Sizes

Sizes are `Dp`, so they scale with screen density. Point and marker sizes are radii.

| Element | Default | Charts |
| --- | --- | --- |
| Grid and axis lines | `1.dp` | Bar, histogram, line, radar |
| Selection line | `1.dp` | Bar, histogram, line, stacked bar, stacked area |
| Slice border | `1.dp` | Pie |
| Series line | `2.dp` | Line (`strokeWidth`), radar (`lineWidth`) |
| Points | `4.dp` | Line, radar (data points) |
| Selected point | `5.dp` (`selection.pointSize`) | Line, radar |
| Touch marker on the line | `3.dp` (`selection.markerSize`) | Line |
| Gap between an axis and its labels | `10.dp` | Bar, histogram, line, stacked bar, stacked area, radar |
| Radar label clamp margin | `6.dp`, only for a label that does not fit | Radar |
| Bar spacing | `10.dp`, `0.dp` on histogram | Bar, histogram, stacked bar |
| Minimum bar width | `10.dp` | Bar, histogram, stacked bar |
| Container padding | `15.dp` | All |

## Text

| Element | Default | Charts |
| --- | --- | --- |
| Title | `20.sp`, `ExtraBold` | All |
| Axis labels | `11.sp` | Bar, histogram, line, stacked bar, stacked area, radar |

## Alpha

Chart color `alpha` defaults to `1f`, so the colors a user passes are drawn exactly as given. Users
who want a softer look pass `alpha` themselves.

| Setting | Default | Charts |
| --- | --- | --- |
| Series `alpha` | `1f` | Bar, histogram, line, pie, stacked bar, stacked area |
| Polygon `fillAlpha` | `0.25f` | Radar |
| Selection `unselectedAlpha` | `0.7f` | Bar, histogram, stacked bar, stacked area, radar |
| Selection `unfocusedSeriesAlpha` | `0.35f` | Radar |

Radar is the one exception to full-opacity fills. Radar polygons overlap each other and the grid,
so a solid fill would hide the series behind it.

Selection dimming applies only while something is selected and multiplies the series alpha; it
does not change the resting colors. `unfocusedSeriesAlpha` is lower than `unselectedAlpha`, because
translucent radar fills barely change at `0.7f`.

Do not add a default mark that draws in the series color on top of its own series, such as the
removed stacked-area boundary line. At full opacity it cannot be seen.

## Chart-specific defaults

| Setting | Default | Charts |
| --- | --- | --- |
| Grid steps | `4` | Bar, histogram, radar |
| Range | Fitted to the data, minimum `0.0` on histogram | Bar, histogram, line |
| Axis label `maxCount` | `null` (as many as fit) | Bar, histogram, line, stacked bar, stacked area |
| Curved lines (`bezier`) | `true` on line, `false` on stacked area | Line, stacked area |
| Points visible | `false`; the selected point is still drawn | Line, radar |
| Radar axis labels visible | `true`; the web shrinks to leave room for them | Radar |
| Radar points use line color | `true` | Radar |
| Donut hole | `0f` (full pie) | Pie |
| Zoom controls visible | `true` | Bar, histogram, line, stacked bar, stacked area |
| Legend visible | `true`; see Legend and Selection for when it shows | Line, pie, radar, stacked bar, stacked area |

## Invalid values

Style values are never rejected; each chart draws a clamped copy of its style. Style Clamping
describes the rules, and Validation Errors lists what is still an error.

## Selection

A thin line cannot contrast with both the background and every bar color, so bar, histogram, and
stacked bar charts let the selected bar mark the selection. The other bars dim to
`unselectedAlpha`, and the selection line is drawn only over the background.

Stacked area follows the same rule. A column one data step wide around the selected point keeps
full color, the rest of the stack dims, and the selection line stops at the top of the stack.

Radar has no selection line. Dragging selects an axis, and its per-axis marks are the points: the
data points and labels of the other axes dim to `unselectedAlpha`. Polygons stay whole, because
their shape is what a radar chart shows. Tapping a series outline focuses the
series and fades the others to `unfocusedSeriesAlpha`.
