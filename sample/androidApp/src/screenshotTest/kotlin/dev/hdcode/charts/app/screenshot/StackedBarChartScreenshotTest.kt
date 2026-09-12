package dev.hdcode.charts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE
import dev.hdcode.charts.app.screenshot.shared.ScreenshotPreview
import dev.hdcode.charts.app.screenshot.shared.ScreenshotSurface
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.ChartContainerDefaults

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
