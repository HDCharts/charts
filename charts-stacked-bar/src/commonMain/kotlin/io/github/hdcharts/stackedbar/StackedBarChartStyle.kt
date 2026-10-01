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
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class StackedBarSegmentStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val alpha: Float,
) {
    constructor(color: Color, colors: List<Color>, alpha: Float) : this(
        color = color,
        colors = colors.toImmutableList(),
        alpha = alpha,
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
}

@Immutable
data class StackedBarLayoutStyle(
    val space: Dp,
    val minBarWidth: Dp,
)

@Immutable
data class StackedBarAxisStyle(
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

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
)

@Immutable
class StackedBarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val segments: StackedBarSegmentStyle,
    val layout: StackedBarLayoutStyle,
    val axis: StackedBarAxisStyle,
    val selection: StackedBarSelectionStyle,
    val zoomControlsVisible: Boolean,
)

object StackedBarChartDefaults {
    /** Returns a [StackedBarChartStyle] with the provided parameters or their default values. */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        segments: StackedBarSegmentStyle = segments(),
        layout: StackedBarLayoutStyle = layout(),
        axis: StackedBarAxisStyle = axis(),
        selection: StackedBarSelectionStyle = selection(),
        zoomControlsVisible: Boolean = true,
    ): StackedBarChartStyle =
        StackedBarChartStyle(
            chartContainerStyle = chartContainerStyle,
            segments = segments,
            layout = layout,
            axis = axis,
            selection = selection,
            zoomControlsVisible = zoomControlsVisible,
        )

    /** Returns a [StackedBarSegmentStyle] for the stacked segments. */
    @Composable
    fun segments(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        alpha: Float = StyleDefaults.seriesAlpha,
    ): StackedBarSegmentStyle = StackedBarSegmentStyle(color = color, colors = colors, alpha = alpha)

    /** Returns a [StackedBarLayoutStyle] for bar spacing and width. */
    @Composable
    fun layout(
        space: Dp = StyleDefaults.barSpacing,
        minBarWidth: Dp = StyleDefaults.minBarWidth,
    ): StackedBarLayoutStyle = StackedBarLayoutStyle(space = space, minBarWidth = minBarWidth)

    /** Returns a [StackedBarAxisStyle] for axis labels. */
    @Composable
    fun axis(
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ): StackedBarAxisStyle = StackedBarAxisStyle(xLabels = xLabels, yLabels = yLabels)

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

    /** Returns a [StackedBarSelectionStyle] for the selection indicator. */
    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = StyleDefaults.selectionColor,
        width: Dp = StyleDefaults.lineWidth,
        unselectedAlpha: Float = StyleDefaults.unselectedAlpha,
    ): StackedBarSelectionStyle =
        StackedBarSelectionStyle(
            visible = visible,
            color = color,
            width = width,
            unselectedAlpha = unselectedAlpha,
        )
}

/** Returns [this] with alphas and sizes clamped to drawable values. */
internal fun StackedBarChartStyle.clamped(density: Density): StackedBarChartStyle =
    StackedBarChartStyle(
        chartContainerStyle = chartContainerStyle,
        segments = segments.copy(alpha = segments.alpha.clampAlpha()),
        layout =
            layout.copy(
                space = layout.space.clampSize(fallback = StyleDefaults.barSpacing, density = density),
                minBarWidth = layout.minBarWidth.clampSize(fallback = StyleDefaults.minBarWidth, density = density),
            ),
        axis = axis,
        selection =
            selection.copy(
                width = selection.width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
                unselectedAlpha = selection.unselectedAlpha.clampAlpha(),
            ),
        zoomControlsVisible = zoomControlsVisible,
    )
