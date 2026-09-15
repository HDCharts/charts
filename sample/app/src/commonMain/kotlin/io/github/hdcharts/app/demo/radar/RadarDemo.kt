package io.github.hdcharts.app.demo.radar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.cd_pause_live_updates
import hdcharts.app.generated.resources.cd_play_live_updates
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.ChartPreset
import io.github.hdcharts.app.ui.composable.ChartPresetToggle
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RadarChartDemo(viewModel: RadarChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChartDemo(
        onRefresh = viewModel::refresh,
        presetContent = {
            ChartPresetToggle(
                selectedPreset = uiState.preset,
                onPresetSelected = viewModel::onPresetSelected,
            )
        },
        extraButtons = {
            IconButton(onClick = viewModel::togglePlaying) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription =
                        stringResource(
                            if (uiState.isPlaying) {
                                Res.string.cd_pause_live_updates
                            } else {
                                Res.string.cd_play_live_updates
                            },
                        ),
                )
            }
        },
    ) {
        when (uiState.preset) {
            ChartPreset.Default ->
                RadarChart(
                    data = uiState.chart.basicData,
                    title = uiState.chart.title,
                )
            ChartPreset.Custom ->
                RadarChart(
                    data = uiState.chart.customData,
                    title = uiState.chart.title,
                    style =
                        ChartTestStyleFixtures.radarCustomStyle(
                            chartContainerStyle = ChartContainerDefaults.style(),
                            seriesKeys = uiState.chart.seriesKeys,
                        ),
                )
        }
    }
}
