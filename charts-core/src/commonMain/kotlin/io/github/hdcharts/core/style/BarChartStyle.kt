package io.github.hdcharts.core.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.model.ChartValueFormatter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Bar visual configuration shared by vertical bar and histogram charts.
 *
 * @property color The fallback bar color when [colors] is empty.
 * @property colors Optional explicit per-bar colors. Empty means every bar uses [color];
 * non-empty must match the source bar count.
 * @property gradient Optional gradient that paints the bars. Its span picks one gradient per bar or
 * one across the plot. Null draws solid bars.
 * @property alpha The alpha value applied to rendered bars. Replaces the source color alpha, and
 * multiplies a gradient's own alphas.
 * @property space The spacing between bars.
 * @property minBarWidth The minimum width of each bar.
 */
@Immutable
data class BarBarsStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val gradient: ChartGradient?,
    val alpha: Float,
    val space: Dp,
    val minBarWidth: Dp,
) {
    /**
     * Copies [colors] into an immutable palette. Use an immutable list with [copy].
     */
    constructor(
        color: Color,
        colors: List<Color>,
        gradient: ChartGradient?,
        alpha: Float,
        space: Dp,
        minBarWidth: Dp,
    ) : this(
        color = color,
        colors = colors.toImmutableList(),
        gradient = gradient,
        alpha = alpha,
        space = space,
        minBarWidth = minBarWidth,
    )

    /**
     * Returns the bar colors the chart draws for [barCount] bars, before [alpha] is applied:
     * [colors] when set, or [color] for every bar when [colors] is empty.
     */
    fun resolveColors(barCount: Int): ImmutableList<Color> =
        when {
            barCount <= 0 -> persistentListOf()
            colors.isEmpty() -> List(barCount) { color }.toImmutableList()
            else -> colors
        }

    /**
     * Returns this block with [alpha], gradient alphas, [space] and [minBarWidth] clamped.
     *
     * Names every field instead of using `copy`, so a field added to the constructor fails to compile
     * here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        BarBarsStyle(
            color = color,
            colors = colors,
            gradient = gradient?.clamp(),
            alpha = alpha.clampAlpha(),
            space = space.clampSize(fallback = StyleDefaults.barSpacing, density = density),
            minBarWidth = minBarWidth.clampSize(fallback = StyleDefaults.minBarWidth, density = density),
        )
}

/**
 * Optional fixed Y-axis range for vertical bar and histogram charts.
 *
 * Null bounds use the zero-inclusive data range. Set bounds must be finite.
 * If both are set and `min >= max`, the chart uses the data range.
 * If only one is set, the other is clamped to keep `min <= max`.
 *
 * @property min Optional minimum value.
 * @property max Optional maximum value.
 */
@Immutable
data class BarRangeStyle(
    val min: Double?,
    val max: Double?,
)

/**
 * Selection indicator configuration for vertical bar and histogram charts.
 *
 * While a bar is selected, the other bars are drawn at [unselectedAlpha] so the selected bar
 * stands out. The selection line is drawn only outside the selected bar.
 *
 * @property visible Whether the selection indicator is visible. `false` also keeps every bar solid.
 * @property color The selection line color.
 * @property width The selection line stroke width in density-independent pixels.
 * @property unselectedAlpha The alpha multiplier for the other bars while a bar is selected, in
 * `0..1`. `1f` keeps every bar solid.
 */
@Immutable
data class BarSelectionStyle(
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
        BarSelectionStyle(
            visible = visible,
            color = color,
            width = width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            unselectedAlpha = unselectedAlpha.clampAlpha(),
        )
}

/**
 * Immutable v3 style for vertical [io.github.hdcharts.bar.BarChart].
 *
 * Configuration is grouped into [bars], [range], [axes], and [selection]
 * blocks. The chart-level selection state is a top-level composable parameter, not part
 * of this style. Compose with [BarChartDefaults.style] for theme-aware defaults.
 *
 * @property chartContainerStyle The shared container presentation.
 * @property bars Bar visual block (colors, spacing, minimum width).
 * @property range Optional fixed Y-axis range.
 * @property axes Axis lines, grid, X/Y labels, and their shared visibility.
 * @property selection Selection indicator configuration.
 * @property zoomControlsVisible Whether zoom controls appear in the chart header.
 */
@Stable
class BarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val bars: BarBarsStyle,
    val range: BarRangeStyle,
    val axes: AxesStyle,
    val selection: BarSelectionStyle,
    val zoomControlsVisible: Boolean,
)

