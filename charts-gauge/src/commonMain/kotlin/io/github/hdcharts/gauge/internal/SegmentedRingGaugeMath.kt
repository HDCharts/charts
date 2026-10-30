package io.github.hdcharts.gauge.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.gauge.SegmentedRingGaugeDirection
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** The full sweep of a ring, in degrees. */
private const val FULL_CIRCLE = 360f

/** The distance between 1.0 and the next representable [Double] value. */
private const val DOUBLE_EPSILON = 2.220446049250313E-16

/** The bounded number of machine-epsilon units allowed for capacity comparisons. */
private const val CAPACITY_COMPARISON_ULPS = 4.0

/** The measured center, radius, and drawable stroke width of a segmented ring gauge. */
internal data class SegmentedRingGaugeLayout(
    val center: Offset,
    val radius: Float,
    val width: Float,
)

/** The start angle and positive sweep used to draw one ring segment. */
internal data class SegmentGeometry(
    val startAngle: Float,
    val sweepAngle: Float,
)

/** Returns the sum of [values] with compensation for rounding lost during accumulation. */
internal fun List<Double>.compensatedSum(): Double {
    var sum = 0.0
    var compensation = 0.0
    for (value in this) {
        val next = sum + value
        compensation +=
            if (abs(sum) >= abs(value)) {
                (sum - next) + value
            } else {
                (value - next) + sum
            }
        sum = next
    }
    return sum + compensation
}

/** Returns whether [total] and [capacity] differ only by the bounded comparison tolerance. */
internal fun isCapacityFull(
    total: Double,
    capacity: Double,
): Boolean {
    val scale = max(abs(total), abs(capacity))
    val tolerance = scale * DOUBLE_EPSILON * CAPACITY_COMPARISON_ULPS
    return abs(total - capacity) <= tolerance
}

/** Returns whether [total] exceeds [capacity] by more than floating-point rounding error. */
internal fun totalExceedsCapacity(
    total: Double,
    capacity: Double,
): Boolean = total > capacity && !isCapacityFull(total = total, capacity = capacity)

/**
 * Returns the geometry revealed at [progress], keeping the configured boundary fixed for the
 * selected [direction].
 */
internal fun animatedSegmentGeometry(
    geometry: SegmentGeometry,
    progress: Float,
    direction: SegmentedRingGaugeDirection,
): SegmentGeometry {
    val animatedSweep = geometry.sweepAngle * progress.coerceIn(0f, 1f)
    return when (direction) {
        SegmentedRingGaugeDirection.Clockwise ->
            SegmentGeometry(startAngle = geometry.startAngle, sweepAngle = animatedSweep)

        SegmentedRingGaugeDirection.CounterClockwise ->
            SegmentGeometry(
                startAngle = geometry.startAngle + geometry.sweepAngle - animatedSweep,
                sweepAngle = animatedSweep,
            )
    }
}

/** Limits a segment corner radius to the ring thickness and the available segment sweep. */
internal fun segmentedRingGaugeCornerRadius(
    cornerRadius: Float,
    width: Float,
    innerRadius: Float,
    sweepRadians: Float,
): Float = min(cornerRadius, min(width / 2f, innerRadius * sweepRadians / 4f))

/** Fits the ring inside [size], reserving [overflowInset] for the selected-segment scale. */
internal fun segmentedRingGaugeLayout(
    size: Size,
    width: Float,
    overflowInset: Float = 0f,
): SegmentedRingGaugeLayout {
    val availableDimension = (size.minDimension - overflowInset * 2f).coerceAtLeast(0f)
    val outerRadius = (availableDimension / 2f).coerceAtLeast(0f)
    val fittedWidth = width.coerceIn(0f, outerRadius * (1f - GAUGE_MIN_HOLE_FRACTION))
    return SegmentedRingGaugeLayout(
        center = Offset(size.width / 2f, size.height / 2f),
        radius = outerRadius - fittedWidth / 2f,
        width = fittedWidth,
    )
}

