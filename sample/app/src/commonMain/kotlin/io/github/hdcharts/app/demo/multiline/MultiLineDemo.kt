package io.github.hdcharts.app.demo.multiline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import hdcharts.app.generated.resources.chart_timeline
import hdcharts.app.generated.resources.line_data_points
import hdcharts.app.generated.resources.line_data_points_range
import io.github.hdcharts.app.demo.timeline.LiveTimelineControls
import io.github.hdcharts.app.demo.timeline.timelineAnimationDurationMillis
import io.github.hdcharts.app.ui.composable.ChartAspectRatioPreset
import io.github.hdcharts.app.ui.composable.ChartAspectRatioToggle
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.toChartModifier
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.model.ChartValueFormatters
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.LineChartDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MultiLineChartDemo(viewModel: MultiLineChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartColors = LocalChartColors.current
    val lineColors = chartColors.seriesColors(uiState.dataSet.seriesKeys.size)
    val timelineAnimationDuration = timelineAnimationDurationMillis(uiState.controlsState.updateIntervalMs)
    var aspectRatioPreset by remember { mutableStateOf(ChartAspectRatioPreset.Square) }
    val chartModifier = aspectRatioPreset.toChartModifier()
    val chartContainerStyle = ChartContainerDefaults.style()

    ChartDemo(
        onRefresh = viewModel::refreshForSelectedPreset,
        refreshVisible = uiState.preset != MultiLineDemoPreset.Timeline,
        presetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MultiLineDemoPresetToggle(
                    selectedPreset = uiState.preset,
                    onPresetSelected = viewModel::onPresetSelected,
                )
                ChartAspectRatioToggle(
                    selectedPreset = aspectRatioPreset,
                    onPresetSelected = { aspectRatioPreset = it },
                )
            }
        },
        extraButtons = {
            if (uiState.preset == MultiLineDemoPreset.Timeline) {
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
            if (uiState.preset == MultiLineDemoPreset.Timeline) {
                LiveTimelineControls(
                    controlsState = uiState.controlsState,
                    onUpdateIntervalChange = viewModel::updateInterval,
                    onWindowSizeChange = viewModel::updateWindowSize,
                )
            } else {
                MultiLineDataPointsControls(
                    points = uiState.dataControlsState.points,
                    minValue = uiState.dataControlsState.minValue,
                    maxValue = uiState.dataControlsState.maxValue,
                    onPointsChange = viewModel::updateDataPoints,
                    onRangeChange = viewModel::updateDataRange,
                )
            }
        },
    ) {
        when (uiState.preset) {
            MultiLineDemoPreset.Default -> {
                LineChart(
                    data = uiState.dataSet.dataSet,
                    modifier = chartModifier,
                    title = uiState.dataSet.title,
                    valueFormatter = ChartValueFormatters.suffix(" ms"),
                    style = LineChartDefaults.style(chartContainerStyle = chartContainerStyle),
                )
            }

            MultiLineDemoPreset.Timeline -> {
                LineChart(
                    data = uiState.dataSet.dataSet,
                    modifier = chartModifier,
                    title = uiState.dataSet.title,
                    valueFormatter = ChartValueFormatters.suffix(" ms"),
                    style = LineChartDefaults.style(chartContainerStyle = chartContainerStyle),
                    renderMode = LineChartRenderMode.Timeline,
                    animationDuration = timelineAnimationDuration.milliseconds,
                )
            }

            MultiLineDemoPreset.Custom -> {
                val customStyle =
                    ChartTestStyleFixtures.multiLineCustomStyle(
                        chartContainerStyle = chartContainerStyle,
                        seriesCount = lineColors.size,
                    )
                LineChart(
                    data = uiState.dataSet.dataSet,
                    modifier = chartModifier,
                    title = uiState.dataSet.title,
                    valueFormatter = ChartValueFormatters.suffix(" ms"),
                    style = customStyle,
                )
            }
        }
    }
}

@Composable
private fun MultiLineDataPointsControls(
    points: Int,
    minValue: Int,
    maxValue: Int,
    onPointsChange: (Int) -> Unit,
    onRangeChange: (Int, Int) -> Unit,
) {
    val minPointsSupported = MultiLineChartViewModel.MIN_SUPPORTED_POINTS.toFloat()
    val maxPointsSupported = MultiLineChartViewModel.MAX_SUPPORTED_POINTS.toFloat()
    val minValueSupported = MultiLineChartViewModel.MIN_SUPPORTED_VALUE.toFloat()
    val maxValueSupported = MultiLineChartViewModel.MAX_SUPPORTED_VALUE.toFloat()
    var draftPoints by remember(points) { mutableFloatStateOf(points.toFloat()) }
    var draftRange by remember(minValue, maxValue) { mutableStateOf(minValue.toFloat()..maxValue.toFloat()) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.line_data_points, draftPoints.roundToInt()),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = draftPoints,
            valueRange = minPointsSupported..maxPointsSupported,
            onValueChange = { draftPoints = it },
            onValueChangeFinished = { onPointsChange(draftPoints.roundToInt()) },
        )
        Text(
            text =
                stringResource(
                    Res.string.line_data_points_range,
                    draftRange.start.roundToInt(),
                    draftRange.endInclusive.roundToInt(),
                ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        RangeSlider(
            value = draftRange,
            valueRange = minValueSupported..maxValueSupported,
            onValueChange = { draftRange = it },
            onValueChangeFinished = {
                onRangeChange(
                    draftRange.start.roundToInt(),
                    draftRange.endInclusive.roundToInt(),
                )
            },
        )
    }
}

@Composable
private fun MultiLineDemoPresetToggle(
    selectedPreset: MultiLineDemoPreset,
    onPresetSelected: (MultiLineDemoPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MultiLineDemoPresetItem(
            label = stringResource(Res.string.chart_default),
            selected = selectedPreset == MultiLineDemoPreset.Default,
            onClick = { onPresetSelected(MultiLineDemoPreset.Default) },
        )
        MultiLineDemoPresetItem(
            label = stringResource(Res.string.chart_timeline),
            selected = selectedPreset == MultiLineDemoPreset.Timeline,
            onClick = { onPresetSelected(MultiLineDemoPreset.Timeline) },
        )
        MultiLineDemoPresetItem(
            label = stringResource(Res.string.chart_custom),
            selected = selectedPreset == MultiLineDemoPreset.Custom,
            onClick = { onPresetSelected(MultiLineDemoPreset.Custom) },
        )
    }
}

@Composable
private fun MultiLineDemoPresetItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
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
                .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
