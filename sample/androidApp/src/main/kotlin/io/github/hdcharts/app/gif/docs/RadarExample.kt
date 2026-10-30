package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.radar.RadarChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Platform Readiness Score"

private data class ReadinessMetric(
    val name: String,
    val score: Double,
)

private val METRICS =
    listOf(
        ReadinessMetric("Performance", 84.0),
        ReadinessMetric("Reliability", 79.0),
        ReadinessMetric("Usability", 76.0),
        ReadinessMetric("Security", 88.0),
        ReadinessMetric("Scalability", 82.0),
        ReadinessMetric("Observability", 74.0),
    )

class RadarViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData =
        METRICS.toChartData(
            value = { it.score },
            category = { it.name },
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
