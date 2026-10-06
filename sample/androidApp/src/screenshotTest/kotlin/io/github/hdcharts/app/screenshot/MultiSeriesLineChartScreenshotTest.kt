package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_THOUSANDS_OF_DOLLARS
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors

// Where Mobile App passes the Web Store.
private const val SELECTION_LABEL = "Oct"

private const val DENSE_SELECTION_LABEL = "Oct 15"
private const val DENSE_POINTS = 365

// Without categories, a selection is an index; this one is October.
private const val NO_CATEGORIES_SELECTION_INDEX = 9

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = sample.dataSet,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = sample.dataSet,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(sample.dataSet.categoryIndex(SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartDensePreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        LineChart(
            data = sample.dataSet,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, the lines show bucket averages, and the legend shows each series' value at the
 * selected source point. Interaction stays on so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartDenseSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        LineChart(
            data = sample.dataSet,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(sample.dataSet.categoryIndex(DENSE_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartNoCategoriesSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = ChartData(categories = emptyList(), series = sample.dataSet.series),
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(NO_CATEGORIES_SELECTION_INDEX),
        )
    }
}

/** With no series named, the legend has nothing to name, so it is hidden. */
@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartUnnamedSeriesSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
        val data =
            ChartData(
                categories = sample.dataSet.categories,
                series = sample.dataSet.series.map { it.copy(name = null) },
            )
        LineChart(
            data = data,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(data.categoryIndex(SELECTION_LABEL)),
        )
    }
}

/** A series without a name shows only its value in the legend. */
@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartPartlyNamedSeriesSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
        val series = sample.dataSet.series
        val data =
            ChartData(
                categories = sample.dataSet.categories,
                series = series.dropLast(1) + series.last().copy(name = null),
            )
        LineChart(
            data = data,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(data.categoryIndex(SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartHiddenLegendPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = sample.dataSet,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            style = LineChartDefaults.style(legend = LineChartDefaults.legend(visible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiSeriesLineChartSeriesColorsPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = sample.dataSet,
            title = sample.title,
            valueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
            style =
                LineChartDefaults.style(
                    line =
                        LineChartDefaults.line(
                            colors = LocalChartColors.current.seriesColors(sample.seriesKeys.size),
                        ),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
