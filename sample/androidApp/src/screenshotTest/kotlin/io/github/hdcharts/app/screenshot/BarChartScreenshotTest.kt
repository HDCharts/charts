package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_BAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.charts.BarChart
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.BarChartDefaults
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor

// A negative month.
private const val BAR_SELECTION_LABEL = "Jul"

// In the middle of the 90 days.
private const val DENSE_SELECTION_LABEL = "Apr 15"

// Wider than the data (-26 to 96) on both sides, with zero as a tick.
private const val FIXED_RANGE_MIN = -40.0
private const val FIXED_RANGE_MAX = 120.0
private const val POSITIVE_COLOR_INDEX = 6
private const val NEGATIVE_COLOR_INDEX = 3

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartDefaultPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet()
        BarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartSelectedBarPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet()
        BarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(data.categoryIndex(BAR_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartDensePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialDenseBarDataSet()
        BarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, a bar shows its bucket's average, and the selection shows the source bar's value.
 * Interaction stays on so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartDenseSelectedBarPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialDenseBarDataSet()
        BarChart(
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
fun BarChartFixedRangePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet()
        BarChart(
            data = data,
            title = data.series.single().name,
            style =
                BarChartDefaults.style(
                    range = BarChartDefaults.range(min = FIXED_RANGE_MIN, max = FIXED_RANGE_MAX),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartHiddenGridPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet()
        BarChart(
            data = data,
            title = data.series.single().name,
            style = BarChartDefaults.style(grid = BarChartDefaults.grid(visible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** One color per bar: positive months in one color, negative months in another. */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartBarColorsPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.initialBarDataSet()
        val chartColors = LocalChartColors.current
        val positive = chartColors.seriesColor(POSITIVE_COLOR_INDEX)
        val negative = chartColors.seriesColor(NEGATIVE_COLOR_INDEX)
        val barColors =
            data.series
                .single()
                .values
                .map { value -> if (value < 0) negative else positive }
        BarChart(
            data = data,
            title = data.series.single().name,
            style = BarChartDefaults.style(bars = BarChartDefaults.bars(colors = barColors)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
