package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hdcharts.app.gif.DocsGifScenariosData
import io.github.hdcharts.app.gif.DocsGifScenariosData.POINTS
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.BarChartStyle
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignedBarViewModel : ViewModel() {
    val chartData: StateFlow<ChartData> = MutableStateFlow(buildChartData()).asStateFlow()

    private fun buildChartData(): ChartData {
        val values =
            DocsGifScenariosData
                .rampedSeries(
                    count = POINTS,
                    start = 40.0,
                    end = 300.0,
                    amplitude = 90.0,
                    seed = 7,
                    targetMax = 300.0,
                ).map { it - 120.0 }
        return values.toChartData(
            categories = DocsGifScenariosData.monthLabels(count = POINTS),
            seriesName = BAR_TITLE,
        )
    }
}

@Composable
fun ShowGradientBar(
    style: BarChartStyle,
    chartData: StateFlow<ChartData>,
) {
    val data by chartData.collectAsStateWithLifecycle()

    BarChart(
        data = data,
        title = BAR_TITLE,
        style = style,
    )
}

/** Three colors repeat inside every bar. */
@Composable
fun barShapeGradientStyle(): BarChartStyle =
    barGradientStyle(ChartGradients.vertical(colors = GradientColors, span = GradientSpan.Shape))

/** One left-to-right gradient across the plot. */
@Composable
fun barPlotHorizontalGradientStyle(): BarChartStyle =
    barGradientStyle(ChartGradients.horizontal(colors = GradientColors, span = GradientSpan.Plot))

/** Bar height selects the visible gradient range. */
@Composable
fun barPlotVerticalGradientStyle(): BarChartStyle =
    barGradientStyle(ChartGradients.vertical(colors = GradientColors, span = GradientSpan.Plot))

/** Positive and negative bars mirror the fade around zero. */
@Composable
fun barFadeGradientStyle(): BarChartStyle = barGradientStyle(ChartGradients.fade(endAlpha = 0.35f))

@Composable
private fun barGradientStyle(gradient: ChartGradient): BarChartStyle =
    BarChartDefaults.style(bars = BarChartDefaults.bars(gradient = gradient))
