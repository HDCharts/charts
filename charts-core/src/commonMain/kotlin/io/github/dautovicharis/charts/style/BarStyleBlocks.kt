package io.github.dautovicharis.charts.style

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Axis label configuration shared by Cartesian charts.
 *
 * @property visible Whether the labels are visible.
 * @property color The label color.
 * @property size The label text size.
 * @property count Maximum or target number of labels to render.
 */
@Immutable
data class AxisLabelStyle(
    val visible: Boolean,
    val color: Color,
    val size: TextUnit,
    val count: Int,
)

/**
 * Bar visual configuration shared by vertical bar and histogram charts.
 *
 * @property color The fallback bar color when [colors] is empty.
 * @property colors Optional explicit per-bar colors. Empty means use [color] / generated shades;
 * non-empty must match the source bar count.
 * @property alpha The alpha value applied to rendered bars. Replaces the source color alpha.
 * @property space The spacing between bars.
 * @property minBarWidth The minimum width of each bar.
 */
@Immutable
data class BarBarsStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
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
        alpha: Float,
        space: Dp,
        minBarWidth: Dp,
    ) : this(
        color = color,
        colors = colors.toImmutableList(),
        alpha = alpha,
        space = space,
        minBarWidth = minBarWidth,
    )
}

/**
 * Optional fixed Y-axis range for vertical bar and histogram charts.
 *
 * `null` for either bound means the chart derives that bound from data.
 * Explicit bounds must be finite. If the resolved range is equal or reversed,
 * both bounds fall back to the zero-inclusive source domain.
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
 * Horizontal grid configuration for vertical bar and histogram charts.
 *
 * @property visible Whether the grid is visible.
 * @property steps Number of horizontal grid intervals.
 * @property color The grid line color.
 * @property lineWidth The grid line stroke width in density-independent pixels.
 */
@Immutable
data class BarGridStyle(
    val visible: Boolean,
    val steps: Int,
    val color: Color,
    val lineWidth: Dp,
)

/**
 * Axis configuration for vertical bar and histogram charts.
 *
 * @property visible Whether the left Y-axis line and baseline are visible.
 * @property color The axis line color.
 * @property lineWidth The axis line stroke width in density-independent pixels.
 * @property xLabels X-axis label configuration.
 * @property yLabels Y-axis label configuration.
 */
@Immutable
data class BarAxisStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Dp,
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

/**
 * Selection indicator configuration for vertical bar and histogram charts.
 *
 * @property visible Whether the selection line is visible.
 * @property color The selection line color.
 * @property width The selection line stroke width in density-independent pixels.
 */
@Immutable
data class BarSelectionLineStyle(
    val visible: Boolean,
    val color: Color,
    val width: Dp,
)
