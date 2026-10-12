package io.github.hdcharts.core.internal.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.axis.AxisYLayoutTick
import io.github.hdcharts.core.style.GridStyle

/**
 * Draws horizontal lines across the plot: one on each of the Y-label [ticks] when they show, otherwise
 * [GridStyle.steps] + 1 evenly spaced lines kept [verticalInsetPx] from the top and bottom edges.
 * Draws nothing when [grid] is hidden or has 0 steps.
 */
@InternalChartsApi
fun DrawScope.drawHorizontalGrid(
    grid: GridStyle,
    ticks: List<AxisYLayoutTick>,
    verticalInsetPx: Float = 0f,
) {
    val strokeWidth = grid.lineWidth.toPx()
    forEachGridLineY(grid = grid, ticks = ticks, heightPx = size.height, verticalInsetPx = verticalInsetPx) { y ->
        drawLine(color = grid.color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = strokeWidth)
    }
}

/** Calls [action] with the Y of each grid line that [drawHorizontalGrid] draws. */
internal inline fun forEachGridLineY(
    grid: GridStyle,
    ticks: List<AxisYLayoutTick>,
    heightPx: Float,
    verticalInsetPx: Float,
    action: (y: Float) -> Unit,
) {
    if (!grid.visible || grid.steps <= 0) return
    if (ticks.size >= 2) {
        for (index in ticks.indices) action(ticks[index].centerY)
        return
    }
    for (step in 0..grid.steps) {
        action(gridLineY(step = step, steps = grid.steps, heightPx = heightPx, verticalInsetPx = verticalInsetPx))
    }
}

/** Y of grid line [step] of [steps] in a plot [heightPx] tall, kept [verticalInsetPx] from its edges. */
internal fun gridLineY(
    step: Int,
    steps: Int,
    heightPx: Float,
    verticalInsetPx: Float,
): Float {
    val safeInset = verticalInsetPx.coerceIn(0f, (heightPx / 2f).coerceAtLeast(0f))
    val drawableHeight = (heightPx - safeInset * 2f).coerceAtLeast(0f)
    return safeInset + drawableHeight * (step / steps.toFloat())
}
