package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.gif.docs.BarViewModel
import io.github.hdcharts.app.gif.docs.HistogramViewModel
import io.github.hdcharts.app.gif.docs.LineViewModel
import io.github.hdcharts.app.gif.docs.MultiLineViewModel
import io.github.hdcharts.app.gif.docs.PieViewModel
import io.github.hdcharts.app.gif.docs.RadarViewModel
import io.github.hdcharts.app.gif.docs.StackedAreaViewModel
import io.github.hdcharts.app.gif.docs.StackedBarViewModel
import io.github.hdcharts.app.screenshot.shared.DocsGifLandscapePreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.histogram.HistogramChart
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.pie.PieChart
import io.github.hdcharts.radar.RadarChart
import io.github.hdcharts.stackedarea.StackedAreaChart
import io.github.hdcharts.stackedbar.StackedBarChart

/**
 * Landscape screenshot tests that mirror the docs GIF scenarios. Each chart
 * reads its data straight off the same ViewModel that produces the GIF,
 * under `io.github.hdcharts.app.gif.docs`, so the screenshot and the
 * recorded GIF can never drift apart.
 */

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun PieDocsGifScenarioPreview() {
    ScreenshotSurface {
        val viewModel = PieViewModel()
        PieChart(data = viewModel.slices.value, title = viewModel.title)
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
        val viewModel = MultiLineViewModel()
        LineChart(
            data = viewModel.chartData.value,
            title = viewModel.title,
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
        val viewModel = StackedBarViewModel()
        StackedBarChart(data = viewModel.chartData.value, title = viewModel.title)
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun StackedAreaDocsGifScenarioPreview() {
    ScreenshotSurface {
        val viewModel = StackedAreaViewModel()
        StackedAreaChart(data = viewModel.chartData.value, title = viewModel.title)
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun RadarDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = RadarViewModel().chartData.value
        RadarChart(
            data = data,
            title = data.series.single().name,
        )
    }
}
