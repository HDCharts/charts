package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.radar.RadarChart

private const val RADAR_SELECTED_AXIS_INDEX = 3

/**
 * A full-width single series draws its axis labels and shows no legend, so the radar stays centered
 * rather than pinned to the start edge.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.deterministic(series = 1)
        RadarChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartSelectedAxisPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.deterministic(series = 1).data
        RadarChart(
            data = data,
            title = data.categories[RADAR_SELECTED_AXIS_INDEX],
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(RADAR_SELECTED_AXIS_INDEX),
        )
    }
}
