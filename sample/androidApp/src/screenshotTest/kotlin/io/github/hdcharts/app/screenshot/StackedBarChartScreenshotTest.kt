package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.StackedBarChart
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartDefaultPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartCustomPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            style =
                ChartTestStyleFixtures.stackedBarCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
                    segmentCount = 4,
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartNoCategoriesPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarNoCategoriesDataSet()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartSelectedBarPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.dataSet.categories[1],
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(1),
        )
    }
}
