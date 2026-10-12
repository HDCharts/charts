package io.github.hdcharts.core.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampGridSteps
import io.github.hdcharts.core.internal.clampSize

/**
 * Horizontal grid configuration shared by charts with a numeric Y axis.
 *
 * @property visible Whether the grid is visible.
 * @property steps Number of horizontal grid intervals when Y labels are hidden. With Y labels shown,
 *   the grid draws one line per label. 0 draws no grid.
 * @property color The grid line color.
 * @property lineWidth The grid line stroke width in density-independent pixels.
 */
@Immutable
data class GridStyle(
    val visible: Boolean,
    val steps: Int,
    val color: Color,
    val lineWidth: Dp,
)

/**
 * Shared defaults for [GridStyle].
 */
object GridDefaults {
    @Composable
    fun style(
        visible: Boolean = StyleDefaults.gridVisible,
        steps: Int = StyleDefaults.gridSteps,
        color: Color = StyleDefaults.gridColor,
        lineWidth: Dp = StyleDefaults.lineWidth,
    ): GridStyle = GridStyle(visible = visible, steps = steps, color = color, lineWidth = lineWidth)
}

/**
 * Returns [this] with [GridStyle.steps] and [GridStyle.lineWidth] clamped. Names every field instead of
 * using `copy`, so a field added to the constructor fails to compile here until it is dealt with.
 */
@InternalChartsApi
fun GridStyle.clamp(density: Density) =
    GridStyle(
        visible = visible,
        steps = steps.clampGridSteps(),
        color = color,
        lineWidth = lineWidth.clampSize(fallback = StyleDefaults.lineWidth, density = density),
    )
