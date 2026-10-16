package io.github.hdcharts.core.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampSize

/**
 * Axis lines, grid, and labels shared by Cartesian charts.
 *
 * @property visible Whether any axes elements show. When false, lines, grid, and labels are hidden
 * and labels reserve no space. Individual visibility settings apply when this is true.
 * @property lines X- and Y-axis stroke configuration. The X-axis line follows the chart's baseline.
 * @property grid Horizontal grid configuration.
 * @property xLabels X-axis label configuration.
 * @property yLabels Numeric Y-axis label configuration.
 */
@Immutable
data class AxesStyle(
    val visible: Boolean,
    val lines: AxisLinesStyle,
    val grid: GridStyle,
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

/** Shared defaults for Cartesian axes. */
object AxesDefaults {
    /** Returns an [AxesStyle] with the supplied values or theme-aware defaults. */
    @Composable
    fun style(
        visible: Boolean = StyleDefaults.axesVisible,
        lines: AxisLinesStyle = AxisLinesDefaults.style(),
        grid: GridStyle = GridDefaults.style(),
        xLabels: AxisLabelStyle = AxisLabelDefaults.style(),
        yLabels: AxisLabelStyle = AxisLabelDefaults.style(),
    ): AxesStyle = AxesStyle(visible = visible, lines = lines, grid = grid, xLabels = xLabels, yLabels = yLabels)
}

/**
 * X- and Y-axis strokes.
 *
 * @property visible Whether both axis lines show when [AxesStyle.visible] is true.
 * @property color The axis line color.
 * @property lineWidth The axis line stroke width in density-independent pixels.
 */
@Immutable
data class AxisLinesStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Dp,
)

/** Shared defaults for axis strokes. */
object AxisLinesDefaults {
    /** Returns an [AxisLinesStyle] with the supplied values or theme-aware defaults. */
    @Composable
    fun style(
        visible: Boolean = StyleDefaults.axisLinesVisible,
        color: Color = StyleDefaults.axisColor,
        lineWidth: Dp = StyleDefaults.lineWidth,
    ): AxisLinesStyle = AxisLinesStyle(visible = visible, color = color, lineWidth = lineWidth)
}

/** Clamps stroke widths and grid steps. Label sizes pass through for validation. */
@InternalChartsApi
fun AxesStyle.clamp(density: Density): AxesStyle =
    AxesStyle(
        visible = visible,
        lines =
            AxisLinesStyle(
                visible = lines.visible,
                color = lines.color,
                lineWidth = lines.lineWidth.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            ),
        grid = grid.clamp(density),
        xLabels = xLabels,
        yLabels = yLabels,
    )
