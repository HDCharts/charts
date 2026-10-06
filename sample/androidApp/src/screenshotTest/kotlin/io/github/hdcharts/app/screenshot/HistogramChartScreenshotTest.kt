package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.histogram.HistogramChart
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor

// The peak of the distribution.
private const val HISTOGRAM_SELECTION_LABEL = "100ms"

// The peak of the distribution in the 150 bins.
private const val DENSE_SELECTION_LABEL = "106ms"

// More bins than fit at the minimum bar width, even on a tablet.
private const val DENSE_BINS = 150
private const val BAR_COLOR_INDEX = 1

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartDefaultPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.deterministic()
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
fun HistogramChartSelectedBarPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.deterministic()
        HistogramChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(data.categoryIndex(HISTOGRAM_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartDensePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.deterministic(bins = DENSE_BINS)
        HistogramChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, the histogram merges neighboring bins, and the selection marks the merged bar holding the bin.
 * Interaction stays on so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartDenseSelectedBarPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.deterministic(bins = DENSE_BINS)
        HistogramChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(data.categoryIndex(DENSE_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HistogramChartBarColorPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.deterministic()
        HistogramChart(
            data = data,
            title = data.series.single().name,
            style =
                HistogramChartDefaults.style(
                    bars = HistogramChartDefaults.bars(color = LocalChartColors.current.seriesColor(BAR_COLOR_INDEX)),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
