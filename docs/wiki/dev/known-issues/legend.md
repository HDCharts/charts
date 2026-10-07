---
title: Legend and Selection Issues
order: 9
---

# Legend and Selection Issues

Known issues and limits of the Legend and Selection page in Chart Internals.

## Need Verification

None yet.

## Confirmed

### Pie has no value formatter

A selected slice shows its share in a fixed format: a percentage rounded to two decimals, with `.`
as the decimal point in every locale. A caller cannot show whole percents, a localized decimal
separator, or the slice's own value instead of its share. Every other chart with a selection takes
a `selectedValueFormatter`.

Confirmed by the code: `calculatePercentages` in `PieChartHelpers.kt` builds the share with
`Double.toString`, and `PieChart` takes no formatter.

Options:

- Add a `selectedValueFormatter` to `PieChart` that formats the share, defaulting to the current
  format.

### Pie hides the share when the category and the title are both blank

A selected slice with a blank category falls back to the caller's title. With no title either, the
pie shows nothing on selection, not even the share.

Confirmed by the code: `PieChartFrame` draws the title row, share included, only when
`displayedTitle.isNotBlank()`.

Options:

- Show the share on its own when there is no category and no title.
