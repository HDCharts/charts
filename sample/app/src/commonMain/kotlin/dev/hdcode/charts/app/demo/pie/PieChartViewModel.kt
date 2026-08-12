package dev.hdcode.charts.app.demo.pie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hdcode.charts.app.ui.composable.ChartPreset
import dev.hdcode.charts.sampleshared.data.PieSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val LIVE_UPDATE_INTERVAL_MS = 2000L

data class PieChartState(
    val dataSet: ChartDataSet,
    val segmentKeys: List<String> = emptyList(),
    val preset: ChartPreset = ChartPreset.Default,
)

class PieChartViewModel(
    private val pieSampleUseCase: PieSampleUseCase,
) : ViewModel() {
    private val initialDefaultSample = pieSampleUseCase.initialPieSample()
    private val initialCustomSample = pieSampleUseCase.initialPieCustomSample()
    private val refreshRange = pieSampleUseCase.pieRefreshRange()
    private val defaultSegmentCount = initialDefaultSample.segmentKeys.size
    private var liveUpdatesJob: Job? = null

    private val _dataSet =
        MutableStateFlow(
            initialDefaultSample.let { sample ->
                PieChartState(
                    dataSet = sample.dataSet,
                    segmentKeys = sample.segmentKeys,
                    preset = ChartPreset.Default,
                )
            },
        )

    val dataSet: StateFlow<PieChartState> = _dataSet.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun onPresetSelected(preset: ChartPreset) {
        if (preset == _dataSet.value.preset) return
        _dataSet.update { it.copy(preset = preset) }
        applyInitialPresetData(preset)
    }

    fun regenerateDefaultDataSet() {
        val sample =
            pieSampleUseCase.pieSample(
                range = refreshRange,
                numOfPoints = defaultSegmentCount..defaultSegmentCount,
            )
        _dataSet.update {
            it.copy(
                dataSet = sample.dataSet,
                segmentKeys = sample.segmentKeys,
            )
        }
    }

    fun regenerateCustomDataSet(range: IntRange = refreshRange) {
        val sample =
            pieSampleUseCase.pieCustomSample(range)
        _dataSet.update {
            it.copy(
                dataSet = sample.dataSet,
                segmentKeys = sample.segmentKeys,
            )
        }
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

    fun refresh() {
        refreshCurrentPreset()
    }

    fun refreshCurrentPresetLive() {
        refreshCurrentPreset()
    }

    private fun applyInitialPresetData(preset: ChartPreset) {
        val sample =
            when (preset) {
                ChartPreset.Default -> initialDefaultSample
                ChartPreset.Custom -> initialCustomSample
            }
        _dataSet.update {
            it.copy(
                dataSet = sample.dataSet,
                segmentKeys = sample.segmentKeys,
            )
        }
    }

    private fun refreshCurrentPreset() {
        when (_dataSet.value.preset) {
            ChartPreset.Default -> regenerateDefaultDataSet()
            ChartPreset.Custom -> regenerateCustomDataSet()
        }
    }

    private fun startLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob =
            viewModelScope.launch {
                refreshCurrentPresetLive()
                while (isActive) {
                    delay(LIVE_UPDATE_INTERVAL_MS)
                    refreshCurrentPresetLive()
                }
            }
    }

    private fun stopLiveUpdates() {
        liveUpdatesJob?.cancel()
        liveUpdatesJob = null
    }

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
    }
}
