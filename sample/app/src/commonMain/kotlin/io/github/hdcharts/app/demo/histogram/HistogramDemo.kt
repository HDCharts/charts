package io.github.hdcharts.app.demo.histogram

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.histogram_data_points
import hdcharts.app.generated.resources.histogram_data_points_range
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.DemoRangeSlider
import io.github.hdcharts.app.ui.composable.DemoSlider
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.histogram.HistogramChart
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistogramChartDemo(viewModel: HistogramChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartContainerStyle = ChartContainerDefaults.style()

    ChartDemo(
        onRefresh = viewModel::refresh,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
        },
        controlsContent = {
            HistogramDataPointsControls(
                points = uiState.controlsState.points,
                minValue = uiState.controlsState.minValue,
                maxValue = uiState.controlsState.maxValue,
                onPointsChange = viewModel::updateDataPoints,
                onRangeChange = viewModel::updateDataRange,
            )
        },
    ) {
        HistogramChart(
            data = uiState.dataSet,
            modifier = Modifier.fillMaxWidth(),
            title =
                uiState.dataSet.series
                    .single()
                    .name,
            style = HistogramChartDefaults.style(chartContainerStyle = chartContainerStyle),
        )
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
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        DemoSlider(
            value = points,
            range = HistogramChartViewModel.MIN_SUPPORTED_POINTS..HistogramChartViewModel.MAX_SUPPORTED_POINTS,
            onValueSelected = onPointsChange,
        ) { draftPoints ->
            Text(
                text = stringResource(Res.string.histogram_data_points, draftPoints),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DemoRangeSlider(
            start = minValue,
            end = maxValue,
            range = HistogramChartViewModel.MIN_SUPPORTED_VALUE..HistogramChartViewModel.MAX_SUPPORTED_VALUE,
            onRangeSelected = onRangeChange,
        ) { draftMinValue, draftMaxValue ->
            Text(
                text = stringResource(Res.string.histogram_data_points_range, draftMinValue, draftMaxValue),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
