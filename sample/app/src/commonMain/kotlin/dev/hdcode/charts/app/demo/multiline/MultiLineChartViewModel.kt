package dev.hdcode.charts.app.demo.multiline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hdcode.charts.app.data.LiveLatencyMultiSeriesWindow
import dev.hdcode.charts.app.data.LiveLatencyTimelineUseCase
import dev.hdcode.charts.app.demo.timeline.LiveTimelineControlsState
import dev.hdcode.charts.app.demo.timeline.LiveTimelineDefaults
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MultiLineChartState(
    val dataSet: MultiChartDataSet,
    val seriesKeys: List<String> = emptyList(),
)

data class MultiLineChartDataControlsState(
    val points: Int,
    val minValue: Int,
    val maxValue: Int,
)

enum class MultiLineDemoPreset {
    Default,
    Timeline,
    Custom,
}

data class MultiLineChartUiState(
    val dataSet: MultiLineChartState,
    val dataControlsState: MultiLineChartDataControlsState,
    val controlsState: LiveTimelineControlsState,
    val preset: MultiLineDemoPreset = MultiLineDemoPreset.Default,
    val isPlaying: Boolean = false,
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
        private const val VALUE_POSTFIX = " ms"
    }

    private val initialControlsState = LiveTimelineControlsState()
    private val initialDataControlsState =
        MultiLineChartDataControlsState(
            points = LiveTimelineDefaults.DEFAULT_WINDOW_SIZE.coerceIn(MIN_SUPPORTED_POINTS, MAX_SUPPORTED_POINTS),
            minValue = DEFAULT_MIN_VALUE,
            maxValue = DEFAULT_MAX_VALUE,
        )

    private var timelineWindow: LiveLatencyMultiSeriesWindow =
        liveLatencyTimelineUseCase.createMultiWindow(
            windowSize = initialControlsState.windowSize,
        )

    private val _uiState =
        MutableStateFlow(
            MultiLineChartUiState(
                dataSet = buildGeneratedDataSet(initialDataControlsState),
                dataControlsState = initialDataControlsState,
                controlsState = initialControlsState,
            ),
        )
    val uiState: StateFlow<MultiLineChartUiState> = _uiState.asStateFlow()

    private var liveUpdatesJob: Job? = null

    fun refresh() {
        refreshForSelectedPreset()
    }

    fun refreshForSelectedPreset() {
        when (_uiState.value.preset) {
            MultiLineDemoPreset.Timeline -> refreshTimeline()
            MultiLineDemoPreset.Default,
            MultiLineDemoPreset.Custom,
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
        val controls = _uiState.value.controlsState
        if (safeInterval == controls.updateIntervalMs) return

        _uiState.update { state ->
            state.copy(
                controlsState = state.controlsState.copy(updateIntervalMs = safeInterval),
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
        val controls = _uiState.value.controlsState
        if (safeWindowSize == controls.windowSize) return

        timelineWindow =
            liveLatencyTimelineUseCase.createMultiWindow(
                windowSize = safeWindowSize,
                endTick = timelineWindow.endTick,
            )
        val timelineDataSet = buildTimelineDataSet(timelineWindow)
        _uiState.update { state ->
            state.copy(
                controlsState = state.controlsState.copy(windowSize = safeWindowSize),
                dataSet = timelineDataSet,
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

    fun onPresetSelected(selectedPreset: MultiLineDemoPreset) {
        val previousPreset = _uiState.value.preset
        if (selectedPreset == previousPreset) return

        if (selectedPreset == MultiLineDemoPreset.Timeline && previousPreset != MultiLineDemoPreset.Timeline) {
            refreshTimeline()
        }

        _uiState.update { state ->
            state.copy(preset = selectedPreset)
        }

        when (selectedPreset) {
            MultiLineDemoPreset.Timeline -> setPlaying(true)
            MultiLineDemoPreset.Default,
            MultiLineDemoPreset.Custom,
            -> {
                setPlaying(false)
                if (previousPreset == MultiLineDemoPreset.Timeline) {
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
        val controls = _uiState.value.controlsState
        timelineWindow =
            liveLatencyTimelineUseCase.createMultiWindow(
                windowSize = controls.windowSize,
                endTick = timelineWindow.endTick,
            )
        publishDataSet()
    }

    private fun appendLiveTick() {
        timelineWindow = liveLatencyTimelineUseCase.advanceMultiWindow(timelineWindow)
        publishDataSet()
    }

    private fun publishDataSet() {
        val timelineDataSet = buildTimelineDataSet(timelineWindow)
        _uiState.update { state ->
            state.copy(dataSet = timelineDataSet)
        }
    }

    private fun buildTimelineDataSet(window: LiveLatencyMultiSeriesWindow): MultiLineChartState =
        MultiLineChartState(
            dataSet = liveLatencyTimelineUseCase.toMultiDataSet(window),
            seriesKeys = liveLatencyTimelineUseCase.multiSeriesKeys,
        )

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
                        _uiState.value.controlsState.updateIntervalMs
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

    private fun buildGeneratedDataSet(controls: MultiLineChartDataControlsState): MultiLineChartState {
        val baseWindow =
            liveLatencyTimelineUseCase.createMultiWindow(
                windowSize = controls.points,
                endTick = timelineWindow.endTick,
            )
        val baseDataSet = liveLatencyTimelineUseCase.toMultiDataSet(baseWindow)
        val labels = baseWindow.labels.toList()
        val safeMin = controls.minValue.toDouble()
        val safeMax = controls.maxValue.toDouble().coerceAtLeast(safeMin + 1.0)
        val sourceValues = baseWindow.p50Values + baseWindow.p95Values
        val sourceMin = sourceValues.minOrNull()?.toDouble() ?: 0.0
        val sourceMax = sourceValues.maxOrNull()?.toDouble() ?: sourceMin
        val sourceRange = sourceMax - sourceMin

        fun normalize(values: List<Float>): List<Float> =
            if (sourceRange == 0.0) {
                List(values.size) { safeMin.toFloat() }
            } else {
                values.map { value ->
                    val normalized = ((value - sourceMin) / sourceRange).coerceIn(0.0, 1.0)
                    (safeMin + normalized * (safeMax - safeMin)).toFloat()
                }
            }

        val p50Values = normalize(baseWindow.p50Values)
        val p95Values = normalize(baseWindow.p95Values)
        val seriesKeys = liveLatencyTimelineUseCase.multiSeriesKeys
        val multiDataSet =
            listOf(
                seriesKeys.getOrElse(0) { "P50 Latency" } to p50Values,
                seriesKeys.getOrElse(1) { "P95 Latency" } to p95Values,
            ).toMultiChartDataSet(
                title = baseDataSet.data.title,
                categories = labels,
                postfix = VALUE_POSTFIX,
            )

        return MultiLineChartState(
            dataSet = multiDataSet,
            seriesKeys = seriesKeys,
        )
    }
}
