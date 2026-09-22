package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.app.gif.DocsGifScenariosData
import io.github.hdcharts.app.gif.DocsGifScenariosData.POINTS
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.LineChartDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Daily Support Tickets"
private const val AXIS_MIN = 0.0
private const val AXIS_MAX = 100.0

class LineWithRangeViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    // Narrow band around a mid value; a derived y-axis would exaggerate these swings.
    private fun buildChartData(): ChartData {
        val values =
            DocsGifScenariosData.jitter(
                values = List(POINTS) { 62.0 },
                amplitude = 6.0,
                seed = 29,
            )
        return values.toChartData(
            categories = DocsGifScenariosData.weekLabels(count = POINTS),
            seriesName = TITLE,
        )
    }
}

@Composable
fun ShowLineWithRange(viewModel: LineWithRangeViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    LineChart(
        data = chartData,
        title = TITLE,
        style =
            LineChartDefaults.style(
                range = LineChartDefaults.range(min = AXIS_MIN, max = AXIS_MAX),
            ),
    )
}
