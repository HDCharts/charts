package io.github.hdcharts.core.internal.interaction

import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import kotlin.math.floor
import kotlin.math.max

/**
 * Index of the point nearest [touchX] when [pointsCount] points span the full [widthPx], the first
 * on the left edge and the last on the right edge, or [NO_SELECTION] without enough geometry.
 */
@InternalChartsApi
fun selectedIndexForTouchX(
    touchX: Float,
    widthPx: Float,
    pointsCount: Int,
): Int {
    if (pointsCount <= 1 || widthPx <= 0f) return NO_SELECTION
    return nearestIndex(position = touchX / widthPx * (pointsCount - 1), count = pointsCount)
}

/**
 * Index of the point nearest [contentX] when point `i` sits at `i * stepPx`, as in scrolling line and
 * area charts, or [NO_SELECTION] without enough geometry.
 */
@InternalChartsApi
fun nearestPointIndexForContentX(
    contentX: Float,
    pointsCount: Int,
    stepPx: Float,
): Int {
    if (pointsCount <= 0 || stepPx <= 0f) return NO_SELECTION
    return nearestIndex(position = contentX / stepPx, count = pointsCount)
}

/**
 * Index of the bar slot `[i * unitWidthPx, (i + 1) * unitWidthPx)` that holds [contentX], where each
 * slot is a bar plus the gap after it, or [NO_SELECTION] without enough geometry.
 */
@InternalChartsApi
fun selectedIndexForContentX(
    contentX: Float,
    dataSize: Int,
    unitWidthPx: Float,
): Int {
    if (dataSize <= 0 || unitWidthPx <= 0f) return NO_SELECTION
    return (contentX / unitWidthPx)
        .toInt()
        .coerceIn(0, dataSize - 1)
}

/** Bar index at [positionX] when all bars fit in [canvasWidthPx], or [NO_SELECTION] without enough geometry. */
@InternalChartsApi
fun selectedIndexForBarFit(
    positionX: Float,
    dataSize: Int,
    canvasWidthPx: Float,
    spacingPx: Float,
): Int {
    if (dataSize <= 0 || canvasWidthPx <= 0f) return NO_SELECTION

    val totalSpacing = spacingPx * (dataSize - 1)
    val availableWidth = max(1f, canvasWidthPx - totalSpacing)
    val barWidth = availableWidth / dataSize
    val unitWidth = barWidth + spacingPx
    val index = (positionX / unitWidth).toInt()
    return index.coerceIn(0, dataSize - 1)
}

// Rounds a fractional point position to the nearest point; a touch exactly on a point, give or take
// float rounding, selects that point.
private fun nearestIndex(
    position: Float,
    count: Int,
): Int = floor(position + 0.5f).toInt().coerceIn(0, count - 1)
