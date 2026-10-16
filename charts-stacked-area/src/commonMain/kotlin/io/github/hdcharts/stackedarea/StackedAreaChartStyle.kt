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
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxesStyle
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.core.style.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Fill configuration for the stacked layers.
 *
 * @property color Single-series fill color and base color for generated palettes.
 * @property colors Optional multi-series palette; when set, its count must match the series count.
 * A single series uses [color], even when a one-color palette is supplied.
 * @property alpha Fill alpha in `0..1`. Replaces solid color alpha and multiplies gradient alphas.
 * @property bezier Draws each layer's edges as smooth curves when `true`, or as straight
 * segments between points when `false`.
 * @property gradient Shared gradient that paints every layer. `null` draws solid fills.
 */
@Immutable
data class StackedAreaFillStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val alpha: Float,
    val bezier: Boolean,
    val gradient: ChartGradient?,
) {
    /** Copies the supplied list into an immutable list. */
    constructor(
        color: Color,
        colors: List<Color>,
        alpha: Float,
        bezier: Boolean,
        gradient: ChartGradient?,
    ) : this(
        color = color,
        colors = colors.toImmutableList(),
        alpha = alpha,
        bezier = bezier,
        gradient = gradient,
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

    /**
     * Returns this block with [alpha] and [gradient] clamped.
     * Names every field instead of using `copy`, so a field added to the constructor fails to compile
     * here until it is dealt with.
     */
    internal fun clamp() =
        StackedAreaFillStyle(
            color = color,
            colors = colors,
            alpha = alpha.clampAlpha(),
            bezier = bezier,
            gradient = gradient?.clamp(),
        )
}

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
) {
    /**
     * Returns this block with [width] and [unselectedAlpha] clamped. Names every field instead of
     * `copy`, so a field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        StackedAreaSelectionStyle(
            visible = visible,
            color = color,
            width = width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            unselectedAlpha = unselectedAlpha.clampAlpha(),
        )
}

/**
 * Presentation for stacked area charts.
 *
 * @property chartContainerStyle Shared content spacing and title and subtitle text styles.
 * @property fill Series palette, alpha, interpolation, and gradient.
 * @property axes Axis lines, grid, and X/Y labels.
 * @property selection Selection line and dimming outside the selected column.
 * @property legend Series legend visibility.
 * @property zoomControlsVisible Whether zoom controls appear when interaction and scrolling are enabled.
 */
@Immutable
class StackedAreaChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val fill: StackedAreaFillStyle,
    val axes: AxesStyle,
    val selection: StackedAreaSelectionStyle,
    val legend: LegendStyle,
    val zoomControlsVisible: Boolean,
)

/** Theme-aware style factories and default value formatters for [StackedAreaChartStyle]. */
object StackedAreaChartDefaults {
    /** Returns a [StackedAreaChartStyle] with the provided parameters or their default values. */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        fill: StackedAreaFillStyle = fill(),
        axes: AxesStyle = AxesDefaults.style(),
        selection: StackedAreaSelectionStyle = selection(),
        legend: LegendStyle = legend(),
        zoomControlsVisible: Boolean = true,
    ) = StackedAreaChartStyle(
        chartContainerStyle = chartContainerStyle,
        fill = fill,
        axes = axes,
        selection = selection,
        legend = legend,
        zoomControlsVisible = zoomControlsVisible,
    )

    /** Returns a [StackedAreaFillStyle] for the stacked layers. */
    @Composable
    fun fill(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        alpha: Float = StyleDefaults.seriesAlpha,
        bezier: Boolean = false,
        gradient: ChartGradient? = null,
    ) = StackedAreaFillStyle(
        color = color,
        colors = colors,
        alpha = alpha,
        bezier = bezier,
        gradient = gradient,
    )

    /** Returns an [AxisLabelStyle] for X-axis labels. */
    @Composable
    fun xLabels(
        visible: Boolean = StyleDefaults.axisLabelsVisible,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = StyleDefaults.axisLabelMaxCount,
    ): AxisLabelStyle = AxisLabelStyle(visible = visible, color = color, size = size, maxCount = maxCount)

    /** Returns an [AxisLabelStyle] for Y-axis labels. */
    @Composable
    fun yLabels(
        visible: Boolean = StyleDefaults.axisLabelsVisible,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = StyleDefaults.axisLabelMaxCount,
    ): AxisLabelStyle = AxisLabelStyle(visible = visible, color = color, size = size, maxCount = maxCount)

    /** Returns a [StackedAreaSelectionStyle] for the selection indicator. */
    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = StyleDefaults.selectionColor,
        width: Dp = StyleDefaults.lineWidth,
        unselectedAlpha: Float = StyleDefaults.unselectedAlpha,
    ) = StackedAreaSelectionStyle(
        visible = visible,
        color = color,
        width = width,
        unselectedAlpha = unselectedAlpha,
    )

    /**
     * Returns a [LegendStyle] with the provided visibility.
     *
     * The chart renders the legend only when the data has more than one series,
     * at least one series has a name, and [visible] is true.
     *
     * @param visible Whether the legend is visible. Defaults to true.
     */
    @Composable
    fun legend(visible: Boolean = StyleDefaults.legendVisible): LegendStyle = LegendDefaults.style(visible = visible)

    /** Default formatter for selected values in the title and legend. */
    val selectedValueFormatter: ChartValueFormatter = StyleDefaults.selectedValueFormatter

    /** Default axis formatter, omitting only the terminal `.0` on whole values. */
    val axisValueFormatter: ChartValueFormatter = StyleDefaults.axisValueFormatter
}

/** Returns [this] with alphas and sizes clamped to drawable values. */
internal fun StackedAreaChartStyle.clamp(density: Density) =
    StackedAreaChartStyle(
        chartContainerStyle = chartContainerStyle,
        fill = fill.clamp(),
        axes = axes.clamp(density),
        selection = selection.clamp(density),
        legend = legend,
        zoomControlsVisible = zoomControlsVisible,
    )
