package io.github.hdcharts.app.demo.gauge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.gauge.SegmentedRingGaugeChart
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SegmentedRingGaugeChartDemo(viewModel: SegmentedRingGaugeChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChartDemo(
        onRefresh = {},
        refreshVisible = false,
    ) {
        SegmentedRingGaugeChart(
            modifier = Modifier.fillMaxSize(),
            data = uiState.data,
            capacity = uiState.capacity,
            title = uiState.title,
            centerSupportingLabel = uiState.centerSupportingLabel,
        )
    }
}
