package dev.hdcode.charts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE
import dev.hdcode.charts.app.screenshot.shared.ScreenshotPreview
import dev.hdcode.charts.app.screenshot.shared.ScreenshotSurface
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.style.ChartContainerDefaults

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartDefaultPreview() {
    ScreenshotSurface {
        StackedBarChart(
            dataSet = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample().dataSet,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartCustomPreview() {
    ScreenshotSurface {
        StackedBarChart(
            dataSet = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample().dataSet,
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
        StackedBarChart(
            dataSet = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarNoCategoriesDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartSelectedBarPreview() {
    ScreenshotSurface {
        StackedBarChart(
            dataSet = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample().dataSet,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selectedBarIndex = 1,
        )
    }
}
