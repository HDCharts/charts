package dev.hdcode.charts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE
import dev.hdcode.charts.app.screenshot.shared.ScreenshotPreview
import dev.hdcode.charts.app.screenshot.shared.ScreenshotSurface
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.ChartContainerDefaults

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
                    barCount = data.series.single().values.size,
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
