package io.github.hdcharts.app.demo.gauge

import androidx.lifecycle.ViewModel
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.sampleshared.data.SegmentedRingGaugeSampleData
import io.github.hdcharts.sampleshared.data.SegmentedRingGaugeSampleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SegmentedRingGaugeChartUiState(
    val data: ChartData,
    val title: String,
    val capacity: Double,
    val centerSupportingLabel: String,
)

class SegmentedRingGaugeChartViewModel(
    segmentedRingGaugeSampleUseCase: SegmentedRingGaugeSampleUseCase,
) : ViewModel() {
    private val sample = segmentedRingGaugeSampleUseCase.deterministic()

    private val _uiState = MutableStateFlow(sample.toUiState())
    val uiState: StateFlow<SegmentedRingGaugeChartUiState> = _uiState.asStateFlow()
}

private fun SegmentedRingGaugeSampleData.toUiState() =
    SegmentedRingGaugeChartUiState(
        data = data,
        title = title,
        capacity = capacity,
        centerSupportingLabel = centerSupportingLabel,
    )
