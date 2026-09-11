package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.BarChartDefaults

private const val BAR_CHART_TITLE = "Daily Net Cash Flow"

@Composable
private fun BarChartPreviewContent() {
    BarChart(
        data = listOf(18.0, 32.0, 26.0, 48.0, 36.0, 28.0, 54.0).toChartData(),
        title = BAR_CHART_TITLE,
        style = BarChartDefaults.style(),
    )
}

@ChartsPreviewLightDark
@Composable
private fun BarChartPreview() {
    ChartsPreviewTheme {
        BarChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun BarChartErrorPreview() {
    ChartsPreviewTheme {
        BarChart(
            data = listOf(42.0).toChartData(),
            style = BarChartDefaults.style(),
        )
    }
}
