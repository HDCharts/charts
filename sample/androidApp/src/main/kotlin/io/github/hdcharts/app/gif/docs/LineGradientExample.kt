package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults

@Composable
fun ShowGradientLine(viewModel: LineViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val gradient = ChartGradients.vertical(colors = LineValueColors, span = GradientSpan.Plot)
    val line = LineChartDefaults.line(gradient = gradient)

    LineChart(
        data = chartData,
        title = chartData.series.single().name,
        style = LineChartDefaults.style(line = line),
    )
}
