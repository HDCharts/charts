package io.github.hdcharts.waterfall

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxesStyle
import io.github.hdcharts.core.style.AxisLinesDefaults
import io.github.hdcharts.core.style.AxisLinesStyle
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.BarRangeStyle
import io.github.hdcharts.core.style.BarSelectionStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.core.style.clamp

/**
 * Bar colors and dimensions. [maxBarWidth] caps each bar's width; bars shrink to fit the chart.
 * [space] is the requested gap, capped at half a category slot in narrow containers.
 */
@Immutable
data class WaterfallBarsStyle(
    val positiveColor: Color,
    val negativeColor: Color,
    val totalColor: Color,
    val maxBarWidth: Dp,
    val space: Dp,
)

/** Presentation for [WaterfallChart], using the shared axes, selection indicator, range and legend. */
@Immutable
class WaterfallChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val bars: WaterfallBarsStyle,
    val connectors: AxisLinesStyle,
    val axes: AxesStyle,
    val range: BarRangeStyle,
    val selection: BarSelectionStyle,
    val legend: LegendStyle,
)

/** Theme-aware defaults for waterfall charts. */
object WaterfallChartDefaults {
    val valueFormatter: ChartValueFormatter = StyleDefaults.selectedValueFormatter
    val axisValueFormatter: ChartValueFormatter = StyleDefaults.axisValueFormatter

    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        bars: WaterfallBarsStyle = bars(),
        connectors: AxisLinesStyle = AxisLinesDefaults.style(),
        axes: AxesStyle = AxesDefaults.style(),
        range: BarRangeStyle = BarChartDefaults.range(),
        selection: BarSelectionStyle = BarChartDefaults.selection(),
        legend: LegendStyle = LegendDefaults.style(),
    ) = WaterfallChartStyle(chartContainerStyle, bars, connectors, axes, range, selection, legend)

    @Composable
    fun bars(
        positiveColor: Color = MaterialTheme.colorScheme.tertiary,
        negativeColor: Color = MaterialTheme.colorScheme.error,
        totalColor: Color = MaterialTheme.colorScheme.primary,
        maxBarWidth: Dp = 56.dp,
        space: Dp = StyleDefaults.barSpacing,
    ) = WaterfallBarsStyle(positiveColor, negativeColor, totalColor, maxBarWidth, space)
}

internal fun WaterfallChartStyle.clamp(density: Density) =
    WaterfallChartStyle(
        chartContainerStyle = chartContainerStyle,
        bars =
            bars.copy(
                maxBarWidth = bars.maxBarWidth.clampSize(56.dp, density),
                space = bars.space.clampSize(StyleDefaults.barSpacing, density),
            ),
        connectors = connectors.copy(lineWidth = connectors.lineWidth.clampSize(StyleDefaults.lineWidth, density)),
        axes = axes.clamp(density),
        range = range,
        selection =
            selection.copy(
                width = selection.width.clampSize(StyleDefaults.lineWidth, density),
                unselectedAlpha = selection.unselectedAlpha.clampAlpha(),
            ),
        legend = legend,
    )
