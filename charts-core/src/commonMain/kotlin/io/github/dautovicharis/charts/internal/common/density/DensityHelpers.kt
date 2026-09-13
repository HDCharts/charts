package io.github.dautovicharis.charts.internal.common.density

import io.github.dautovicharis.charts.internal.InternalChartsApi
import kotlin.math.ceil

@InternalChartsApi
const val DEFAULT_DENSE_THRESHOLD = 50

@InternalChartsApi
fun shouldUseScrollableDensity(
    pointsCount: Int,
    threshold: Int = DEFAULT_DENSE_THRESHOLD,
): Boolean = pointsCount >= threshold

@InternalChartsApi
fun bucketSizeForTarget(
    totalPoints: Int,
    targetPoints: Int,
): Int {
    if (totalPoints <= 0 || targetPoints <= 0) return 0
    return ceil(totalPoints.toDouble() / targetPoints.toDouble()).toInt().coerceAtLeast(1)
}

@InternalChartsApi
fun buildBucketRanges(
    totalPoints: Int,
    bucketSize: Int,
): List<IntRange> {
    if (totalPoints <= 0 || bucketSize <= 0) return emptyList()
    val ranges = mutableListOf<IntRange>()
    var start = 0
    while (start < totalPoints) {
        val endExclusive = minOf(start + bucketSize, totalPoints)
        ranges += start until endExclusive
        start = endExclusive
    }
    return ranges
}

@InternalChartsApi
fun bucketCenterIndex(range: IntRange): Int = range.first + ((range.last - range.first) / 2)

@InternalChartsApi
fun aggregatePointsByAverage(
    sourcePoints: List<Double>,
    bucketRanges: List<IntRange>,
): List<Double> {
    if (bucketRanges.isEmpty()) return emptyList()
    return bucketRanges.map { range ->
        var sum = 0.0
        var count = 0
        for (index in range) {
            sum += sourcePoints[index]
            count++
        }
        if (count == 0) 0.0 else sum / count.toDouble()
    }
}

@InternalChartsApi
fun aggregateLabelsByLastValue(
    sourceLabels: List<String>,
    bucketRanges: List<IntRange>,
): List<String> {
    if (bucketRanges.isEmpty()) return emptyList()
    return bucketRanges.mapIndexed { bucketIndex, range ->
        sourceLabels.getOrNull(range.last) ?: "Bucket ${bucketIndex + 1}"
    }
}

@InternalChartsApi
fun aggregateLabelsByCenterValue(
    sourceLabels: List<String>,
    bucketRanges: List<IntRange>,
): List<String> {
    if (bucketRanges.isEmpty()) return emptyList()
    return bucketRanges.mapIndexed { bucketIndex, range ->
        val centerIndex = bucketCenterIndex(range)
        sourceLabels.getOrNull(centerIndex)
            ?: sourceLabels.getOrNull(range.last)
            ?: "Bucket ${bucketIndex + 1}"
    }
}
