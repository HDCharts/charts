package io.github.hdcharts.app.demo.line

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.app.demo.timeline.LiveTimelineDefaults
import io.github.hdcharts.app.demo.timeline.LiveTimelineStreamer
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.sampleshared.data.LiveLatencySingleSeriesWindow
import io.github.hdcharts.sampleshared.data.LiveLatencyTimelineUseCase
import io.github.hdcharts.sampleshared.data.LiveTimelineProfile
import io.github.hdcharts.sampleshared.data.MIN_SCALE_SWITCH_POINTS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LineScaleDropControlsState(
    val updateIntervalMs: Int = LiveTimelineDefaults.DEFAULT_UPDATE_INTERVAL_MS,
    val windowSize: Int = LiveTimelineDefaults.DEFAULT_WINDOW_SIZE,
    val scaleSwitchPoints: Int = 20,
)

data class LineScaleDropUiState(
    val dataSet: ChartData,
    val controlsState: LineScaleDropControlsState,
    val isPlaying: Boolean = true,
)

/**
 * Drives the live line chart whose values swing between magnitudes.
 *
 * The streamed window stays in the millions for
 * [LineScaleDropControlsState.scaleSwitchPoints] points, drops below one hundred for the same
 * number of points, and climbs back, which shows how the timeline render mode rescales while it
 * runs. A phase at least as long as the window makes the window hold a single magnitude, so the
 * chart rescales completely. It owns its own window and playback, so the steady timeline demo stays
 * unchanged.
 */
class LineScaleDropViewModel(
    private val liveLatencyTimelineUseCase: LiveLatencyTimelineUseCase,
) : ViewModel() {
    companion object {
        val SWITCH_POINTS_RANGE = MIN_SCALE_SWITCH_POINTS..LiveTimelineDefaults.MAX_WINDOW_SIZE
    }

    private val initialControlsState = LineScaleDropControlsState()

    private var window: LiveLatencySingleSeriesWindow = createWindow(controls = initialControlsState)

    private val _uiState =
        MutableStateFlow(
            LineScaleDropUiState(
                dataSet = liveLatencyTimelineUseCase.toSingleDataSet(window),
                controlsState = initialControlsState,
            ),
        )
    val uiState: StateFlow<LineScaleDropUiState> = _uiState.asStateFlow()

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

        updateControls { controls -> controls.copy(windowSize = safeWindowSize) }
    }

    fun updateScaleSwitchPoints(scaleSwitchPoints: Int) {
        val safeScaleSwitchPoints = scaleSwitchPoints.coerceIn(SWITCH_POINTS_RANGE)
        if (safeScaleSwitchPoints == _uiState.value.controlsState.scaleSwitchPoints) return

        updateControls { controls -> controls.copy(scaleSwitchPoints = safeScaleSwitchPoints) }
    }

    override fun onCleared() {
        liveUpdates.stop()
        super.onCleared()
    }

    private fun updateControls(transform: (LineScaleDropControlsState) -> LineScaleDropControlsState) {
        val controls = transform(_uiState.value.controlsState)
        window = createWindow(controls = controls, endTick = window.endTick)
        _uiState.update { state ->
            state.copy(
                controlsState = controls,
                dataSet = liveLatencyTimelineUseCase.toSingleDataSet(window),
            )
        }
    }

    private fun createWindow(
        controls: LineScaleDropControlsState,
        endTick: Int? = null,
    ): LiveLatencySingleSeriesWindow =
        liveLatencyTimelineUseCase.createSingleWindow(
            windowSize = controls.windowSize,
            endTick = endTick,
            profile = LiveTimelineProfile.ScaleDrop,
            scaleSwitchPoints = controls.scaleSwitchPoints,
        )

    private fun appendLiveTick() {
        window = liveLatencyTimelineUseCase.advanceSingleWindow(window)
        val dataSet = liveLatencyTimelineUseCase.toSingleDataSet(window)
        _uiState.update { state ->
            state.copy(dataSet = dataSet)
        }
    }

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
