package io.github.dautovicharis.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults

private const val STACKED_AREA_CHART_TITLE = "Stacked Area Chart"
private val CATEGORIES = listOf("Jan", "Feb", "Mar")

private val STACKED_AREA_VALUES =
    listOf(
        ChartSeries(name = "Item 1", values = listOf(8261.68, 8810.34, 30000.57)),
        ChartSeries(name = "Item 2", values = listOf(8261.68, 8810.34, 30000.57)),
        ChartSeries(name = "Item 3", values = listOf(1500.87, 2765.58, 33245.81)),
        ChartSeries(name = "Item 4", values = listOf(5444.87, 233.58, 67544.81)),
    )

private val STACKED_AREA_INVALID_VALUES =
    listOf(
        ChartSeries(name = "Item 1", values = listOf(8261.68, 8810.34, 30000.57)),
        ChartSeries(name = "Item 2", values = listOf(8261.68, 8810.34)),
        ChartSeries(name = "Item 3", values = listOf(1500.87, 2765.58, 33245.81)),
        ChartSeries(name = "Item 4", values = listOf(5444.87, 233.58)),
    )

@Composable
private fun StackedAreaChartPreviewContent() {
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        )
    val style =
        StackedAreaChartDefaults.style(
            fill = StackedAreaChartDefaults.fill(colors = colors, alpha = 0.32f),
            boundary = StackedAreaChartDefaults.boundary(colors = colors, bezier = false),
            chartContainerStyle = ChartContainerDefaults.style(width = 300.dp),
        )
    StackedAreaChart(
        data = chartDataOf(categories = CATEGORIES, *STACKED_AREA_VALUES.toTypedArray()),
        title = STACKED_AREA_CHART_TITLE,
        style = style,
    )
}

@ChartsPreviewLightDark
@Composable
private fun StackedAreaChartPreview() {
    ChartsPreviewTheme {
        StackedAreaChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun StackedAreaChartErrorPreview() {
    ChartsPreviewTheme {
        StackedAreaChart(
            data =
                chartDataOf(
                    categories = CATEGORIES.dropLast(1),
                    *STACKED_AREA_INVALID_VALUES.toTypedArray(),
                ),
            title = STACKED_AREA_CHART_TITLE,
            style = StackedAreaChartDefaults.style(),
        )
    }
}
