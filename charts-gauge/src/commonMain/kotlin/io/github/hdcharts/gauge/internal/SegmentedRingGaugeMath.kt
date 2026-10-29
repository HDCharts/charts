package io.github.hdcharts.gauge.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.gauge.SegmentedRingGaugeDirection
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

private const val FULL_CIRCLE = 360f

internal data class SegmentedRingGaugeLayout(
    val center: Offset,
    val radius: Float,
    val width: Float,
)

internal data class SegmentGeometry(
    val startAngle: Float,
    val sweepAngle: Float,
)

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

internal fun segmentGeometries(
    values: List<Double>,
    capacity: Double,
    gapAngle: Float,
    startAngle: Float,
    direction: SegmentedRingGaugeDirection,
): List<SegmentGeometry> {
    var cursor = startAngle
    val hasSingleFullSegment = values.count { it > 0.0 } == 1 && values.sum() == capacity
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

internal fun remainingSegmentGeometry(
    values: List<Double>,
    capacity: Double,
    gapAngle: Float,
    startAngle: Float,
    direction: SegmentedRingGaugeDirection,
): SegmentGeometry? {
    val consumedSweep = (values.sum() / capacity * FULL_CIRCLE).toFloat()
    val remainingSweep = (FULL_CIRCLE - consumedSweep).coerceAtLeast(0f)
    if (remainingSweep <= 0f) return null
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

internal fun gapAngleFor(
    gap: Float,
    radius: Float,
): Float =
    if (radius > 0f) {
        (gap / radius).toDegrees()
    } else {
        0f
    }

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

internal fun Float.toRadians(): Float = this / 180f * PI.toFloat()

internal fun Float.toDegrees(): Float = this * 180f / PI.toFloat()
