package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_SCATTER_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.scatter.ScatterChart

// The second point of the second series, after the four North points.
private const val SELECTED_BUBBLE_INDEX = 5

@PreviewTest
@ScreenshotPreview
@Composable
fun ScatterChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_SCATTER_SAMPLE_USE_CASE.deterministic()
        ScatterChart(data = sample.data, title = sample.title)
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun ScatterChartBubblePreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_SCATTER_SAMPLE_USE_CASE.deterministic(bubbles = true)
        ScatterChart(data = sample.data, title = sample.title)
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun ScatterChartSelectedBubblePreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_SCATTER_SAMPLE_USE_CASE.deterministic(bubbles = true)
        ScatterChart(
            data = sample.data,
            title = sample.title,
            selection = staticChartSelection(SELECTED_BUBBLE_INDEX),
            interactionEnabled = false,
        )
    }
}
