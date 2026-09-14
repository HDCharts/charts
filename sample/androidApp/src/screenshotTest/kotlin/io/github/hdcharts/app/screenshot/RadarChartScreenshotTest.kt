package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.RadarChartDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures

private const val SCREENSHOT_RADAR_DEFAULT_TITLE = "Platform Readiness Score"
private const val SCREENSHOT_RADAR_EDGE_TITLE = "Stress-Test Profile"
private const val SCREENSHOT_RADAR_NO_CATEGORIES_TITLE = "Multi-Series Without Category Labels"
private const val RADAR_SELECTED_AXIS_INDEX = 2

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartDefaultPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarDefaultData()
        RadarChart(
            data = data,
            title = SCREENSHOT_RADAR_DEFAULT_TITLE,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartSingleAxisLabelsPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarDefaultData()
        RadarChart(
            data = data,
            title = SCREENSHOT_RADAR_DEFAULT_TITLE,
            style =
                RadarChartDefaults.style(
                    chartContainerStyle = ChartContainerDefaults.style(),
                    axes = RadarChartDefaults.axes(labelVisible = true),
                    categories =
                        RadarChartDefaults.categories(
                            legendVisible = false,
                            pinsVisible = false,
                        ),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartSingleEdgePinsPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarEdgeData()
        RadarChart(
            data = data,
            title = SCREENSHOT_RADAR_EDGE_TITLE,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartCustomPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarSample()
        RadarChart(
            data = sample.customData,
            title = sample.title,
            style =
                ChartTestStyleFixtures.radarCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
                    seriesKeys = sample.seriesKeys,
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartMultiNoCategoriesPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarMultiNoCategoriesData()
        RadarChart(
            data = data,
            title = SCREENSHOT_RADAR_NO_CATEGORIES_TITLE,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartSelectedAxisPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarDefaultData()
        RadarChart(
            data = data,
            title = data.categories[RADAR_SELECTED_AXIS_INDEX],
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(RADAR_SELECTED_AXIS_INDEX),
        )
    }
}
