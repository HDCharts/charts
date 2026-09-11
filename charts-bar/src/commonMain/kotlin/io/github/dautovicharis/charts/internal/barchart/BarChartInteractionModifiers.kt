package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import io.github.dautovicharis.charts.internal.common.interaction.buildHorizontalDragGestureModifier
import io.github.dautovicharis.charts.internal.common.interaction.buildPinchZoomModifier
import io.github.dautovicharis.charts.internal.common.interaction.buildTapGestureModifier

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
        interactionEnabled && !isScrollable,
        dataSize,
        spacingPx,
        viewportWidthPx,
        chartHeightPx,
        onTap = { offset ->
            val index =
                getSelectedIndexForContentX(
                    contentX = offset.x,
                    dataSize = dataSize,
                    unitWidthPx = (viewportWidthPx + spacingPx) / dataSize.coerceAtLeast(1),
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
        interactionEnabled && dragSelectionEnabled && !isScrollable,
        dataSize,
        spacingPx,
        viewportWidthPx,
        chartHeightPx,
        onDragStart = { offset ->
            val index =
                getSelectedIndexForContentX(
                    contentX = offset.x,
                    dataSize = dataSize,
                    unitWidthPx = (viewportWidthPx + spacingPx) / dataSize.coerceAtLeast(1),
                )
            onDragIndex(index)
        },
        onHorizontalDrag = { position ->
            val index =
                getSelectedIndexForContentX(
                    contentX = position.x,
                    dataSize = dataSize,
                    unitWidthPx = (viewportWidthPx + spacingPx) / dataSize.coerceAtLeast(1),
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
        interactionEnabled && isScrollable,
        dataSize,
        unitWidthPx,
        onTap = { offset ->
            val contentX = offset.x + scrollState.value.toFloat()
            val index =
                getSelectedIndexForContentX(
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
        isScrollable,
        zoomMin,
        zoomMax,
        getZoomScale,
        setZoomScale,
        dataSize,
        zoomMin,
        zoomMax,
    )
