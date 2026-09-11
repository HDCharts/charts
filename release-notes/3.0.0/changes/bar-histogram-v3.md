# PR Changeset

- type: `feat`
- module: `charts-core`
- release_note: `Migrated BarChart and HistogramChart to the shared v3 API: new `BarChart(data: ChartData, ...)` and `HistogramChart(data: ChartData, ...)` composables with top-level `Modifier`, optional `title`, hoisted `ChartSelection`, and grouped `BarChartStyle`/`HistogramChartStyle` blocks (bars, range, grid, axis, selectionLine). Legacy `ChartDataSet` overloads and the flat BarChartStyle are removed; only Double values are accepted, categories are explicit, and histogram defaults enforce contiguous bins with a zero baseline.`
