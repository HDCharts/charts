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
import io.github.hdcharts.core.internal.axis.visibleIndexRange
import io.github.hdcharts.core.internal.bezier.CUBIC_CONTROL_POINT_COUNT
import io.github.hdcharts.core.internal.bezier.cubicControlPointsInto
import io.github.hdcharts.core.internal.drawing.drawSelectionLine
import io.github.hdcharts.core.internal.interaction.selectedIndexForTouchX
import io.github.hdcharts.line.LineChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.ceil
import kotlin.math.max

/**
 * Draw resources one line chart frame reuses across its series.
 *
 * A chart of many points redraws every frame of the reveal, a morph, and a live shift, so the path,
 * the canvas heights, and the control points all live here instead of being built per series.
 */
internal class LineChartDrawScratch(
    valuesCapacity: Int,
) {
    /** Path of the series being drawn, rewound before each one. */
    val path = Path()

    /** Canvas heights of the points being drawn, indexed by point index. */
    val heights = FloatArray(valuesCapacity)

    /** Control points of the bezier segment being drawn. */
    val controlPoints = FloatArray(CUBIC_CONTROL_POINT_COUNT)
}

/**
 * Points an expanded chart draws: those on screen, plus the overscan that keeps the segments and
 * markers crossing each viewport edge whole.
 *
 * The overscan covers the widest thing a point draws, so a half-visible marker is not dropped and
 * the segment that enters the viewport is not cut. Returns the whole series for the modes that are
 * always fully on screen, and for a viewport that covers the content.
 */
internal fun lineChartDrawRange(
    valuesCount: Int,
    stepX: Float,
    viewportStartPx: Float,
    viewportWidthPx: Float,
    overscanPx: Float,
): IntRange {
    if (valuesCount <= 1 || stepX <= 0f) return 0..max(valuesCount - 1, 0)
    val visible =
        visibleIndexRange(
            dataSize = valuesCount,
            viewportWidthPx = viewportWidthPx,
            scrollOffsetPx = viewportStartPx,
            unitWidthPx = stepX,
        )
    if (visible.isEmpty()) return 0 until valuesCount
    val overscanPoints = 1 + ceil(overscanPx / stepX).toInt()
    return (visible.first - overscanPoints).coerceAtLeast(
        0,
    )..(visible.last + overscanPoints).coerceAtMost(valuesCount - 1)
}

/** Radius of the widest thing a point draws: its stroke, its markers, and the selection markers. */
private fun DrawScope.maxLineDrawRadiusPx(style: LineChartStyle): Float =
    max(
        style.line.strokeWidth.toPx() / 2f,
        max(
            if (style.points.visible) style.points.size.toPx() else 0f,
            max(style.selection.markerSize.toPx(), style.selection.pointSize.toPx()),
        ),
    )

/**
 * Draws one series' line from [values], which hold heights already scaled to the canvas.
 *
 * Only [drawRange] is drawn. [scratch] holds the path, the canvas heights, and the control points,
 * so a frame allocates nothing per series or per segment.
 *
 * [revealViewportStartPx] and [revealViewportWidthPx] describe the on-screen slice of this canvas,
 * which is the whole canvas unless the chart scrolls. The entry reveal sweeps that slice instead of
 * the canvas, so a chart far wider than the screen still reveals across the visible plot.
 */
