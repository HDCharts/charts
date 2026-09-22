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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Daily Support Tickets"

class LineViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData {
        val values =
            DocsGifScenariosData.rampedSeries(
                count = POINTS,
                start = 30.0,
                end = 360.0,
                amplitude = 45.0,
                seed = 19,
                targetMax = 360.0,
            )
        return values.toChartData(
            categories = DocsGifScenariosData.weekLabels(count = POINTS),
            seriesName = TITLE,
        )
    }
}

@Composable
fun ShowLine(viewModel: LineViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    LineChart(
        data = chartData,
        title = TITLE,
    )
}
