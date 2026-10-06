package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults

private const val LINE_SELECTION_LABEL = "Jan 18"

// Near the seasonal peak.
private const val DENSE_SELECTION_LABEL = "Dec 16"
private const val DENSE_POINTS = 365
private const val SIGNED_POINTS = 24

// Wider than the data (about 930 to 1,500) on both sides.
private const val FIXED_RANGE_MIN = 500.0
private const val FIXED_RANGE_MAX = 2_000.0

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartDefaultPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartSelectedPointPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(data.categoryIndex(LINE_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartDensePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        LineChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, selecting a point highlights the bucket that contains it. Interaction stays on
 * so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartDenseSelectedPointPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        LineChart(
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
fun LineChartNegativeValuesPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic(points = SIGNED_POINTS, signed = true)
        LineChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartFixedRangePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = data,
            title = data.series.single().name,
            style =
                LineChartDefaults.style(
                    range = LineChartDefaults.range(min = FIXED_RANGE_MIN, max = FIXED_RANGE_MAX),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartPointsPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = data,
            title = data.series.single().name,
            style = LineChartDefaults.style(points = LineChartDefaults.points(visible = true)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartStraightLinesPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = data,
            title = data.series.single().name,
            style = LineChartDefaults.style(line = LineChartDefaults.line(bezier = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** With the axis lines and labels hidden, the chart works as a sparkline. */
@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartHiddenAxisPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic()
        LineChart(
            data = data,
            title = data.series.single().name,
            style =
                LineChartDefaults.style(
                    axis =
                        LineChartDefaults.axis(
                            visible = false,
                            xLabels = LineChartDefaults.xLabels(visible = false),
                            yLabels = LineChartDefaults.yLabels(visible = false),
                        ),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
