package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Platform Readiness Score"
private val CATEGORIES =
    listOf(
        "Performance",
        "Reliability",
        "Usability",
        "Security",
        "Scalability",
        "Observability",
    )
private val VALUES = listOf(84.0, 79.0, 76.0, 88.0, 82.0, 74.0)

class RadarViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData =
        VALUES.toChartData(
            categories = CATEGORIES,
            seriesName = TITLE,
        )
}

@Composable
fun ShowRadar(viewModel: RadarViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    RadarChart(
        data = chartData,
        title = TITLE,
    )
}
