package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.HistogramChart
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartDefaultPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet()
        HistogramChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartCustomPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet()
        HistogramChart(
            data = data,
            title = data.series.single().name,
            style = ChartTestStyleFixtures.histogramCustomStyle(chartContainerStyle = ChartContainerDefaults.style()),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartCustomBarColorsPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet()
        HistogramChart(
            data = data,
            title = data.series.single().name,
            style =
                ChartTestStyleFixtures.histogramCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
                    barCount =
                        data.series
                            .single()
                            .values.size,
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
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.initialHistogramDataSet()
        HistogramChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(1),
        )
    }
}
