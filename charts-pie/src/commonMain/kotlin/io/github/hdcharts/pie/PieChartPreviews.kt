package io.github.hdcharts.pie

import androidx.compose.runtime.Composable
import io.github.hdcharts.core.ChartsPreviewLightDark
import io.github.hdcharts.core.ChartsPreviewTheme
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.ChartContainerDefaults

private const val PIE_CHART_TITLE = "Pie Chart"
private val PIE_VALUES = listOf(32.0, 21.0, 24.0, 14.0, 9.0)
private val PIE_LABELS = listOf("North", "East", "South", "West", "Other")

@Composable
private fun PieChartPreviewContent() {
    val style: PieChartStyle =
        PieChartDefaults.style(
            chartContainerStyle = ChartContainerDefaults.style(),
        )
    PieChart(
        data = PIE_VALUES.toChartData(categories = PIE_LABELS),
        style = style,
        title = PIE_CHART_TITLE,
    )
}

@ChartsPreviewLightDark
@Composable
private fun PieChartPreview() {
    ChartsPreviewTheme {
        PieChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun PieChartErrorPreview() {
    ChartsPreviewTheme {
        PieChart(
            data = listOf(42.0).toChartData(categories = listOf("Slice 1")),
            style = PieChartDefaults.style(),
            title = PIE_CHART_TITLE,
        )
    }
}
