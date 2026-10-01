package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import io.github.hdcharts.stackedarea.StackedAreaChart
import io.github.hdcharts.stackedarea.StackedAreaChartDefaults

// As Pro closes in on Starter.
private const val STACKED_AREA_SELECTION_LABEL = "Sep '25"

private const val DENSE_SELECTION_LABEL = "Jan '25"

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaSample()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaSample()
        StackedAreaChart(
            data = sample.data,
            title = STACKED_AREA_SELECTION_LABEL,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(sample.data.categoryIndex(STACKED_AREA_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartNoCategoriesPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaNoCategoriesData()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartDensePreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialDenseStackedAreaSample()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, selecting a point highlights the bucket that contains it.
 * Interaction stays on so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartDenseSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialDenseStackedAreaSample()
        StackedAreaChart(
            data = sample.data,
            title = DENSE_SELECTION_LABEL,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(sample.data.categoryIndex(DENSE_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartSeriesColorsPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.initialStackedAreaSample()
        val colors = LocalChartColors.current.seriesColors(sample.seriesKeys.size)
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            style =
                StackedAreaChartDefaults.style(
                    fill = StackedAreaChartDefaults.fill(colors = colors),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
