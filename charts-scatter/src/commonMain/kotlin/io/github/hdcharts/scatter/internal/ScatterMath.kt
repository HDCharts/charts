package io.github.hdcharts.scatter.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.model.normalizeValue
import io.github.hdcharts.core.internal.model.resolveOptionalRange
import io.github.hdcharts.scatter.ScatterPoint
import io.github.hdcharts.scatter.ScatterRangeStyle
import kotlin.math.abs
import kotlin.math.sqrt

/** Numeric bounds used to position points and label both axes. */
internal data class ScatterDomain(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
)

/** Resolves automatic bounds from validated, non-empty points. */
internal fun scatterDomain(
    points: List<ScatterPoint>,
    range: ScatterRangeStyle,
): ScatterDomain {
    val xRange = scatterAxisRange(points.minOf { it.x }, points.maxOf { it.x }, range.xMin, range.xMax)
    val yRange = scatterAxisRange(points.minOf { it.y }, points.maxOf { it.y }, range.yMin, range.yMax)
    return ScatterDomain(minX = xRange.first, maxX = xRange.second, minY = yRange.first, maxY = yRange.second)
}

private fun scatterAxisRange(
    dataMin: Double,
    dataMax: Double,
    minValue: Double?,
    maxValue: Double?,
): Pair<Double, Double> {
    if (dataMin == dataMax && minValue == null && maxValue == null) {
        // Give constant observations 5% padding on each side, with at least one unit of space.
        val padding = maxOf(abs(dataMin) * 0.05, 1.0)
        return (dataMin - padding).coerceAtLeast(-Double.MAX_VALUE) to
            (dataMax + padding).coerceAtMost(Double.MAX_VALUE)
    }
    return resolveOptionalRange(dataMin, dataMax, minValue, maxValue)
}

/** Normalizes points in source order, clamping out-of-range values to the plot edges. */
internal fun normalizedScatterPositions(
    points: List<ScatterPoint>,
    domain: ScatterDomain,
    range: ScatterRangeStyle,
): List<Offset> =
    points.map { point ->
        Offset(
            x = normalizeValue(point.x, domain.minX, domain.maxX, rangeZeroPosition(range.xMin, range.xMax)),
            y = 1f - normalizeValue(point.y, domain.minY, domain.maxY, rangeZeroPosition(range.yMin, range.yMax)),
        )
    }

private fun rangeZeroPosition(
    minValue: Double?,
    maxValue: Double?,
): Float =
    when {
        minValue != null && maxValue == null -> 0f
        minValue == null && maxValue != null -> 1f
        else -> 0f
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

/** Keeps the largest point and its selection outline inside the canvas at every plot edge. */
internal fun scatterPlotPadding(
    contentPadding: Float,
    pointRadii: List<Float>,
    selectionPadding: Float,
    selectionWidth: Float,
): Float = maxOf(contentPadding, (pointRadii.maxOrNull() ?: 0f) + selectionPadding + selectionWidth / 2f)

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

/** Returns the nearest touchable point in pixel space, or [NO_SELECTION] when none is close enough. */
internal fun nearestScatterPointIndex(
    point: Offset,
    positions: List<Offset>,
    radii: List<Float>,
    layout: ScatterPlotLayout,
    touchRadius: Float,
): Int {
    var nearestIndex = NO_SELECTION
    var nearestDistanceSquared = Float.POSITIVE_INFINITY
    for (index in positions.indices) {
        val target = layout.position(positions[index])
        val dx = target.x - point.x
        val dy = target.y - point.y
        val distanceSquared = dx * dx + dy * dy
        val hitRadius = maxOf(touchRadius, radii.getOrElse(index) { 0f })
        if (distanceSquared <= hitRadius * hitRadius && distanceSquared < nearestDistanceSquared) {
            nearestIndex = index
            nearestDistanceSquared = distanceSquared
        }
    }
    return nearestIndex
}

/** Resolves a flattened source point index to its owning series. */
internal fun scatterSeriesIndexForPointIndex(
    pointCounts: List<Int>,
    pointIndex: Int,
): Int {
    if (pointIndex < 0) return NO_SELECTION
    var startIndex = 0
    pointCounts.forEachIndexed { seriesIndex, count ->
        if (pointIndex < startIndex + count) return seriesIndex
        startIndex += count
    }
    return NO_SELECTION
}
