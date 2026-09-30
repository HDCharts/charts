package io.github.hdcharts.app.demo.multiline

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
import hdcharts.app.generated.resources.line_data_points
import hdcharts.app.generated.resources.line_data_points_range
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.DemoRangeSlider
import io.github.hdcharts.app.ui.composable.DemoSlider
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MultiLineChartDemo(viewModel: MultiLineChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartContainerStyle = ChartContainerDefaults.style()

    ChartDemo(
        onRefresh = viewModel::refresh,
        controlsContent = {
            MultiLineDataPointsControls(
                points = uiState.dataControlsState.points,
                minValue = uiState.dataControlsState.minValue,
                maxValue = uiState.dataControlsState.maxValue,
                onPointsChange = viewModel::updateDataPoints,
                onRangeChange = viewModel::updateDataRange,
            )
        },
    ) {
        LineChart(
            data = uiState.dataSet.dataSet,
            modifier = Modifier.fillMaxWidth(),
            title = uiState.dataSet.title,
            valueFormatter = ChartValueFormatters.suffix(" ms"),
            style = LineChartDefaults.style(chartContainerStyle = chartContainerStyle),
        )
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
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        DemoSlider(
            value = points,
            range = MultiLineChartViewModel.MIN_SUPPORTED_POINTS..MultiLineChartViewModel.MAX_SUPPORTED_POINTS,
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
            range = MultiLineChartViewModel.MIN_SUPPORTED_VALUE..MultiLineChartViewModel.MAX_SUPPORTED_VALUE,
            onRangeSelected = onRangeChange,
        ) { draftMinValue, draftMaxValue ->
            Text(
                text = stringResource(Res.string.line_data_points_range, draftMinValue, draftMaxValue),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
