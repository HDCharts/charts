---
title: Legend and Selection
order: 9
---

# Legend and Selection

The shared `Legend` names what colors distinguish. Indexed-series readouts use `selectedTitle` and
`selectedLegendValues` from
[SelectedTitle.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/SelectedTitle.kt)
to show a selected category and its values.

## Visibility

`Legend` in [Legend.kt](https://github.com/HDCharts/charts/blob/main/charts-core/src/commonMain/kotlin/io/github/hdcharts/core/internal/composable/Legend.kt) displays when its style is visible, it has
at least two items, and at least one item has a name. A blank name alone adds no useful key.

The chart supplies the items: series names for a multi-series chart, or category names when colors
distinguish values within one series. A chart using a single color can omit the legend.

Hiding the legend also hides any selected values displayed there. Apps can read selection state
and show those values in their own UI.

## Indexed-series readouts

- A selection names its category in the title, falling back to the caller's title for a blank
  category.
- A single-series readout uses `Category: value`, or just the value when no title is available.
- Multi-series readouts put values in the legend as `Name - value`. The title shows the category;
  a blank series name leaves only its value in the legend.
- Values use the chart's `selectedValueFormatter`.

Pie formats a selected slice's share in the title, with a percent suffix, and keeps its legend
unchanged. Ring gauges also keep category legends unchanged while displaying the selected value.
Live line shows its series key while its window moves and has no selection readout.

Charts with other data models own their readouts in their entry or content. Follow the chart's
implementation when changing selection behavior.
