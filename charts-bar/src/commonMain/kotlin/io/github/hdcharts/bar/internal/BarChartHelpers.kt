package io.github.hdcharts.bar.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.core.internal.density.aggregateLabelsByCenterValue
import io.github.hdcharts.core.internal.density.bucketSizeForTarget
import io.github.hdcharts.core.internal.density.buildBucketRanges
import io.github.hdcharts.core.internal.density.shouldUseScrollableDensity
import io.github.hdcharts.core.internal.model.resolveOptionalRange
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import kotlin.math.abs
import kotlin.math.max

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
    shouldUseScrollableDensity(
        pointsCount = pointsCount,
        threshold = BAR_DENSE_THRESHOLD,
    )

/**
 * The same data with neighbouring bars averaged into [targetPoints] buckets, for compact dense mode.
 *
 * Rebuilds the caller's own model, so the compacted chart reads exactly what an uncompacted one does.
 */
internal fun aggregateForCompactDensity(
    data: ChartData,
    targetPoints: Int = BAR_DENSE_THRESHOLD,
): ChartData {
    val values = data.barValues
    val sourcePointsCount = values.size
    if (sourcePointsCount <= targetPoints.coerceAtLeast(1)) return data

    val bucketRanges = compactDensityRanges(sourcePointsCount, targetPoints)
    val aggregatedPoints =
        bucketRanges.map { range ->
            val sum = range.sumOf { values[it] }
            if (sum.isFinite()) {
                sum / range.count()
            } else {
                val scale = range.maxOf { abs(values[it]) }
                (range.sumOf { values[it] / scale } / range.count()).coerceIn(-1.0, 1.0) * scale
            }
        }
    return ChartData(
        categories = aggregateLabelsByCenterValue(data.categories, bucketRanges),
        series = listOf(ChartSeries(name = data.series.single().name, values = aggregatedPoints)),
    )
}

/**
 * The one series bar and histogram draw, as values.
 *
 * Both policies declare `singleSeries`, so validation has already checked there is exactly one, and
 * every read below the seam can take the values without re-checking that.
 */
internal val ChartData.barValues: List<Double> get() = series.single().values

internal fun compactDensityRanges(
    sourcePointsCount: Int,
    targetPoints: Int,
): List<IntRange> {
    if (sourcePointsCount <= 0) return emptyList()
    val bucketSize = bucketSizeForTarget(sourcePointsCount, targetPoints.coerceAtLeast(1))
    return buildBucketRanges(sourcePointsCount, bucketSize)
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
    val values = barValues
    val autoMin = minOf(0.0, values.min())
    val autoMax = maxOf(0.0, values.max())
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
