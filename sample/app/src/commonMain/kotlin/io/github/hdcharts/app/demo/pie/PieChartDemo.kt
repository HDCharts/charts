package io.github.hdcharts.app.demo.pie

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
            modifier = Modifier.fillMaxSize(),
            data = uiState.data,
            title = uiState.title,
            style = PieChartDefaults.style(),
        )
    }
}
