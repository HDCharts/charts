package io.github.hdcharts.app.demo.line

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.cd_pause_live_updates
import hdcharts.app.generated.resources.cd_play_live_updates
import hdcharts.app.generated.resources.chart_custom
import hdcharts.app.generated.resources.chart_default
import hdcharts.app.generated.resources.chart_scale_drop
import hdcharts.app.generated.resources.chart_timeline
import hdcharts.app.generated.resources.line_data_points
import hdcharts.app.generated.resources.line_data_points_range
import io.github.hdcharts.app.demo.timeline.LiveTimelineControls
import io.github.hdcharts.app.demo.timeline.timelineAnimationDurationMillis
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.DemoRangeSlider
import io.github.hdcharts.app.ui.composable.DemoSlider
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.LineChartDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LineChartDemo(viewModel: LineChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timelineAnimationDuration = timelineAnimationDurationMillis(uiState.timelineControlsState.updateIntervalMs)
    val chartContainerStyle = ChartContainerDefaults.style()
    val presetContent: @Composable () -> Unit = {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.controlSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LineDemoPresetToggle(
                selectedPreset = uiState.preset,
                onPresetSelected = viewModel::onPresetSelected,
            )
        }
    }

    if (uiState.preset == LineDemoPreset.ScaleDrop) {
        LineScaleDropDemo(presetContent = presetContent)
        return
    }

    ChartDemo(
        onRefresh = viewModel::refreshForSelectedPreset,
        refreshVisible = uiState.preset != LineDemoPreset.Timeline,
        presetContent = presetContent,
        extraButtons = {
            if (uiState.preset == LineDemoPreset.Timeline) {
                IconButton(
                    onClick = viewModel::togglePlaying,
                ) {
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
            }
        },
        controlsContent = {
            if (uiState.preset == LineDemoPreset.Timeline) {
                LiveTimelineControls(
                    controlsState = uiState.timelineControlsState,
                    onUpdateIntervalChange = viewModel::updateInterval,
                    onWindowSizeChange = viewModel::updateWindowSize,
                )
            } else {
                LineDataPointsControls(
                    points = uiState.dataControlsState.points,
                    minValue = uiState.dataControlsState.minValue,
                    maxValue = uiState.dataControlsState.maxValue,
                    onPointsChange = viewModel::updateDataPoints,
                    onRangeChange = viewModel::updateDataRange,
                )
            }
        },
    ) {
        val chartTitle =
            uiState.dataSet.series
                .firstOrNull()
                ?.name
                .orEmpty()
        when (uiState.preset) {
            LineDemoPreset.Default -> {
                LineChart(
                    data = uiState.dataSet,
                    modifier = Modifier.fillMaxWidth(),
                    title = chartTitle,
                    style = LineChartDefaults.style(chartContainerStyle = chartContainerStyle),
                )
            }

            LineDemoPreset.Timeline -> {
                LineChart(
                    data = uiState.dataSet,
                    modifier = Modifier.fillMaxWidth(),
                    title = chartTitle,
                    style = LineChartDefaults.style(chartContainerStyle = chartContainerStyle),
                    renderMode = LineChartRenderMode.Timeline,
                    animationDuration = timelineAnimationDuration.milliseconds,
                )
            }

            LineDemoPreset.Custom -> {
                LineChart(
                    data = uiState.dataSet,
                    modifier = Modifier.fillMaxWidth(),
                    title = chartTitle,
                    style = ChartTestStyleFixtures.lineCustomStyle(chartContainerStyle = chartContainerStyle),
                )
            }

            LineDemoPreset.ScaleDrop -> Unit
        }
    }
}

@Composable
private fun LineDemoPresetToggle(
    selectedPreset: LineDemoPreset,
    onPresetSelected: (LineDemoPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        LineDemoPresetItem(
            label = stringResource(Res.string.chart_default),
            selected = selectedPreset == LineDemoPreset.Default,
            onClick = { onPresetSelected(LineDemoPreset.Default) },
        )
        LineDemoPresetItem(
            label = stringResource(Res.string.chart_timeline),
            selected = selectedPreset == LineDemoPreset.Timeline,
            onClick = { onPresetSelected(LineDemoPreset.Timeline) },
        )
        LineDemoPresetItem(
            label = stringResource(Res.string.chart_scale_drop),
            selected = selectedPreset == LineDemoPreset.ScaleDrop,
            onClick = { onPresetSelected(LineDemoPreset.ScaleDrop) },
        )
        LineDemoPresetItem(
            label = stringResource(Res.string.chart_custom),
            selected = selectedPreset == LineDemoPreset.Custom,
            onClick = { onPresetSelected(LineDemoPreset.Custom) },
        )
    }
}

@Composable
private fun LineDataPointsControls(
    points: Int,
    minValue: Int,
    maxValue: Int,
    onPointsChange: (Int) -> Unit,
    onRangeChange: (Int, Int) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        DemoSlider(
            value = points,
            range = LineChartViewModel.MIN_SUPPORTED_POINTS..LineChartViewModel.MAX_SUPPORTED_POINTS,
            onValueSelected = onPointsChange,
        ) { draftPoints ->
            Text(
                text = stringResource(Res.string.line_data_points, draftPoints),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DemoRangeSlider(
            start = minValue,
            end = maxValue,
            range = LineChartViewModel.MIN_SUPPORTED_VALUE..LineChartViewModel.MAX_SUPPORTED_VALUE,
            onRangeSelected = onRangeChange,
        ) { draftMinValue, draftMaxValue ->
            Text(
                text = stringResource(Res.string.line_data_points_range, draftMinValue, draftMaxValue),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun LineDemoPresetItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(Dimens.md)
    val backgroundColor =
        if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        }
    val textColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        }

    Text(
        text = label,
        color = textColor,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        modifier =
            Modifier
                .clip(shape)
                .background(backgroundColor, shape)
                .clickable(onClick = onClick)
                .semantics { role = Role.Button }
                .padding(horizontal = 14.dp, vertical = Dimens.sm),
    )
}
