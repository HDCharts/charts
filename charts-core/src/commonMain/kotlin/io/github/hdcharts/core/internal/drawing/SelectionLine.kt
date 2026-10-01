package io.github.hdcharts.core.internal.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.hdcharts.core.internal.InternalChartsApi

/**
 * Draws a vertical selection line at [x].
 *
 * [mark] is the selected mark's vertical span, which the line skips so the mark shows through on its
 * own. Leave it `null` for a chart with no mark to skip, and for a mark that is not on screen.
 */
@InternalChartsApi
fun DrawScope.drawSelectionLine(
    x: Float,
    color: Color,
    strokeWidth: Float,
    mark: ClosedFloatingPointRange<Float>? = null,
) {
    if (mark == null) {
        drawSegment(x = x, startY = 0f, endY = size.height, color = color, strokeWidth = strokeWidth)
        return
    }
    drawSegment(x = x, startY = 0f, endY = mark.start, color = color, strokeWidth = strokeWidth)
    drawSegment(x = x, startY = mark.endInclusive, endY = size.height, color = color, strokeWidth = strokeWidth)
}

private fun DrawScope.drawSegment(
    x: Float,
    startY: Float,
    endY: Float,
    color: Color,
    strokeWidth: Float,
) {
    if (endY <= startY) return
    drawLine(color = color, start = Offset(x, startY), end = Offset(x, endY), strokeWidth = strokeWidth)
}
