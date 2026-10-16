package io.github.hdcharts.stackedbar

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
 * Colors and fills for stacked bar segments.
 *
 * @property color Base color for the generated series palette.
 * @property colors Optional palette in series order; empty generates shades of [color].
 * When set, its count must match the series count.
 * @property alpha Segment alpha in `0..1`. Replaces solid color alpha and multiplies gradient alphas.
 * @property gradient Optional gradient per shape or across the plot. Null draws solid segments.
 */
@Immutable
data class StackedBarSegmentStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val alpha: Float,
    val gradient: ChartGradient?,
) {
    /** Copies the supplied list into an immutable list. */
    constructor(
        color: Color,
        colors: List<Color>,
        alpha: Float,
        gradient: ChartGradient? = null,
    ) : this(
        color = color,
        colors = colors.toImmutableList(),
        alpha = alpha,
        gradient = gradient,
    )

    /**
     * Returns the segment colors the chart draws for [seriesCount] series, before [alpha] is applied:
     * [colors] when set, or generated shades of [color] when [colors] is empty.
     */
    fun resolveColors(seriesCount: Int): ImmutableList<Color> =
        resolvePaletteColors(
            baseColor = color,
            colors = colors,
            count = seriesCount,
            singleItemUsesBase = false,
        )

    /**
     * Returns this block with [alpha] clamped. Names every field instead of using `copy`, so a field
     * added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp() =
        StackedBarSegmentStyle(
            color = color,
            colors = colors,
            alpha = alpha.clampAlpha(),
            gradient = gradient?.clamp(),
        )
}

/**
 * Spacing and minimum width of stacked bars.
 *
 * @property space Gap between bars in density-independent pixels.
 * @property minBarWidth Minimum bar width used to determine when data needs compact mode or scrolling.
 */
@Immutable
data class StackedBarLayoutStyle(
    val space: Dp,
    val minBarWidth: Dp,
) {
    /**
     * Returns this block with [space] and [minBarWidth] clamped. Names every field instead of using
     * `copy`, so a field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        StackedBarLayoutStyle(
            space = space.clampSize(fallback = StyleDefaults.barSpacing, density = density),
            minBarWidth = minBarWidth.clampSize(fallback = StyleDefaults.minBarWidth, density = density),
        )
}

/**
 * Selection indicator configuration for stacked bar charts.
 *
 * While a bar is selected, the other bars are drawn at [unselectedAlpha] so the selected bar
 * stands out. The selection line is drawn only above the selected bar.
 *
 * @property visible Whether the selection indicator is visible. `false` also keeps every bar solid.
 * @property color The selection line color.
 * @property width The selection line stroke width in density-independent pixels.
 * @property unselectedAlpha The alpha multiplier for the other bars while a bar is selected, in
 * `0..1`. `1f` keeps every bar solid.
 */
@Immutable
data class StackedBarSelectionStyle(
    val visible: Boolean,
    val color: Color,
    val width: Dp,
    val unselectedAlpha: Float,
) {
    /**
     * Returns this block with [width] and [unselectedAlpha] clamped. Names every field instead of
     * using `copy`, so a field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        StackedBarSelectionStyle(
            visible = visible,
            color = color,
            width = width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            unselectedAlpha = unselectedAlpha.clampAlpha(),
        )
}

/**
 * Presentation for stacked bar charts.
 *
 * @property chartContainerStyle Shared content spacing and title and subtitle text styles.
 * @property segments Series palette, alpha, and gradient.
 * @property layout Bar spacing and minimum width.
 * @property axes Axis lines, grid, and X/Y labels.
 * @property selection Selection line and dimming of other bars.
 * @property legend Segment legend visibility.
 * @property zoomControlsVisible Whether zoom controls appear when interaction and scrolling are enabled.
 */
@Immutable
class StackedBarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val segments: StackedBarSegmentStyle,
    val layout: StackedBarLayoutStyle,
    val axes: AxesStyle,
    val selection: StackedBarSelectionStyle,
    val legend: LegendStyle,
    val zoomControlsVisible: Boolean,
)

/** Theme-aware style factories and default value formatters for [StackedBarChartStyle]. */
object StackedBarChartDefaults {
    /** Returns a [StackedBarChartStyle] with the provided parameters or their default values. */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        segments: StackedBarSegmentStyle = segments(),
        layout: StackedBarLayoutStyle = layout(),
        axes: AxesStyle = AxesDefaults.style(),
        selection: StackedBarSelectionStyle = selection(),
        legend: LegendStyle = legend(),
        zoomControlsVisible: Boolean = true,
    ) = StackedBarChartStyle(
        chartContainerStyle = chartContainerStyle,
        segments = segments,
        layout = layout,
        axes = axes,
        selection = selection,
        legend = legend,
        zoomControlsVisible = zoomControlsVisible,
    )

    /** Returns a [StackedBarSegmentStyle] for the stacked segments. */
    @Composable
    fun segments(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        alpha: Float = StyleDefaults.seriesAlpha,
        gradient: ChartGradient? = null,
    ): StackedBarSegmentStyle =
        StackedBarSegmentStyle(
            color = color,
            colors = colors,
            alpha = alpha,
            gradient = gradient,
        )

    /** Returns a [StackedBarLayoutStyle] for bar spacing and width. */
    @Composable
    fun layout(
        space: Dp = StyleDefaults.barSpacing,
        minBarWidth: Dp = StyleDefaults.minBarWidth,
    ): StackedBarLayoutStyle = StackedBarLayoutStyle(space = space, minBarWidth = minBarWidth)

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

    /** Returns a [StackedBarSelectionStyle] for the selection indicator. */
    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = StyleDefaults.selectionColor,
        width: Dp = StyleDefaults.lineWidth,
        unselectedAlpha: Float = StyleDefaults.unselectedAlpha,
    ) = StackedBarSelectionStyle(
        visible = visible,
        color = color,
        width = width,
        unselectedAlpha = unselectedAlpha,
    )

    /**
     * Returns a [LegendStyle] with the provided visibility.
     *
     * The chart renders the legend only when the data has more than one segment,
     * at least one segment has a name, and [visible] is true.
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
internal fun StackedBarChartStyle.clamp(density: Density) =
    StackedBarChartStyle(
        chartContainerStyle = chartContainerStyle,
        segments = segments.clamp(),
        layout = layout.clamp(density),
        axes = axes.clamp(density),
        selection = selection.clamp(density),
        legend = legend,
        zoomControlsVisible = zoomControlsVisible,
    )
