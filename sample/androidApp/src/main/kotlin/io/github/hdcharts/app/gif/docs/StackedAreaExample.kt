package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.app.gif.DocsGifScenariosData
import io.github.hdcharts.app.gif.DocsGifScenariosData.POINTS
import io.github.hdcharts.charts.StackedAreaChart
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Monthly Active Subscribers by Plan"

class StackedAreaViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData {
        val plans =
            listOf(
                DocsGifScenariosData.jitteredRamped(
                    count = POINTS,
                    start = 240.0,
                    end = 880.0,
                    amplitude = 25.0,
                    seed = 71,
                ),
                DocsGifScenariosData.jitteredRamped(
                    count = POINTS,
                    start = 100.0,
                    end = 700.0,
                    amplitude = 30.0,
                    seed = 83,
                ),
                DocsGifScenariosData.jitteredBent(
                    count = POINTS,
                    firstStart = 60.0,
                    firstEnd = 660.0,
                    amplitude = 25.0,
                    seed = 89,
                    dip = 0.7,
                    dipStart = 9,
                    dipEnd = 12,
                    lastEnd = 360.0,
                ),
            )
        val (freePlan, standardPlan, premiumPlan) =
            DocsGifScenariosData.normalizeStacked(series = plans, targetMax = 1600.0)
        val items =
            listOf(
                "Free Plan" to freePlan,
                "Standard Plan" to standardPlan,
                "Premium Plan" to premiumPlan,
            )
        return items.toChartData(categories = DocsGifScenariosData.monthLabels(count = POINTS))
    }
}

@Composable
fun ShowStackedArea(viewModel: StackedAreaViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    StackedAreaChart(
        data = chartData,
        title = TITLE,
    )
}
