package io.github.hdcharts.app.demo.stackedbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.stacked_bar_data_points
import hdcharts.app.generated.resources.stacked_bar_data_points_range
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.DemoRangeSlider
import io.github.hdcharts.app.ui.composable.DemoSlider
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.sampleshared.theme.Dimens
import io.github.hdcharts.stackedbar.StackedBarChart
import io.github.hdcharts.stackedbar.StackedBarChartDefaults
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StackedBarChartDemo(viewModel: StackedBarChartViewModel = koinViewModel()) {
    StackedBarChartDemoContent(viewModel = viewModel, gradient = null)
}

@Composable
fun StackedBarGradientDemo(viewModel: StackedBarChartViewModel = koinViewModel()) {
    StackedBarChartDemoContent(viewModel = viewModel, gradient = ChartGradients.fade(endAlpha = 0.35f))
}

@Composable
private fun StackedBarChartDemoContent(
    viewModel: StackedBarChartViewModel,
    gradient: ChartGradient?,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartContainerStyle = ChartContainerDefaults.style()

    ChartDemo(
        onRefresh = viewModel::refresh,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
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
        StackedBarChart(
            modifier = Modifier.fillMaxSize(),
            data = uiState.chart.dataSet,
            title = uiState.chart.title,
            style =
                StackedBarChartDefaults.style(
                    chartContainerStyle = chartContainerStyle,
                    segments = StackedBarChartDefaults.segments(gradient = gradient),
                ),
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
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        DemoSlider(
            value = points,
            range = StackedBarChartViewModel.MIN_SUPPORTED_POINTS..StackedBarChartViewModel.MAX_SUPPORTED_POINTS,
            onValueSelected = onPointsChange,
        ) { draftPoints ->
            Text(
                text = stringResource(Res.string.stacked_bar_data_points, draftPoints),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DemoRangeSlider(
            start = minValue,
            end = maxValue,
            range = StackedBarChartViewModel.MIN_SUPPORTED_VALUE..StackedBarChartViewModel.MAX_SUPPORTED_VALUE,
            onRangeSelected = onRangeChange,
        ) { draftMinValue, draftMaxValue ->
            Text(
                text = stringResource(Res.string.stacked_bar_data_points_range, draftMinValue, draftMaxValue),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
