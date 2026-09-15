package io.github.hdcharts.app.demo.pie

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PieChartDemo(viewModel: PieChartViewModel = koinViewModel()) {
    val state by viewModel.dataSet.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val chartColors = LocalChartColors.current
    val slices =
        when (state.preset) {
            ChartPreset.Default -> state.slices
            ChartPreset.Custom ->
                remember(state.slices, chartColors) {
                    val palette = chartColors.seriesColors(state.slices.size)
                    state.slices.mapIndexed { index, slice -> slice.copy(color = palette[index]) }
                }
        }

    ChartDemo(
        onRefresh = viewModel::refresh,
        presetContent = {
            ChartPresetToggle(
                selectedPreset = state.preset,
                onPresetSelected = { viewModel.onPresetSelected(it) },
            )
        },
        extraButtons = {
            IconButton(
                onClick = viewModel::togglePlaying,
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription =
                        stringResource(
                            if (isPlaying) Res.string.cd_pause_live_updates else Res.string.cd_play_live_updates,
                        ),
                )
            }
        },
    ) {
        PieChart(
            data = slices,
            title = state.title,
            style =
                if (state.preset ==
                    ChartPreset.Default
                ) {
                    PieChartDefaults.style()
                } else {
                    ChartTestStyleFixtures.pieCustomStyle(
                        chartContainerStyle = ChartContainerDefaults.style(),
                    )
                },
        )
    }
}
