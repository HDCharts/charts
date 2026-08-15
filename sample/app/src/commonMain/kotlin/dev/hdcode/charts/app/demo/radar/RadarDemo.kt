package dev.hdcode.charts.app.demo.radar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.cd_pause_live_updates
import chartsproject.app.generated.resources.cd_play_live_updates
import dev.hdcode.charts.app.ui.composable.ChartDemo
import dev.hdcode.charts.app.ui.composable.ChartPreset
import dev.hdcode.charts.app.ui.composable.ChartPresetToggle
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.RadarChart
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RadarChartDemo(viewModel: RadarChartViewModel = koinViewModel()) {
    val dataSet by viewModel.dataSet.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val seriesKeys = dataSet.seriesKeys

    val refresh: () -> Unit = viewModel::refresh

    ChartDemo(
        onRefresh = refresh,
        presetContent = {
            ChartPresetToggle(
                selectedPreset = dataSet.preset,
                onPresetSelected = viewModel::onPresetSelected,
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
        when (dataSet.preset) {
            ChartPreset.Default -> {
                RadarChart(
                    dataSet = dataSet.basicDataSet,
                )
            }

            ChartPreset.Custom -> {
                RadarChart(
                    dataSet = dataSet.customDataSet,
                    style =
                        ChartTestStyleFixtures.radarCustomStyle(
                            chartContainerStyle = ChartContainerDefaults.style(),
                            seriesKeys = seriesKeys,
                        ),
                )
            }
        }
    }
}
