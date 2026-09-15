package io.github.hdcharts.charts.internal.common.model

fun MultiChartData.normalizeByMinMax(
    minMax: Pair<Double, Double>,
    zeroRangeValue: Float,
): List<List<Float>> {
    val (minValue, maxValue) = minMax
    val range = maxValue - minValue
    return items.map { item ->
        item.item.points.map { value ->
            when (range) {
                0.0 -> zeroRangeValue
                else -> ((value - minValue) / range).toFloat().coerceIn(0f, 1f)
            }
        }
    }
}

fun ChartData.normalizeBarValues(
    minValue: Double,
    maxValue: Double,
    useFixedRange: Boolean,
): List<Float> {
    val rangeValue = maxValue - minValue
    if (rangeValue == 0.0) {
        return points.map { value ->
            when {
                value > 0.0 -> 1f
                value < 0.0 -> -1f
                else -> 0f
            }
        }
    }
    val allPositive = minValue >= 0.0
    val allNegative = maxValue <= 0.0
    return points.map { value ->
        val clamped = value.coerceIn(minValue, maxValue)
        when {
            allPositive ->
                if (useFixedRange) {
                    ((clamped - minValue) / rangeValue).toFloat()
                } else {
                    (clamped / maxValue).toFloat()
                }
            allNegative ->
                if (useFixedRange) {
                    ((clamped - maxValue) / rangeValue).toFloat()
                } else {
                    (clamped / kotlin.math.abs(minValue)).toFloat()
                }
            else -> (clamped / rangeValue).toFloat()
        }
    }
}

fun ChartData.resolveBarRange(
    minValue: Float?,
    maxValue: Float?,
): Pair<Double, Double> {
    val dataMin = points.min()
    val dataMax = points.max()
    val resolvedMin = minValue?.toDouble() ?: dataMin
    val resolvedMax = maxValue?.toDouble() ?: dataMax

    return if (resolvedMax <= resolvedMin) {
        dataMin to dataMax
    } else {
        resolvedMin to resolvedMax
    }
}

fun MultiChartData.normalizeStackedValues(): List<Float> {
    val dataMax = items.maxOfOrNull { it.item.points.sum() } ?: 0.0
    val range = if (dataMax == 0.0) 1.0 else dataMax
    return items.map { item ->
        (item.item.points.sum() / range).toFloat().coerceIn(0f, 1f)
    }
}

fun MultiChartData.normalizeStackedAreaValues(): List<List<Float>> {
    if (items.isEmpty()) return emptyList()
    val pointsCount = getFirstPointsSize()
    if (pointsCount == 0) return items.map { emptyList() }

    val maxStackedTotal =
        (0 until pointsCount)
            .maxOfOrNull { pointIndex ->
                items.sumOf { it.item.points[pointIndex] }
            } ?: 0.0
    val range = if (maxStackedTotal == 0.0) 1.0 else maxStackedTotal
    val runningTotals = DoubleArray(pointsCount)

    return items.map { item ->
        item.item.points.mapIndexed { pointIndex, value ->
            runningTotals[pointIndex] += value
            (runningTotals[pointIndex] / range).toFloat().coerceIn(0f, 1f)
        }
    }
}