/**
 * Defaults factory for [BarChartStyle]. All parameters have theme-aware defaults.
 */
object BarChartDefaults {
    /** Default formatter for selected values in the title and legend. */
    val selectedValueFormatter: ChartValueFormatter = StyleDefaults.selectedValueFormatter

    /** Default axis formatter, omitting only the terminal `.0` on whole values. */
    val axisValueFormatter: ChartValueFormatter = StyleDefaults.axisValueFormatter

    /**
     * Returns a [BarChartStyle] with the provided parameters or their default values.
     */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        bars: BarBarsStyle = bars(),
        range: BarRangeStyle = range(),
        axes: AxesStyle = AxesDefaults.style(),
        selection: BarSelectionStyle = selection(),
        zoomControlsVisible: Boolean = true,
    ) = BarChartStyle(
        chartContainerStyle = chartContainerStyle,
        bars = bars,
        range = range,
        axes = axes,
        selection = selection,
        zoomControlsVisible = zoomControlsVisible,
    )

    /**
     * Returns a [BarBarsStyle] with bar-aware defaults.
     *
     * @param color The fallback bar color.
     * @param colors Optional explicit per-bar colors; must match bar count or be empty.
     * @param gradient Optional gradient that paints the bars. Defaults to null, which draws solid bars.
     * @param alpha The bar alpha. Defaults to 1f.
     * @param space The spacing between bars. Defaults to 10.dp.
     * @param minBarWidth The minimum width of each bar. Defaults to 10.dp.
     */
    @Composable
    fun bars(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        gradient: ChartGradient? = null,
        alpha: Float = StyleDefaults.seriesAlpha,
        space: Dp = StyleDefaults.barSpacing,
        minBarWidth: Dp = StyleDefaults.minBarWidth,
    ) = BarBarsStyle(
        color = color,
        colors = colors,
        gradient = gradient,
        alpha = alpha,
        space = space,
        minBarWidth = minBarWidth,
    )

    /**
     * Returns a [BarRangeStyle] for the optional fixed Y-axis range.
     */
    fun range(
        min: Double? = null,
        max: Double? = null,
    ): BarRangeStyle = BarRangeStyle(min = min, max = max)

    /**
     * Returns an [AxisLabelStyle] for X-axis labels.
     */
    @Composable
    fun xLabels(
        visible: Boolean = StyleDefaults.axisLabelsVisible,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = StyleDefaults.axisLabelMaxCount,
    ) = AxisLabelStyle(
        visible = visible,
        color = color,
        size = size,
        maxCount = maxCount,
    )

    /**
     * Returns an [AxisLabelStyle] for Y-axis labels.
     */
    @Composable
    fun yLabels(
        visible: Boolean = StyleDefaults.axisLabelsVisible,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = StyleDefaults.axisLabelMaxCount,
    ) = AxisLabelStyle(
        visible = visible,
        color = color,
        size = size,
        maxCount = maxCount,
    )

    /**
     * Returns a [BarSelectionStyle] for the selection indicator.
     */
    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = StyleDefaults.selectionColor,
        width: Dp = StyleDefaults.lineWidth,
        unselectedAlpha: Float = StyleDefaults.unselectedAlpha,
    ) = BarSelectionStyle(
        visible = visible,
        color = color,
        width = width,
        unselectedAlpha = unselectedAlpha,
    )
}

/** Returns [this] with alphas, sizes, and grid steps clamped to drawable values. */
@InternalChartsApi
fun BarChartStyle.clamp(density: Density) =
    BarChartStyle(
        chartContainerStyle = chartContainerStyle,
        bars = bars.clamp(density),
        range = range,
        axes = axes.clamp(density),
        selection = selection.clamp(density),
        zoomControlsVisible = zoomControlsVisible,
    )
