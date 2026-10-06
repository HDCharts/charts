package io.github.hdcharts.app.demo.stackedarea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.sampleshared.data.StackedAreaSampleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val LIVE_UPDATE_INTERVAL_MS = 2000L

data class StackedAreaChartControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

data class StackedAreaChartState(
    val data: ChartData,
    val title: String = "",
)

data class StackedAreaChartUiState(
    val chart: StackedAreaChartState,
    val controlsState: StackedAreaChartControlsState,
    val isPlaying: Boolean = false,
)

class StackedAreaChartViewModel(
    private val stackedAreaSampleUseCase: StackedAreaSampleUseCase,
) : ViewModel() {
    companion object {
        const val MIN_SUPPORTED_POINTS = 2
        const val MAX_SUPPORTED_POINTS = 500
        const val MIN_SUPPORTED_VALUE = 0
        const val MAX_SUPPORTED_VALUE = 2000
        private const val DEFAULT_POINTS = 24
        private const val DEFAULT_MIN_VALUE = 100
        private const val DEFAULT_MAX_VALUE = 1000
    }

    private val initialSample = stackedAreaSampleUseCase.deterministic(points = DEFAULT_POINTS)

    private val initialControlsState =
        StackedAreaChartControlsState(
            points = DEFAULT_POINTS,
            minValue = DEFAULT_MIN_VALUE,
            maxValue = DEFAULT_MAX_VALUE,
        )

    private val _uiState =
        MutableStateFlow(
            StackedAreaChartUiState(
                chart =
                    StackedAreaChartState(
                        data = initialSample.data,
                        title = initialSample.title,
                    ),
                controlsState = initialControlsState,
            ),
        )

    val uiState: StateFlow<StackedAreaChartUiState> = _uiState.asStateFlow()
    private var liveUpdatesJob: Job? = null

    fun togglePlaying() {
        setPlaying(!_uiState.value.isPlaying)
    }

    fun refresh() {
        regenerateDataSet()
    }

    fun updateDataPoints(points: Int) {
        val controls = _uiState.value.controlsState
        val safePoints = points.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS)
        if (safePoints == controls.points) return

        val updatedControls = controls.copy(points = safePoints)
        regenerateDataSet(
            points = updatedControls.points,
            range = updatedControls.minValue..updatedControls.maxValue,
            onRegenerated = { chart ->
                _uiState.update { state ->
                    state.copy(controlsState = updatedControls, chart = chart)
                }
            },
        )
    }

    fun updateDataRange(
        minValue: Int,
        maxValue: Int,
    ) {
        val safeMin = minValue.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
        val safeMax = maxValue.coerceIn(safeMin, MAX_SUPPORTED_VALUE)
        val controls = _uiState.value.controlsState

        if (
            controls.minValue == safeMin &&
            controls.maxValue == safeMax
        ) {
            return
        }

        val updatedControls = controls.copy(minValue = safeMin, maxValue = safeMax)
        regenerateDataSet(
            points = updatedControls.points,
            range = safeMin..safeMax,
            onRegenerated = { chart ->
                _uiState.update { state ->
                    state.copy(controlsState = updatedControls, chart = chart)
                }
            },
        )
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
    }

    private fun regenerateDataSet(
        points: Int = _uiState.value.controlsState.points,
        range: IntRange = _uiState.value.controlsState.minValue.._uiState.value.controlsState.maxValue,
        onRegenerated: ((StackedAreaChartState) -> Unit)? = null,
    ) {
        val safePoints =
            points.coerceIn(
                minimumValue = MIN_SUPPORTED_POINTS,
                maximumValue = MAX_SUPPORTED_POINTS,
            )
        val safeRangeStart = range.first.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
        val safeRangeEnd = range.last.coerceIn(safeRangeStart, MAX_SUPPORTED_VALUE)
        val sample =
            stackedAreaSampleUseCase.random(
                points = safePoints,
                range = safeRangeStart..safeRangeEnd,
            )
        val chart =
            StackedAreaChartState(
                data = sample.data,
                title = sample.title,
            )
        if (onRegenerated != null) {
            onRegenerated(chart)
        } else {
            _uiState.update { it.copy(chart = chart) }
        }
    }

    private fun setPlaying(playing: Boolean) {
        if (_uiState.value.isPlaying == playing) return
        _uiState.update { it.copy(isPlaying = playing) }
        if (playing) {
            startLiveUpdates()
        } else {
            stopLiveUpdates()
        }
    }

    private fun startLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob =
            viewModelScope.launch {
                refresh()
                while (isActive) {
                    delay(LIVE_UPDATE_INTERVAL_MS)
                    refresh()
                }
            }
    }

    private fun stopLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob = null
    }
}
