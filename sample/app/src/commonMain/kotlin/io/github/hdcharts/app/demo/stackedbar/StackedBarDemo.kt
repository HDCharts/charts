package io.github.hdcharts.app.demo.stackedbar

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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.cd_pause_live_updates
import hdcharts.app.generated.resources.cd_play_live_updates
import hdcharts.app.generated.resources.stacked_bar_data_points
import hdcharts.app.generated.resources.stacked_bar_data_points_range
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.ChartPreset
import io.github.hdcharts.app.ui.composable.ChartPresetToggle
import io.github.hdcharts.charts.StackedBarChart
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.StackedBarChartDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.hdcharts.sampleshared.theme.Dimens
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun StackedBarChartDemo(viewModel: StackedBarChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartContainerStyle = ChartContainerDefaults.style()
    val segmentColors =
        LocalChartColors.current.seriesColors(uiState.chart.segmentKeys.size)

    ChartDemo(
        onRefresh = viewModel::refresh,
        presetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.controlSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ChartPresetToggle(
                    selectedPreset = uiState.preset,
                    onPresetSelected = viewModel::onPresetSelected,
                )
            }
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
        controlsContent = {
            StackedBarDataPointsControls(
                points = uiState.controlsState.points,
                minValue = uiState.controlsState.minValue,
                maxValue = uiState.controlsState.maxValue,
                onPointsChange = viewModel::updateDataPoints,
                onRangeChange = viewModel::updateDataRange,
            )
        },
    ) {
        val style =
            when (uiState.preset) {
                ChartPreset.Default -> StackedBarChartDefaults.style(chartContainerStyle = chartContainerStyle)
                ChartPreset.Custom ->
                    ChartTestStyleFixtures.stackedBarCustomStyle(
                        chartContainerStyle = chartContainerStyle,
                        segmentCount = segmentColors.size,
                    )
            }
        StackedBarChart(
            data = uiState.chart.dataSet,
            modifier = Modifier.fillMaxWidth(),
            title = uiState.chart.title,
            style = style,
        )
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
                .padding(top = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
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
