package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.stackedarea.StackedAreaChart
import io.github.hdcharts.stackedarea.StackedAreaChartDefaults
import io.github.hdcharts.stackedarea.StackedAreaChartStyle

@Composable
fun ShowGradientStackedArea(viewModel: StackedAreaViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    StackedAreaChart(
        data = chartData,
        title = viewModel.title,
        style = stackedAreaFadeGradientStyle(),
    )
}

@Composable
fun stackedAreaFadeGradientStyle(): StackedAreaChartStyle =
    StackedAreaChartDefaults.style(
        fill =
            StackedAreaChartDefaults.fill(
                colors = StackedSeriesColors,
                gradient = ChartGradients.fade(endAlpha = 0.35f),
            ),
    )
