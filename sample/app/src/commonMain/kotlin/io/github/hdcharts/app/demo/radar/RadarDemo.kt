package io.github.hdcharts.app.demo.radar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.radar.RadarChart
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RadarChartDemo(viewModel: RadarChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChartDemo(
        onRefresh = viewModel::refresh,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
        },
    ) {
        RadarChart(
            data = uiState.chart.data,
            title = uiState.chart.title,
        )
    }
}
