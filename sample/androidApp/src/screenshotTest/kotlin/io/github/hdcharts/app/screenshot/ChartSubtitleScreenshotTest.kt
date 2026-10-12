package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_PIE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.gauge.RingGaugeChart
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.pie.PieChart

// Dense, so the header also shows the density toggle next to the subtitle.
private const val DENSE_POINTS = 365
private const val PIE_SELECTION_INDEX = 1

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartSubtitlePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        LineChart(
            data = data,
            title = data.series.single().name,
            subtitle = "Daily net revenue across all regions during the last twelve months, in thousands of dollars",
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartSelectedSubtitlePreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        PieChart(
            data = sample.data,
            title = sample.title,
            subtitle = "Share of monthly revenue",
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(PIE_SELECTION_INDEX),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun RingGaugeChartSubtitleOnlyPreview() {
    val sample = SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        RingGaugeChart(
            data = sample.data,
            subtitle = "Progress toward quarterly targets",
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
