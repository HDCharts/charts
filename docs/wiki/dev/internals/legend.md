---
title: Legend and Selection
order: 9
---

# Legend and Selection

The legend names what the colors stand for. While a point is selected, the title and the legend
show its details. The rules below define when each one appears and what it says.

The shared legend is `Legend` in `charts-core`, in `internal/composable/Legend.kt`. It takes the
chart's `LegendStyle` and hides itself when `visible` is false or without two items and a name. The
selected title and legend values come from `selectedTitle` and `selectedLegendValues` in
`internal/SelectedTitle.kt`.

## Rules

1. **The legend lists what color tells apart.** That is the series for line, live line, radar,
   stacked bar and stacked area, and the categories for pie and ring gauge. Bar and histogram use one
   color, so they have no legend.
2. **A legend needs two or more items and at least one name.** One item needs no key, and a list of
   blank names tells the reader nothing.
3. **`legend.visible = false` always hides it.** Every chart with a legend has a `legend` style
   block. A hidden legend also hides the selected values of several series; the caller can read
   `selectedIndex` from `rememberChartSelection()` and show them in its own UI.
4. **A selection names its category in the title.** The title shows the selected category, or the
   caller's title when the category is blank.
5. **One series puts its value in the title,** as `Category: value`. Pie is the exception; see
   rule 8.
6. **Several series put their values in the legend,** as `Name - value` on each item. An item with
   a blank name shows only its value. The title then shows only the category.
7. **Selected values use the chart's `selectedValueFormatter`.** Pie's formats the slice's share,
   in percent from 0 to 100.
8. **Pie shows the slice's share in the title**, such as `Mobile 42.5%`, next to its category, or
   the caller's title when the category is blank. Its legend does not change.

## By Chart

| Chart | Legend lists | Shown when | Selected title | Selected legend |
| --- | --- | --- | --- | --- |
| Line | Series names | 2+ series, a non-blank name, and `legend.visible` | One series: `Category: value`, or the value without a category. Several: category | Several series: `Name - value` |
| Live line | Series names | 2+ series, a non-blank name, and `legend.visible` | No selection | No selection |
| Bar | — | Never | `Category: value`, or the value without a category | — |
| Histogram | — | Never | Same as bar | — |
| Pie | Categories | 2+ categories, a non-blank name, and `legend.visible` | Category, or the caller's title when it is blank, then the share in percent | No change |
| Radar | Series names | 2+ series, a non-blank name, and `legend.visible` | Same as line | Several series: `Name - value` |
| Stacked bar | Segment names | 2+ segments, a non-blank name, and `legend.visible` | Same as line | Several segments: `Name - value` |
| Stacked area | Series names | 2+ series, a non-blank name, and `legend.visible` | Same as line | Several series: `Name - value` |
| Ring gauge | Categories | 2+ categories, a non-blank name, and `legend.visible` | `Category: value`, or the value without a category | No change |

## Known Issues

Known issues and limits are listed in Legend and Selection Issues, in the Known Issues section.
