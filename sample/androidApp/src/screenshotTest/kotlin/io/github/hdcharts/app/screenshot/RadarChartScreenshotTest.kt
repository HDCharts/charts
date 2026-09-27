package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.RadarChartDefaults

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
