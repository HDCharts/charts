package io.github.hdcharts.gauge.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Rings never grow into the innermost part of the half circle, so the gauge keeps its arc shape. */
internal const val RING_GAUGE_MIN_HOLE_FRACTION = 0.35f

/** The arc starts on the left and sweeps clockwise over the top to the right. */
internal const val GAUGE_START_ANGLE = 180f
internal const val GAUGE_SWEEP_ANGLE = 180f

/** One drawn ring: the radius of its stroke center and its stroke width, in pixels. */
internal data class RingGeometry(
    val centerRadius: Float,
    val width: Float,
) {
    val outerRadius: Float get() = centerRadius + width / 2f
    val innerRadius: Float get() = centerRadius - width / 2f
}

/**
 * Where a ring gauge draws inside its canvas: the arc [center] on the baseline, the [outerRadius] of
 * the outermost ring, and the [rings] from the outside in. The space under the baseline is left for
 * the range labels.
 */
internal data class RingGaugeLayout(
    val center: Offset,
    val outerRadius: Float,
    val rings: List<RingGeometry>,
    val spacing: Float,
)

/** The share of the arc [value] fills in [min]..[max], from 0 to 1. Values outside the range stop at an end. */
internal fun gaugeFraction(
    value: Double,
    min: Double,
    max: Double,
): Float {
    val span = max - min
    if (span <= 0.0) return 0f
    return ((value - min) / span).coerceIn(0.0, 1.0).toFloat()
}

/**
 * Lays out [ringCount] rings in a canvas of [size], keeping [labelBand] pixels under the baseline.
 * Each ring is at most [maxRingWidth] wide; when that many rings would not fit between the outer
 * edge and the hole, they get thinner. Spacing that leaves no room for rings is dropped.
 */
internal fun ringGaugeLayout(
    size: Size,
    ringCount: Int,
    maxRingWidth: Float,
    spacing: Float,
    labelBand: Float,
): RingGaugeLayout {
    val outerRadius = max(0f, min(size.width / 2f, size.height - labelBand))
    val contentHeight = outerRadius + labelBand
    val center = Offset(x = size.width / 2f, y = (size.height - contentHeight) / 2f + outerRadius)
    if (ringCount <=
        0
    ) {
        return RingGaugeLayout(center = center, outerRadius = outerRadius, rings = emptyList(), spacing = 0f)
    }

    val band = outerRadius * (1f - RING_GAUGE_MIN_HOLE_FRACTION)
    val gaps = ringCount - 1
    val fittedSpacing = if (band - spacing * gaps > 0f) spacing else 0f
    val width = min(maxRingWidth, (band - fittedSpacing * gaps) / ringCount).coerceAtLeast(0f)
    val rings =
        List(ringCount) { index ->
            val outerEdge = outerRadius - index * (width + fittedSpacing)
            RingGeometry(centerRadius = outerEdge - width / 2f, width = width)
        }
    return RingGaugeLayout(center = center, outerRadius = outerRadius, rings = rings, spacing = fittedSpacing)
}

/**
 * The ring under [point], or [NO_SELECTION]. A ring is hit anywhere along its full arc, so a ring with
 * a small value is as easy to tap as a full one. Half of the spacing on each side counts towards the
 * nearer ring, and points under the baseline hit nothing.
 */
internal fun ringIndexAt(
    point: Offset,
    layout: RingGaugeLayout,
): Int {
    if (point.y > layout.center.y) return NO_SELECTION
    val dx = point.x - layout.center.x
    val dy = point.y - layout.center.y
    val distance = sqrt(dx * dx + dy * dy)
    val tolerance = layout.spacing / 2f
    return layout.rings.indexOfFirst { ring ->
        distance >= ring.innerRadius - tolerance && distance <= ring.outerRadius + tolerance
    }
}