internal fun DrawScope.drawChartPath(
    values: FloatArray,
    valuesCount: Int,
    drawRange: IntRange,
    style: LineChartStyle,
    lineStroke: Stroke,
    lineColor: Color,
    bezierTension: Float,
    scratch: LineChartDrawScratch,
    lineAnimationProgress: Float,
    markerRevealProgress: Float,
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
    val firstIndex = drawRange.first.coerceIn(0, valuesLastIndex)
    val lastIndex = drawRange.last.coerceIn(firstIndex, valuesLastIndex)
    if (lastIndex < firstIndex) return

    // Each value maps to its canvas height once, because a point is shared by the segments on
    // either side of it. The one point of overscan on each side is what lets the segments at the
    // edges of [drawRange] read their true neighbours.
    val heights = scratch.heights
    val heightsStart = (firstIndex - 1).coerceAtLeast(0)
    val heightsEnd = (lastIndex + 1).coerceAtMost(valuesLastIndex)
    for (index in heightsStart..heightsEnd) {
        heights[index] =
            mapScaledValueToCanvasY(
                scaledValue = values[index],
                canvasHeight = canvasHeight,
                verticalInset = verticalInset,
            )
    }

    val path = scratch.path.apply { rewind() }
    val controlPoints = scratch.controlPoints
    path.moveTo(
        horizontalOffsetPx + (firstIndex * stepX),
        heights[firstIndex],
    )

    if (!style.line.bezier) {
        for (i in firstIndex + 1..lastIndex) {
            path.lineTo(
                horizontalOffsetPx + (i * stepX),
                heights[i],
            )
        }
    } else {
        for (segmentStart in firstIndex until lastIndex) {
            val p1x = horizontalOffsetPx + (segmentStart * stepX)
            val p1y = heights[segmentStart]
            val p2x = p1x + stepX
            val p2y = heights[segmentStart + 1]
            // The neighbours are the real points of the series, not the edges of the drawn range,
            // so a culled range keeps the curve's shape where it meets the viewport edge.
            val p0x = if (segmentStart > 0) p1x - stepX else p1x
            val p0y = if (segmentStart > 0) heights[segmentStart - 1] else p1y
            val hasP3 = segmentStart + 2 < valuesCount
            val p3x = if (hasP3) p2x + stepX else p2x
            val p3y = if (hasP3) heights[segmentStart + 2] else p2y
            cubicControlPointsInto(
                out = controlPoints,
                p0x = p0x,
                p0y = p0y,
                p1x = p1x,
                p1y = p1y,
                p2x = p2x,
                p2y = p2y,
                p3x = p3x,
                p3y = p3y,
                tension = bezierTension,
                minY = verticalInset,
                maxY = canvasHeight - verticalInset,
            )
            path.cubicTo(
                controlPoints[0],
                controlPoints[1],
                controlPoints[2],
                controlPoints[3],
                p2x,
                p2y,
            )
        }
    }

    if (timelineWindowPoints != null) {
        clipRect(left = 0f, top = 0f, right = canvasWidth, bottom = canvasHeight) {
            drawChartLineAndMarkers(
                path = path,
                heights = heights,
                drawRange = firstIndex..lastIndex,
                style = style,
                lineStroke = lineStroke,
                lineColor = lineColor,
                lineAnimationProgress = lineAnimationProgress,
                markerRevealProgress = markerRevealProgress,
                stepX = stepX,
                horizontalOffsetPx = horizontalOffsetPx,
                revealViewportStartPx = revealViewportStartPx,
                revealViewportWidthPx = revealViewportWidthPx,
            )
        }
    } else {
        drawChartLineAndMarkers(
            path = path,
            heights = heights,
            drawRange = firstIndex..lastIndex,
            style = style,
            lineStroke = lineStroke,
            lineColor = lineColor,
            lineAnimationProgress = lineAnimationProgress,
            markerRevealProgress = markerRevealProgress,
            stepX = stepX,
            horizontalOffsetPx = horizontalOffsetPx,
            revealViewportStartPx = revealViewportStartPx,
            revealViewportWidthPx = revealViewportWidthPx,
        )
    }
}

/** Draws the line of one series and the markers of the points in [drawRange]. */
private fun DrawScope.drawChartLineAndMarkers(
    path: Path,
    heights: FloatArray,
    drawRange: IntRange,
    style: LineChartStyle,
    lineStroke: Stroke,
    lineColor: Color,
    lineAnimationProgress: Float,
    markerRevealProgress: Float,
    stepX: Float,
    horizontalOffsetPx: Float,
    revealViewportStartPx: Float,
    revealViewportWidthPx: Float,
) {
    val canvasHeight = size.height
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
                canvasWidthPx = size.width,
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
        heights = heights,
        drawRange = drawRange,
        style = style,
        markerRevealProgress = markerRevealProgress,
        stepX = stepX,
        horizontalOffsetPx = horizontalOffsetPx,
    )
}

private fun DrawScope.tryDrawPathPoints(
    heights: FloatArray,
    drawRange: IntRange,
    style: LineChartStyle,
    markerRevealProgress: Float,
    stepX: Float,
    horizontalOffsetPx: Float,
) {
    if (!style.points.visible || drawRange.isEmpty() || size.width <= 0f || markerRevealProgress <= 0f) return

    val progress = markerRevealProgress.coerceIn(0f, 1f)
    val animatedColor = style.points.color.copy(alpha = style.points.color.alpha * progress)
    val animatedRadius =
        style.points.size.toPx() * (MARKER_REVEAL_START_SCALE + (1f - MARKER_REVEAL_START_SCALE) * progress)

    for (i in drawRange) {
        drawCircle(
            color = animatedColor,
            radius = animatedRadius,
            center = Offset(horizontalOffsetPx + (i * stepX), heights[i]),
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
    scratch: LineChartDrawScratch,
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
    // Only an expanded chart scrolls, so only it has points off screen to cull. A live shift slides
    // a window that is on screen for the whole frame, and a fitted chart is the plot itself.
    val isExpanded = activeShift == null && isDenseMode
    val lineStroke =
        Stroke(
            width =
                style.line.strokeWidth
                    .toPx()
                    .coerceAtLeast(0.5f),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
    val overscanPx = maxLineDrawRadiusPx(style)

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
        val drawRange =
            if (isExpanded) {
                lineChartDrawRange(
                    valuesCount = valuesCount,
                    stepX = denseStepX,
                    viewportStartPx = revealViewportStartPx,
                    viewportWidthPx = revealViewportWidthPx,
                    overscanPx = overscanPx,
                )
            } else {
                0 until valuesCount
            }
        drawChartPath(
            values = valuesBuffer,
            valuesCount = valuesCount,
            drawRange = drawRange,
            style = style,
            lineStroke = lineStroke,
            lineColor = colors[index],
            bezierTension = bezierTension,
            scratch = scratch,
            lineAnimationProgress = lineAnimationProgress,
            markerRevealProgress = markerRevealProgress,
            timelineWindowPoints = if (activeShift != null) pointsCount else null,
            horizontalOffsetPx = shiftPx,
            stepXOverride = if (isExpanded) denseStepX else null,
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
