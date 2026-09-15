package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.StackedAreaChart
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartDefaultPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaSample()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartCustomPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaSample()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            style =
                ChartTestStyleFixtures.stackedAreaCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
                    seriesCount = 3,
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartNoCategoriesPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaNoCategoriesData()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartSelectedPointPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaSample()
        StackedAreaChart(
            data = sample.data,
            title = sample.data.categories[1],
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(1),
        )
    }
}
