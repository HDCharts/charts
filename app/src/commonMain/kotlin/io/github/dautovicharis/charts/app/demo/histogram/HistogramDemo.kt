package io.github.dautovicharis.charts.app.demo.histogram

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
import chartsproject.app.generated.resources.histogram_data_points
import chartsproject.app.generated.resources.histogram_data_points_range
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioPreset
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioToggle
import io.github.dautovicharis.charts.app.ui.composable.ChartDemo
import io.github.dautovicharis.charts.app.ui.composable.ChartPreset
import io.github.dautovicharis.charts.app.ui.composable.ChartPresetToggle
import io.github.dautovicharis.charts.app.ui.composable.StyleItems
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun HistogramChartDemo(
    viewModel: HistogramChartViewModel = koinViewModel(),
    onStyleItemsChanged: (StyleItems?) -> Unit = {},
) {
    val dataSet by viewModel.dataSet.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val controlsState by viewModel.controlsState.collectAsStateWithLifecycle()
    var preset by remember { mutableStateOf(ChartPreset.Default) }
    var aspectRatioPreset by remember { mutableStateOf(ChartAspectRatioPreset.Square) }
    val refresh: () -> Unit = viewModel::refresh

    val styleItems =
        when (preset) {
            ChartPreset.Default -> HistogramChartStyleItems.default(aspectRatioPreset)
            ChartPreset.Custom ->
                HistogramChartStyleItems.custom(
                    barCount = dataSet.data.item.points.size,
                    minValue = controlsState.minValue.toFloat(),
                    maxValue = controlsState.maxValue.toFloat(),
                    aspectRatioPreset = aspectRatioPreset,
                )
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
            HistogramDataPointsControls(
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
                    HistogramChart(
                        dataSet,
                        style = HistogramChartStyleItems.defaultStyle(aspectRatioPreset),
                    )
                }

                ChartPreset.Custom -> {
                    HistogramChart(
                        dataSet = dataSet,
                        style =
                            HistogramChartStyleItems.customStyle(
                                barCount = dataSet.data.item.points.size,
                                minValue = controlsState.minValue.toFloat(),
                                maxValue = controlsState.maxValue.toFloat(),
                                aspectRatioPreset = aspectRatioPreset,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistogramDataPointsControls(
    points: Int,
    minValue: Int,
    maxValue: Int,
    onPointsChange: (Int) -> Unit,
    onRangeChange: (Int, Int) -> Unit,
) {
    val minPointsSupported = HistogramChartViewModel.MIN_SUPPORTED_POINTS.toFloat()
    val maxPointsSupported = HistogramChartViewModel.MAX_SUPPORTED_POINTS.toFloat()
    val minValueSupported = HistogramChartViewModel.MIN_SUPPORTED_VALUE.toFloat()
    val maxValueSupported = HistogramChartViewModel.MAX_SUPPORTED_VALUE.toFloat()
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
            text = stringResource(Res.string.histogram_data_points, draftPoints.roundToInt()),
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
                    Res.string.histogram_data_points_range,
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
