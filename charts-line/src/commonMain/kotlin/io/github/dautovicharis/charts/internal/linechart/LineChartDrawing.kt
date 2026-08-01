package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import io.github.dautovicharis.charts.internal.ANIMATION_TARGET
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.bezier.cubicControlPointsForSegment
import io.github.dautovicharis.charts.style.LineChartStyle
import kotlinx.collections.immutable.ImmutableList

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
) {
    if (values.size <= 1) return

    val valuesSize = values.size
    val canvasWidth = size.width
    val canvasHeight = size.height
    val verticalInset = LINE_VERTICAL_SAFE_INSET.coerceAtMost(canvasHeight / 2f)
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

            if (!style.bezier) {
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
            width = LINE_STROKE_WIDTH,
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
            clipRect(left = 0f, top = 0f, right = revealX + LINE_STROKE_WIDTH, bottom = canvasHeight) {
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
            lineColor = lineColor,
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
    lineColor: Color,
    markerRevealProgress: Float,
    stepX: Float,
    horizontalOffsetPx: Float,
    verticalInset: Float,
) {
    if (!style.pointVisible || values.size <= 1 || size.width <= 0f || markerRevealProgress <= 0f) return

    val pointColor =
        when (style.pointColorSameAsLine) {
            true -> lineColor
            else -> style.pointColor
        }
    val progress = markerRevealProgress.coerceIn(0f, 1f)
    val animatedColor = pointColor.copy(alpha = pointColor.alpha * progress)
    val animatedRadius = style.pointSize * (MARKER_REVEAL_START_SCALE + (1f - MARKER_REVEAL_START_SCALE) * progress)

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
    lineColor: Color,
    bezierTension: Float,
) {
    if ((!style.dragPointVisible && !style.pointVisible) || values.size <= 1 || size.width <= 0f) return

    val selectedIndex =
        selectedIndexForTouch(
            touchX = touchX,
            width = size.width,
            pointsCount = values.size,
        )
    if (selectedIndex == NO_SELECTION) return

    val dragPointColor =
        when (style.dragPointColorSameAsLine) {
            true -> lineColor
            else -> style.dragPointColor
        }
    val verticalInset = LINE_VERTICAL_SAFE_INSET.coerceAtMost(size.height / 2f)
    val maxDragY = (size.height - verticalInset).coerceAtLeast(verticalInset)

    if (style.pointVisible) {
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
            radius = style.dragActivePointSize,
            color = dragPointColor,
        )
    }

    if (style.dragPointVisible) {
        val nearestPoint =
            findNearestPoint(
                touchX = touchX,
                scaledValues = values,
                size = size,
                bezier = style.bezier,
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
            radius = style.dragPointSize,
            color = dragPointColor,
        )
    }
}

internal fun resolveSelectionLineColor(
    style: LineChartStyle,
    colors: ImmutableList<Color>,
): Color =
    when (style.dragPointColorSameAsLine) {
        true -> colors.firstOrNull() ?: style.dragPointColor
        else -> style.dragPointColor
    }
