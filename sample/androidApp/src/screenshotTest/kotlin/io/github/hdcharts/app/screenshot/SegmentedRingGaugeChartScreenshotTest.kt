package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_SEGMENTED_RING_GAUGE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.gauge.SegmentedRingGaugeChart
import io.github.hdcharts.gauge.SegmentedRingGaugeChartDefaults
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors

private const val SEGMENTED_RING_GAUGE_SELECTION_LABEL = "Protein"

/** The default capacity ring presents its categories in the legend. */
@PreviewTest
@ScreenshotPreview
@Composable
fun SegmentedRingGaugeChartDefaultPreview() {
    val sample = SCREENSHOT_SEGMENTED_RING_GAUGE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        SegmentedRingGaugeChart(
            data = sample.data,
            capacity = sample.capacity,
            centerSupportingLabel = sample.centerSupportingLabel,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** Selecting a segment scales it and names the selected category without dimming the other segments. */
@PreviewTest
@ScreenshotPreview
@Composable
fun SegmentedRingGaugeChartSelectedSegmentPreview() {
    val sample = SCREENSHOT_SEGMENTED_RING_GAUGE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        SegmentedRingGaugeChart(
            data = sample.data,
            capacity = sample.capacity,
            centerSupportingLabel = sample.centerSupportingLabel,
            selection = staticChartSelection(sample.data.categoryIndex(SEGMENTED_RING_GAUGE_SELECTION_LABEL)),
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A hidden legend lets the capacity ring use the full chart area. */
@PreviewTest
@ScreenshotPreview
@Composable
fun SegmentedRingGaugeChartHiddenLegendPreview() {
    val sample = SCREENSHOT_SEGMENTED_RING_GAUGE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        SegmentedRingGaugeChart(
            data = sample.data,
            capacity = sample.capacity,
            centerSupportingLabel = sample.centerSupportingLabel,
            style =
                SegmentedRingGaugeChartDefaults.style(
                    segments =
                        SegmentedRingGaugeChartDefaults.segments(
                            colors = LocalChartColors.current.seriesColors(sample.data.categories.size),
                        ),
                    legend = SegmentedRingGaugeChartDefaults.legend(visible = false),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
