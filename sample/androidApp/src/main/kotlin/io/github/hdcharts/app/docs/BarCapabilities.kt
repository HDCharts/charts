package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartGradients

// BEGIN WIKI EXAMPLE: bar-fixed-range
@Composable
fun CompareCashFlow(data: ChartData) {
    BarChart(
        data = data,
        style =
            BarChartDefaults.style(
                range = BarChartDefaults.range(min = -50.0, max = 50.0),
            ),
    )
}
// END WIKI EXAMPLE: bar-fixed-range

// BEGIN WIKI EXAMPLE: bar-gradient
@Composable
fun DailyCashFlowGradient(data: ChartData) {
    val colors =
        listOf(
            Color(0xFF2563EB),
            Color(0xFF8B5CF6),
            Color(0xFFEC4899),
        )
    val gradient = ChartGradients.vertical(colors = colors)

    BarChart(
        data = data,
        style =
            BarChartDefaults.style(
                bars = BarChartDefaults.bars(gradient = gradient),
            ),
    )
}
// END WIKI EXAMPLE: bar-gradient
