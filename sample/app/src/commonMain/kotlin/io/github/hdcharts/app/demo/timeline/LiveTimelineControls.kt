package io.github.hdcharts.app.demo.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.timeline_update_interval
import hdcharts.app.generated.resources.timeline_window_size
import io.github.hdcharts.app.ui.composable.DemoSlider
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.stringResource

data class LiveTimelineControlsState(
    val updateIntervalMs: Int = LiveTimelineDefaults.DEFAULT_UPDATE_INTERVAL_MS,
    val windowSize: Int = LiveTimelineDefaults.DEFAULT_WINDOW_SIZE,
)

object LiveTimelineDefaults {
    const val DEFAULT_UPDATE_INTERVAL_MS = 200
    const val MIN_UPDATE_INTERVAL_MS = 200
    const val MAX_UPDATE_INTERVAL_MS = 2000
    const val MIN_WINDOW_SIZE = 10
    const val MAX_WINDOW_SIZE = 120
    const val DEFAULT_WINDOW_SIZE = MAX_WINDOW_SIZE
}

@Composable
fun LiveTimelineControls(
    controlsState: LiveTimelineControlsState,
    onUpdateIntervalChange: (Int) -> Unit,
    onWindowSizeChange: (Int) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        DemoSlider(
            value = controlsState.updateIntervalMs,
            range = LiveTimelineDefaults.MIN_UPDATE_INTERVAL_MS..LiveTimelineDefaults.MAX_UPDATE_INTERVAL_MS,
            onValueSelected = onUpdateIntervalChange,
        ) { draftIntervalMs ->
            Text(
                text = stringResource(Res.string.timeline_update_interval, draftIntervalMs),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DemoSlider(
            value = controlsState.windowSize,
            range = LiveTimelineDefaults.MIN_WINDOW_SIZE..LiveTimelineDefaults.MAX_WINDOW_SIZE,
            onValueSelected = onWindowSizeChange,
        ) { draftWindowSize ->
            Text(
                text = stringResource(Res.string.timeline_window_size, draftWindowSize),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

fun timelineAnimationDurationMillis(updateIntervalMs: Int): Int {
    val safeInterval =
        updateIntervalMs.coerceIn(
            minimumValue = LiveTimelineDefaults.MIN_UPDATE_INTERVAL_MS,
            maximumValue = LiveTimelineDefaults.MAX_UPDATE_INTERVAL_MS,
        )
    val target = (safeInterval * 0.8f).toInt()
    val maxDuration = (safeInterval - 40).coerceAtLeast(120)
    return target.coerceIn(120, maxDuration)
}
