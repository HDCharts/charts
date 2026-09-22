package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

private const val TITLE = "Live Sensor Reading"
private const val WINDOW = 120
private const val PULSE_PERIOD = 15
private val INTERVAL = 150.milliseconds

class LiveSensorReadingViewModel(
    private val tickerScope: CoroutineScope? = null,
) : ViewModel() {
    private val scope get() = tickerScope ?: viewModelScope
    private val firstWindow = initialWindow()

    val chartData: StateFlow<ChartData> =
        flow {
            var current = firstWindow
            var tick = WINDOW
            while (true) {
                delay(INTERVAL)
                current = windowAfter(current = current, tick = tick)
                tick++
                emit(current)
            }
        }.stateIn(scope, SharingStarted.Eagerly, firstWindow)

    private fun initialWindow(): ChartData {
        val ticks = 0 until WINDOW
        return ticks.map(::reading).toChartData(
            categories = ticks.map { (it + 1).toString() },
            seriesName = TITLE,
        )
    }

    private fun windowAfter(
        current: ChartData,
        tick: Int,
    ): ChartData {
        val series = current.series.single()
        val values = series.values.drop(1) + reading(index = tick)
        val labels = current.categories.drop(1) + (tick + 1).toString()
        return values.toChartData(categories = labels, seriesName = TITLE)
    }

    // Heartbeat-style pulse: a repeating P-QRS-T shape resting on a flat baseline.
    private fun reading(index: Int): Double {
        val pulse =
            when (index % PULSE_PERIOD) {
                0 -> 6.0
                1 -> -8.0
                2 -> 62.0
                3 -> -14.0
                4 -> 12.0
                else -> 2.0
            }
        return (18.0 + pulse).coerceAtLeast(0.0)
    }
}

@Composable
fun ShowLiveLine(viewModel: LiveSensorReadingViewModel = viewModel()) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    LineChart(
        data = chartData,
        title = TITLE,
        animateOnStart = false,
        renderMode = LineChartRenderMode.Timeline,
        animationDuration = 120.milliseconds,
    )
}
