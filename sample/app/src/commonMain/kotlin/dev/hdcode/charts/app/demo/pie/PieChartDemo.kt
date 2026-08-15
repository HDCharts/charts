package dev.hdcode.charts.app.demo.pie

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
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.cd_pause_live_updates
import chartsproject.app.generated.resources.cd_play_live_updates
import dev.hdcode.charts.app.ui.composable.ChartDemo
import dev.hdcode.charts.app.ui.composable.ChartPreset
import dev.hdcode.charts.app.ui.composable.ChartPresetToggle
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import dev.hdcode.charts.sampleshared.theme.LocalChartColors
import dev.hdcode.charts.sampleshared.theme.seriesColors
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.PieChartDefaults
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
