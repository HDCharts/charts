package io.github.hdcharts.app.demo.line

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.timeline_scale_switch
import hdcharts.app.generated.resources.timeline_scale_switch_hint
import io.github.hdcharts.app.demo.timeline.LiveTimelineControls
import io.github.hdcharts.app.demo.timeline.LiveTimelineControlsState
import io.github.hdcharts.app.demo.timeline.timelineAnimationDurationMillis
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.app.ui.composable.DemoSlider
import io.github.hdcharts.app.ui.composable.PlayPauseButton
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.line.LiveLineChart
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

/**
 * Live line chart whose values swing between magnitudes.
 *
 * The window runs in the millions, drops below one hundred, and climbs back, so [LiveLineChart] is
 * seen rescaling while it streams.
 */
@Composable
fun LineScaleDropDemo(
    modifier: Modifier = Modifier,
    viewModel: LineScaleDropViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val controls = uiState.controlsState
    val animationDuration = timelineAnimationDurationMillis(controls.updateIntervalMs)
    val chartContainerStyle = ChartContainerDefaults.style()

    LifecycleStartEffect(viewModel) {
        viewModel.onEnterDemo()
        onStopOrDispose { viewModel.onLeaveDemo() }
    }

    ChartDemo(
        onRefresh = {},
        modifier = modifier,
        refreshVisible = false,
        extraButtons = {
            PlayPauseButton(isPlaying = uiState.isPlaying, onToggle = viewModel::togglePlaying)
        },
        controlsContent = {
            LiveTimelineControls(
                controlsState =
                    LiveTimelineControlsState(
                        updateIntervalMs = controls.updateIntervalMs,
                        windowSize = controls.windowSize,
                    ),
                onUpdateIntervalChange = viewModel::updateInterval,
                onWindowSizeChange = viewModel::updateWindowSize,
            )
            LineScaleSwitchControl(
                scaleSwitchPoints = controls.scaleSwitchPoints,
                onScaleSwitchPointsChange = viewModel::updateScaleSwitchPoints,
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

@Composable
private fun LineScaleSwitchControl(
    scaleSwitchPoints: Int,
    onScaleSwitchPointsChange: (Int) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        DemoSlider(
            value = scaleSwitchPoints,
            range = LineScaleDropViewModel.SWITCH_POINTS_RANGE,
            onValueSelected = onScaleSwitchPointsChange,
        ) { draftSwitchPoints ->
            Text(
                text = stringResource(Res.string.timeline_scale_switch, draftSwitchPoints),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = stringResource(Res.string.timeline_scale_switch_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}
