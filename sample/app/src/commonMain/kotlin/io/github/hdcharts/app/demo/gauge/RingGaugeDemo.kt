package io.github.hdcharts.app.demo.gauge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.gauge.RingGaugeChart
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RingGaugeChartDemo(viewModel: RingGaugeChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChartDemo(
        onRefresh = viewModel::refresh,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
        },
    ) {
        RingGaugeChart(
            data = uiState.data,
            title = uiState.title,
        )
    }
}
