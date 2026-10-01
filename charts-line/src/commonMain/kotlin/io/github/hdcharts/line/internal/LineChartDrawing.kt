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
import io.github.hdcharts.core.internal.bezier.cubicControlPointsForSegment
import io.github.hdcharts.core.internal.interaction.selectedIndexForTouchX
import io.github.hdcharts.line.LineChartStyle

internal fun DrawScope.drawChartPath(
    values: List<Float>,
    style: LineChartStyle,
    lineAnimationProgress: Float,
    markerRevealProgress: Float,
    bezierTension: Float,
    lineColor: Color,
    timelineWindowPoints: Int? = null,
    horizontalOffsetPx: Float = 0f,
    stepXOverride: Float? = null,
    verticalInset: Float,
) {
    if (values.size <= 1) return

    val valuesSize = values.size
    val canvasWidth = size.width
    val canvasHeight = size.height
    val valuesLastIndex = valuesSize - 1
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
                    scaledValue = values.first(),
                    canvasHeight = canvasHeight,
                    verticalInset = verticalInset,
                )
            moveTo(initX, initY)

            if (!style.line.bezier) {
                for (i in 1 until valuesSize) {
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
                val points =
                    List(valuesSize) { index ->
                        Offset(
                            x = horizontalOffsetPx + (index * stepX),
                            y =
                                mapScaledValueToCanvasY(
                                    scaledValue = values[index],
                                    canvasHeight = canvasHeight,
                                    verticalInset = verticalInset,
                                ),
                        )
                    }
                for (segmentStart in 0 until points.lastIndex) {
                    val controls =
                        cubicControlPointsForSegment(
                            points = points,
                            segmentStartIndex = segmentStart,
                            tension = bezierTension,
                            minY = verticalInset,
                            maxY = canvasHeight - verticalInset,
                        )
                    val segmentEnd = points[segmentStart + 1]
                    cubicTo(
                        controls.first.x,
                        controls.first.y,
                        controls.second.x,
                        controls.second.y,
                        segmentEnd.x,
                        segmentEnd.y,
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
            val revealX = (canvasWidth * lineAnimationProgress).coerceIn(0f, canvasWidth)
            // Reveal from left to right to keep perceived speed steady across steep curves.
            clipRect(left = 0f, top = 0f, right = revealX + style.line.strokeWidth.toPx(), bottom = canvasHeight) {
                drawPath(
                    path = path,
                    color = lineColor,
                    style = lineStroke,
                )
            }
        }

        tryDrawPathPoints(
            values = values,
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
    values: List<Float>,
    style: LineChartStyle,
    markerRevealProgress: Float,
    stepX: Float,
    horizontalOffsetPx: Float,
    verticalInset: Float,
) {
    if (!style.points.visible || values.size <= 1 || size.width <= 0f || markerRevealProgress <= 0f) return

    val progress = markerRevealProgress.coerceIn(0f, 1f)
    val animatedColor = style.points.color.copy(alpha = style.points.color.alpha * progress)
    val animatedRadius =
        style.points.size.toPx() * (MARKER_REVEAL_START_SCALE + (1f - MARKER_REVEAL_START_SCALE) * progress)

    for (i in values.indices) {
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

internal fun DrawScope.drawDragMarker(
    touchX: Float,
    values: List<Float>,
    style: LineChartStyle,
    bezierTension: Float,
    verticalInset: Float,
) {
    if ((!style.selection.visible && !style.points.visible) || values.size <= 1 || size.width <= 0f) return

    val selectedIndex =
        selectedIndexForTouchX(
            touchX = touchX,
            widthPx = size.width,
            pointsCount = values.size,
        )
    if (selectedIndex == NO_SELECTION) return

    val maxDragY = (size.height - verticalInset).coerceAtLeast(verticalInset)

    if (style.points.visible) {
        val stepX = size.width / (values.size - 1)
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
