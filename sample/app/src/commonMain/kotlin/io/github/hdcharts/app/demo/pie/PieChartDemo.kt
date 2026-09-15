package io.github.hdcharts.app.demo.pie

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
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.PieChartDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PieChartDemo(viewModel: PieChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val slices =
        when (uiState.preset) {
            ChartPreset.Default -> uiState.slices
            ChartPreset.Custom -> ChartTestStyleFixtures.pieCustomSlices(uiState.slices)
        }
    val chartStyle =
        when (uiState.preset) {
            ChartPreset.Default -> PieChartDefaults.style()
            ChartPreset.Custom ->
                ChartTestStyleFixtures.pieCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
                )
        }

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
        PieChart(
            data = slices,
            title = uiState.title,
            style = chartStyle,
        )
    }
}
