package io.github.hdcharts.app.demo.histogram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.app.ui.composable.ChartPreset
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.sampleshared.data.HistogramSampleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HistogramChartControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

data class HistogramChartUiState(
    val dataSet: ChartData,
    val controlsState: HistogramChartControlsState,
    val preset: ChartPreset = ChartPreset.Default,
    val isPlaying: Boolean = false,
)

class HistogramChartViewModel(
    private val histogramSampleUseCase: HistogramSampleUseCase,
) : ViewModel() {
    companion object {
        const val MIN_SUPPORTED_POINTS = 10
        const val MAX_SUPPORTED_POINTS = 500
        const val MIN_SUPPORTED_VALUE = 0
        const val MAX_SUPPORTED_VALUE = 500
        private const val LIVE_UPDATE_INTERVAL_MS = 2000L
    }

    private val defaultPoints =
        histogramSampleUseCase
            .histogramDefaultPoints()
            .coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS)
    private val defaultRange =
        histogramSampleUseCase
            .histogramDefaultRange()
            .let { range ->
                val safeStart = range.first.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
                val safeEnd = range.last.coerceIn(safeStart, MAX_SUPPORTED_VALUE)
                safeStart..safeEnd
            }

    private val initialControlsState =
        HistogramChartControlsState(
            points = defaultPoints,
            minValue = defaultRange.first,
            maxValue = defaultRange.last,
        )

    private val _uiState =
        MutableStateFlow(
            HistogramChartUiState(
                dataSet =
                    histogramSampleUseCase.histogramDataSet(
                        points = initialControlsState.points,
                        range = defaultRange,
                    ),
                controlsState = initialControlsState,
            ),
        )

    val uiState: StateFlow<HistogramChartUiState> = _uiState.asStateFlow()
    private var liveUpdatesJob: Job? = null

    fun onPresetSelected(preset: ChartPreset) {
        if (preset == _uiState.value.preset) return
        _uiState.update { it.copy(preset = preset) }
    }

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
            histogramSampleUseCase.histogramDataSet(
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