/** Converts each contribution in [values] into a gap-aware segment geometry. */
internal fun segmentGeometries(
    values: List<Double>,
    capacity: Double,
    gapAngle: Float,
    startAngle: Float,
    direction: SegmentedRingGaugeDirection,
): List<SegmentGeometry> {
    var cursor = startAngle
    val hasSingleFullSegment =
        values.count { it > 0.0 } == 1 &&
            isCapacityFull(
                total = values.compensatedSum(),
                capacity = capacity,
            )
    val effectiveGapAngle = if (hasSingleFullSegment) 0f else gapAngle
    return values.map { value ->
        val rawSweep = (value / capacity * FULL_CIRCLE).toFloat()
        val gap = min(effectiveGapAngle, rawSweep / 2f)
        val sweepAngle = (rawSweep - gap).coerceAtLeast(0f)
        when (direction) {
            SegmentedRingGaugeDirection.Clockwise ->
                SegmentGeometry(startAngle = cursor + gap / 2f, sweepAngle = sweepAngle).also {
                    cursor += rawSweep
                }

            SegmentedRingGaugeDirection.CounterClockwise ->
                SegmentGeometry(startAngle = cursor - rawSweep + gap / 2f, sweepAngle = sweepAngle).also {
                    cursor -= rawSweep
                }
        }
    }
}

/** Returns the gap-aware geometry for the unconsumed part of the ring, or null when it is full. */
internal fun remainingSegmentGeometry(
    values: List<Double>,
    capacity: Double,
    gapAngle: Float,
    startAngle: Float,
    direction: SegmentedRingGaugeDirection,
): SegmentGeometry? {
    val consumedSweep = (values.compensatedSum() / capacity * FULL_CIRCLE).toFloat()
    val remainingSweep = (FULL_CIRCLE - consumedSweep).coerceAtLeast(0f)
    if (remainingSweep <= 0f) return null
    if (consumedSweep <= 0f) return SegmentGeometry(startAngle = startAngle, sweepAngle = FULL_CIRCLE)
    val gap = min(gapAngle, remainingSweep / 2f)
    val sweepAngle = (remainingSweep - gap).coerceAtLeast(0f)
    return when (direction) {
        SegmentedRingGaugeDirection.Clockwise ->
            SegmentGeometry(startAngle = startAngle + consumedSweep + gap / 2f, sweepAngle = sweepAngle)

        SegmentedRingGaugeDirection.CounterClockwise ->
            SegmentGeometry(
                startAngle = startAngle - consumedSweep - remainingSweep + gap / 2f,
                sweepAngle = sweepAngle,
            )
    }
}

/** Converts a physical [gap] at [radius] into its equivalent angular gap. */
internal fun gapAngleFor(
    gap: Float,
    radius: Float,
): Float =
    if (radius > 0f) {
        (gap / radius).toDegrees()
    } else {
        0f
    }

/** Returns the visible segment containing [point], or [NO_SELECTION] when the point misses. */
internal fun segmentIndexAt(
    point: Offset,
    layout: SegmentedRingGaugeLayout,
    geometries: List<SegmentGeometry>,
): Int {
    val dx = point.x - layout.center.x
    val dy = point.y - layout.center.y
    val distance = sqrt(dx * dx + dy * dy)
    if (distance !in (layout.radius - layout.width / 2f)..(layout.radius + layout.width / 2f)) return NO_SELECTION
    val angle = (atan2(dy, dx) * 180f / PI.toFloat() + FULL_CIRCLE) % FULL_CIRCLE
    return geometries.indexOfFirst { geometry ->
        val start = (geometry.startAngle + FULL_CIRCLE) % FULL_CIRCLE
        val end = start + geometry.sweepAngle
        val normalizedAngle = if (angle < start) angle + FULL_CIRCLE else angle
        normalizedAngle in start..end
    }
}

/** Converts an angle in degrees to radians. */
internal fun Float.toRadians(): Float = this / 180f * PI.toFloat()

/** Converts an angle in radians to degrees. */
internal fun Float.toDegrees(): Float = this * 180f / PI.toFloat()
