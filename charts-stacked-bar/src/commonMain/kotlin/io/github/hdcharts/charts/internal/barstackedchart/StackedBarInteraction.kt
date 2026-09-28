package io.github.hdcharts.charts.internal.barstackedchart

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import io.github.hdcharts.charts.internal.common.interaction.buildHorizontalDragGestureModifier
import io.github.hdcharts.charts.internal.common.interaction.buildPinchZoomModifier
import io.github.hdcharts.charts.internal.common.interaction.buildTapGestureModifier
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForBarFit
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForContentX

internal fun buildFitTapModifier(
    interactionEnabled: Boolean,
    isScrollable: Boolean,
    dataSize: Int,
    spacingPx: Float,
    viewportWidthPx: Float,
    onTapIndex: (Int) -> Unit,
): Modifier =
    buildTapGestureModifier(
        enabled = interactionEnabled && !isScrollable,
        dataSize,
        spacingPx,
        viewportWidthPx,
        onTap = { offset ->
            val index =
                selectedIndexForBarFit(
                    positionX = offset.x,
                    dataSize = dataSize,
                    canvasWidthPx = viewportWidthPx,
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
    onDragIndex: (Int) -> Unit,
    onDragFinished: () -> Unit,
): Modifier =
    buildHorizontalDragGestureModifier(
        enabled = interactionEnabled && dragSelectionEnabled && !isScrollable,
        dataSize,
        spacingPx,
        viewportWidthPx,
        onDragStart = { offset ->
            val index =
                selectedIndexForBarFit(
                    positionX = offset.x,
                    dataSize = dataSize,
                    canvasWidthPx = viewportWidthPx,
                    spacingPx = spacingPx,
                )
            onDragIndex(index)
        },
        onHorizontalDrag = { position ->
            val index =
                selectedIndexForBarFit(
                    positionX = position.x,
                    dataSize = dataSize,
                    canvasWidthPx = viewportWidthPx,
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
