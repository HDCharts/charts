package io.github.hdcharts.core.internal.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.InternalChartsApi

/** Draws the Y-axis line along the left edge of the plot and the baseline along its bottom edge. */
@InternalChartsApi
fun DrawScope.drawPlotAxisLines(
    color: Color,
    lineWidth: Dp,
) {
    drawPlotYAxisLine(color = color, lineWidth = lineWidth)
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = lineWidth.toPx(),
    )
}

/** Draws the Y-axis line along the left edge of the plot. */
@InternalChartsApi
fun DrawScope.drawPlotYAxisLine(
    color: Color,
    lineWidth: Dp,
) {
    drawLine(color = color, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = lineWidth.toPx())
}
