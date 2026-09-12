package dev.hdcode.charts.app.demo.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hdcode.charts.app.ui.composable.ChartPreset
import dev.hdcode.charts.sampleshared.data.RadarSampleUseCase
import io.github.dautovicharis.charts.model.ChartData
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
    val preset: ChartPreset = ChartPreset.Default,
)

class RadarChartViewModel(
    private val radarSampleUseCase: RadarSampleUseCase,
) : ViewModel() {
    private val initialSample = radarSampleUseCase.initialRadarSample()
    private val initialDefaultData = radarSampleUseCase.initialRadarDefaultData()
    private val refreshRange = radarSampleUseCase.radarRefreshRange()
    private var liveUpdatesJob: Job? = null

    private val _dataSet =
        MutableStateFlow(
            RadarChartState(
                basicData = initialDefaultData,
                customData = initialSample.customData,
                seriesKeys = initialSample.seriesKeys,
                title = initialSample.title,
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

    fun regenerateBasicData(range: IntRange = refreshRange) {
        val data = radarSampleUseCase.radarDefaultData(range = range)
        _dataSet.update {
            it.copy(basicData = data)
        }
    }

    fun regenerateCustomData(range: IntRange = refreshRange) {
        val sample = radarSampleUseCase.radarCustomSample(range = range)
        _dataSet.update {
            it.copy(
                customData = sample.data,
                seriesKeys = sample.seriesKeys,
            )
        }
    }

    fun refresh() {
        when (_dataSet.value.preset) {
            ChartPreset.Default -> regenerateBasicData()
            ChartPreset.Custom -> regenerateCustomData()
        }
    }

    private fun applyInitialPresetData(preset: ChartPreset) {
        when (preset) {
            ChartPreset.Default -> {
                _dataSet.update {
                    it.copy(basicData = initialDefaultData)
                }
            }

            ChartPreset.Custom -> {
                _dataSet.update {
                    it.copy(
                        customData = initialSample.customData,
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
