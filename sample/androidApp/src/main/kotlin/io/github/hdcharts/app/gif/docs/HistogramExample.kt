package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.app.gif.DocsGifScenariosData
import io.github.hdcharts.app.gif.DocsGifScenariosData.POINTS
import io.github.hdcharts.charts.HistogramChart
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Request Duration Distribution"

class HistogramViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData {
        val values =
            DocsGifScenariosData.bentSeries(
                count = POINTS,
                firstStart = 4.0,
                firstEnd = 80.0,
                amplitude = 4.0,
                seed = 31,
                targetMax = 80.0,
                dip = 0.75,
                dipStart = 10,
                dipEnd = 18,
                lastEnd = 36.0,
            )
        return values.toChartData(
            categories = DocsGifScenariosData.msBuckets(count = POINTS, stepMs = 25),
            seriesName = TITLE,
        )
    }
}

@Composable
fun ShowHistogram(viewModel: HistogramViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    HistogramChart(
        data = chartData,
        title = TITLE,
    )
}
