package io.github.hdcharts.core.internal.model

import kotlin.math.abs

/**
 * Normalizes [value] onto 0..1 from [minValue] to [maxValue], the form every chart
 * animation draws from. A zero range has no scale to divide by, so it resolves to
 * [zeroRangeValue].
 */
fun normalizeValue(
    value: Double,
    minValue: Double,
    maxValue: Double,
    zeroRangeValue: Float = 0f,
): Float {
    if (minValue == maxValue) return zeroRangeValue
    val clamped = value.coerceIn(minValue, maxValue)
    val range = maxValue - minValue
    val normalized =
        if (range.isFinite()) {
            (clamped - minValue) / range
        } else {
            // Finite bounds can have an infinite span. Scaling first keeps both differences finite.
            val scale = maxOf(abs(minValue), abs(maxValue))
            (clamped / scale - minValue / scale) / (maxValue / scale - minValue / scale)
        }
    return normalized.toFloat().coerceIn(0f, 1f)
}

fun ChartRenderData.normalizeByMinMax(
    minMax: Pair<Double, Double>,
    zeroRangeValue: Float,
): List<List<Float>> {
    val (minValue, maxValue) = minMax
    return series.map { item ->
        item.values.map { value -> normalizeValue(value, minValue, maxValue, zeroRangeValue) }
    }
}

/**
 * Applies optional [minValue]/[maxValue] overrides to a data-derived domain, independently. A
 * null override falls back to the corresponding data bound. If both bounds are explicit
 * overrides and they are equal or reversed, both bounds fall back to the data-derived domain
 * (the caller supplied a self-contradictory range). If only one bound is overridden and the
 * data-derived opposite bound crosses it, the override still wins and the opposite bound is
 * clamped to it, rather than discarding the override entirely. Shared by chart types whose fixed
 * range differs only in how the data-derived fallback domain ([dataMin], [dataMax]) is computed.
 */
fun resolveOptionalRange(
    dataMin: Double,
    dataMax: Double,
    minValue: Double?,
    maxValue: Double?,
): Pair<Double, Double> {
    val resolvedMin = minValue ?: dataMin
    val resolvedMax = maxValue ?: dataMax
    if (resolvedMax > resolvedMin) return resolvedMin to resolvedMax
    return when {
        minValue != null && maxValue != null -> dataMin to dataMax
        minValue != null -> resolvedMin to resolvedMin
        else -> resolvedMax to resolvedMax
    }
}

fun ChartRenderData.normalizeStackedValues(): List<Float> {
    val dataMax = seriesTotals.maxOrNull() ?: 0.0
    val range = if (dataMax == 0.0) 1.0 else dataMax
    return seriesTotals.map { total -> (total / range).toFloat().coerceIn(0f, 1f) }
}

fun ChartRenderData.normalizeStackedAreaValues(): List<List<Float>> {
    if (series.isEmpty()) return emptyList()
    val pointsCount = valueCount()
    if (pointsCount == 0) return series.map { emptyList() }

    val maxStackedTotal =
        (0 until pointsCount)
            .maxOfOrNull { pointIndex ->
                series.sumOf { item -> item.values[pointIndex] }
            } ?: 0.0
    val range = if (maxStackedTotal == 0.0) 1.0 else maxStackedTotal
    val runningTotals = DoubleArray(pointsCount)

    return series.map { item ->
        item.values.mapIndexed { pointIndex, value ->
            runningTotals[pointIndex] += value
            (runningTotals[pointIndex] / range).toFloat().coerceIn(0f, 1f)
        }
    }
}
