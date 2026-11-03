package io.github.hdcharts.scatter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxesStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.core.style.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** Point and bubble appearance. Bubble radii are interpolated by area from [minSize] to [maxSize]. */
@Immutable
data class ScatterPointStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val alpha: Float,
    val size: Dp,
    val minSize: Dp,
    val maxSize: Dp,
) {
    constructor(
        color: Color,
        colors: List<Color>,
        alpha: Float,
        size: Dp,
        minSize: Dp,
        maxSize: Dp,
    ) : this(color, colors.toImmutableList(), alpha, size, minSize, maxSize)

    internal fun resolveColors(count: Int) = resolvePaletteColors(color, colors, count, singleItemUsesBase = true)

    internal fun clamp(density: Density): ScatterPointStyle {
        val clampedMinSize = minSize.clampSize(StyleDefaults.pointSize, density)
        val clampedMaxSize =
            maxSize
                .clampSize(fallback = StyleDefaults.scatterMaxBubbleSize, density = density)
                .coerceAtLeast(clampedMinSize)
        return copy(
            alpha = alpha.clampAlpha(),
            size = size.clampSize(StyleDefaults.pointSize, density),
            minSize = clampedMinSize,
            maxSize = clampedMaxSize,
        )
    }
}

/**
 * Optional fixed bounds for numeric axes. Null bounds are derived from data. Out-of-range points
 * draw at the nearest plot edge.
 */
@Immutable
data class ScatterRangeStyle(
    val xMin: Double? = null,
    val xMax: Double? = null,
    val yMin: Double? = null,
    val yMax: Double? = null,
)

/** Presentation for scatter and bubble charts. */
@Immutable
class ScatterChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val points: ScatterPointStyle,
    val axes: AxesStyle,
    val range: ScatterRangeStyle,
    val legend: LegendStyle,
)

/** Defaults for scatter and bubble charts. */
object ScatterChartDefaults {
    /** Formats numeric axis labels with the shared chart defaults. */
    val axisValueFormatter = StyleDefaults.axisValueFormatter

    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        points: ScatterPointStyle = points(),
        axes: AxesStyle = AxesDefaults.style(),
        range: ScatterRangeStyle = ScatterRangeStyle(),
        legend: LegendStyle = LegendDefaults.style(),
    ) = ScatterChartStyle(chartContainerStyle, points, axes, range, legend)

    @Composable
    fun points(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        alpha: Float = StyleDefaults.scatterPointAlpha,
        size: Dp = StyleDefaults.pointSize,
        minSize: Dp = StyleDefaults.pointSize,
        maxSize: Dp = StyleDefaults.scatterMaxBubbleSize,
    ) = ScatterPointStyle(color, colors, alpha, size, minSize, maxSize)
}

internal fun ScatterChartStyle.clamp(density: Density) =
    ScatterChartStyle(chartContainerStyle, points.clamp(density), axes.clamp(density), range, legend)
