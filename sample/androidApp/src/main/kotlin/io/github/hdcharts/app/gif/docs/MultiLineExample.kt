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
import io.github.hdcharts.charts.model.ChartValueFormatters
import io.github.hdcharts.charts.model.toChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Weekly Revenue by Channel"

class MultiLineViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData {
        val items =
            listOf(
                "Web Store" to
                    DocsGifScenariosData.rampedSeries(
                        count = POINTS,
                        start = 180.0,
                        end = 720.0,
                        amplitude = 25.0,
                        seed = 13,
                        targetMax = 720.0,
                        dip = 0.85,
                        dipStart = 7,
                        dipEnd = 8,
                    ),
                "Mobile App" to
                    DocsGifScenariosData.rampedSeries(
                        count = POINTS,
                        start = 120.0,
                        end = 580.0,
                        amplitude = 20.0,
                        seed = 17,
                        targetMax = 580.0,
                        dip = 0.92,
                        dipStart = 6,
                        dipEnd = 7,
                    ),
                "Partner Sales" to
                    DocsGifScenariosData.rampedSeries(
                        count = POINTS,
                        start = 60.0,
                        end = 340.0,
                        amplitude = 12.0,
                        seed = 23,
                        targetMax = 340.0,
                        dip = 1.0,
                        dipStart = 9,
                        dipEnd = 12,
                    ),
            )
        return items.toChartData(categories = DocsGifScenariosData.weekLabels(count = POINTS))
    }
}

@Composable
fun ShowMultiLine(viewModel: MultiLineViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    LineChart(
        data = chartData,
        title = TITLE,
        valueFormatter = ChartValueFormatters.prefix("$"),
    )
}
