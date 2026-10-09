package io.github.hdcharts.core.internal.axis

import androidx.compose.ui.unit.IntOffset
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.model.normalizeValue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Estimated width of one character, as a share of the font size.
private const val AXIS_LABEL_CHAR_WIDTH_FACTOR = 0.58f

// Estimated height of one line of text, as a share of the font size.
private const val AXIS_LABEL_LINE_HEIGHT_FACTOR = 1.2f

// Extra room between neighboring axis labels, as a share of the label line height.
private const val AXIS_LABEL_GAP_FACTOR = 0.5f

// Slack for the float plot width being rounded to a whole-pixel label row.
internal const val X_AXIS_LABEL_EDGE_TOLERANCE_PX = 1f

// Slack for the float plot height being rounded to a whole-pixel label column.
private const val Y_AXIS_LABEL_EDGE_TOLERANCE_PX = 1f

/** Tilt of every X-axis label, in degrees. Labels read upward to the right. */
@InternalChartsApi
const val X_AXIS_LABEL_TILT_DEGREES = 34f

// Sine and cosine of the label tilt, computed once for the row height and spacing formulas.
private val xAxisLabelTiltSin = sin(X_AXIS_LABEL_TILT_DEGREES * PI.toFloat() / 180f)
private val xAxisLabelTiltCos = cos(X_AXIS_LABEL_TILT_DEGREES * PI.toFloat() / 180f)

/** Label for item [index]: its text in [labels], or the 1-based item number when it is missing or blank. */
@InternalChartsApi
fun resolveAxisLabel(
    labels: List<String>,
    index: Int,
): String = labels.getOrNull(index).orEmpty().ifBlank { (index + 1).toString() }

/**
 * Estimated extent of the longest tilted X-axis label: the [heightPx] it takes below the plot and the
 * [halfWidthPx] it reaches to either side of its tick.
 */
@InternalChartsApi
data class AxisXLabelExtent(
    val heightPx: Float,
    val halfWidthPx: Float,
)

/** Extent of the tilted X-axis labels, estimated from the longest label. */
@InternalChartsApi
fun estimateXAxisLabelExtent(
    labels: List<String>,
    dataSize: Int,
    fontSizePx: Float,
): AxisXLabelExtent {
    if (dataSize <= 0 || fontSizePx <= 0f) {
        return AxisXLabelExtent(heightPx = fontSizePx.coerceAtLeast(1f), halfWidthPx = 0f)
    }

    val longestLabelLength = longestResolvedAxisLabelLength(labels, dataSize)
    val labelWidthPx = longestLabelLength.coerceAtLeast(1) * fontSizePx * AXIS_LABEL_CHAR_WIDTH_FACTOR
    val lineHeightPx = fontSizePx * AXIS_LABEL_LINE_HEIGHT_FACTOR
    return AxisXLabelExtent(
        heightPx = (labelWidthPx * xAxisLabelTiltSin + lineHeightPx * xAxisLabelTiltCos).coerceAtLeast(1f),
        halfWidthPx = (labelWidthPx * xAxisLabelTiltCos + lineHeightPx * xAxisLabelTiltSin) / 2f,
    )
}

// Length of the longest label resolveAxisLabel returns for items 0 until dataSize, without building
// the item-number fallbacks: a missing or blank label at index i prints i + 1.
private fun longestResolvedAxisLabelLength(
    labels: List<String>,
    dataSize: Int,
): Int {
    var longest = 0
    var lastFallbackIndex = if (labels.size < dataSize) dataSize - 1 else -1
    for (index in 0 until minOf(labels.size, dataSize)) {
        val label = labels[index]
        if (label.isBlank()) {
            lastFallbackIndex = maxOf(lastFallbackIndex, index)
        } else {
            longest = maxOf(longest, label.length)
        }
    }
    if (lastFallbackIndex >= 0) longest = maxOf(longest, digitCount(lastFallbackIndex + 1))
    return longest
}

private fun digitCount(value: Int): Int {
    var digits = 1
    var rest = value / 10
    while (rest > 0) {
        digits++
        rest /= 10
    }
    return digits
}

/**
 * Smallest distance between two X-axis label ticks that keeps their labels apart. Tilted labels
 * are parallel, so only their line height can make them touch, never their length.
 */
@InternalChartsApi
fun xAxisLabelMinSpacingPx(fontSizePx: Float): Float =
    fontSizePx.coerceAtLeast(0f) * AXIS_LABEL_LINE_HEIGHT_FACTOR * (1f + AXIS_LABEL_GAP_FACTOR) / xAxisLabelTiltSin

