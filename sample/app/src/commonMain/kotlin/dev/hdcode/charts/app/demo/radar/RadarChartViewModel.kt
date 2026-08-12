package dev.hdcode.charts.app.demo.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hdcode.charts.app.ui.composable.ChartPreset
import dev.hdcode.charts.sampleshared.data.RadarSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet
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
    val basicDataSet: ChartDataSet,
    val customDataSet: MultiChartDataSet,
    val seriesKeys: List<String> = emptyList(),
    val preset: ChartPreset = ChartPreset.Default,
)

class RadarChartViewModel(
    private val radarSampleUseCase: RadarSampleUseCase,
) : ViewModel() {
    private val initialSample = radarSampleUseCase.initialRadarSample()
    private val initialDefaultDataSet = radarSampleUseCase.initialRadarDefaultDataSet()
    private val refreshRange = radarSampleUseCase.radarRefreshRange()
    private var liveUpdatesJob: Job? = null

    private val _dataSet =
        MutableStateFlow(
            RadarChartState(
                basicDataSet = initialDefaultDataSet,
                customDataSet = initialSample.customDataSet,
                seriesKeys = initialSample.seriesKeys,
                preset = ChartPreset.Default,
            ),
        )

    val dataSet: StateFlow<RadarChartState> = _dataSet.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun onPresetSelected(preset: ChartPreset) {
        if (preset == _dataSet.value.preset) return
        _dataSet.update { it.copy(preset = preset) }
        applyInitialPresetData(preset)
    }

    fun regenerateBasicDataSet(range: IntRange = refreshRange) {
        val dataSet = radarSampleUseCase.radarDefaultDataSet(range = range)
        _dataSet.update {
            it.copy(basicDataSet = dataSet)
        }
    }

    fun regenerateCustomDataSet(range: IntRange = refreshRange) {
        val sample = radarSampleUseCase.radarCustomSample(range = range)
        _dataSet.update {
            it.copy(
                customDataSet = sample.dataSet,
                seriesKeys = sample.seriesKeys,
            )
        }
    }

    fun refresh() {
        when (_dataSet.value.preset) {
            ChartPreset.Default -> regenerateBasicDataSet()
            ChartPreset.Custom -> regenerateCustomDataSet()
        }
    }

    private fun applyInitialPresetData(preset: ChartPreset) {
        when (preset) {
            ChartPreset.Default -> {
                _dataSet.update {
                    it.copy(
                        basicDataSet = initialDefaultDataSet,
                    )
                }
            }

            ChartPreset.Custom -> {
                _dataSet.update {
                    it.copy(
                        customDataSet = initialSample.customDataSet,
                        seriesKeys = initialSample.seriesKeys,
                    )
                }
            }
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

    override fun onCleared() {
        stopLiveUpdates()
        super.onCleared()
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
