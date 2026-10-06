package io.github.hdcharts.app.demo.gauge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.sampleshared.data.RingGaugeSampleData
import io.github.hdcharts.sampleshared.data.RingGaugeSampleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val LIVE_UPDATE_INTERVAL_MS = 2000L

data class RingGaugeChartUiState(
    val data: ChartData,
    val title: String,
    val isPlaying: Boolean = false,
)

class RingGaugeChartViewModel(
    private val ringGaugeSampleUseCase: RingGaugeSampleUseCase,
) : ViewModel() {
    private val initialSample = ringGaugeSampleUseCase.deterministic()
    private var liveUpdatesJob: Job? = null

    private val _uiState =
        MutableStateFlow(
            RingGaugeChartUiState(
                data = initialSample.data,
                title = initialSample.title,
            ),
        )

    val uiState: StateFlow<RingGaugeChartUiState> = _uiState.asStateFlow()

    fun togglePlaying() {
        setPlaying(!_uiState.value.isPlaying)
    }

    fun refresh() {
        showSample(ringGaugeSampleUseCase.random())
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
    }

    private fun showSample(sample: RingGaugeSampleData) {
        _uiState.update {
            it.copy(
                data = sample.data,
                title = sample.title,
            )
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
