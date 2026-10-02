package io.github.hdcharts.line.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import io.github.hdcharts.core.internal.ANIMATION_TARGET
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.bezier.cubicControlPoints
import io.github.hdcharts.core.internal.drawing.drawSelectionLine
import io.github.hdcharts.core.internal.interaction.selectedIndexForTouchX
import io.github.hdcharts.line.LineChartStyle
import kotlinx.collections.immutable.ImmutableList

/**
 * Draws one series' line from [values], which hold heights already scaled to the canvas.
 *
 * [revealViewportStartPx] and [revealViewportWidthPx] describe the on-screen slice of this canvas,
 * which is the whole canvas unless the chart scrolls. The entry reveal sweeps that slice instead of
 * the canvas, so a chart far wider than the screen still reveals across the visible plot.
 */
internal fun DrawScope.drawChartPath(
    values: FloatArray,
    valuesCount: Int,
    style: LineChartStyle,
    lineAnimationProgress: Float,
    markerRevealProgress: Float,
    bezierTension: Float,
    lineColor: Color,
    timelineWindowPoints: Int? = null,
    horizontalOffsetPx: Float = 0f,
    stepXOverride: Float? = null,
    verticalInset: Float,
    revealViewportStartPx: Float,
    revealViewportWidthPx: Float,
) {
    if (valuesCount <= 1) return

    val canvasWidth = size.width
    val canvasHeight = size.height
    val valuesLastIndex = valuesCount - 1
    val stepX =
        when {
            stepXOverride != null && stepXOverride > 0f -> {
                stepXOverride
            }
            timelineWindowPoints != null && timelineWindowPoints > 1 -> {
                canvasWidth / (timelineWindowPoints - 1)
            }
            valuesLastIndex > 0 -> {
                canvasWidth / valuesLastIndex
            }
            else -> return
        }

    val path =
        Path().apply {
            val initX = horizontalOffsetPx
            val initY =
                mapScaledValueToCanvasY(
                    scaledValue = values[0],
                    canvasHeight = canvasHeight,
                    verticalInset = verticalInset,
                )
            moveTo(initX, initY)

            if (!style.line.bezier) {
                for (i in 1 until valuesCount) {
                    val x = horizontalOffsetPx + (i * stepX)
                    val y =
                        mapScaledValueToCanvasY(
                            scaledValue = values[i],
                            canvasHeight = canvasHeight,
                            verticalInset = verticalInset,
                        )
                    lineTo(x, y)
                }
            } else {
                // Control points are read one segment at a time so a frame does not allocate an
                // offset per point. Each value maps to its canvas height once, because a point is
                // shared by the segments on either side of it.
                val canvasYs =
                    FloatArray(valuesCount) { index ->
                        mapScaledValueToCanvasY(
                            scaledValue = values[index],
                            canvasHeight = canvasHeight,
                            verticalInset = verticalInset,
                        )
                    }
                for (segmentStart in 0 until valuesLastIndex) {
                    val p1x = horizontalOffsetPx + (segmentStart * stepX)
                    val p1y = canvasYs[segmentStart]
                    val p2x = p1x + stepX
                    val p2y = canvasYs[segmentStart + 1]
                    val p0x = if (segmentStart > 0) p1x - stepX else p1x
                    val p0y = if (segmentStart > 0) canvasYs[segmentStart - 1] else p1y
                    val p3x = if (segmentStart + 2 < valuesCount) p2x + stepX else p2x
                    val p3y = if (segmentStart + 2 < valuesCount) canvasYs[segmentStart + 2] else p2y
                    val controls =
                        cubicControlPoints(
                            p0 = Offset(p0x, p0y),
                            p1 = Offset(p1x, p1y),
                            p2 = Offset(p2x, p2y),
                            p3 = Offset(p3x, p3y),
                            tension = bezierTension,
                            minY = verticalInset,
                            maxY = canvasHeight - verticalInset,
                        )
                    cubicTo(
                        controls.first.x,
                        controls.first.y,
                        controls.second.x,
                        controls.second.y,
                        p2x,
                        p2y,
                    )
                }
            }
        }

    val lineStroke =
        Stroke(
            width =
                style.line.strokeWidth
                    .toPx()
                    .coerceAtLeast(0.5f),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )

    val drawPathContent = {
        if (lineAnimationProgress >= ANIMATION_TARGET) {
            drawPath(
                path = path,
                color = lineColor,
                style = lineStroke,
            )
        } else {
            val reveal =
                lineChartRevealWindow(
                    viewportStartPx = revealViewportStartPx,
                    viewportWidthPx = revealViewportWidthPx,
                    canvasWidthPx = canvasWidth,
                    progress = lineAnimationProgress,
                )
            // Reveal from left to right to keep perceived speed steady across steep curves.
            clipRect(
                left = reveal.leftPx,
                top = 0f,
                right = reveal.rightPx + style.line.strokeWidth.toPx(),
                bottom = canvasHeight,
            ) {
                drawPath(
                    path = path,
                    color = lineColor,
                    style = lineStroke,
                )
            }
        }

        tryDrawPathPoints(
            values = values,
            valuesCount = valuesCount,
            style = style,
            markerRevealProgress = markerRevealProgress,
            stepX = stepX,
            horizontalOffsetPx = horizontalOffsetPx,
            verticalInset = verticalInset,
        )
    }

    if (timelineWindowPoints != null) {
        clipRect(left = 0f, top = 0f, right = canvasWidth, bottom = canvasHeight) {
            drawPathContent()
        }
    } else {
        drawPathContent()
    }
}

