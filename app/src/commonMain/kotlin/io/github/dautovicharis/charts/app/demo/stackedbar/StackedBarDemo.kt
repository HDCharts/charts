package io.github.dautovicharis.charts.app.demo.stackedbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.cd_pause_live_updates
import chartsproject.app.generated.resources.cd_play_live_updates
import chartsproject.app.generated.resources.stacked_bar_data_points
import chartsproject.app.generated.resources.stacked_bar_data_points_range
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioPreset
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioToggle
import io.github.dautovicharis.charts.app.ui.composable.ChartDemo
import io.github.dautovicharis.charts.app.ui.composable.ChartPreset
import io.github.dautovicharis.charts.app.ui.composable.ChartPresetToggle
import io.github.dautovicharis.charts.app.ui.composable.StyleItems
import io.github.dautovicharis.charts.demoshared.theme.LocalChartColors
import io.github.dautovicharis.charts.demoshared.theme.seriesColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun StackedBarChartDemo(
    viewModel: StackedBarChartViewModel = koinViewModel(),
    onStyleItemsChanged: (StyleItems?) -> Unit = {},
) {
    val dataSet by viewModel.dataSet.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val controlsState by viewModel.controlsState.collectAsStateWithLifecycle()
    val chartColors = LocalChartColors.current
    var preset by remember { mutableStateOf(ChartPreset.Default) }
    var aspectRatioPreset by remember { mutableStateOf(ChartAspectRatioPreset.Square) }
    val barColors =
        remember(dataSet.segmentKeys, chartColors) {
            chartColors.seriesColors(dataSet.segmentKeys.size)
        }

    val refresh: () -> Unit = viewModel::refresh

    val styleItems =
        when (preset) {
            ChartPreset.Default -> StackedBarChartStyleItems.default(aspectRatioPreset)
            ChartPreset.Custom -> StackedBarChartStyleItems.custom(barColors, aspectRatioPreset)
        }

    ChartDemo(
        styleItems = styleItems,
        onRefresh = refresh,
        onStyleItemsChanged = onStyleItemsChanged,
        presetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ChartPresetToggle(
                    selectedPreset = preset,
                    onPresetSelected = { preset = it },
                )
                ChartAspectRatioToggle(
                    selectedPreset = aspectRatioPreset,
                    onPresetSelected = { aspectRatioPreset = it },
                )
            }
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
        controlsContent = {
            StackedBarDataPointsControls(
                points = controlsState.points,
                minValue = controlsState.minValue,
                maxValue = controlsState.maxValue,
                onPointsChange = viewModel::updateDataPoints,
                onRangeChange = viewModel::updateDataRange,
            )
        },
    ) {
        key(controlsState.points, controlsState.minValue, controlsState.maxValue, preset, aspectRatioPreset) {
            when (preset) {
                ChartPreset.Default -> {
                    StackedBarChart(
                        dataSet = dataSet.dataSet,
                        style = StackedBarChartStyleItems.defaultStyle(aspectRatioPreset),
                    )
                }

                ChartPreset.Custom -> {
                    StackedBarChart(
                        dataSet = dataSet.dataSet,
                        style = StackedBarChartStyleItems.customStyle(barColors, aspectRatioPreset),
                    )
                }
            }
        }
    }
}

@Composable
private fun StackedBarDataPointsControls(
    points: Int,
    minValue: Int,
    maxValue: Int,
    onPointsChange: (Int) -> Unit,
    onRangeChange: (Int, Int) -> Unit,
) {
    val minPointsSupported = StackedBarChartViewModel.MIN_SUPPORTED_POINTS.toFloat()
    val maxPointsSupported = StackedBarChartViewModel.MAX_SUPPORTED_POINTS.toFloat()
    val minValueSupported = StackedBarChartViewModel.MIN_SUPPORTED_VALUE.toFloat()
    val maxValueSupported = StackedBarChartViewModel.MAX_SUPPORTED_VALUE.toFloat()
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
            text = stringResource(Res.string.stacked_bar_data_points, draftPoints.roundToInt()),
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
                    Res.string.stacked_bar_data_points_range,
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
