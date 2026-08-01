package io.github.dautovicharis.charts.internal.common.bezier

import androidx.compose.ui.geometry.Offset
import io.github.dautovicharis.charts.internal.InternalChartsApi

@InternalChartsApi
data class CubicControlPoints(
    val first: Offset,
    val second: Offset,
)

@InternalChartsApi
const val DEFAULT_BEZIER_TENSION = 0.95f

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
