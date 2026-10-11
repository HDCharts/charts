package io.github.hdcharts.app.demo.bar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.bar_data_points
import hdcharts.app.generated.resources.bar_data_points_range
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.DemoBarGradient
import io.github.hdcharts.app.ui.composable.DemoBarGradientPicker
import io.github.hdcharts.app.ui.composable.DemoRangeSlider
import io.github.hdcharts.app.ui.composable.DemoSlider
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BarChartDemo(viewModel: BarChartViewModel = koinViewModel()) {
    BarChartDemoContent(viewModel = viewModel, gradient = null)
}

@Composable
fun BarGradientDemo(viewModel: BarChartViewModel = koinViewModel()) {
    var gradient by rememberSaveable { mutableStateOf(DemoBarGradient.PerBar) }

    BarChartDemoContent(
        viewModel = viewModel,
        gradient = gradient.gradient,
        gradientControls = { DemoBarGradientPicker(selected = gradient, onSelected = { gradient = it }) },
    )
}

@Composable
private fun BarChartDemoContent(
    viewModel: BarChartViewModel,
    gradient: ChartGradient?,
    gradientControls: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartContainerStyle = ChartContainerDefaults.style()

    ChartDemo(
        onRefresh = viewModel::refresh,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
        },
        controlsContent = {
            gradientControls()
            BarDataPointsControls(
                points = uiState.controlsState.points,
                minValue = uiState.controlsState.minValue,
                maxValue = uiState.controlsState.maxValue,
                onPointsChange = viewModel::updateDataPoints,
                onRangeChange = viewModel::updateDataRange,
            )
        },
    ) {
        BarChart(
            data = uiState.dataSet,
            modifier = Modifier.fillMaxWidth(),
            title =
                uiState.dataSet.series
                    .single()
                    .name,
            style =
                BarChartDefaults.style(
                    chartContainerStyle = chartContainerStyle,
                    bars = BarChartDefaults.bars(gradient = gradient),
                ),
        )
    }
}

@Composable
private fun BarDataPointsControls(
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
            range = BarChartViewModel.MIN_SUPPORTED_POINTS..BarChartViewModel.MAX_SUPPORTED_POINTS,
            onValueSelected = onPointsChange,
        ) { draftPoints ->
            Text(
                text = stringResource(Res.string.bar_data_points, draftPoints),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DemoRangeSlider(
            start = minValue,
            end = maxValue,
            range = BarChartViewModel.MIN_SUPPORTED_VALUE..BarChartViewModel.MAX_SUPPORTED_VALUE,
            onRangeSelected = onRangeChange,
        ) { draftMinValue, draftMaxValue ->
            Text(
                text = stringResource(Res.string.bar_data_points_range, draftMinValue, draftMaxValue),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
