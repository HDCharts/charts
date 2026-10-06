package io.github.hdcharts.pie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.DONUT_MAX_PERCENTAGE
import io.github.hdcharts.core.internal.DONUT_MIN_PERCENTAGE
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.internal.layout.fillMaxSizeChartModifier
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * The style for a Pie Chart, grouped into cohesive sub-styles.
 *
 * @property chartContainerStyle The shared container/layout presentation.
 * @property donut The donut configuration of the chart.
 * @property slices The slice configuration of the chart.
 * @property border The border configuration of the chart.
 * @property legend The legend configuration of the chart.
 */
@Stable
class PieChartStyle(
    internal val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val donut: PieChartDonutStyle,
    val slices: PieChartSlicesStyle,
    val border: PieChartBorderStyle,
    val legend: LegendStyle,
)

/**
 * Donut configuration for a [PieChartStyle].
 *
 * @property holePercentage The percentage of the chart that is a donut hole. The chart clamps it to
 * [io.github.hdcharts.core.internal.DONUT_MIN_PERCENTAGE]..[io.github.hdcharts.core.internal.DONUT_MAX_PERCENTAGE]
 * when drawing.
 */
@Immutable
data class PieChartDonutStyle(
    val holePercentage: Float,
) {
    /**
     * Returns this block with [holePercentage] inside the drawable range. Names every field instead of
     * using `copy`, so a field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp() =
        PieChartDonutStyle(
            holePercentage =
                if (holePercentage.isNaN()) {
                    StyleDefaults.pieDonutHole
                } else {
                    holePercentage.coerceIn(DONUT_MIN_PERCENTAGE, DONUT_MAX_PERCENTAGE)
                },
        )
}

/**
 * Slice configuration for a [PieChartStyle].
 *
 * @property alpha The alpha value applied to rendered pie slices.
 * @property baseColor The base color used to generate shades when [colors] is empty.
 * @property colors The colors the chart draws for slices, in slice order. When empty, a shade is
 * generated for every slice from [baseColor]. When set, its count must match the slice count.
 */
@Immutable
data class PieChartSlicesStyle(
    val alpha: Float,
    val baseColor: Color,
    val colors: ImmutableList<Color>,
) {
    /**
     * Returns the default colors for [sliceCount] slices, before `alpha` is applied:
     * [colors] when set, or generated shades of [baseColor] when it is empty.
     */
    fun resolveColors(sliceCount: Int): ImmutableList<Color> =
        resolvePaletteColors(
            baseColor = baseColor,
            colors = colors,
            count = sliceCount,
            singleItemUsesBase = false,
        )

    /**
     * Returns this block with [alpha] clamped. Names every field instead of using `copy`, so a field
     * added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp() =
        PieChartSlicesStyle(
            alpha = alpha.clampAlpha(),
            baseColor = baseColor,
            colors = colors,
        )
}

/**
 * Border configuration for a [PieChartStyle].
 *
 * @property width The width of the border around the pie chart.
 * @property color The color of the border around the pie chart.
 */
@Immutable
data class PieChartBorderStyle(
    val width: Dp,
    val color: Color,
) {
    /**
     * Returns this block with [width] clamped. Names every field instead of using `copy`, so a field
     * added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        PieChartBorderStyle(
            width = width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            color = color,
        )
}

/**
 * An object that provides default styles for a Pie Chart.
 */
object PieChartDefaults {
    /**
     * Returns a [PieChartStyle] with the provided parameters or their default values.
     *
     * @param chartContainerStyle The style to be applied to the chart view. Defaults to the default style of ChartContainerDefaults.
     * @param donut The donut configuration. Defaults to a chart without a donut hole.
     * @param slices The slice configuration. Defaults to theme-derived slice colors and alpha.
     * @param border The border configuration. Defaults to a 1.dp border using the surface color.
     * @param legend The legend configuration. Defaults to a visible legend.
     */
    @Composable
    @OptIn(InternalChartsApi::class)
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        donut: PieChartDonutStyle = donut(),
        slices: PieChartSlicesStyle = slices(),
        border: PieChartBorderStyle = border(),
        legend: LegendStyle = legend(),
    ): PieChartStyle {
        val modifier: Modifier = fillMaxSizeChartModifier(chartContainerStyle)
        return PieChartStyle(
            modifier = modifier,
            chartContainerStyle = chartContainerStyle,
            donut = donut,
            slices = slices,
            border = border,
            legend = legend,
        )
    }

    /**
     * Returns a [PieChartDonutStyle] with the provided parameters or their default values.
     *
     * @param holePercentage The percentage of the chart that is a donut hole. Defaults to 0f.
     */
    @Composable
    fun donut(holePercentage: Float = StyleDefaults.pieDonutHole) = PieChartDonutStyle(holePercentage = holePercentage)

    /**
     * Returns a [PieChartSlicesStyle] with the provided parameters or their default values.
     *
     * @param baseColor The base color used to generate shades when [colors] is empty. Defaults to
     * the primary color of the MaterialTheme.
     * @param alpha The alpha value applied to rendered pie slices. Defaults to 1f.
     * @param colors The colors to draw slices in, in slice order. Empty by default, which draws a
     * generated shade per slice. When set, the count must match the slice count.
     */
    @Composable
    fun slices(
        baseColor: Color = StyleDefaults.seriesColor,
        alpha: Float = StyleDefaults.seriesAlpha,
        colors: List<Color> = emptyList(),
    ) = PieChartSlicesStyle(
        alpha = alpha,
        baseColor = baseColor,
        colors = colors.toImmutableList(),
    )

    /**
     * Returns a [PieChartBorderStyle] with the provided parameters or their default values.
     *
     * @param color The color of the border around the pie chart. Defaults to the surface color of the MaterialTheme.
     * @param width The width of the border around the pie chart. Defaults to 1.dp.
     */
    @Composable
    fun border(
        color: Color = StyleDefaults.pieBorderColor,
        width: Dp = StyleDefaults.lineWidth,
    ) = PieChartBorderStyle(
        width = width,
        color = color,
    )

    /**
     * Returns a [LegendStyle] with the provided parameters or their default values.
     *
     * @param visible Whether the legend is visible. Defaults to true.
     */
    @Composable
    fun legend(visible: Boolean = StyleDefaults.legendVisible): LegendStyle = LegendDefaults.style(visible = visible)
}

/** Returns [this] with alpha, donut hole, and sizes clamped to drawable values. */
internal fun PieChartStyle.clamp(density: Density) =
    PieChartStyle(
        modifier = modifier,
        chartContainerStyle = chartContainerStyle,
        donut = donut.clamp(),
        slices = slices.clamp(),
        border = border.clamp(density),
        legend = legend,
    )
