package io.github.hdcharts.app.demo.line

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hdcharts.app.demo.timeline.LiveTimelineControls
import io.github.hdcharts.app.demo.timeline.timelineAnimationDurationMillis
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.charts.LiveLineChart
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.LineChartDefaults
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LiveLineChartDemo(viewModel: LiveLineChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val animationDuration = timelineAnimationDurationMillis(uiState.controlsState.updateIntervalMs)
    val chartContainerStyle = ChartContainerDefaults.style()

    LifecycleStartEffect(viewModel) {
        viewModel.onEnterDemo()
        onStopOrDispose { viewModel.onLeaveDemo() }
    }

    ChartDemo(
        onRefresh = {},
        refreshVisible = false,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
        },
        controlsContent = {
            LiveTimelineControls(
                controlsState = uiState.controlsState,
                onUpdateIntervalChange = viewModel::updateInterval,
                onWindowSizeChange = viewModel::updateWindowSize,
            )
        },
    ) {
        LiveLineChart(
            data = uiState.dataSet,
            modifier = Modifier.fillMaxWidth(),
            title =
                uiState.dataSet.series
                    .firstOrNull()
                    ?.name
                    .orEmpty(),
            style = LineChartDefaults.style(chartContainerStyle = chartContainerStyle),
            shiftDuration = animationDuration.milliseconds,
        )
    }
}
