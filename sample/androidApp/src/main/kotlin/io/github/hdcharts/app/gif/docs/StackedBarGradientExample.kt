package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.stackedbar.StackedBarChart
import io.github.hdcharts.stackedbar.StackedBarChartDefaults
import io.github.hdcharts.stackedbar.StackedBarChartStyle

@Composable
fun ShowGradientStackedBar(viewModel: StackedBarViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    StackedBarChart(
        data = chartData,
        title = viewModel.title,
        style = stackedBarFadeGradientStyle(),
    )
}

@Composable
fun stackedBarFadeGradientStyle(): StackedBarChartStyle =
    StackedBarChartDefaults.style(
        segments =
            StackedBarChartDefaults.segments(
                colors = StackedSeriesColors,
                gradient = ChartGradients.fade(endAlpha = 0.35f),
            ),
    )
