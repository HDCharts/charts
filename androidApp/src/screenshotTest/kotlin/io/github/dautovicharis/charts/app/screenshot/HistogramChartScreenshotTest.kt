package io.github.dautovicharis.charts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.dautovicharis.charts.app.screenshot.shared.SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE
import io.github.dautovicharis.charts.app.screenshot.shared.ScreenshotPreview
import io.github.dautovicharis.charts.app.screenshot.shared.ScreenshotSurface
import io.github.dautovicharis.charts.demoshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartDefaultPreview() {
    ScreenshotSurface {
        HistogramChart(
            dataSet = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartCustomPreview() {
    ScreenshotSurface {
        val dataSet = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet()
        HistogramChart(
            dataSet = dataSet,
            style = ChartTestStyleFixtures.histogramCustomStyle(chartViewStyle = ChartViewDefaults.style()),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartCustomBarColorsPreview() {
    ScreenshotSurface {
        val dataSet = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet()
        HistogramChart(
            dataSet = dataSet,
            style =
                ChartTestStyleFixtures.histogramCustomStyle(
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
fun HistogramChartSelectedBarPreview() {
    ScreenshotSurface {
        HistogramChart(
            dataSet = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selectedBarIndex = 1,
        )
    }
}
