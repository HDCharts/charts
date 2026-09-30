package io.github.hdcharts.app.demo.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.sampleshared.data.RadarSampleData
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
    val data: ChartData,
    val title: String,
)

data class RadarChartUiState(
    val chart: RadarChartState,
    val isPlaying: Boolean = false,
)

class RadarChartViewModel(
    private val radarSampleUseCase: RadarSampleUseCase,
) : ViewModel() {
    private val initialSample = radarSampleUseCase.initialRadarSample()
    private val refreshRange = radarSampleUseCase.radarRefreshRange()
    private var liveUpdatesJob: Job? = null

    private val _uiState =
        MutableStateFlow(
            RadarChartUiState(
                chart = initialSample.toChartState(),
            ),
        )

    val uiState: StateFlow<RadarChartUiState> = _uiState.asStateFlow()

    fun togglePlaying() {
        setPlaying(!_uiState.value.isPlaying)
    }

    fun refresh() {
        val sample = radarSampleUseCase.radarSample(range = refreshRange)
        _uiState.update { it.copy(chart = sample.toChartState()) }
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
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

    private fun RadarSampleData.toChartState(): RadarChartState =
        RadarChartState(
            data = data,
            title = title,
        )
}
