package io.github.hdcharts.app.demo.multiline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.app.demo.timeline.LiveTimelineControlsState
import io.github.hdcharts.app.demo.timeline.LiveTimelineDefaults
import io.github.hdcharts.app.demo.timeline.LiveTimelineStreamer
import io.github.hdcharts.sampleshared.data.LiveLatencyMultiSeriesWindow
import io.github.hdcharts.sampleshared.data.LiveLatencyTimelineUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LiveMultiLineChartUiState(
    val dataSet: MultiLineChartState,
    val controlsState: LiveTimelineControlsState,
    val isPlaying: Boolean = true,
)

/** Streams one P50 and one P95 latency reading per update interval into a sliding window. */
class LiveMultiLineChartViewModel(
    private val liveLatencyTimelineUseCase: LiveLatencyTimelineUseCase,
) : ViewModel() {
    private val initialControlsState = LiveTimelineControlsState()

    private var window: LiveLatencyMultiSeriesWindow =
        liveLatencyTimelineUseCase.createMultiWindow(windowSize = initialControlsState.windowSize)

    private val _uiState =
        MutableStateFlow(
            LiveMultiLineChartUiState(
                dataSet = buildDataSet(window),
                controlsState = initialControlsState,
            ),
        )
    val uiState: StateFlow<LiveMultiLineChartUiState> = _uiState.asStateFlow()

    private val liveUpdates =
        LiveTimelineStreamer(
            scope = viewModelScope,
            intervalMillis = {
                _uiState.value.controlsState.updateIntervalMs
                    .toLong()
            },
            onTick = ::appendLiveTick,
        )

    fun onEnterDemo() {
        if (_uiState.value.isPlaying) {
            liveUpdates.start()
        }
    }

    fun onLeaveDemo() {
        liveUpdates.stop()
    }

    fun togglePlaying() {
        setPlaying(!_uiState.value.isPlaying)
    }

    fun updateInterval(intervalMs: Int) {
        val safeInterval =
            intervalMs.coerceIn(
                minimumValue = LiveTimelineDefaults.MIN_UPDATE_INTERVAL_MS,
                maximumValue = LiveTimelineDefaults.MAX_UPDATE_INTERVAL_MS,
            )
        if (safeInterval == _uiState.value.controlsState.updateIntervalMs) return

        _uiState.update { state ->
            state.copy(controlsState = state.controlsState.copy(updateIntervalMs = safeInterval))
        }
        liveUpdates.restartIfRunning()
    }

    fun updateWindowSize(windowSize: Int) {
        val safeWindowSize =
            windowSize.coerceIn(
                minimumValue = LiveTimelineDefaults.MIN_WINDOW_SIZE,
                maximumValue = LiveTimelineDefaults.MAX_WINDOW_SIZE,
            )
        if (safeWindowSize == _uiState.value.controlsState.windowSize) return

        window =
            liveLatencyTimelineUseCase.createMultiWindow(
                windowSize = safeWindowSize,
                endTick = window.endTick,
            )
        _uiState.update { state ->
            state.copy(
                controlsState = state.controlsState.copy(windowSize = safeWindowSize),
                dataSet = buildDataSet(window),
            )
        }
    }

    override fun onCleared() {
        liveUpdates.stop()
        super.onCleared()
    }

    private fun appendLiveTick() {
        window = liveLatencyTimelineUseCase.advanceMultiWindow(window)
        val dataSet = buildDataSet(window)
        _uiState.update { state ->
            state.copy(dataSet = dataSet)
        }
    }

    private fun buildDataSet(window: LiveLatencyMultiSeriesWindow): MultiLineChartState =
        MultiLineChartState(
            dataSet = liveLatencyTimelineUseCase.toMultiDataSet(window),
            title = liveLatencyTimelineUseCase.multiSeriesTitle,
        )

    private fun setPlaying(playing: Boolean) {
        if (_uiState.value.isPlaying == playing) return

        _uiState.update { state ->
            state.copy(isPlaying = playing)
        }
        if (playing) {
            liveUpdates.start()
        } else {
            liveUpdates.stop()
        }
    }
}
