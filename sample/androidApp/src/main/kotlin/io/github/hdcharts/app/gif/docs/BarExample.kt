package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.app.gif.DocsGifScenariosData
import io.github.hdcharts.app.gif.DocsGifScenariosData.POINTS
import io.github.hdcharts.charts.BarChart
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Daily Net Cash Flow"

class BarViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData {
        val values =
            DocsGifScenariosData.rampedSeries(
                count = POINTS,
                start = 80.0,
                end = 280.0,
                amplitude = 18.0,
                seed = 7,
                targetMax = 280.0,
            )
        return values.toChartData(
            categories = DocsGifScenariosData.monthLabels(count = POINTS),
            seriesName = TITLE,
        )
    }
}

@Composable
fun ShowBar(viewModel: BarViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    BarChart(
        data = chartData,
        title = TITLE,
    )
}
