package io.github.hdcharts.app.demo.bar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.sampleshared.data.BarSampleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class BarChartControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

data class BarChartUiState(
    val dataSet: ChartData,
    val controlsState: BarChartControlsState,
    val isPlaying: Boolean = false,
)

class BarChartViewModel(
    private val barSampleUseCase: BarSampleUseCase,
) : ViewModel() {
    companion object {
        const val MIN_SUPPORTED_POINTS = 10
        const val MAX_SUPPORTED_POINTS = 500
        const val MIN_SUPPORTED_VALUE = -500
        const val MAX_SUPPORTED_VALUE = 500
        private const val LIVE_UPDATE_INTERVAL_MS = 2000L
        private const val DEFAULT_POINTS = 120
        private const val DEFAULT_MIN_VALUE = -100
        private const val DEFAULT_MAX_VALUE = 100
    }

    private val initialControlsState =
        BarChartControlsState(
            points = DEFAULT_POINTS,
            minValue = DEFAULT_MIN_VALUE,
            maxValue = DEFAULT_MAX_VALUE,
        )

    private val _uiState =
        MutableStateFlow(
            BarChartUiState(
                dataSet =
                    barSampleUseCase.random(
                        points = initialControlsState.points,
                        range = initialControlsState.minValue..initialControlsState.maxValue,
                    ),
                controlsState = initialControlsState,
            ),
        )

    val uiState: StateFlow<BarChartUiState> = _uiState.asStateFlow()
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
            onRegenerated = { dataSet ->
                _uiState.update { state ->
                    state.copy(controlsState = updatedControls, dataSet = dataSet)
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
            onRegenerated = { dataSet ->
                _uiState.update { state ->
                    state.copy(controlsState = updatedControls, dataSet = dataSet)
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
        onRegenerated: ((ChartData) -> Unit)? = null,
    ) {
        val safePoints =
            points.coerceIn(
                minimumValue = MIN_SUPPORTED_POINTS,
                maximumValue = MAX_SUPPORTED_POINTS,
            )
        val safeRangeStart = range.first.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
        val safeRangeEnd = range.last.coerceIn(safeRangeStart, MAX_SUPPORTED_VALUE)
        val dataSet =
            barSampleUseCase.random(
                points = safePoints,
                range = safeRangeStart..safeRangeEnd,
            )
        if (onRegenerated != null) {
            onRegenerated(dataSet)
        } else {
            _uiState.update { it.copy(dataSet = dataSet) }
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
