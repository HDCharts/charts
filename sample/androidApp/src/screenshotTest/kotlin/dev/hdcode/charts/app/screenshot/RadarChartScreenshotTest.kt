package dev.hdcode.charts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import dev.hdcode.charts.app.screenshot.shared.ScreenshotPreview
import dev.hdcode.charts.app.screenshot.shared.ScreenshotSurface
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.RadarChart
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.RadarChartDefaults

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartDefaultPreview() {
    ScreenshotSurface {
        RadarChart(
            dataSet = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarDefaultDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartSingleAxisLabelsPreview() {
    ScreenshotSurface {
        RadarChart(
            dataSet = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarDefaultDataSet(),
            style =
                RadarChartDefaults.style(
                    chartContainerStyle = ChartContainerDefaults.style(),
                    axisLabelVisible = true,
                    categoryLegendVisible = false,
                    categoryPinsVisible = false,
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
        RadarChart(
            dataSet = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarEdgeDataSet(),
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
            dataSet = sample.customDataSet,
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
        RadarChart(
            dataSet = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarMultiNoCategoriesDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RadarChartSelectedAxisPreview() {
    ScreenshotSurface {
        RadarChart(
            dataSet = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarDefaultDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selectedAxisIndex = 2,
        )
    }
}
