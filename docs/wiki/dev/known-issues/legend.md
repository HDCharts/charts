---
title: Legend and Selection Issues
order: 9
---

# Legend and Selection Issues

Known issues and limits of the Legend and Selection page in Chart Internals.

## Need Verification

None yet.

## Confirmed

### Pie hides the share when the category and the title are both blank

A selected slice with a blank category falls back to the caller's title. With no title either, the
pie shows nothing on selection, not even the share.

Confirmed by the code: `PieChartFrame` draws the title row, share included, only when
`displayedTitle.isNotBlank()`.

Options:

- Show the share on its own when there is no category and no title.
