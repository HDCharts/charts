package dev.hdcode.charts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_LINE_SAMPLE_USE_CASE
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE
import dev.hdcode.charts.app.screenshot.shared.ScreenshotPreview
import dev.hdcode.charts.app.screenshot.shared.ScreenshotSurface
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.ChartViewDefaults

private const val MULTI_LINE_SELECTION_INDEX = 4

private val MULTI_LINE_SELECTION_DATA_SET =
    listOf(
        "P50 Latency" to listOf(122.5, 149.125, 134.333, 126.75, 101.322397132296, 114.667, 129.75),
        "P95 Latency" to listOf(167.75, 219.2, 176.85, 161.45, 151.31476088115193, 166.42, 188.95),
    ).toMultiChartDataSet(
        title = "14:00:28",
        categories = listOf("14:00:00", "14:00:07", "14:00:14", "14:00:21", "14:00:28", "14:00:35", "14:00:42"),
        postfix = " ms",
    )

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartDefaultPreview() {
    ScreenshotSurface {
        LineChart(
            dataSet = SCREENSHOT_LINE_SAMPLE_USE_CASE.initialLineDataSet(),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartCustomPreview() {
    ScreenshotSurface {
        LineChart(
            dataSet = SCREENSHOT_LINE_SAMPLE_USE_CASE.initialLineDataSet(),
            style = ChartTestStyleFixtures.lineCustomStyle(chartViewStyle = ChartViewDefaults.style()),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiLineChartDefaultPreview() {
    ScreenshotSurface {
        LineChart(
            dataSet = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.initialMultiLineSample().dataSet,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiLineChartCustomPreview() {
    ScreenshotSurface {
        LineChart(
            dataSet = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.initialMultiLineSample().dataSet,
            style =
                ChartTestStyleFixtures.multiLineCustomStyle(
                    chartViewStyle = ChartViewDefaults.style(),
                    seriesCount = 3,
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun MultiLineChartSelectionLegendPreview() {
    ScreenshotSurface {
        LineChart(
            dataSet = MULTI_LINE_SELECTION_DATA_SET,
            style =
                ChartTestStyleFixtures.multiLineCustomStyle(
                    chartViewStyle = ChartViewDefaults.style(),
                    seriesCount = MULTI_LINE_SELECTION_DATA_SET.data.items.size,
                ),
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selectedPointIndex = MULTI_LINE_SELECTION_INDEX,
        )
    }
}
