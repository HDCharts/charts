package io.github.hdcharts.app.demo.pie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.app.ui.composable.ChartPreset
import io.github.hdcharts.charts.model.PieSlice
import io.github.hdcharts.sampleshared.data.PieSampleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val LIVE_UPDATE_INTERVAL_MS = 2000L

data class PieChartUiState(
    val slices: List<PieSlice>,
    val title: String,
    val preset: ChartPreset = ChartPreset.Default,
    val isPlaying: Boolean = false,
)

class PieChartViewModel(
    private val pieSampleUseCase: PieSampleUseCase,
) : ViewModel() {
    private val initialSample = pieSampleUseCase.initialPieSample()
    private val refreshRange = pieSampleUseCase.pieRefreshRange()
    private val segmentCount = initialSample.slices.size
    private var liveUpdatesJob: Job? = null

    private val _uiState =
        MutableStateFlow(
            PieChartUiState(
                slices = initialSample.slices,
                title = initialSample.title,
                preset = ChartPreset.Default,
            ),
        )

    val uiState: StateFlow<PieChartUiState> = _uiState.asStateFlow()

    fun onPresetSelected(preset: ChartPreset) {
        if (preset == _uiState.value.preset) return
        _uiState.update {
            it.copy(
                slices = initialSample.slices,
                title = initialSample.title,
                preset = preset,
            )
        }
    }

    fun togglePlaying() {
        setPlaying(!_uiState.value.isPlaying)
    }

    fun refresh() {
        regenerateDataSet()
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
    }

    private fun regenerateDataSet() {
        val sample =
            pieSampleUseCase.pieSample(
                range = refreshRange,
                numOfPoints = segmentCount..segmentCount,
            )
        _uiState.update {
            it.copy(
                slices = sample.slices,
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
                regenerateDataSet()
                while (isActive) {
                    delay(LIVE_UPDATE_INTERVAL_MS)
                    regenerateDataSet()
                }
            }
    }

    private fun stopLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob = null
    }
}
