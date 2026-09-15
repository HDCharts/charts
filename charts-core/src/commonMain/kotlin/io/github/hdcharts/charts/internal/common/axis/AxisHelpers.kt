package io.github.hdcharts.charts.internal.common.axis

import io.github.hdcharts.charts.internal.InternalChartsApi
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

private const val AXIS_LABEL_CHAR_WIDTH_FACTOR = 0.58f
private const val AXIS_LABEL_LINE_HEIGHT_FACTOR = 1.2f

@InternalChartsApi
data class AxisLabelFootprintPx(
    val width: Float,
    val height: Float,
)

@InternalChartsApi
fun resolveAxisLabel(
    labels: List<String>,
    index: Int,
): String = labels.getOrNull(index).orEmpty().ifBlank { (index + 1).toString() }

@InternalChartsApi
fun formatNumericAxisValue(value: Double): String {
    val rounded = ((value * 100.0).roundToInt()) / 100.0
    val normalized = if (abs(rounded) < 0.005) 0.0 else rounded
    return normalized.toString().removeSuffix(".0")
}

@InternalChartsApi
fun estimateXAxisLabelFootprintPx(
    labels: List<String>,
    dataSize: Int,
    fontSizePx: Float,
    tiltDegrees: Float,
): AxisLabelFootprintPx {
    if (dataSize <= 0 || fontSizePx <= 0f) {
        return AxisLabelFootprintPx(width = 1f, height = fontSizePx.coerceAtLeast(1f))
    }

    var longestLabelLength = 1
    repeat(dataSize) { index ->
        val resolvedLabel = resolveAxisLabel(labels = labels, index = index)
        longestLabelLength = max(longestLabelLength, resolvedLabel.length)
    }

    val baseWidth = longestLabelLength * fontSizePx * AXIS_LABEL_CHAR_WIDTH_FACTOR
    val baseHeight = fontSizePx * AXIS_LABEL_LINE_HEIGHT_FACTOR
    val normalizedTilt = tiltDegrees.coerceIn(0f, 75f)
    if (normalizedTilt <= 0f) {
        return AxisLabelFootprintPx(
            width = baseWidth.coerceAtLeast(1f),
            height = baseHeight.coerceAtLeast(1f),
        )
    }

    val theta = normalizedTilt * PI.toFloat() / 180f
    val cosTheta = cos(theta)
    val sinTheta = sin(theta)
    val rotatedWidth = baseWidth * cosTheta + baseHeight * sinTheta
    val rotatedHeight = baseWidth * sinTheta + baseHeight * cosTheta

    return AxisLabelFootprintPx(
        width = rotatedWidth.coerceAtLeast(1f),
        height = rotatedHeight.coerceAtLeast(1f),
    )
}

@InternalChartsApi
fun estimateYAxisLabelWidthPx(
    labels: List<String>,
    fontSizePx: Float,
): Float {
    if (labels.isEmpty() || fontSizePx <= 0f) return 0f

    val longestLabelLength = labels.maxOf { it.length }.coerceAtLeast(1)
    return longestLabelLength * fontSizePx * AXIS_LABEL_CHAR_WIDTH_FACTOR
}

@InternalChartsApi
fun sampledLabelIndices(
    dataSize: Int,
    maxCount: Int,
    visibleRange: IntRange? = null,
): List<Int> {
    if (dataSize <= 0) return emptyList()
    val safeMaxCount = maxCount.coerceAtLeast(2)
    val fullRange = 0..(dataSize - 1)
    val range =
        when {
            visibleRange == null -> {
                fullRange
            }
            visibleRange.isEmpty() -> {
                return emptyList()
            }
            else -> {
                val start = visibleRange.first.coerceIn(0, dataSize - 1)
                val end = visibleRange.last.coerceIn(start, dataSize - 1)
                start..end
            }
        }

    val size = range.last - range.first + 1
    if (size <= safeMaxCount) {
        return range.toList()
    }

    val step = (size - 1).toFloat() / (safeMaxCount - 1).toFloat()
    val sampled =
        (0 until safeMaxCount)
            .map { tick ->
                val raw = range.first + tick * step
                raw.roundToInt().coerceIn(range.first, range.last)
            }.distinct()
            .toMutableList()

    if (sampled.firstOrNull() != range.first) sampled.add(0, range.first)
    if (sampled.lastOrNull() != range.last) sampled.add(range.last)

    return sampled.distinct().sorted()
}

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

