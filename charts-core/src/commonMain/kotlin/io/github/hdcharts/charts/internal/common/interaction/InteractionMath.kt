package io.github.hdcharts.charts.internal.common.interaction

import io.github.hdcharts.charts.internal.InternalChartsApi
import kotlin.math.max

@InternalChartsApi
fun selectedIndexForTouchX(
    touchX: Float,
    widthPx: Float,
    pointsCount: Int,
    invalidIndex: Int = -1,
): Int {
    if (pointsCount <= 1 || widthPx <= 0f) return invalidIndex
    return ((touchX / widthPx) * (pointsCount - 1))
        .toInt()
        .coerceIn(0, pointsCount - 1)
}

@InternalChartsApi
fun selectedIndexForContentX(
    contentX: Float,
    dataSize: Int,
    unitWidthPx: Float,
    invalidIndex: Int = -1,
): Int {
    if (dataSize <= 0 || unitWidthPx <= 0f) return invalidIndex
    return (contentX / unitWidthPx)
        .toInt()
        .coerceIn(0, dataSize - 1)
}

@InternalChartsApi
fun selectedIndexForBarFit(
    positionX: Float,
    dataSize: Int,
    canvasWidthPx: Float,
    spacingPx: Float,
    invalidIndex: Int = 0,
): Int {
    if (dataSize <= 0 || canvasWidthPx <= 0f) return invalidIndex

    val totalSpacing = spacingPx * (dataSize - 1)
    val availableWidth = max(1f, canvasWidthPx - totalSpacing)
    val barWidth = availableWidth / dataSize
    val unitWidth = barWidth + spacingPx
    val index = (positionX / unitWidth).toInt()
    return index.coerceIn(0, dataSize - 1)
}
