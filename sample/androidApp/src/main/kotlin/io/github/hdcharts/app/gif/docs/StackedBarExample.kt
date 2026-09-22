package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.app.gif.DocsGifScenariosData
import io.github.hdcharts.app.gif.DocsGifScenariosData.POINTS
import io.github.hdcharts.charts.StackedBarChart
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Quarterly Revenue by Channel"

class StackedBarViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData {
        val channels =
            listOf(
                DocsGifScenariosData.jitteredRamped(
                    count = POINTS,
                    start = 280.0,
                    end = 1080.0,
                    amplitude = 18.0,
                    seed = 41,
                ),
                DocsGifScenariosData.jitteredRamped(
                    count = POINTS,
                    start = 480.0,
                    end = 1400.0,
                    amplitude = 25.0,
                    seed = 53,
                ),
                DocsGifScenariosData.jitteredBent(
                    count = POINTS,
                    firstStart = 360.0,
                    firstEnd = 760.0,
                    amplitude = 30.0,
                    seed = 67,
                    dip = 0.55,
                    dipStart = 9,
                    dipEnd = 12,
                    lastEnd = 720.0,
                ),
            )
        val (online, retail, wholesale) =
            DocsGifScenariosData.normalizeStacked(series = channels, targetMax = 3200.0)
        val items = listOf("Online" to online, "Retail" to retail, "Wholesale" to wholesale)
        return items.toChartData(categories = DocsGifScenariosData.quarterLabels(count = POINTS))
    }
}

@Composable
fun ShowStackedBar(viewModel: StackedBarViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    StackedBarChart(
        data = chartData,
        title = TITLE,
    )
}
