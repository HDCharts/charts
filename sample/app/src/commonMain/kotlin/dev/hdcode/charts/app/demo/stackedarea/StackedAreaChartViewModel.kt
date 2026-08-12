package dev.hdcode.charts.app.demo.stackedarea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hdcode.charts.sampleshared.data.StackedAreaSampleUseCase
import io.github.dautovicharis.charts.model.MultiChartDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val LIVE_UPDATE_INTERVAL_MS = 2000L

data class StackedAreaChartControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

data class StackedAreaChartState(
    val dataSet: MultiChartDataSet,
    val seriesKeys: List<String> = emptyList(),
)

class StackedAreaChartViewModel(
    private val stackedAreaSampleUseCase: StackedAreaSampleUseCase,
) : ViewModel() {
    companion object {
        const val MIN_SUPPORTED_POINTS = 2
        const val MAX_SUPPORTED_POINTS = 500
        const val MIN_SUPPORTED_VALUE = 0
        const val MAX_SUPPORTED_VALUE = 2000
    }

    private val refreshRange = stackedAreaSampleUseCase.stackedAreaRefreshRange()
    private val initialSample = stackedAreaSampleUseCase.initialStackedAreaSample()
    private val defaultPoints =
        initialSample
            .dataSet
            .data
            .items
            .firstOrNull()
            ?.item
            ?.points
            ?.size
            ?.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS) ?: MIN_SUPPORTED_POINTS
    private val defaultRange =
        refreshRange.let { range ->
            val safeStart = range.first.coerceIn(MIN_SUPPORTED_VALUE, MAX_SUPPORTED_VALUE)
            val safeEnd = range.last.coerceIn(safeStart, MAX_SUPPORTED_VALUE)
            safeStart..safeEnd
        }

    private val _dataSet =
        MutableStateFlow(
            initialSample.let { sample ->
                StackedAreaChartState(
                    dataSet = sample.dataSet,
                    seriesKeys = sample.seriesKeys,
                )
            },
        )

    val dataSet: StateFlow<StackedAreaChartState> = _dataSet.asStateFlow()
    private val _controlsState =
        MutableStateFlow(
            StackedAreaChartControlsState(
                points = defaultPoints,
                minValue = defaultRange.first,
                maxValue = defaultRange.last,
            ),
        )
    val controlsState: StateFlow<StackedAreaChartControlsState> = _controlsState.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    private var liveUpdatesJob: Job? = null

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
        val sample =
            stackedAreaSampleUseCase.stackedAreaSample(
                points = safePoints,
                range = safeRangeStart..safeRangeEnd,
            )
        _dataSet.value =
            StackedAreaChartState(
                dataSet = sample.dataSet,
                seriesKeys = sample.seriesKeys,
            )
    }

    fun refresh() {
        regenerateDataSet()
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
