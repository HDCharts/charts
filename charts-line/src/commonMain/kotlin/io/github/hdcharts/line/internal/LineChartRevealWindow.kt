package io.github.hdcharts.line.internal

import io.github.hdcharts.core.internal.ANIMATION_TARGET

/** The horizontal band of a chart canvas that is on screen. */
internal data class LineChartRevealWindow(
    val leftPx: Float,
    val rightPx: Float,
)

/**
 * The slice of a chart canvas that the entry reveal has reached.
 *
 * Runs once, when the chart first appears: the line is wiped in from left to right, and its markers
 * fade and grow in once the wipe has nearly finished.
 *
 * The reveal sweeps the visible viewport, not the whole canvas. A scrolling chart sizes its canvas
 * to the full content width, which grows with the point count, so revealing across the canvas
 * finishes long before the on-screen part of the line appears.
 *
 * [progress] runs from 0 to 1, revealing [viewportWidthPx] from the viewport's left edge.
 */
internal fun lineChartRevealWindow(
    viewportStartPx: Float,
    viewportWidthPx: Float,
    canvasWidthPx: Float,
    progress: Float,
): LineChartRevealWindow {
    val span = viewportWidthPx.coerceIn(0f, canvasWidthPx.coerceAtLeast(0f))
    val left = viewportStartPx.coerceIn(0f, (canvasWidthPx - span).coerceAtLeast(0f))
    val right = (left + (span * progress.coerceIn(0f, ANIMATION_TARGET))).coerceIn(left, canvasWidthPx)
    return LineChartRevealWindow(leftPx = left, rightPx = right)
}
