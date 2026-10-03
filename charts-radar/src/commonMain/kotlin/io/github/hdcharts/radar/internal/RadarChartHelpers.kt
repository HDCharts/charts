package io.github.hdcharts.radar.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.core.internal.NO_SELECTION
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Below this a label sits straight up or sideways, where its reach is already measured exactly. */
private const val DIAGONAL_EPSILON = 0.0001f

/**
 * The polar frame the whole chart shares: axis 0 points up and the axes are evenly spaced.
 *
 * Hit testing, labels, polygons, and the web radius all read their angles from one frame, so the
 * tap that selects an axis cannot drift from the axis that gets drawn.
 */
internal class RadarFrame private constructor(
    val axisCount: Int,
    /** The angle of axis 0, in radians. */
    val startAngle: Float,
    val angleStep: Float,
) {
    /** The angle of the axis at [index], in radians. */
    fun angleAt(index: Int): Float = startAngle + angleStep * index

    companion object {
        fun of(axisCount: Int) =
            RadarFrame(
                axisCount = axisCount.coerceAtLeast(0),
                startAngle = (-PI / 2f).toFloat(),
                angleStep = if (axisCount > 0) (2f * PI / axisCount).toFloat() else 0f,
            )
    }
}

internal fun seriesAnimationProgress(
    index: Int,
    total: Int,
    animationProgress: Float,
): Float {
    if (total <= 1) return animationProgress.coerceIn(0f, 1f)
    val staggerWindow = 0.45f
    val stagger = staggerWindow / (total - 1)
    val delay = index * stagger
    val available = (1f - delay).coerceAtLeast(0.01f)
    return ((animationProgress - delay) / available).coerceIn(0f, 1f)
}

internal fun axisIndexForOffset(
    offset: Offset,
    size: IntSize,
    axisCount: Int,
): Int {
    if (axisCount <= 0) return NO_SELECTION
    val center = Offset(size.width / 2f, size.height / 2f)
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    if (dx == 0f && dy == 0f) return NO_SELECTION

    val frame = RadarFrame.of(axisCount)
    val touchAngle = atan2(dy, dx)
    val twoPi = (2f * PI).toFloat()
    var normalized = touchAngle - frame.startAngle
    while (normalized < 0f) normalized += twoPi
    while (normalized >= twoPi) normalized -= twoPi
    return (normalized / frame.angleStep).roundToInt() % axisCount
}

internal fun buildAxisLabelPositions(
    axisCount: Int,
    center: Offset,
    radius: Float,
): List<Offset> {
    if (axisCount == 0) return emptyList()
    val frame = RadarFrame.of(axisCount)
    return List(axisCount) { index ->
        val angle = frame.angleAt(index)
        Offset(
            x = center.x + cos(angle) * radius,
            y = center.y + sin(angle) * radius,
        )
    }
}

internal fun radarPolygons(
    normalizedValues: List<List<Float>>,
    axisCount: Int,
    center: Offset,
    radius: Float,
): List<List<Offset>> {
    val frame = RadarFrame.of(axisCount)
    return normalizedValues.map { series ->
        List(axisCount) { index ->
            val angle = frame.angleAt(index)
            val distance = (series.getOrNull(index) ?: 0f) * radius
            Offset(x = center.x + cos(angle) * distance, y = center.y + sin(angle) * distance)
        }
    }
}

/** Returns series with an outline within [touchRadius], closest first, else the containing ones, smallest first. */
internal fun seriesCandidatesAt(
    tap: Offset,
    polygons: List<List<Offset>>,
    touchRadius: Float,
): List<Int> {
    val nearOutline =
        polygons.indices
            .map { index -> index to distanceToOutline(point = tap, polygon = polygons[index]) }
            .filter { (_, distance) -> distance <= touchRadius }
            .sortedBy { (_, distance) -> distance }
            .map { (index, _) -> index }
    if (nearOutline.isNotEmpty()) return nearOutline
    return polygons.indices
        .filter { index -> containsPoint(polygon = polygons[index], point = tap) }
        .sortedBy { index -> polygonArea(polygons[index]) }
}

