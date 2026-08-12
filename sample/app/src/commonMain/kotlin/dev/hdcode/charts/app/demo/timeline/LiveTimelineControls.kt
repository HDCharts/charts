package dev.hdcode.charts.app.demo.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.timeline_update_interval
import chartsproject.app.generated.resources.timeline_window_size
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

data class LiveTimelineControlsState(
    val updateIntervalMs: Int = LiveTimelineDefaults.DEFAULT_UPDATE_INTERVAL_MS,
    val windowSize: Int = LiveTimelineDefaults.DEFAULT_WINDOW_SIZE,
)

object LiveTimelineDefaults {
    const val DEFAULT_UPDATE_INTERVAL_MS = 500
    const val MIN_UPDATE_INTERVAL_MS = 200
    const val MAX_UPDATE_INTERVAL_MS = 2000
    const val DEFAULT_WINDOW_SIZE = 100
    const val MIN_WINDOW_SIZE = 10
    const val MAX_WINDOW_SIZE = 120
}

@Composable
fun LiveTimelineControls(
    controlsState: LiveTimelineControlsState,
    onUpdateIntervalChange: (Int) -> Unit,
    onWindowSizeChange: (Int) -> Unit,
) {
    var draftIntervalMs by remember(controlsState.updateIntervalMs) {
        mutableFloatStateOf(controlsState.updateIntervalMs.toFloat())
    }
    var draftWindowSize by remember(controlsState.windowSize) {
        mutableFloatStateOf(controlsState.windowSize.toFloat())
    }
    val minUpdateInterval = LiveTimelineDefaults.MIN_UPDATE_INTERVAL_MS.toFloat()
    val maxUpdateInterval = LiveTimelineDefaults.MAX_UPDATE_INTERVAL_MS.toFloat()
    val minWindowSize = LiveTimelineDefaults.MIN_WINDOW_SIZE.toFloat()
    val maxWindowSize = LiveTimelineDefaults.MAX_WINDOW_SIZE.toFloat()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.timeline_update_interval, draftIntervalMs.roundToInt()),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = draftIntervalMs,
            valueRange = minUpdateInterval..maxUpdateInterval,
            onValueChange = { value -> draftIntervalMs = value },
            onValueChangeFinished = { onUpdateIntervalChange(draftIntervalMs.roundToInt()) },
        )

        Text(
            text = stringResource(Res.string.timeline_window_size, draftWindowSize.roundToInt()),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = draftWindowSize,
            valueRange = minWindowSize..maxWindowSize,
            onValueChange = { value -> draftWindowSize = value },
            onValueChangeFinished = { onWindowSizeChange(draftWindowSize.roundToInt()) },
        )
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