@InternalChartsApi
fun scrollableLabelIndices(
    dataSize: Int,
    maxCount: Int,
    visibleRange: IntRange,
    stableVisibleCount: Int? = null,
): List<Int> {
    if (dataSize <= 0 || visibleRange.isEmpty()) return emptyList()

    val start = visibleRange.first.coerceIn(0, dataSize - 1)
    val end = visibleRange.last.coerceIn(start, dataSize - 1)
    val safeMaxCount = maxCount.coerceAtLeast(2)
    val visibleCount = end - start + 1
    val effectiveVisibleCount =
        stableVisibleCount
            ?.takeIf { count -> count > 0 }
            ?.coerceIn(1, dataSize)
            ?: visibleCount
    val stride =
        ceil(((effectiveVisibleCount - 1).coerceAtLeast(1)).toFloat() / safeMaxCount.toFloat())
            .toInt()
            .coerceAtLeast(1)

    val firstIndex = (((start - stride).coerceAtLeast(0)) / stride) * stride
    val lastIndex = (end + stride).coerceAtMost(dataSize - 1)

    val indices = mutableListOf<Int>()
    var index = firstIndex
    while (index <= lastIndex) {
        indices.add(index)
        index += stride
    }

    if (start == 0 && indices.firstOrNull() != 0) indices.add(0, 0)
    if (end == dataSize - 1 && indices.lastOrNull() != dataSize - 1) indices.add(dataSize - 1)

    // Keep global scroll cadence, but avoid forcing dataset edge labels when the
    // current visible window does not include those edges.
    return indices
        .distinct()
        .filterNot { index ->
            (index == 0 && start > 0) || (index == dataSize - 1 && end < dataSize - 1)
        }
}

@InternalChartsApi
fun centeredLabelIndexRange(
    dataSize: Int,
    unitWidthPx: Float,
    viewportWidthPx: Float,
    scrollOffsetPx: Float,
    firstCenterPx: Float = 0f,
    labelWidthPx: Float = 0f,
    edgePaddingPx: Float = 0f,
): IntRange {
    if (dataSize <= 0 || unitWidthPx <= 0f || viewportWidthPx <= 0f) return IntRange.EMPTY

    val safeLabelHalfWidth = (labelWidthPx.coerceAtLeast(0f)) / 2f
    val safeEdgePadding = edgePaddingPx.coerceAtLeast(0f)
    val minCenterX = safeLabelHalfWidth + safeEdgePadding
    val maxCenterX = viewportWidthPx - safeLabelHalfWidth - safeEdgePadding
    if (maxCenterX < minCenterX) return IntRange.EMPTY

    val rawStart = ceil((minCenterX - firstCenterPx + scrollOffsetPx) / unitWidthPx).toInt()
    val rawEnd = floor((maxCenterX - firstCenterPx + scrollOffsetPx) / unitWidthPx).toInt()
    val start = rawStart.coerceIn(0, dataSize - 1)
    val end = rawEnd.coerceIn(0, dataSize - 1)
    if (end < start) return IntRange.EMPTY
    return start..end
}

@InternalChartsApi
fun baselineYForRange(
    minValue: Double,
    maxValue: Double,
    heightPx: Float,
): Float {
    if (heightPx <= 0f) return 0f
    val rangeValue = maxValue - minValue
    if (rangeValue == 0.0) {
        return if (maxValue < 0.0) 0f else heightPx
    }

    val normalizedZero = ((0.0 - minValue) / rangeValue).toFloat()
    val baseline = heightPx * (1f - normalizedZero)
    return baseline.coerceIn(0f, heightPx)
}