/** Returns the series after [focusedIndex] in [candidates], or the first one when it is not there. */
internal fun nextFocusedSeries(
    candidates: List<Int>,
    focusedIndex: Int,
): Int {
    if (candidates.isEmpty()) return NO_SELECTION
    val position = candidates.indexOf(focusedIndex)
    return if (position < 0) candidates.first() else candidates[(position + 1) % candidates.size]
}

private fun distanceToOutline(
    point: Offset,
    polygon: List<Offset>,
): Float =
    polygon.indices.minOfOrNull { index ->
        distanceToSegment(point = point, start = polygon[index], end = polygon[(index + 1) % polygon.size])
    } ?: Float.POSITIVE_INFINITY

private fun distanceToSegment(
    point: Offset,
    start: Offset,
    end: Offset,
): Float {
    val segment = end - start
    val lengthSquared = segment.x * segment.x + segment.y * segment.y
    if (lengthSquared == 0f) return (point - start).getDistance()
    val toPoint = point - start
    val t = ((toPoint.x * segment.x + toPoint.y * segment.y) / lengthSquared).coerceIn(0f, 1f)
    return (point - (start + segment * t)).getDistance()
}

private fun containsPoint(
    polygon: List<Offset>,
    point: Offset,
): Boolean {
    var inside = false
    var previous = polygon.lastIndex
    for (current in polygon.indices) {
        val a = polygon[current]
        val b = polygon[previous]
        if ((a.y > point.y) != (b.y > point.y) && point.x < (b.x - a.x) * (point.y - a.y) / (b.y - a.y) + a.x) {
            inside = !inside
        }
        previous = current
    }
    return inside
}

private fun polygonArea(polygon: List<Offset>): Float =
    abs(
        polygon.indices.sumOf { index ->
            val a = polygon[index]
            val b = polygon[(index + 1) % polygon.size]
            (a.x * b.y - b.x * a.y).toDouble()
        } / 2.0,
    ).toFloat()

/**
 * Returns the web radius that leaves room for axis labels outside it, but at least a quarter of the
 * smaller side.
 *
 * Each label keeps its own size and is centered on its axis, so it only reaches
 * `cos * width + sin * height` past the anchor. Reserving that per axis keeps a diagonal label from
 * shrinking the web as much as a horizontal one would.
 */
internal fun radarPlotRadius(
    axisCount: Int,
    widthPx: Float,
    heightPx: Float,
    labelWidthPx: Float,
    labelHeightPx: Float,
    labelPaddingPx: Float,
): Float {
    val fullRadius = min(widthPx, heightPx) / 2f
    if (axisCount <= 0 || (labelWidthPx <= 0f && labelHeightPx <= 0f)) return fullRadius
    val frame = RadarFrame.of(axisCount)
    var fitRadius = fullRadius
    repeat(axisCount) { index ->
        val angle = frame.angleAt(index)
        val unitX = cos(angle)
        val unitY = sin(angle)
        val reach = (abs(unitX) * labelWidthPx + abs(unitY) * labelHeightPx) / 2f
        if (abs(unitX) > DIAGONAL_EPSILON) {
            val limit = (widthPx / 2f - labelWidthPx / 2f) / abs(unitX) - labelPaddingPx - reach
            fitRadius = min(fitRadius, limit)
        }
        if (abs(unitY) > DIAGONAL_EPSILON) {
            val limit = (heightPx / 2f - labelHeightPx / 2f) / abs(unitY) - labelPaddingPx - reach
            fitRadius = min(fitRadius, limit)
        }
    }
    return fitRadius.coerceIn(fullRadius / 2f, fullRadius)
}

/**
 * Returns the top-left of a label so its inner edge sits on [anchor], on the side away from [center].
 *
 * The label keeps its own width and height, so a wide label on a vertical axis needs only its height
 * of clearance, not half its width.
 */
internal fun axisLabelTopLeft(
    anchor: Offset,
    center: Offset,
    widthPx: Int,
    heightPx: Int,
): Offset {
    val direction = anchor - center
    val length = direction.getDistance()
    val unit = if (length > 0f) direction / length else Offset.Zero
    // Half of the label measured along the axis, which is how far it moves away from the anchor.
    val push = (abs(unit.x) * widthPx + abs(unit.y) * heightPx) / 2f
    return Offset(
        x = anchor.x - widthPx / 2f + unit.x * push,
        y = anchor.y - heightPx / 2f + unit.y * push,
    )
}