private fun DrawScope.tryDrawPathPoints(
    values: FloatArray,
    valuesCount: Int,
    style: LineChartStyle,
    markerRevealProgress: Float,
    stepX: Float,
    horizontalOffsetPx: Float,
    verticalInset: Float,
) {
    if (!style.points.visible || valuesCount <= 1 || size.width <= 0f || markerRevealProgress <= 0f) return

    val progress = markerRevealProgress.coerceIn(0f, 1f)
    val animatedColor = style.points.color.copy(alpha = style.points.color.alpha * progress)
    val animatedRadius =
        style.points.size.toPx() * (MARKER_REVEAL_START_SCALE + (1f - MARKER_REVEAL_START_SCALE) * progress)

    for (i in 0 until valuesCount) {
        val x = horizontalOffsetPx + (i * stepX)
        val y =
            mapScaledValueToCanvasY(
                scaledValue = values[i],
                canvasHeight = size.height,
                verticalInset = verticalInset,
            )
        drawCircle(
            color = animatedColor,
            radius = animatedRadius,
            center = Offset(x, y),
        )
    }
}

/**
 * Draws the line of every series, scaled into [valuesBuffer] one series at a time.
 *
 * A live shift slides the previous window sideways; every other update blends the two value sets.
 * Both write into the same reused buffer and share every other argument, so one pass covers them.
 */
internal fun DrawScope.drawLineChartSeries(
    transition: LineChartTransitionState,
    seriesCount: Int,
    pointsCount: Int,
    valuesBuffer: FloatArray,
    style: LineChartStyle,
    colors: ImmutableList<Color>,
    bezierTension: Float,
    lineAnimationProgress: Float,
    markerRevealProgress: Float,
    isDenseMode: Boolean,
    denseStepX: Float,
    verticalInset: Float,
    revealViewportStartPx: Float,
    revealViewportWidthPx: Float,
) {
    val activeShift = transition.activeShift
    val shiftProgress = transition.shiftProgress
    val morphProgress = transition.morph.progress.value
    val shiftPx =
        if (activeShift != null) {
            -(shiftProgress * timelineStep(size.width, pointsCount))
        } else {
            0f
        }

    for (index in 0 until seriesCount) {
        val valuesCount =
            if (activeShift != null) {
                val timelineValues = activeShift.drawValues.getOrNull(index).orEmpty()
                if (timelineValues.isEmpty()) continue
                copyInto(
                    source = timelineValues,
                    into = valuesBuffer,
                    scaleBy = size.height,
                )
            } else {
                blendInto(
                    into = valuesBuffer,
                    from =
                        transition.morph.from
                            .getOrNull(index)
                            .orEmpty(),
                    to =
                        transition.morph.to
                            .getOrNull(index)
                            .orEmpty(),
                    progress = morphProgress,
                    scaleBy = size.height,
                )
            }
        drawChartPath(
            values = valuesBuffer,
            valuesCount = valuesCount,
            style = style,
            lineAnimationProgress = lineAnimationProgress,
            markerRevealProgress = markerRevealProgress,
            bezierTension = bezierTension,
            lineColor = colors[index],
            timelineWindowPoints = if (activeShift != null) pointsCount else null,
            horizontalOffsetPx = shiftPx,
            stepXOverride = if (activeShift == null && isDenseMode) denseStepX else null,
            verticalInset = verticalInset,
            revealViewportStartPx = revealViewportStartPx,
            revealViewportWidthPx = revealViewportWidthPx,
        )
    }
}

