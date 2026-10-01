package io.github.hdcharts.stackedarea

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Fill configuration for the stacked layers.
 *
 * @property bezier Draws each layer's edges as smooth curves when `true`, or as straight
 * segments between points when `false`.
 */
@Immutable
data class StackedAreaFillStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val alpha: Float,
    val bezier: Boolean,
) {
    constructor(
        color: Color,
        colors: List<Color>,
        alpha: Float,
        bezier: Boolean,
    ) : this(
        color = color,
        colors = colors.toImmutableList(),
        alpha = alpha,
        bezier = bezier,
    )

    /**
     * Returns the fill colors the chart draws for [seriesCount] series, before [alpha] is applied.
     *
     * A single series uses [color]. Multiple series use [colors] when set, or generated
     * shades of [color] when [colors] is empty.
     */
    fun resolveColors(seriesCount: Int): ImmutableList<Color> =
        resolvePaletteColors(
            baseColor = color,
            colors = colors,
            count = seriesCount,
            singleItemUsesBase = true,
        )
}

@Immutable
data class StackedAreaAxisStyle(
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

/**
 * Selection indicator configuration for stacked area charts.
 *
 * While a point is selected, a column around it keeps full color and the rest of the stack is drawn
 * at [unselectedAlpha]. The selection line is drawn only above the stack, at the selected point.
 *
 * @property visible Whether the selection indicator is visible. `false` also keeps the stack at full color.
 * @property color The selection line color.
 * @property width The selection line stroke width in density-independent pixels.
 * @property unselectedAlpha The alpha multiplier for the stack outside the selected column, in
 * `0..1`. `1f` keeps the whole stack at full color.
 */
@Immutable
data class StackedAreaSelectionStyle(
    val visible: Boolean,
    val color: Color,
    val width: Dp,
    val unselectedAlpha: Float,
)

@Immutable
class StackedAreaChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val fill: StackedAreaFillStyle,
    val axis: StackedAreaAxisStyle,
    val selection: StackedAreaSelectionStyle,
    val zoomControlsVisible: Boolean,
)

object StackedAreaChartDefaults {
    /** Returns a [StackedAreaChartStyle] with the provided parameters or their default values. */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        fill: StackedAreaFillStyle = fill(),
        axis: StackedAreaAxisStyle = axis(),
        selection: StackedAreaSelectionStyle = selection(),
        zoomControlsVisible: Boolean = true,
    ): StackedAreaChartStyle =
        StackedAreaChartStyle(
            chartContainerStyle = chartContainerStyle,
            fill = fill,
            axis = axis,
            selection = selection,
            zoomControlsVisible = zoomControlsVisible,
        )

    /** Returns a [StackedAreaFillStyle] for the stacked layers. */
    @Composable
    fun fill(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        alpha: Float = StyleDefaults.seriesAlpha,
        bezier: Boolean = false,
    ): StackedAreaFillStyle =
        StackedAreaFillStyle(
            color = color,
            colors = colors,
            alpha = alpha,
            bezier = bezier,
        )

    /** Returns a [StackedAreaAxisStyle] for axis labels. */
    @Composable
    fun axis(
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ): StackedAreaAxisStyle = StackedAreaAxisStyle(xLabels = xLabels, yLabels = yLabels)

    /** Returns an [AxisLabelStyle] for X-axis labels. */
    @Composable
    fun xLabels(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = null,
    ): AxisLabelStyle = AxisLabelStyle(visible = visible, color = color, size = size, maxCount = maxCount)

    /** Returns an [AxisLabelStyle] for Y-axis labels. */
    @Composable
    fun yLabels(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = null,
    ): AxisLabelStyle = AxisLabelStyle(visible = visible, color = color, size = size, maxCount = maxCount)

    /** Returns a [StackedAreaSelectionStyle] for the selection indicator. */
    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = StyleDefaults.selectionColor,
        width: Dp = StyleDefaults.lineWidth,
        unselectedAlpha: Float = StyleDefaults.unselectedAlpha,
    ): StackedAreaSelectionStyle =
        StackedAreaSelectionStyle(
            visible = visible,
            color = color,
            width = width,
            unselectedAlpha = unselectedAlpha,
        )
}

/** Returns [this] with alphas and sizes clamped to drawable values. */
internal fun StackedAreaChartStyle.clamped(density: Density): StackedAreaChartStyle =
    StackedAreaChartStyle(
        chartContainerStyle = chartContainerStyle,
        fill = fill.copy(alpha = fill.alpha.clampAlpha()),
        axis = axis,
        selection =
            selection.copy(
                width = selection.width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
                unselectedAlpha = selection.unselectedAlpha.clampAlpha(),
            ),
        zoomControlsVisible = zoomControlsVisible,
    )
