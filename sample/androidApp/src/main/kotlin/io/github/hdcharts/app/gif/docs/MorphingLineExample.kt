package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.LineChartDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

private const val TITLE = "Daily Support Tickets"
private const val AXIS_MIN = 0.0
private const val AXIS_MAX = 80.0

// Leaves the first morph room to settle within the recording window; a morph itself runs 1700ms.
private val STEP = 1400.milliseconds
private val CATEGORIES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// Three consecutive weeks of the same metric, few enough points that each one is trackable as it moves.
private val WEEKS =
    listOf(
        listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0),
        listOf(46.0, 52.0, 68.0, 74.0, 61.0, 50.0, 44.0),
        listOf(58.0, 55.0, 49.0, 43.0, 39.0, 31.0, 28.0),
    )

class MorphingLineViewModel(
    private val tickerScope: CoroutineScope? = null,
) : ViewModel() {
    private val scope get() = tickerScope ?: viewModelScope

    val values: StateFlow<List<Double>> =
        flow {
            var index = 0
            while (true) {
                delay(STEP)
                index = (index + 1) % WEEKS.size
                emit(WEEKS[index])
            }
        }.stateIn(scope, SharingStarted.Eagerly, WEEKS.first())
}

@Composable
fun ShowMorphingLine(viewModel: MorphingLineViewModel = viewModel()) {
    val values by viewModel.values.collectAsStateWithLifecycle()

    LineChart(
        data = values.toChartData(categories = CATEGORIES, seriesName = TITLE),
        title = TITLE,
        style =
            LineChartDefaults.style(
                range = LineChartDefaults.range(min = AXIS_MIN, max = AXIS_MAX),
            ),
        animateOnStart = false,
        renderMode = LineChartRenderMode.Morph,
    )
}
