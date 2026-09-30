package io.github.hdcharts.app.demo.pie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.pie.PieChart
import io.github.hdcharts.pie.PieChartDefaults
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PieChartDemo(viewModel: PieChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChartDemo(
        onRefresh = viewModel::refresh,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
        },
    ) {
        PieChart(
            data = uiState.slices,
            title = uiState.title,
            style = PieChartDefaults.style(),
        )
    }
}
