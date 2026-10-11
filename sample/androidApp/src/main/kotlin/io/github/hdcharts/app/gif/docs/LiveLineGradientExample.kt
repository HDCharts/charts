package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.line.LiveLineChart
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ShowGradientLiveLine(viewModel: LiveSensorReadingViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val gradient = ChartGradients.horizontal(colors = GradientColors)
    val line = LineChartDefaults.line(gradient = gradient)

    LiveLineChart(
        data = chartData,
        title = chartData.series.single().name,
        style = LineChartDefaults.style(line = line),
        shiftDuration = 120.milliseconds,
        animateOnStart = false,
    )
}
