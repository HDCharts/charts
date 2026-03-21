package io.github.dautovicharis.charts.app.demo.histogram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dautovicharis.charts.demoshared.data.HistogramSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HistogramChartControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
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

    private val _dataSet =
        MutableStateFlow(
            histogramSampleUseCase.histogramDataSet(
                points = defaultPoints,
                range = defaultRange,
            ),
        )

    val dataSet: StateFlow<ChartDataSet> = _dataSet.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    private var liveUpdatesJob: Job? = null
    private val _controlsState =
        MutableStateFlow(
            HistogramChartControlsState(
                points = defaultPoints,
                minValue = defaultRange.first,
                maxValue = defaultRange.last,
            ),
        )
    val controlsState: StateFlow<HistogramChartControlsState> = _controlsState.asStateFlow()

    fun refresh() {
        regenerateDataSet()
    }

    fun regenerateDataSet(
        points: Int = _controlsState.value.points,
        range: IntRange = _controlsState.value.minValue.._controlsState.value.maxValue,
    ) {
        val safePoints =
            points.coerceIn(
                minimumValue = MIN_SUPPORTED_POINTS,
                maximumValue = MAX_SUPPORTED_POINTS,
            )
        val safeRangeStart = range.first.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
        val safeRangeEnd = range.last.coerceIn(safeRangeStart, MAX_SUPPORTED_VALUE)
        _dataSet.value =
            histogramSampleUseCase.histogramDataSet(
                points = safePoints,
                range = safeRangeStart..safeRangeEnd,
            )
    }

    fun updateDataPoints(points: Int) {
        val controls = _controlsState.value
        val safePoints = points.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS)
        if (safePoints == controls.points) return

        _controlsState.value = controls.copy(points = safePoints)
        regenerateDataSet(
            points = safePoints,
            range = controls.minValue..controls.maxValue,
        )
    }

    fun updateDataRange(
        minValue: Int,
        maxValue: Int,
    ) {
        val safeMin = minValue.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
        val safeMax = maxValue.coerceIn(safeMin, MAX_SUPPORTED_VALUE)
        val controls = _controlsState.value

        if (
            controls.minValue == safeMin &&
            controls.maxValue == safeMax
        ) {
            return
        }

        _controlsState.value =
            controls.copy(
                minValue = safeMin,
                maxValue = safeMax,
            )
        regenerateDataSet(
            points = controls.points,
            range = safeMin..safeMax,
        )
    }

    fun togglePlaying() {
        val shouldPlay = !_isPlaying.value
        _isPlaying.value = shouldPlay
        if (shouldPlay) {
            startLiveUpdates()
        } else {
            stopLiveUpdates()
        }
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
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
