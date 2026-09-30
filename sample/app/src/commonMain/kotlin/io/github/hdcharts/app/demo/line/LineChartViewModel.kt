package io.github.hdcharts.app.demo.line

import androidx.lifecycle.ViewModel
import io.github.hdcharts.app.demo.timeline.LiveTimelineDefaults
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.LiveLatencyTimelineUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LineChartDataControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

data class LineChartUiState(
    val dataSet: ChartData,
    val dataControlsState: LineChartDataControlsState,
)

class LineChartViewModel(
    private val liveLatencyTimelineUseCase: LiveLatencyTimelineUseCase,
) : ViewModel() {
    companion object {
        const val MIN_SUPPORTED_POINTS = 10
        const val MAX_SUPPORTED_POINTS = 500
        const val MIN_SUPPORTED_VALUE = 50
        const val MAX_SUPPORTED_VALUE = 400
        private const val DEFAULT_MIN_VALUE = 90
        private const val DEFAULT_MAX_VALUE = 220
    }

    private val initialDataControlsState =
        LineChartDataControlsState(
            points = LiveTimelineDefaults.DEFAULT_WINDOW_SIZE.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS),
            minValue = DEFAULT_MIN_VALUE,
            maxValue = DEFAULT_MAX_VALUE,
        )

    private val _uiState =
        MutableStateFlow(
            LineChartUiState(
                dataSet = buildGeneratedDataSet(initialDataControlsState),
                dataControlsState = initialDataControlsState,
            ),
        )
    val uiState: StateFlow<LineChartUiState> = _uiState.asStateFlow()

    fun refresh() {
        val controls = _uiState.value.dataControlsState
        val dataSet = buildGeneratedDataSet(controls)
        _uiState.update { state ->
            state.copy(dataSet = dataSet)
        }
    }

    fun updateDataPoints(points: Int) {
        val controls = _uiState.value.dataControlsState
        val safePoints = points.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS)
        if (safePoints == controls.points) return

        val updatedControls = controls.copy(points = safePoints)
        val generatedDataSet = buildGeneratedDataSet(updatedControls)
        _uiState.update { state ->
            state.copy(
                dataControlsState = updatedControls,
                dataSet = generatedDataSet,
            )
        }
    }

    fun updateDataRange(
        minValue: Int,
        maxValue: Int,
    ) {
        val safeMin = minValue.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
        val safeMax = maxValue.coerceIn(safeMin, MAX_SUPPORTED_VALUE)
        val controls = _uiState.value.dataControlsState
        if (controls.minValue == safeMin && controls.maxValue == safeMax) return

        val updatedControls = controls.copy(minValue = safeMin, maxValue = safeMax)
        val generatedDataSet = buildGeneratedDataSet(updatedControls)
        _uiState.update { state ->
            state.copy(
                dataControlsState = updatedControls,
                dataSet = generatedDataSet,
            )
        }
    }

    private fun buildGeneratedDataSet(controls: LineChartDataControlsState): ChartData {
        val baseWindow = liveLatencyTimelineUseCase.createSingleWindow(windowSize = controls.points)
        val baseDataSet = liveLatencyTimelineUseCase.toSingleDataSet(baseWindow)
        val basePoints = baseDataSet.series.first().values
        val labels = baseDataSet.categories.toList()
        val safeMin = controls.minValue.toDouble()
        val safeMax = controls.maxValue.toDouble().coerceAtLeast(safeMin + 1.0)
        val sourceMin = basePoints.minOrNull() ?: 0.0
        val sourceMax = basePoints.maxOrNull() ?: sourceMin
        val sourceRange = sourceMax - sourceMin

        val normalizedValues =
            if (sourceRange == 0.0) {
                List(basePoints.size) { safeMin }
            } else {
                basePoints.map { point ->
                    val normalized = ((point - sourceMin) / sourceRange).coerceIn(0.0, 1.0)
                    safeMin + normalized * (safeMax - safeMin)
                }
            }

        return normalizedValues.toChartData(categories = labels, seriesName = baseDataSet.series.first().name)
    }
}