/**
 * Smallest distance between two Y-axis label ticks that keeps their labels apart: one line of text
 * plus the same gap as between X-axis labels.
 */
@InternalChartsApi
fun yAxisLabelMinSpacingPx(fontSizePx: Float): Float =
    fontSizePx.coerceAtLeast(0f) * AXIS_LABEL_LINE_HEIGHT_FACTOR * (1f + AXIS_LABEL_GAP_FACTOR)

/**
 * Top-left position of an unrotated X-axis label that, once tilted around its center, is centered
 * on [tickX] with its center at [centerY]. All labels share one center line, so evenly spaced
 * ticks keep labels of any length apart. A label is drawn whenever its tick is inside the row, and
 * edge labels may extend into the chart padding. Returns null when the tick is outside the row.
 */
@InternalChartsApi
fun placeXAxisLabel(
    tickX: Float,
    labelWidthPx: Int,
    labelHeightPx: Int,
    rowWidthPx: Int,
    centerY: Float,
): IntOffset? {
    val tolerance = X_AXIS_LABEL_EDGE_TOLERANCE_PX
    if (!tickX.isFinite() || tickX < -tolerance || tickX > rowWidthPx + tolerance) return null
    return IntOffset(
        x = (tickX - labelWidthPx / 2f).roundToInt(),
        y = (centerY - labelHeightPx / 2f).roundToInt(),
    )
}

/**
 * Top-left position of a Y-axis label that is right-aligned [edgePaddingPx] from the right edge of
 * its column and vertically centered on [tickY], but kept inside the column. Returns null when the
 * tick is outside the column height, with 1 px of slack for rounding.
 */
@InternalChartsApi
fun placeYAxisLabel(
    tickY: Float,
    labelWidthPx: Int,
    labelHeightPx: Int,
    columnWidthPx: Int,
    columnHeightPx: Int,
    edgePaddingPx: Int,
): IntOffset? {
    val tolerance = Y_AXIS_LABEL_EDGE_TOLERANCE_PX
    if (!tickY.isFinite() || tickY < -tolerance || tickY > columnHeightPx + tolerance) return null
    val maxY = (columnHeightPx - labelHeightPx).coerceAtLeast(0)
    return IntOffset(
        x = (columnWidthPx - labelWidthPx - edgePaddingPx).coerceAtLeast(0),
        y = (tickY - labelHeightPx / 2f).roundToInt().coerceIn(0, maxY),
    )
}

/**
 * Width the Y-axis labels need, estimated from the longest label at [fontSizePx]. Returns 0 when
 * there are no labels or the font size is not positive.
 */
@InternalChartsApi
fun estimateYAxisLabelWidthPx(
    labels: List<String>,
    fontSizePx: Float,
): Float {
    if (labels.isEmpty() || fontSizePx <= 0f) return 0f

    val longestLabelLength = labels.maxOf { it.length }.coerceAtLeast(1)
    return longestLabelLength * fontSizePx * AXIS_LABEL_CHAR_WIDTH_FACTOR
}

/**
 * Range of item indexes whose slots overlap the viewport, from the item at [scrollOffsetPx] to the
 * item at the viewport's far edge, both clamped to [dataSize]. Each item takes [unitWidthPx].
 * Returns an empty range when there is nothing to show.
 */
@InternalChartsApi
fun visibleIndexRange(
    dataSize: Int,
    viewportWidthPx: Float,
    scrollOffsetPx: Float,
    unitWidthPx: Float,
): IntRange {
    if (dataSize <= 0 || viewportWidthPx <= 0f || unitWidthPx <= 0f) return IntRange.EMPTY
    val firstVisible = (scrollOffsetPx / unitWidthPx).toInt().coerceIn(0, dataSize - 1)
    val lastVisible =
        ((scrollOffsetPx + viewportWidthPx) / unitWidthPx)
            .toInt()
            .coerceIn(firstVisible, dataSize - 1)
    return firstVisible..lastVisible
}

/**
 * Y position of the zero line in a plot [heightPx] tall that shows [minValue] at the bottom and
 * [maxValue] at the top, clamped to the plot. A range with no width puts zero at the bottom, or at
 * the top when every value is negative.
 */
@InternalChartsApi
fun baselineYForRange(
    minValue: Double,
    maxValue: Double,
    heightPx: Float,
): Float {
    if (heightPx <= 0f) return 0f
    if (minValue == maxValue) {
        return if (maxValue < 0.0) 0f else heightPx
    }

    val normalizedZero = normalizeValue(0.0, minValue, maxValue)
    val baseline = heightPx * (1f - normalizedZero)
    return baseline.coerceIn(0f, heightPx)
}
