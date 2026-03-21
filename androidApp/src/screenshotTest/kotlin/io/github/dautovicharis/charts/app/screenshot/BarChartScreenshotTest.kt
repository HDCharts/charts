package io.github.dautovicharis.charts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.dautovicharis.charts.app.screenshot.shared.SCREENSHOT_BAR_SAMPLE_USE_CASE
import io.github.dautovicharis.charts.app.screenshot.shared.ScreenshotPreview
import io.github.dautovicharis.charts.app.screenshot.shared.ScreenshotSurface
import io.github.dautovicharis.charts.demoshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartDefaultPreview() {
    ScreenshotSurface {
        BarChart(
            dataSet = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartCustomPreview() {
    ScreenshotSurface {
        val dataSet = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet()
        BarChart(
            dataSet = dataSet,
            style = ChartTestStyleFixtures.barCustomStyle(chartViewStyle = ChartViewDefaults.style()),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartCustomBarColorsPreview() {
    ScreenshotSurface {
        val dataSet = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet()
        BarChart(
            dataSet = dataSet,
            style =
                ChartTestStyleFixtures.barCustomStyle(
                    chartViewStyle = ChartViewDefaults.style(),
                    barCount = dataSet.data.item.points.size,
                    useBarColors = true,
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartSelectedBarPreview() {
    ScreenshotSurface {
        BarChart(
            dataSet = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selectedBarIndex = 1,
        )
    }
}
