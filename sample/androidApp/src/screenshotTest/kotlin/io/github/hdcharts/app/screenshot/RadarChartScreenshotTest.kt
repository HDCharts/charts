package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.radar.RadarChart
import io.github.hdcharts.radar.RadarChartDefaults

private const val RADAR_SELECTED_AXIS_INDEX = 3

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartDefaultPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialSingleSeriesRadarData()
        RadarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartSelectedAxisPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialSingleSeriesRadarData()
        RadarChart(
            data = data,
            title = data.categories[RADAR_SELECTED_AXIS_INDEX],
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(RADAR_SELECTED_AXIS_INDEX),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartAxisLabelsPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialSingleSeriesRadarData()
        RadarChart(
            data = data,
            title = data.series.single().name,
            style = RadarChartDefaults.style(axes = RadarChartDefaults.axes(labelVisible = true)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A full-width chart with no legend keeps the radar centered, not pinned to the start edge. */
@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartHiddenLegendPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialSingleSeriesRadarData()
        RadarChart(
            data = data,
            modifier = Modifier.fillMaxWidth(),
            title = data.series.single().name,
            style = RadarChartDefaults.style(categories = RadarChartDefaults.categories(legendVisible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
