package io.github.dautovicharis.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.style.StackedBarChartDefaults

private const val STACKED_BAR_CHART_TITLE = "Stacked Bar Chart"
private val CATEGORIES = listOf("Jan", "Feb", "Mar")

private val STACKED_VALUES =
    listOf(
        "Item 1" to listOf(8261.68, 8810.34, 30000.57),
        "Item 2" to listOf(8261.68, 8810.34, 30000.57),
        "Item 3" to listOf(1500.87, 2765.58, 33245.81),
        "Item 4" to listOf(5444.87, 233.58, 67544.81),
    )

private val STACKED_INVALID_VALUES =
    listOf(
        "Item 1" to listOf(8261.68, 8810.34, 30000.57),
        "Item 2" to listOf(8261.68, 8810.34),
        "Item 3" to listOf(1500.87, 2765.58, 33245.81),
        "Item 4" to listOf(5444.87, 233.58),
    )

@Composable
private fun StackedBarChartPreviewContent() {
    StackedBarChart(
        data = stackedData(STACKED_VALUES),
        title = STACKED_BAR_CHART_TITLE,
        style = StackedBarChartDefaults.style(),
    )
}

@ChartsPreviewLightDark
@Composable
private fun StackedBarChartPreview() {
    ChartsPreviewTheme {
        StackedBarChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun StackedBarChartErrorPreview() {
    val style =
        StackedBarChartDefaults.style(
            segments = StackedBarChartDefaults.segments(colors = listOf(MaterialTheme.colorScheme.primary)),
            layout = StackedBarChartDefaults.layout(space = 8.dp),
        )
    ChartsPreviewTheme {
        StackedBarChart(
            data = stackedData(STACKED_INVALID_VALUES),
            title = STACKED_BAR_CHART_TITLE,
            style = style,
        )
    }
}

private fun stackedData(rows: List<Pair<String, List<Double>>>) =
    chartDataOf(
        categories = rows.map { (barLabel, _) -> barLabel },
        *List(CATEGORIES.size) { segmentIndex ->
            ChartSeries(
                name = CATEGORIES[segmentIndex],
                values = rows.map { it.second.getOrNull(segmentIndex)?.toDouble() ?: Double.NaN },
            )
        }.toTypedArray(),
    )
