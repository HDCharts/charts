package io.github.hdcharts.charts.internal.barchart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.charts.internal.common.model.ChartData
import io.github.hdcharts.charts.internal.common.model.resolveOptionalRange
import kotlin.math.abs
import kotlin.math.max
import io.github.hdcharts.charts.internal.common.density.aggregateLabelsByCenterValue as aggregateLabelsByCenterValueCore
import io.github.hdcharts.charts.internal.common.density.bucketSizeForTarget as bucketSizeForTargetCore
import io.github.hdcharts.charts.internal.common.density.buildBucketRanges as buildBucketRangesCore
import io.github.hdcharts.charts.internal.common.density.shouldUseScrollableDensity as shouldUseScrollableDensityCore

internal const val BAR_DENSE_THRESHOLD = 50

internal fun getSelectedIndex(
    position: Offset,
    dataSize: Int,
    canvasSize: IntSize,
    spacingPx: Float,
): Int =
    getSelectedIndexForContentX(
        contentX = position.x,
        dataSize = dataSize,
        unitWidthPx = (canvasSize.width + spacingPx) / dataSize.coerceAtLeast(1),
    )

internal fun getSelectedIndexForContentX(
    contentX: Float,
    dataSize: Int,
    unitWidthPx: Float,
): Int = if (dataSize <= 0 || unitWidthPx <= 0f) 0 else (contentX / unitWidthPx).toInt().coerceIn(0, dataSize - 1)

internal fun shouldUseScrollableDensity(pointsCount: Int): Boolean =
    shouldUseScrollableDensityCore(
        pointsCount = pointsCount,
        threshold = BAR_DENSE_THRESHOLD,
    )

internal fun aggregateForCompactDensity(
    data: ChartData,
    targetPoints: Int = BAR_DENSE_THRESHOLD,
): ChartData {
    val sourcePointsCount = data.points.size
    if (sourcePointsCount <= targetPoints.coerceAtLeast(1)) return data

    val bucketRanges = compactDensityRanges(sourcePointsCount, targetPoints)
    val aggregatedPoints =
        bucketRanges.map { range ->
            val sum = range.sumOf { data.points[it] }
            if (sum.isFinite()) {
                sum / range.count()
            } else {
                val scale = range.maxOf { abs(data.points[it]) }
                (range.sumOf { data.points[it] / scale } / range.count()).coerceIn(-1.0, 1.0) * scale
            }
        }
    val aggregatedLabels = aggregateLabelsByCenterValueCore(data.labels, bucketRanges)
    return ChartData(aggregatedLabels.zip(aggregatedPoints))
}

internal fun compactDensityRanges(
    sourcePointsCount: Int,
    targetPoints: Int,
): List<IntRange> {
    if (sourcePointsCount <= 0) return emptyList()
    val bucketSize = bucketSizeForTargetCore(sourcePointsCount, targetPoints.coerceAtLeast(1))
    return buildBucketRangesCore(sourcePointsCount, bucketSize)
}

internal fun compactDensityCenterIndices(
    sourcePointsCount: Int,
    targetPoints: Int = BAR_DENSE_THRESHOLD,
): List<Int> {
    val bucketRanges = compactDensityRanges(sourcePointsCount, targetPoints)
    return bucketRanges.map { range ->
        range.first + ((range.last - range.first) / 2)
    }
}

internal fun maxBarsThatFit(
    viewportWidthPx: Float,
    spacingPx: Float,
    minBarWidthPx: Float,
): Int {
    val safeViewportWidthPx = max(1f, viewportWidthPx)
    val safeSpacingPx = spacingPx.coerceAtLeast(0f)
    val safeMinBarWidthPx = minBarWidthPx.coerceAtLeast(1f)
    val unitWidthPx = safeMinBarWidthPx + safeSpacingPx
    return ((safeViewportWidthPx + safeSpacingPx) / unitWidthPx).toInt().coerceAtLeast(1)
}

internal fun unitWidth(
    barWidthPx: Float,
    spacingPx: Float,
): Float = max(Float.MIN_VALUE, barWidthPx + spacingPx)

internal fun contentWidth(
    dataSize: Int,
    unitWidthPx: Float,
    spacingPx: Float,
): Float {
    if (dataSize <= 0) return 0f
    return max(1f, dataSize * unitWidthPx - spacingPx)
}

internal fun baselineYForRange(
    minValue: Double,
    maxValue: Double,
    heightPx: Float,
): Float = barValueYFraction(0.0, minValue, maxValue).toFloat() * heightPx.coerceAtLeast(0f)

/** One zero-inclusive source domain for bars, ticks and both density modes. */
internal fun ChartData.resolveBarRange(
    minValue: Double?,
    maxValue: Double?,
): Pair<Double, Double> {
    val autoMin = minOf(0.0, points.min())
    val autoMax = maxOf(0.0, points.max())
    val fallback = if (autoMin == autoMax) 0.0 to 1.0 else autoMin to autoMax
    return resolveOptionalRange(fallback.first, fallback.second, minValue, maxValue)
}

/** Overflow-safe mapping; Float conversion happens only at the drawing boundary. */
internal fun barValueYFraction(
    value: Double,
    min: Double,
    max: Double,
): Double {
    if (min == max) return if (max < 0.0) 0.0 else 1.0
    val clamped = value.coerceIn(min, max)
    val span = max - min
    return if (span.isFinite()) {
        (max - clamped) / span
    } else {
        val scale = maxOf(abs(min), abs(max))
        (max / scale - clamped / scale) / (max / scale - min / scale)
    }.coerceIn(0.0, 1.0)
}
