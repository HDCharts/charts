package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.HistogramChartDefaults

private const val HISTOGRAM_CHART_TITLE = "Histogram"
private val HISTOGRAM_VALUES = listOf(4f, 8f, 11f, 9f, 6f, 3f)

@Composable
private fun HistogramChartPreviewContent() {
    HistogramChart(
        dataSet =
            HISTOGRAM_VALUES.toChartDataSet(
                title = HISTOGRAM_CHART_TITLE,
                labels = listOf("0-10", "10-20", "20-30", "30-40", "40-50", "50-60"),
            ),
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
            dataSet = listOf(-2f, 4f).toChartDataSet(title = HISTOGRAM_CHART_TITLE),
            style = HistogramChartDefaults.style(),
        )
    }
}
