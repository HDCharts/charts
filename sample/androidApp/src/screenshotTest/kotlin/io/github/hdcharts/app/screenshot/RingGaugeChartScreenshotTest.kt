package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.gauge.RingGaugeChart
import io.github.hdcharts.gauge.RingGaugeChartDefaults
import io.github.hdcharts.sampleshared.data.RingGaugeSampleData
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors

private const val RING_GAUGE_SELECTION_INDEX = 1

// Enough rings that they get thinner.
private const val MANY_RINGS = 8
private val PERCENT_FORMATTER = ChartValueFormatters.suffix("%")
private val CELSIUS_FORMATTER = ChartValueFormatters.suffix("°C")

@PreviewTest
@ScreenshotPreview
@Composable
fun RingGaugeChartDefaultPreview() {
    val sample = SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        RingGaugeChart(
            data = sample.data,
            title = sample.title,
            valueFormatter = PERCENT_FORMATTER,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A selected ring names its value in the title, and the other rings fade. */
@PreviewTest
@ScreenshotPreview
@Composable
fun RingGaugeChartSelectedRingPreview() {
    val sample = SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        RingGaugeChart(
            data = sample.data,
            title = sample.title,
            valueFormatter = PERCENT_FORMATTER,
            selection = staticChartSelection(RING_GAUGE_SELECTION_INDEX),
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A single value is a valid gauge: one ring at full width, with no legend. */
@PreviewTest
@ScreenshotPreview
@Composable
fun RingGaugeChartSingleRingPreview() {
    val sample = SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE.deterministic(rings = 1)
    ScreenshotChartSurface {
        RingGaugeChart(
            data = sample.data,
            title = sample.title,
            valueFormatter = PERCENT_FORMATTER,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** Many rings get thinner so the half circle keeps its hole. */
@PreviewTest
@ScreenshotPreview
@Composable
fun RingGaugeChartManyRingsPreview() {
    val sample = SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE.deterministic(rings = MANY_RINGS)
    ScreenshotChartSurface {
        RingGaugeChart(
            data = sample.data,
            title = sample.title,
            valueFormatter = PERCENT_FORMATTER,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A range can start below zero, and a value past the end stops at the end of the arc. */
@PreviewTest
@ScreenshotPreview
@Composable
fun RingGaugeChartSignedRangePreview() {
    val sample = SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE.deterministic(signed = true)
    ScreenshotChartSurface {
        RingGaugeChart(
            data = sample.data,
            title = sample.title,
            style = RingGaugeChartDefaults.style(range = sample.range()),
            valueFormatter = CELSIUS_FORMATTER,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A palette is drawn in ring order; with the track and range labels hidden, only the values remain. */
@PreviewTest
@ScreenshotPreview
@Composable
fun RingGaugeChartColorsWithoutTrackPreview() {
    val sample = SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE.deterministic()
    ScreenshotChartSurface {
        val colors = LocalChartColors.current.seriesColors(sample.data.categories.size)
        RingGaugeChart(
            data = sample.data,
            title = sample.title,
            style =
                RingGaugeChartDefaults.style(
                    rings = RingGaugeChartDefaults.rings(colors = colors),
                    track = RingGaugeChartDefaults.track(visible = false),
                    labels = RingGaugeChartDefaults.labels(visible = false),
                ),
            valueFormatter = PERCENT_FORMATTER,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@Composable
private fun RingGaugeSampleData.range() = RingGaugeChartDefaults.range(min = rangeMin, max = rangeMax)
