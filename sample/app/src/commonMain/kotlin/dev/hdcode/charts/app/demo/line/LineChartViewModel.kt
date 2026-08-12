package dev.hdcode.charts.app.demo.line

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hdcode.charts.app.data.LiveLatencySingleSeriesWindow
import dev.hdcode.charts.app.data.LiveLatencyTimelineUseCase
import dev.hdcode.charts.app.demo.timeline.LiveTimelineControlsState
import dev.hdcode.charts.app.demo.timeline.LiveTimelineDefaults
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LineChartDataControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

enum class LineDemoPreset {
    Default,
    Timeline,
    Custom,
}

data class LineChartUiState(
    val dataSet: ChartDataSet,
    val dataControlsState: LineChartDataControlsState,
    val timelineControlsState: LiveTimelineControlsState,
    val preset: LineDemoPreset = LineDemoPreset.Default,
    val isPlaying: Boolean = false,
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

    private val initialTimelineControlsState = LiveTimelineControlsState()
    private val initialDataControlsState =
        LineChartDataControlsState(
            points = LiveTimelineDefaults.DEFAULT_WINDOW_SIZE.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS),
            minValue = DEFAULT_MIN_VALUE,
            maxValue = DEFAULT_MAX_VALUE,
        )

    private var timelineWindow: LiveLatencySingleSeriesWindow =
        liveLatencyTimelineUseCase.createSingleWindow(
            windowSize = initialTimelineControlsState.windowSize,
        )

    private val _uiState =
        MutableStateFlow(
            LineChartUiState(
                dataSet = buildGeneratedDataSet(initialDataControlsState),
                dataControlsState = initialDataControlsState,
                timelineControlsState = initialTimelineControlsState,
            ),
        )
    val uiState: StateFlow<LineChartUiState> = _uiState.asStateFlow()

    private var liveUpdatesJob: Job? = null

    fun refreshForSelectedPreset() {
        when (_uiState.value.preset) {
            LineDemoPreset.Timeline -> refreshTimeline()
            LineDemoPreset.Default,
            LineDemoPreset.Custom,
            -> refreshGeneratedData()
        }
    }

    fun refreshTimeline() {
        regenerateWindow()
    }

    fun refreshGeneratedData() {
        val controls = _uiState.value.dataControlsState
        val dataSet = buildGeneratedDataSet(controls)
        _uiState.update { state ->
            state.copy(dataSet = dataSet)
        }
    }

    fun updateInterval(intervalMs: Int) {
        val safeInterval =
            intervalMs.coerceIn(
                minimumValue = LiveTimelineDefaults.MIN_UPDATE_INTERVAL_MS,
                maximumValue = LiveTimelineDefaults.MAX_UPDATE_INTERVAL_MS,
            )
        val controls = _uiState.value.timelineControlsState
        if (safeInterval == controls.updateIntervalMs) return

        _uiState.update { state ->
            state.copy(
                timelineControlsState = state.timelineControlsState.copy(updateIntervalMs = safeInterval),
            )
        }
        restartLiveUpdatesIfNeeded()
    }

    fun updateWindowSize(windowSize: Int) {
        val safeWindowSize =
            windowSize.coerceIn(
                minimumValue = LiveTimelineDefaults.MIN_WINDOW_SIZE,
                maximumValue = LiveTimelineDefaults.MAX_WINDOW_SIZE,
            )
        val controls = _uiState.value.timelineControlsState
        if (safeWindowSize == controls.windowSize) return

        timelineWindow =
            liveLatencyTimelineUseCase.createSingleWindow(
                windowSize = safeWindowSize,
                endTick = timelineWindow.endTick,
            )
        val dataSet = liveLatencyTimelineUseCase.toSingleDataSet(timelineWindow)
        _uiState.update { state ->
            state.copy(
                timelineControlsState = state.timelineControlsState.copy(windowSize = safeWindowSize),
                dataSet = dataSet,
            )
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

    fun togglePlaying() {
        setPlaying(!_uiState.value.isPlaying)
    }

    fun onPresetSelected(selectedPreset: LineDemoPreset) {
        val previousPreset = _uiState.value.preset
        if (selectedPreset == previousPreset) return

        if (selectedPreset == LineDemoPreset.Timeline && previousPreset != LineDemoPreset.Timeline) {
            refreshTimeline()
        }

        _uiState.update { state ->
            state.copy(preset = selectedPreset)
        }

        when (selectedPreset) {
            LineDemoPreset.Timeline -> setPlaying(true)
            LineDemoPreset.Default,
            LineDemoPreset.Custom,
            -> {
                setPlaying(false)
                if (previousPreset == LineDemoPreset.Timeline) {
                    refreshGeneratedData()
                }
            }
        }
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
    }

    private fun regenerateWindow() {
        val controls = _uiState.value.timelineControlsState
        timelineWindow =
            liveLatencyTimelineUseCase.createSingleWindow(
                windowSize = controls.windowSize,
                endTick = timelineWindow.endTick,
            )
        val dataSet = liveLatencyTimelineUseCase.toSingleDataSet(timelineWindow)
        _uiState.update { state ->
            state.copy(dataSet = dataSet)
        }
    }

    private fun appendLiveTick() {
        timelineWindow = liveLatencyTimelineUseCase.advanceSingleWindow(timelineWindow)
        val dataSet = liveLatencyTimelineUseCase.toSingleDataSet(timelineWindow)
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
            startLiveUpdates()
        } else {
            stopLiveUpdates()
        }
    }

    private fun startLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob =
            viewModelScope.launch {
                while (isActive) {
                    val intervalMs =
                        _uiState.value.timelineControlsState.updateIntervalMs
                            .toLong()
                    delay(intervalMs)
                    appendLiveTick()
                }
            }
    }

    private fun restartLiveUpdatesIfNeeded() {
        if (_uiState.value.isPlaying) {
            startLiveUpdates()
        }
    }

    private fun stopLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob = null
    }

    private fun buildGeneratedDataSet(controls: LineChartDataControlsState): ChartDataSet {
        val baseWindow =
            liveLatencyTimelineUseCase.createSingleWindow(
                windowSize = controls.points,
                endTick = timelineWindow.endTick,
            )
        val baseDataSet = liveLatencyTimelineUseCase.toSingleDataSet(baseWindow)
        val basePoints = baseDataSet.data.item.points
        val labels =
            baseDataSet.data.item.labels
                .toList()
        val safeMin = controls.minValue.toDouble()
        val safeMax = controls.maxValue.toDouble().coerceAtLeast(safeMin + 1.0)
        val sourceMin = basePoints.minOrNull() ?: 0.0
        val sourceMax = basePoints.maxOrNull() ?: sourceMin
        val sourceRange = sourceMax - sourceMin

        val normalizedValues =
            if (sourceRange == 0.0) {
                List(basePoints.size) { safeMin.toFloat() }
            } else {
                basePoints.map { point ->
                    val normalized = ((point - sourceMin) / sourceRange).coerceIn(0.0, 1.0)
                    (safeMin + normalized * (safeMax - safeMin)).toFloat()
                }
            }

        return normalizedValues.toChartDataSet(
            title = baseDataSet.data.label,
            labels = labels,
        )
    }
}
