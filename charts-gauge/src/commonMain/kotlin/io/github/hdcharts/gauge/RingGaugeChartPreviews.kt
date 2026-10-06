package io.github.hdcharts.gauge

import androidx.compose.runtime.Composable
import io.github.hdcharts.core.ChartsPreviewLightDark
import io.github.hdcharts.core.ChartsPreviewTheme
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.toChartData

private const val RING_GAUGE_CHART_TITLE = "Ring Gauge Chart"
private val RING_GAUGE_VALUES = listOf(25.0, 39.0)
private val RING_GAUGE_LABELS = listOf("Last year", "This year")

@ChartsPreviewLightDark
@Composable
private fun RingGaugeChartPreview() {
    ChartsPreviewTheme {
        RingGaugeChart(
            data = RING_GAUGE_VALUES.toChartData(categories = RING_GAUGE_LABELS),
            title = RING_GAUGE_CHART_TITLE,
            valueFormatter = ChartValueFormatters.suffix("%"),
        )
    }
}

@ChartsPreviewLightDark
@Composable
private fun RingGaugeChartErrorPreview() {
    ChartsPreviewTheme {
        RingGaugeChart(
            data = listOf(25.0, Double.NaN).toChartData(),
            title = RING_GAUGE_CHART_TITLE,
        )
    }
}
