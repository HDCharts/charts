package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.gif.docs.BarViewModel
import io.github.hdcharts.app.gif.docs.HistogramViewModel
import io.github.hdcharts.app.gif.docs.LineValueColors
import io.github.hdcharts.app.gif.docs.LineViewModel
import io.github.hdcharts.app.gif.docs.MultiLineViewModel
import io.github.hdcharts.app.gif.docs.PieViewModel
import io.github.hdcharts.app.gif.docs.RadarViewModel
import io.github.hdcharts.app.gif.docs.RingGaugeViewModel
import io.github.hdcharts.app.gif.docs.SegmentedRingGaugeViewModel
import io.github.hdcharts.app.gif.docs.SignedBarViewModel
import io.github.hdcharts.app.gif.docs.StackedAreaViewModel
import io.github.hdcharts.app.gif.docs.StackedBarViewModel
import io.github.hdcharts.app.gif.docs.barFadeGradientStyle
import io.github.hdcharts.app.gif.docs.barPlotHorizontalGradientStyle
import io.github.hdcharts.app.gif.docs.barPlotVerticalGradientStyle
import io.github.hdcharts.app.gif.docs.barShapeGradientStyle
import io.github.hdcharts.app.gif.docs.histogramFadeGradientStyle
import io.github.hdcharts.app.gif.docs.histogramPlotHorizontalGradientStyle
import io.github.hdcharts.app.gif.docs.histogramPlotVerticalGradientStyle
import io.github.hdcharts.app.gif.docs.histogramShapeGradientStyle
import io.github.hdcharts.app.gif.docs.stackedAreaFadeGradientStyle
import io.github.hdcharts.app.gif.docs.stackedBarFadeGradientStyle
import io.github.hdcharts.app.screenshot.shared.DocsGifLandscapePreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.gauge.RingGaugeChart
import io.github.hdcharts.gauge.SegmentedRingGaugeChart
import io.github.hdcharts.histogram.HistogramChart
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults
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
        PieChart(data = viewModel.data.value, title = viewModel.title)
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
            selectedValueFormatter = ChartValueFormatters.prefix("$"),
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
fun LineGradientDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = LineViewModel().chartData.value
        val gradient = ChartGradients.vertical(colors = LineValueColors, span = GradientSpan.Plot)
        val line = LineChartDefaults.line(gradient = gradient)
        LineChart(data = data, title = data.series.single().name, style = LineChartDefaults.style(line = line))
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun BarGradientShapeDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = BarViewModel().chartData.value
        BarChart(data = data, title = data.series.single().name, style = barShapeGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun BarGradientPlotHorizontalDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = BarViewModel().chartData.value
        BarChart(data = data, title = data.series.single().name, style = barPlotHorizontalGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun BarGradientPlotVerticalDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = BarViewModel().chartData.value
        BarChart(data = data, title = data.series.single().name, style = barPlotVerticalGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun BarGradientFadeDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = SignedBarViewModel().chartData.value
        BarChart(data = data, title = data.series.single().name, style = barFadeGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun HistogramGradientShapeDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = HistogramViewModel().chartData.value
        HistogramChart(data = data, title = data.series.single().name, style = histogramShapeGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun HistogramGradientPlotHorizontalDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = HistogramViewModel().chartData.value
        HistogramChart(data = data, title = data.series.single().name, style = histogramPlotHorizontalGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun HistogramGradientPlotVerticalDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = HistogramViewModel().chartData.value
        HistogramChart(data = data, title = data.series.single().name, style = histogramPlotVerticalGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun HistogramGradientFadeDocsGifScenarioPreview() {
    ScreenshotSurface {
        val data = HistogramViewModel().chartData.value
        HistogramChart(data = data, title = data.series.single().name, style = histogramFadeGradientStyle())
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun StackedBarGradientDocsGifScenarioPreview() {
    ScreenshotSurface {
        val viewModel = StackedBarViewModel()
        StackedBarChart(
            data = viewModel.chartData.value,
            title = viewModel.title,
            style = stackedBarFadeGradientStyle(),
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun StackedAreaGradientDocsGifScenarioPreview() {
    ScreenshotSurface {
        val viewModel = StackedAreaViewModel()
        StackedAreaChart(
            data = viewModel.chartData.value,
            title = viewModel.title,
            style = stackedAreaFadeGradientStyle(),
        )
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

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun RingGaugeDocsGifScenarioPreview() {
    ScreenshotSurface {
        val viewModel = RingGaugeViewModel()
        RingGaugeChart(
            data = viewModel.data.value,
            title = viewModel.title,
            selectedValueFormatter = viewModel.selectedValueFormatter,
            axisValueFormatter = viewModel.axisValueFormatter,
        )
    }
}

@PreviewTest
@DocsGifLandscapePreview
@Composable
fun SegmentedRingGaugeDocsGifScenarioPreview() {
    ScreenshotSurface {
        val viewModel = SegmentedRingGaugeViewModel()
        SegmentedRingGaugeChart(
            data = viewModel.data.value,
            capacity = 2_000.0,
            title = "Daily Nutrition",
            centerSupportingLabel = "of 2000 kcal",
        )
    }
}
