package io.github.hdcharts.app.demo.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.app.ui.composable.ChartPreset
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.sampleshared.data.RadarSampleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val LIVE_UPDATE_INTERVAL_MS = 2000L

data class RadarChartState(
    val basicData: ChartData,
    val customData: ChartData,
    val seriesKeys: List<String> = emptyList(),
    val title: String,
)

data class RadarChartUiState(
    val chart: RadarChartState,
    val preset: ChartPreset = ChartPreset.Default,
    val isPlaying: Boolean = false,
)

class RadarChartViewModel(
    private val radarSampleUseCase: RadarSampleUseCase,
) : ViewModel() {
    private val initialSample = radarSampleUseCase.initialRadarSample()
    private val initialDefaultData = radarSampleUseCase.initialRadarDefaultData()
    private val refreshRange = radarSampleUseCase.radarRefreshRange()
    private var liveUpdatesJob: Job? = null

    private val _uiState =
        MutableStateFlow(
            RadarChartUiState(
                chart =
                    RadarChartState(
                        basicData = initialDefaultData,
                        customData = initialSample.customData,
                        seriesKeys = initialSample.seriesKeys,
                        title = initialSample.title,
                    ),
                preset = ChartPreset.Default,
            ),
        )

    val uiState: StateFlow<RadarChartUiState> = _uiState.asStateFlow()

    fun onPresetSelected(preset: ChartPreset) {
        if (preset == _uiState.value.preset) return
        applyInitialPresetData(preset)
        _uiState.update { it.copy(preset = preset) }
    }

    fun togglePlaying() {
        setPlaying(!_uiState.value.isPlaying)
    }

    fun refresh() {
        refreshCurrentPreset()
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
    }

    private fun refreshCurrentPreset() {
        when (_uiState.value.preset) {
            ChartPreset.Default -> regenerateBasicData()
            ChartPreset.Custom -> regenerateCustomData()
        }
    }

    private fun regenerateBasicData(range: IntRange = refreshRange) {
        val data = radarSampleUseCase.radarDefaultData(range = range)
        _uiState.update { state ->
            state.copy(chart = state.chart.copy(basicData = data))
        }
    }

    private fun regenerateCustomData(range: IntRange = refreshRange) {
        val sample = radarSampleUseCase.radarCustomSample(range = range)
        _uiState.update { state ->
            state.copy(
                chart =
                    state.chart.copy(
                        customData = sample.data,
                        seriesKeys = sample.seriesKeys,
                    ),
            )
        }
    }

    private fun applyInitialPresetData(preset: ChartPreset) {
        when (preset) {
            ChartPreset.Default -> {
                _uiState.update { state ->
                    state.copy(chart = state.chart.copy(basicData = initialDefaultData))
                }
            }

            ChartPreset.Custom -> {
                _uiState.update { state ->
                    state.copy(
                        chart =
                            state.chart.copy(
                                customData = initialSample.customData,
                                seriesKeys = initialSample.seriesKeys,
                            ),
                    )
                }
            }
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
