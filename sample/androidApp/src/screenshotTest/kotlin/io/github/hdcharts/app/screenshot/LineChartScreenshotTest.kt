package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.model.ChartValueFormatters
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures

private const val MULTI_LINE_SELECTION_INDEX = 4

private val MULTI_LINE_SELECTION_DATA =
    listOf(
        "P50 Latency" to listOf(122.5, 149.125, 134.333, 126.75, 101.322397132296, 114.667, 129.75),
        "P95 Latency" to listOf(167.75, 219.2, 176.85, 161.45, 151.31476088115193, 166.42, 188.95),
    ).toChartData(
        categories = listOf("14:00:00", "14:00:07", "14:00:14", "14:00:21", "14:00:28", "14:00:35", "14:00:42"),
    )

@PreviewTest
@ScreenshotPreview
@Composable
fun LineChartDefaultPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.initialLineDataSet()
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
fun LineChartCustomPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.initialLineDataSet()
        LineChart(
            data = data,
            title = data.series.single().name,
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
