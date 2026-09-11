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
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.ChartContainerDefaults

private const val MULTI_LINE_SELECTION_INDEX = 4

private val MULTI_LINE_SELECTION_DATA =
    chartDataOf(
        categories = listOf("14:00:00", "14:00:07", "14:00:14", "14:00:21", "14:00:28", "14:00:35", "14:00:42"),
        *arrayOf(
            ChartSeries(
                name = "P50 Latency",
                values = listOf(122.5, 149.125, 134.333, 126.75, 101.322397132296, 114.667, 129.75),
            ),
            ChartSeries(
                name = "P95 Latency",
                values = listOf(167.75, 219.2, 176.85, 161.45, 151.31476088115193, 166.42, 188.95),
            ),
        ),
    )

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartDefaultPreview() {
    ScreenshotSurface {
        LineChart(
            data = SCREENSHOT_LINE_SAMPLE_USE_CASE.initialLineDataSet(),
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
            data = SCREENSHOT_LINE_SAMPLE_USE_CASE.initialLineDataSet(),
            style = ChartTestStyleFixtures.lineCustomStyle(chartContainerStyle = ChartContainerDefaults.style()),
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
            data = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.initialMultiLineSample().dataSet,
            title = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.initialMultiLineSample().title,
            valueFormatter = ChartValueFormatters.prefix("$"),
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
            data = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.initialMultiLineSample().dataSet,
            title = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.initialMultiLineSample().title,
            valueFormatter = ChartValueFormatters.prefix("$"),
            style =
                ChartTestStyleFixtures.multiLineCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
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
            data = MULTI_LINE_SELECTION_DATA,
            title = "14:00:28",
            valueFormatter = ChartValueFormatters.suffix(" ms"),
            style =
                ChartTestStyleFixtures.multiLineCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
                    seriesCount = MULTI_LINE_SELECTION_DATA.series.size,
                ),
            interactionEnabled = false,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(MULTI_LINE_SELECTION_INDEX),
        )
    }
}
