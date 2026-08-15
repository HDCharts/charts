package io.github.dautovicharis.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults

private const val STACKED_AREA_CHART_TITLE = "Stacked Area Chart"
private val CATEGORIES = listOf("Jan", "Feb", "Mar")

private val STACKED_AREA_VALUES =
    listOf(
        "Item 1" to listOf(8261.68f, 8810.34f, 30000.57f),
        "Item 2" to listOf(8261.68f, 8810.34f, 30000.57f),
        "Item 3" to listOf(1500.87f, 2765.58f, 33245.81f),
        "Item 4" to listOf(5444.87f, 233.58f, 67544.81f),
    )

private val STACKED_AREA_INVALID_VALUES =
    listOf(
        "Item 1" to listOf(8261.68f, 8810.34f, 30000.57f),
        "Item 2" to listOf(8261.68f, 8810.34f),
        "Item 3" to listOf(1500.87f, 2765.58f, 33245.81f),
        "Item 4" to listOf(5444.87f, 233.58f),
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
            areaColors = colors,
            lineColors = colors,
            fillAlpha = 0.32f,
            bezier = false,
            chartContainerStyle = ChartContainerDefaults.style(width = 300.dp),
        )
    StackedAreaChart(
        dataSet =
            STACKED_AREA_VALUES.toMultiChartDataSet(
                title = STACKED_AREA_CHART_TITLE,
                categories = CATEGORIES,
            ),
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
            dataSet =
                STACKED_AREA_INVALID_VALUES.toMultiChartDataSet(
                    title = STACKED_AREA_CHART_TITLE,
                    categories = CATEGORIES.dropLast(1),
                ),
            style = StackedAreaChartDefaults.style(),
        )
    }
}
