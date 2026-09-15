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
    private val initialDefaultSample = pieSampleUseCase.initialPieSample()
    private val initialCustomSample = pieSampleUseCase.initialPieCustomSample()
    private val refreshRange = pieSampleUseCase.pieRefreshRange()
    private val defaultSegmentCount = initialDefaultSample.slices.size
    private var liveUpdatesJob: Job? = null

    private val _uiState =
        MutableStateFlow(
            PieChartUiState(
                slices = initialDefaultSample.slices,
                title = initialDefaultSample.title,
                preset = ChartPreset.Default,
            ),
        )

    val uiState: StateFlow<PieChartUiState> = _uiState.asStateFlow()

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

    private fun applyInitialPresetData(preset: ChartPreset) {
        val sample =
            when (preset) {
                ChartPreset.Default -> initialDefaultSample
                ChartPreset.Custom -> initialCustomSample
            }
        _uiState.update {
            it.copy(
                slices = sample.slices,
                title = sample.title,
            )
        }
    }

    private fun refreshCurrentPreset() {
        when (_uiState.value.preset) {
            ChartPreset.Default -> regenerateDefaultDataSet()
            ChartPreset.Custom -> regenerateCustomDataSet()
        }
    }

    private fun regenerateDefaultDataSet() {
        val sample =
            pieSampleUseCase.pieSample(
                range = refreshRange,
                numOfPoints = defaultSegmentCount..defaultSegmentCount,
            )
        _uiState.update {
            it.copy(
                slices = sample.slices,
                title = sample.title,
            )
        }
    }

    private fun regenerateCustomDataSet(range: IntRange = refreshRange) {
        val sample =
            pieSampleUseCase.pieCustomSample(range)
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
                refreshCurrentPreset()
                while (isActive) {
                    delay(LIVE_UPDATE_INTERVAL_MS)
                    refreshCurrentPreset()
                }
            }
    }

    private fun stopLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob = null
    }
}
