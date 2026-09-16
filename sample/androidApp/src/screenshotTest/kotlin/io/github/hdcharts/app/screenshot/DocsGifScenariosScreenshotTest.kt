package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.gif.DocsGifScenariosData
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
import io.github.hdcharts.charts.model.toChartData

/**
 * Landscape screenshot tests that mirror the docs GIF scenarios. Pie and
 * radar still pull from the existing sample use cases (their data is
 * sensible at any aspect ratio). All other charts use the shared
 * `DocsGifScenariosData` so the same fixtures drive the recorder and the
 * screenshot baselines.
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
        val scenario = DocsGifScenariosData.line()
        LineChart(
            data =
                DocsGifScenariosData.buildSingleSeries(
                    scenario,
                    DocsGifScenariosData.LINE_TITLE,
                ),
            title = DocsGifScenariosData.LINE_TITLE,
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun MultiLineDocsGifScenarioPreview() {
    ScreenshotSurface {
        val scenario = DocsGifScenariosData.multiLine()
        LineChart(
            data = scenario.items.toChartData(categories = scenario.categories),
            title = DocsGifScenariosData.MULTI_LINE_TITLE,
            valueFormatter = ChartValueFormatters.prefix("$"),
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun BarDocsGifScenarioPreview() {
    ScreenshotSurface {
        val scenario = DocsGifScenariosData.bar()
        BarChart(
            data =
                DocsGifScenariosData.buildSingleSeries(
                    scenario,
                    DocsGifScenariosData.BAR_TITLE,
                ),
            title = DocsGifScenariosData.BAR_TITLE,
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun HistogramDocsGifScenarioPreview() {
    ScreenshotSurface {
        val scenario = DocsGifScenariosData.histogram()
        HistogramChart(
            data =
                DocsGifScenariosData.buildSingleSeries(
                    scenario,
                    DocsGifScenariosData.HISTOGRAM_TITLE,
                ),
            title = DocsGifScenariosData.HISTOGRAM_TITLE,
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun StackedBarDocsGifScenarioPreview() {
    ScreenshotSurface {
        val scenario = DocsGifScenariosData.stackedBar()
        StackedBarChart(
            data = scenario.items.toChartData(categories = scenario.categories),
            title = DocsGifScenariosData.STACKED_BAR_TITLE,
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun StackedAreaDocsGifScenarioPreview() {
    ScreenshotSurface {
        val scenario = DocsGifScenariosData.stackedArea()
        StackedAreaChart(
            data = scenario.items.toChartData(categories = scenario.categories),
            title = DocsGifScenariosData.STACKED_AREA_TITLE,
        )
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
