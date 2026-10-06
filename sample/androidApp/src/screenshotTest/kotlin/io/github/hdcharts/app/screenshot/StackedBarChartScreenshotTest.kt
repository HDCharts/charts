package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_THOUSANDS_OF_DOLLARS
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import io.github.hdcharts.stackedbar.StackedBarChart
import io.github.hdcharts.stackedbar.StackedBarChartDefaults

// The holiday quarter.
private const val STACKED_BAR_SELECTION_LABEL = "Q4 '25"

private const val DENSE_SELECTION_LABEL = "Q4 '24"

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartSelectedBarPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = STACKED_BAR_SELECTION_LABEL,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(sample.dataSet.categoryIndex(STACKED_BAR_SELECTION_LABEL)),
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
        )
    }
}

/** One segment needs no legend, so the title shows the selected value. */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartSingleSegmentSelectedBarPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        val data =
            ChartData(
                categories = sample.dataSet.categories,
                series = sample.dataSet.series.take(1),
            )
        StackedBarChart(
            data = data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(data.categoryIndex(STACKED_BAR_SELECTION_LABEL)),
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartHiddenLegendPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            style = StackedBarChartDefaults.style(legend = StackedBarChartDefaults.legend(visible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartNoCategoriesPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarNoCategoriesDataSet()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartDensePreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialDenseStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, selecting a bar highlights the bucket that contains it.
 * Interaction stays on so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartDenseSelectedBarPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialDenseStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = DENSE_SELECTION_LABEL,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(sample.dataSet.categoryIndex(DENSE_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedBarChartSegmentColorsPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.initialStackedBarSample()
        StackedBarChart(
            data = sample.dataSet,
            title = sample.title,
            style =
                StackedBarChartDefaults.style(
                    segments =
                        StackedBarChartDefaults.segments(
                            colors = LocalChartColors.current.seriesColors(sample.segmentKeys.size),
                        ),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
