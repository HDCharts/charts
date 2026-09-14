package io.github.hdcharts.charts.internal.linechart

import io.github.hdcharts.charts.internal.NO_SELECTION
import kotlin.math.max
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForContentX as selectedIndexForContentXCore
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForTouchX as selectedIndexForTouchXCore

internal fun selectedIndexForTouch(
    touchX: Float,
    width: Float,
    pointsCount: Int,
): Int =
    selectedIndexForTouchXCore(
        touchX = touchX,
        widthPx = width,
        pointsCount = pointsCount,
        invalidIndex = NO_SELECTION,
    )

internal fun selectedIndexForContentX(
    contentX: Float,
    pointsCount: Int,
    stepX: Float,
): Int =
    selectedIndexForContentXCore(
        contentX = contentX,
        dataSize = pointsCount,
        unitWidthPx = stepX,
        invalidIndex = NO_SELECTION,
    )

internal fun denseStepForViewport(
    viewportWidth: Float,
    pointsCount: Int,
    zoomScale: Float,
): Float {
    if (viewportWidth <= 0f || pointsCount <= 1) return 0f
    val fitStep = viewportWidth / (pointsCount - 1)
    return max(fitStep, LINE_DENSE_MIN_STEP_PX) * zoomScale.coerceAtLeast(1f)
}
