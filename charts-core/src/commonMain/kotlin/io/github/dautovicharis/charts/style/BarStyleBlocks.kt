package io.github.dautovicharis.charts.style

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

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
 * @property color The fallback bar color when [colors] is empty or shorter than the bar count.
 * @property colors Optional explicit per-bar colors. Empty means use [color] / generated shades;
 * non-empty must match the source bar count.
 * @property alpha The alpha value applied to rendered bars. Replaces the source color alpha.
 * @property space The spacing between bars.
 * @property minBarWidth The minimum width of each bar.
 */
@Immutable
data class BarBarsStyle(
    val color: Color,
    val colors: List<Color>,
    val alpha: Float,
    val space: androidx.compose.ui.unit.Dp,
    val minBarWidth: androidx.compose.ui.unit.Dp,
)

/**
 * Optional fixed Y-axis range for vertical bar and histogram charts.
 *
 * `null` for either bound means the chart derives that bound from data.
 * When both bounds are non-null, equal or reversed bounds fall back to data bounds.
 *
 * @property min Optional minimum value.
 * @property max Optional maximum value.
 */
@Immutable
data class BarRangeStyle(
    val min: Float?,
    val max: Float?,
)

/**
 * Horizontal grid configuration for vertical bar and histogram charts.
 *
 * @property visible Whether the grid is visible.
 * @property steps Number of horizontal grid intervals.
 * @property color The grid line color.
 * @property lineWidth The grid line stroke width in pixels.
 */
@Immutable
data class BarGridStyle(
    val visible: Boolean,
    val steps: Int,
    val color: Color,
    val lineWidth: Float,
)

/**
 * Axis configuration for vertical bar and histogram charts.
 *
 * @property visible Whether the left Y-axis line and baseline are visible.
 * @property color The axis line color.
 * @property lineWidth The axis line stroke width in pixels.
 * @property xLabels X-axis label configuration.
 * @property yLabels Y-axis label configuration.
 */
@Immutable
data class BarAxisStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Float,
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

/**
 * Selection indicator configuration for vertical bar and histogram charts.
 *
 * @property visible Whether the selection line is visible.
 * @property color The selection line color.
 * @property width The selection line stroke width in pixels.
 */
@Immutable
data class BarSelectionLineStyle(
    val visible: Boolean,
    val color: Color,
    val width: Float,
)
