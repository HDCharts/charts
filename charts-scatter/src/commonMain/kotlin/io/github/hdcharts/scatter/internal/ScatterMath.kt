package io.github.hdcharts.scatter.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.model.resolveOptionalRange
import io.github.hdcharts.scatter.ScatterPoint
import io.github.hdcharts.scatter.ScatterRangeStyle
import kotlin.math.hypot
import kotlin.math.sqrt

/** Numeric bounds used to position points and label both axes. */
internal data class ScatterDomain(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
) {
    val xRange: Double = (maxX - minX).takeIf { it.isFinite() && it > 0.0 } ?: 1.0
    val yRange: Double = (maxY - minY).takeIf { it.isFinite() && it > 0.0 } ?: 1.0
}

/** Resolves automatic bounds from validated, non-empty points. */
internal fun scatterDomain(
    points: List<ScatterPoint>,
    range: ScatterRangeStyle,
): ScatterDomain {
    val xRange = resolveOptionalRange(points.minOf { it.x }, points.maxOf { it.x }, range.xMin, range.xMax)
    val yRange = resolveOptionalRange(points.minOf { it.y }, points.maxOf { it.y }, range.yMin, range.yMax)
    return ScatterDomain(minX = xRange.first, maxX = xRange.second, minY = yRange.first, maxY = yRange.second)
}

/** Normalizes points in source order, with Y increasing toward the top of the plot. */
internal fun normalizedScatterPositions(
    points: List<ScatterPoint>,
    domain: ScatterDomain,
): List<Offset> =
    points.map { point ->
        Offset(
            x = ((point.x - domain.minX) / domain.xRange).toFloat(),
            y = (1.0 - (point.y - domain.minY) / domain.yRange).toFloat(),
        )
    }

/** Interpolates bubble area between the radius limits; unsized points use [pointRadius]. */
internal fun scatterPointRadii(
    points: List<ScatterPoint>,
    pointRadius: Float,
    minBubbleRadius: Float,
    maxBubbleRadius: Float,
): List<Float> {
    val sizes = points.mapNotNull { it.size }
    val minSize = sizes.minOrNull() ?: 0.0
    val maxSize = sizes.maxOrNull() ?: minSize
    return points.map { point ->
        val value = point.size
        if (value == null) {
            pointRadius
        } else {
            val fraction = if (maxSize == minSize) 0.5 else ((value - minSize) / (maxSize - minSize)).coerceIn(0.0, 1.0)
            sqrt(
                minBubbleRadius * minBubbleRadius +
                    fraction.toFloat() * (maxBubbleRadius * maxBubbleRadius - minBubbleRadius * minBubbleRadius),
            )
        }
    }
}

/** Pixel bounds shared by drawing and pointer hit testing. */
internal data class ScatterPlotLayout(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
) {
    fun position(normalized: Offset): Offset = Offset(left + normalized.x * width, top + normalized.y * height)
}

/** Reserves space for axis labels and keeps a positive drawable span in small viewports. */
internal fun scatterPlotLayout(
    size: Size,
    padding: Float,
    yAxisSpace: Float = 0f,
    xAxisSpace: Float = 0f,
    xAxisEdgeSpace: Float = 0f,
): ScatterPlotLayout =
    ScatterPlotLayout(
        left = maxOf(padding, yAxisSpace, xAxisEdgeSpace),
        top = padding,
        width =
            (
                size.width -
                    maxOf(
                        padding,
                        xAxisEdgeSpace,
                    ) - maxOf(padding, yAxisSpace, xAxisEdgeSpace)
            ).coerceAtLeast(1f),
        height = (size.height - padding - maxOf(padding, xAxisSpace)).coerceAtLeast(1f),
    )

/** Evenly spaced numeric X-axis values, including both bounds without subtraction overflow. */
internal fun scatterXAxisValues(
    domain: ScatterDomain,
    count: Int,
): List<Double> {
    val steps = count.coerceAtLeast(2) - 1
    return (0..steps).map { step ->
        val fraction = step.toDouble() / steps
        (domain.minX * (1.0 - fraction) + domain.maxX * fraction).coerceIn(domain.minX, domain.maxX)
    }
}

/** Returns the nearest point in pixel space, or [NO_SELECTION] when no points are present. */
internal fun nearestScatterPointIndex(
    point: Offset,
    positions: List<Offset>,
    layout: ScatterPlotLayout,
): Int =
    positions.indices.minByOrNull { index ->
        val target = layout.position(positions[index])
        hypot(target.x - point.x, target.y - point.y)
    } ?: NO_SELECTION
