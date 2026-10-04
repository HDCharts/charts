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

/** Number of floats [cubicControlPointsInto] writes: the X and Y of both control points. */
@InternalChartsApi
const val CUBIC_CONTROL_POINT_COUNT = 4

/**
 * Control points for the cubic segment from [p1] to [p2], smoothed against the neighbouring points
 * [p0] and [p3]. A segment at either end of the series repeats its own endpoint for the missing
 * neighbour, so the line leaves and arrives flat.
 *
 * Takes the four points as arguments rather than a list so a chart drawing one segment at a time
 * does not have to allocate a list of offsets for the whole series on every frame. A chart that
 * draws a segment per frame reads the same values through [cubicControlPointsInto] instead, which
 * writes into a buffer the chart already holds.
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
    val controls = FloatArray(CUBIC_CONTROL_POINT_COUNT)
    cubicControlPointsInto(
        out = controls,
        p0x = p0.x,
        p0y = p0.y,
        p1x = p1.x,
        p1y = p1.y,
        p2x = p2.x,
        p2y = p2.y,
        p3x = p3.x,
        p3y = p3.y,
        tension = tension,
        minY = minY,
        maxY = maxY,
    )

    return CubicControlPoints(
        first = Offset(x = controls[0], y = controls[1]),
        second = Offset(x = controls[2], y = controls[3]),
    )
}

/**
 * Writes the control points of the cubic segment from `p1` to `p2` into [out], as
 * `[c1x, c1y, c2x, c2y]`, smoothed against the neighbouring points `p0` and `p3`.
 *
 * Same rule and same arithmetic as [cubicControlPoints], without the object per segment: a chart
 * that draws a whole series of segments on one frame writes them all through this.
 */
@InternalChartsApi
fun cubicControlPointsInto(
    out: FloatArray,
    p0x: Float,
    p0y: Float,
    p1x: Float,
    p1y: Float,
    p2x: Float,
    p2y: Float,
    p3x: Float,
    p3y: Float,
    tension: Float = DEFAULT_BEZIER_TENSION,
    minY: Float = Float.NEGATIVE_INFINITY,
    maxY: Float = Float.POSITIVE_INFINITY,
) {
    val factor = tension / 6f
    val lowerYBound = minY.coerceAtMost(maxY)
    val upperYBound = maxY.coerceAtLeast(minY)
    out[0] = p1x + (p2x - p0x) * factor
    out[1] = (p1y + (p2y - p0y) * factor).coerceIn(lowerYBound, upperYBound)
    out[2] = p2x - (p3x - p1x) * factor
    out[3] = (p2y - (p3y - p1y) * factor).coerceIn(lowerYBound, upperYBound)
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
