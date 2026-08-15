package io.github.dautovicharis.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.LineChartStyle

private const val LINE_CHART_TITLE = "Line Chart"
private const val VALUE_PREFIX = "$"

private val CATEGORIES = listOf("Jan", "Feb", "Mar")

private val MULTI_LINE_VALUES =
    listOf(
        "Item 1" to listOf(8261.68f, 8810.34f, 30000.57f),
        "Item 2" to listOf(8261.68f, 8810.34f, 30000.57f),
        "Item 3" to listOf(1500.87f, 2765.58f, 33245.81f),
        "Item 4" to listOf(5444.87f, 233.58f, 67544.81f),
    )

private val SIMPLE_LINE_VALUES = listOf(24f, 30f, 42f, 35f, 48f, 44f, 53f)

private val MULTI_LINE_INVALID_VALUES =
    listOf(
        "Item 1" to listOf(8261.68f, 8810.34f),
        "Item 2" to listOf(8261.68f, 8810.34f, 30000.57f),
        "Item 3" to listOf(1500.87f, 2765.58f),
        "Item 4" to listOf(5444.87f, 233.58f, 67544.81f),
    )

@Composable
private fun lineStyle(lineColors: List<Color>): LineChartStyle =
    LineChartDefaults.style(
        bezier = true,
        lineColors = lineColors,
        dragPointSize = 5f,
        pointVisible = true,
        chartContainerStyle = ChartContainerDefaults.style(width = 300.dp),
    )

@Composable
private fun LineChartPreviewContent() {
    LineChart(
        dataSet = SIMPLE_LINE_VALUES.toChartDataSet(title = LINE_CHART_TITLE),
        style = lineStyle(lineColors = listOf(MaterialTheme.colorScheme.primary)),
    )
}

@Composable
private fun MultiLineChartPreviewContent() {
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.error,
        )
    LineChart(
        dataSet =
            MULTI_LINE_VALUES.toMultiChartDataSet(
                title = LINE_CHART_TITLE,
                categories = CATEGORIES,
                prefix = VALUE_PREFIX,
            ),
        style = lineStyle(lineColors = colors),
    )
}

@ChartsPreviewLightDark
@Composable
private fun LineChartPreview() {
    ChartsPreviewTheme {
        LineChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun MultiLineChartPreview() {
    ChartsPreviewTheme {
        MultiLineChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun MultiLineChartErrorPreview() {
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
        )
    ChartsPreviewTheme {
        LineChart(
            dataSet =
                MULTI_LINE_INVALID_VALUES.toMultiChartDataSet(
                    title = LINE_CHART_TITLE,
                    categories = CATEGORIES.dropLast(1),
                    prefix = VALUE_PREFIX,
                ),
            style = lineStyle(lineColors = colors),
        )
    }
}
