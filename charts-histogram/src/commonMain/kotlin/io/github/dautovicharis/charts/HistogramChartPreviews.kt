package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.HistogramChartDefaults

private const val HISTOGRAM_CHART_TITLE = "Histogram"

@Composable
private fun HistogramChartPreviewContent() {
    HistogramChart(
        data =
            listOf(4.0, 8.0, 11.0, 9.0, 6.0, 3.0).toChartData(
                categories = listOf("0-10", "10-20", "20-30", "30-40", "40-50", "50-60"),
            ),
        title = HISTOGRAM_CHART_TITLE,
        style = HistogramChartDefaults.style(),
    )
}

@ChartsPreviewLightDark
@Composable
private fun HistogramChartPreview() {
    ChartsPreviewTheme {
        HistogramChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun HistogramChartErrorPreview() {
    ChartsPreviewTheme {
        HistogramChart(
            data = listOf(-2.0, 4.0).toChartData(),
            style = HistogramChartDefaults.style(),
        )
    }
}
