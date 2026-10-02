package io.github.hdcharts.core.internal.bezier

import androidx.compose.ui.geometry.Offset
import io.github.hdcharts.core.internal.InternalChartsApi

@InternalChartsApi
data class CubicControlPoints(
    val first: Offset,
    val second: Offset,
)

@InternalChartsApi
const val DEFAULT_BEZIER_TENSION = 0.95f

/**
 * Control points for the cubic segment from [p1] to [p2], smoothed against the neighbouring points
 * [p0] and [p3]. A segment at either end of the series repeats its own endpoint for the missing
 * neighbour, so the line leaves and arrives flat.
 *
 * Takes the four points as arguments rather than a list so a chart drawing one segment at a time
 * does not have to allocate a list of offsets for the whole series on every frame.
 */
@InternalChartsApi
fun cubicControlPoints(
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    tension: Float = DEFAULT_BEZIER_TENSION,
    minY: Float = Float.NEGATIVE_INFINITY,
    maxY: Float = Float.POSITIVE_INFINITY,
): CubicControlPoints {
    val factor = tension / 6f
    val lowerYBound = minY.coerceAtMost(maxY)
    val upperYBound = maxY.coerceAtLeast(minY)
    val control1 =
        Offset(
            x = p1.x + (p2.x - p0.x) * factor,
            y = (p1.y + (p2.y - p0.y) * factor).coerceIn(lowerYBound, upperYBound),
        )
    val control2 =
        Offset(
            x = p2.x - (p3.x - p1.x) * factor,
            y = (p2.y - (p3.y - p1.y) * factor).coerceIn(lowerYBound, upperYBound),
        )

    return CubicControlPoints(first = control1, second = control2)
}

@InternalChartsApi
fun cubicControlPointsForSegment(
    points: List<Offset>,
    segmentStartIndex: Int,
    tension: Float = DEFAULT_BEZIER_TENSION,
    minY: Float = Float.NEGATIVE_INFINITY,
    maxY: Float = Float.POSITIVE_INFINITY,
): CubicControlPoints {
    val p1 = points[segmentStartIndex]
    val p2 = points[segmentStartIndex + 1]
    val p0 =
        when {
            segmentStartIndex > 0 -> points[segmentStartIndex - 1]
            else -> p1
        }
    val p3 =
        when {
            segmentStartIndex + 2 < points.size -> points[segmentStartIndex + 2]
            else -> p2
        }

    return cubicControlPoints(
        p0 = p0,
        p1 = p1,
        p2 = p2,
        p3 = p3,
        tension = tension,
        minY = minY,
        maxY = maxY,
    )
}
