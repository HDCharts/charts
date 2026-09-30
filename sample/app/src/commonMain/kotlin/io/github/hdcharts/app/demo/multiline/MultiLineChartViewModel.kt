package io.github.hdcharts.app.demo.multiline

import androidx.lifecycle.ViewModel
import io.github.hdcharts.app.demo.timeline.LiveTimelineDefaults
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.LiveLatencyTimelineUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MultiLineChartState(
    val dataSet: ChartData,
    val title: String,
)

data class MultiLineChartDataControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

data class MultiLineChartUiState(
    val dataSet: MultiLineChartState,
    val dataControlsState: MultiLineChartDataControlsState,
)

class MultiLineChartViewModel(
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
        MultiLineChartDataControlsState(
            points = LiveTimelineDefaults.DEFAULT_WINDOW_SIZE.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS),
            minValue = DEFAULT_MIN_VALUE,
            maxValue = DEFAULT_MAX_VALUE,
        )

    private val _uiState =
        MutableStateFlow(
            MultiLineChartUiState(
                dataSet = buildGeneratedDataSet(initialDataControlsState),
                dataControlsState = initialDataControlsState,
            ),
        )
    val uiState: StateFlow<MultiLineChartUiState> = _uiState.asStateFlow()

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

    private fun buildGeneratedDataSet(controls: MultiLineChartDataControlsState): MultiLineChartState {
        val baseWindow = liveLatencyTimelineUseCase.createMultiWindow(windowSize = controls.points)
        val labels = baseWindow.labels.toList()
        val safeMin = controls.minValue.toDouble()
        val safeMax = controls.maxValue.toDouble().coerceAtLeast(safeMin + 1.0)
        val sourceValues = baseWindow.p50Values + baseWindow.p95Values
        val sourceMin = sourceValues.minOrNull() ?: 0.0
        val sourceMax = sourceValues.maxOrNull() ?: sourceMin
        val sourceRange = sourceMax - sourceMin

        fun normalize(values: List<Double>): List<Double> =
            if (sourceRange == 0.0) {
                List(values.size) { safeMin }
            } else {
                values.map { value ->
                    val normalized = ((value - sourceMin) / sourceRange).coerceIn(0.0, 1.0)
                    safeMin + normalized * (safeMax - safeMin)
                }
            }

        val p50Values = normalize(baseWindow.p50Values)
        val p95Values = normalize(baseWindow.p95Values)
        val seriesKeys = liveLatencyTimelineUseCase.multiSeriesKeys
        val multiDataSet =
            listOf(
                seriesKeys.getOrElse(0) { "P50 Latency" } to p50Values,
                seriesKeys.getOrElse(1) { "P95 Latency" } to p95Values,
            ).toChartData(categories = labels)

        return MultiLineChartState(
            dataSet = multiDataSet,
            title = liveLatencyTimelineUseCase.multiSeriesTitle,
        )
    }
}
