package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.histogram.HistogramChart

// BEGIN WIKI EXAMPLE: histogram-bin-width
@Composable
fun ShowWiderRequestBins(data: ChartData) {
    HistogramChart(
        data = data,
        style =
            HistogramChartDefaults.style(
                bars = HistogramChartDefaults.bars(minBarWidth = 16.dp),
            ),
    )
}
// END WIKI EXAMPLE: histogram-bin-width

// BEGIN WIKI EXAMPLE: histogram-gradient
@Composable
fun RequestDurationGradient(data: ChartData) {
    val colors =
        listOf(
            Color(0xFF0F766E),
            Color(0xFF84CC16),
            Color(0xFFF59E0B),
        )
    val gradient = ChartGradients.horizontal(colors = colors, span = GradientSpan.Plot)

    HistogramChart(
        data = data,
        style =
            HistogramChartDefaults.style(
                bars = HistogramChartDefaults.bars(gradient = gradient),
            ),
    )
}
// END WIKI EXAMPLE: histogram-gradient
