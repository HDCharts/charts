package io.github.hdcharts.charts.internal.barstackedchart

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.common.interaction.buildHorizontalDragGestureModifier
import io.github.hdcharts.charts.internal.common.interaction.buildPinchZoomModifier
import io.github.hdcharts.charts.internal.common.interaction.buildTapGestureModifier
import kotlin.math.roundToInt
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForBarFit as selectedIndexForBarFitCore
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForContentX as selectedIndexForContentXCore

internal fun selectedIndexForFit(
    positionX: Float,
    dataSize: Int,
    canvasSize: IntSize,
    spacingPx: Float,
): Int =
    selectedIndexForBarFitCore(
        positionX = positionX,
        dataSize = dataSize,
        canvasWidthPx = canvasSize.width.toFloat(),
        spacingPx = spacingPx,
        invalidIndex = NO_SELECTION,
    )

internal fun selectedIndexForContentX(
    contentX: Float,
    dataSize: Int,
    unitWidthPx: Float,
): Int =
    selectedIndexForContentXCore(
        contentX = contentX,
        dataSize = dataSize,
        unitWidthPx = unitWidthPx,
        invalidIndex = NO_SELECTION,
    )

internal fun buildFitTapModifier(
    interactionEnabled: Boolean,
    isScrollable: Boolean,
    dataSize: Int,
    spacingPx: Float,
    viewportWidthPx: Float,
    chartHeightPx: Float,
    onTapIndex: (Int) -> Unit,
): Modifier =
    buildTapGestureModifier(
        enabled = interactionEnabled && !isScrollable,
        dataSize,
        spacingPx,
        viewportWidthPx,
        chartHeightPx,
        onTap = { offset ->
            val index =
                selectedIndexForFit(
                    positionX = offset.x,
                    dataSize = dataSize,
                    canvasSize =
                        IntSize(
                            width = viewportWidthPx.roundToInt(),
                            height = chartHeightPx.roundToInt(),
                        ),
                    spacingPx = spacingPx,
                )
            onTapIndex(index)
        },
    )

internal fun buildFitDragModifier(
    interactionEnabled: Boolean,
    dragSelectionEnabled: Boolean,
    isScrollable: Boolean,
    dataSize: Int,
    spacingPx: Float,
    viewportWidthPx: Float,
    chartHeightPx: Float,
    onDragIndex: (Int) -> Unit,
    onDragFinished: () -> Unit,
): Modifier =
    buildHorizontalDragGestureModifier(
        enabled = interactionEnabled && dragSelectionEnabled && !isScrollable,
        dataSize,
        spacingPx,
        viewportWidthPx,
        chartHeightPx,
        onDragStart = { offset ->
            val index =
                selectedIndexForFit(
                    positionX = offset.x,
                    dataSize = dataSize,
                    canvasSize =
                        IntSize(
                            width = viewportWidthPx.roundToInt(),
                            height = chartHeightPx.roundToInt(),
                        ),
                    spacingPx = spacingPx,
                )
            onDragIndex(index)
        },
        onHorizontalDrag = { position ->
            val index =
                selectedIndexForFit(
                    positionX = position.x,
                    dataSize = dataSize,
                    canvasSize =
                        IntSize(
                            width = viewportWidthPx.roundToInt(),
                            height = chartHeightPx.roundToInt(),
                        ),
                    spacingPx = spacingPx,
                )
            onDragIndex(index)
        },
        onDragEnd = { onDragFinished() },
        onDragCancel = { onDragFinished() },
    )

internal fun buildScrollTapModifier(
    interactionEnabled: Boolean,
    isScrollable: Boolean,
    dataSize: Int,
    unitWidthPx: Float,
    scrollState: ScrollState,
    onTapIndex: (Int) -> Unit,
    onDoubleTap: () -> Unit,
): Modifier =
    buildTapGestureModifier(
        enabled = interactionEnabled && isScrollable,
        dataSize,
        unitWidthPx,
        onTap = { offset ->
            val contentX = offset.x + scrollState.value.toFloat()
            val index =
                selectedIndexForContentX(
                    contentX = contentX,
                    dataSize = dataSize,
                    unitWidthPx = unitWidthPx,
                )
            onTapIndex(index)
        },
        onDoubleTap = { onDoubleTap() },
    )

internal fun buildPinchModifier(
    isScrollable: Boolean,
    dataSize: Int,
    zoomMin: Float,
    zoomMax: Float,
    getZoomScale: () -> Float,
    setZoomScale: (Float) -> Unit,
): Modifier =
    buildPinchZoomModifier(
        enabled = isScrollable,
        zoomMin = zoomMin,
        zoomMax = zoomMax,
        getZoomScale = getZoomScale,
        setZoomScale = setZoomScale,
        dataSize,
        zoomMin,
        zoomMax,
    )