/**
 * Draws the selection line and the marker on each series at [selectedIndex].
 *
 * The markers sit at the value the morph is drawing, so they are left out while a live shift slides
 * the window: those values describe the window that is leaving, not the one on screen.
 */
internal fun DrawScope.drawLineChartSelection(
    transition: LineChartTransitionState,
    selectedIndex: Int,
    seriesCount: Int,
    pointsCount: Int,
    isDragging: Boolean,
    stepX: Float,
    style: LineChartStyle,
    verticalInset: Float,
) {
    if (transition.activeShift != null) return
    if (selectedIndex == NO_SELECTION || pointsCount <= 1 || stepX <= 0f) return

    val safeSelectedIndex = selectedIndex.coerceIn(0, pointsCount - 1)
    val selectedX = safeSelectedIndex * stepX

    if (style.selection.visible) {
        drawSelectionLine(
            x = selectedX,
            color = style.selection.color,
            strokeWidth = style.selection.width.toPx(),
        )
    }

    if (isDragging || (!style.selection.visible && !style.points.visible)) return

    val markerRadius =
        style.selection.pointSize
            .toPx()
            .coerceAtLeast(1f)
    val morphProgress = transition.morph.progress.value
    for (seriesIndex in 0 until seriesCount) {
        val normalized =
            transition.morph.drawnValueAt(
                seriesIndex = seriesIndex,
                pointIndex = safeSelectedIndex,
                progress = morphProgress,
            )
        drawCircle(
            color = style.selection.markerColor,
            radius = markerRadius,
            center =
                Offset(
                    x = selectedX,
                    y =
                        mapScaledValueToCanvasY(
                            scaledValue = normalized * size.height,
                            canvasHeight = size.height,
                            verticalInset = verticalInset,
                        ),
                ),
        )
    }
}

internal fun DrawScope.drawDragMarker(
    touchX: Float,
    values: FloatArray,
    valuesCount: Int,
    style: LineChartStyle,
    bezierTension: Float,
    verticalInset: Float,
) {
    if ((!style.selection.visible && !style.points.visible) || valuesCount <= 1 || size.width <= 0f) return

    val selectedIndex =
        selectedIndexForTouchX(
            touchX = touchX,
            widthPx = size.width,
            pointsCount = valuesCount,
        )
    if (selectedIndex == NO_SELECTION) return

    val maxDragY = (size.height - verticalInset).coerceAtLeast(verticalInset)

    if (style.points.visible) {
        val stepX = size.width / (valuesCount - 1)
        val selectedX = selectedIndex * stepX
        val selectedY =
            mapScaledValueToCanvasY(
                scaledValue = values[selectedIndex],
                canvasHeight = size.height,
                verticalInset = verticalInset,
            )
        drawCircle(
            center = Offset(selectedX, selectedY),
            radius = style.selection.pointSize.toPx(),
            color = style.selection.markerColor,
        )
    }

    if (style.selection.visible) {
        val nearestPoint =
            findNearestPoint(
                touchX = touchX,
                scaledValues = values,
                scaledValuesCount = valuesCount,
                size = size,
                bezier = style.line.bezier,
                verticalInset = verticalInset,
                bezierTension = bezierTension,
            )

        val draggingCircleOffset =
            Offset(
                nearestPoint.x.coerceIn(0f, size.width),
                nearestPoint.y.coerceIn(verticalInset, maxDragY),
            )

        drawCircle(
            center = draggingCircleOffset,
            radius = style.selection.markerSize.toPx(),
            color = style.selection.markerColor,
        )
    }
}
