package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.radar.RadarChart
import io.github.hdcharts.radar.RadarChartDefaults
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors

private const val SELECTED_AXIS_INDEX = 3

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesRadarChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarSample()
        RadarChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** With several series, the legend shows each series' value on the selected axis. */
@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesRadarChartSelectedAxisPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarSample()
        RadarChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(SELECTED_AXIS_INDEX),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesRadarChartNoCategoriesPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarNoCategoriesSample()
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
fun MultiSeriesRadarChartHiddenFillPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarSample()
        RadarChart(
            data = sample.data,
            title = sample.title,
            style = RadarChartDefaults.style(polygon = RadarChartDefaults.polygon(fillVisible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesRadarChartSeriesColorsPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarSample()
        RadarChart(
            data = sample.data,
            title = sample.title,
            style =
                RadarChartDefaults.style(
                    polygon =
                        RadarChartDefaults.polygon(
                            lineColors = LocalChartColors.current.seriesColors(sample.seriesKeys),
                        ),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
