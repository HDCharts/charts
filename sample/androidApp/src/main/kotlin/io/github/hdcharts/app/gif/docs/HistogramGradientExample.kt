package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.core.style.HistogramChartStyle
import io.github.hdcharts.histogram.HistogramChart

@Composable
fun ShowGradientHistogram(
    style: HistogramChartStyle,
    viewModel: HistogramViewModel = viewModel(),
) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    HistogramChart(
        data = chartData,
        title = chartData.series.single().name,
        style = style,
    )
}

/** Three colors repeat inside every bin. */
@Composable
fun histogramShapeGradientStyle(): HistogramChartStyle =
    histogramGradientStyle(ChartGradients.vertical(colors = GradientColors, span = GradientSpan.Shape))

/** One left-to-right gradient across the plot. */
@Composable
fun histogramPlotHorizontalGradientStyle(): HistogramChartStyle =
    histogramGradientStyle(ChartGradients.horizontal(colors = GradientColors, span = GradientSpan.Plot))

/** Bin height selects the visible gradient range. */
@Composable
fun histogramPlotVerticalGradientStyle(): HistogramChartStyle =
    histogramGradientStyle(ChartGradients.vertical(colors = GradientColors, span = GradientSpan.Plot))

/** Each bin fades toward the baseline. */
@Composable
fun histogramFadeGradientStyle(): HistogramChartStyle = histogramGradientStyle(ChartGradients.fade(endAlpha = 0.35f))

@Composable
private fun histogramGradientStyle(gradient: ChartGradient): HistogramChartStyle =
    HistogramChartDefaults.style(bars = HistogramChartDefaults.bars(gradient = gradient))
