package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.gif.docs.BarViewModel
import io.github.hdcharts.app.gif.docs.HistogramViewModel
import io.github.hdcharts.app.gif.docs.LineViewModel
import io.github.hdcharts.app.gif.docs.MultiLineViewModel
import io.github.hdcharts.app.gif.docs.StackedAreaViewModel
import io.github.hdcharts.app.gif.docs.StackedBarViewModel
import io.github.hdcharts.app.screenshot.shared.DocsGifLandscapePreview
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_PIE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.BarChart
import io.github.hdcharts.charts.HistogramChart
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.StackedAreaChart
import io.github.hdcharts.charts.StackedBarChart
import io.github.hdcharts.charts.model.ChartValueFormatters

/**
 * Landscape screenshot tests that mirror the docs GIF scenarios. Pie and
 * radar still pull from the existing sample use cases (their data is
 * sensible at any aspect ratio). All other charts read the ChartData
 * straight off the same ViewModel that produces the GIF, under
 * `io.github.hdcharts.app.gif.docs`, so the screenshot and the recorded
 * GIF can never drift apart.
 */

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun PieDocsGifScenarioPreview() {
    ScreenshotSurface {
        val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
        PieChart(data = sample.slices, title = sample.title)
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun LineDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = LineViewModel().chartData.value
        LineChart(data = data, title = data.series.single().name)
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun MultiLineDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = MultiLineViewModel().chartData.value
        LineChart(
            data = data,
            title = "Weekly Revenue by Channel",
            valueFormatter = ChartValueFormatters.prefix("$"),
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun BarDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = BarViewModel().chartData.value
        BarChart(data = data, title = data.series.single().name)
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun HistogramDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = HistogramViewModel().chartData.value
        HistogramChart(data = data, title = data.series.single().name)
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun StackedBarDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = StackedBarViewModel().chartData.value
        StackedBarChart(data = data, title = "Quarterly Revenue by Channel")
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun StackedAreaDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = StackedAreaViewModel().chartData.value
        StackedAreaChart(data = data, title = "Monthly Active Subscribers by Plan")
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun RadarDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = SCREENSHOT_RADAR_SAMPLE_USE_CASE.initialRadarDefaultData()
        RadarChart(
            data = data,
            title = data.series.single().name,
        )
    }
}
