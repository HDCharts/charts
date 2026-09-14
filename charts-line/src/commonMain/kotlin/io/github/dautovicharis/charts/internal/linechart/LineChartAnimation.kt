package io.github.dautovicharis.charts.internal.linechart

import io.github.dautovicharis.charts.LineChartRenderMode
import kotlin.time.Duration

internal fun hasSameSeriesStructure(
    previous: List<List<Double>>,
    current: List<List<Double>>,
): Boolean {
    if (previous.size != current.size) return false
    return previous.indices.all { index -> previous[index].size == current[index].size }
}

/**
 * Converts this [Duration] to an integer millisecond value safe for Compose animation specs.
 *
 * Clamps the result between [MIN_TIMELINE_DURATION_MS] and [Int.MAX_VALUE] to prevent non-positive
 * timeline shift durations and integer overflow on very large or infinite durations.
 */
internal fun Duration.toTimelineDurationMillis(): Int =
    inWholeMilliseconds
        .coerceIn(MIN_TIMELINE_DURATION_MS.toLong(), Int.MAX_VALUE.toLong())
        .toInt()

internal fun decideLineChartUpdate(
    previousRawSeries: List<List<Double>>?,
    currentRawSeries: List<List<Double>>,
    currentMinMax: Pair<Double, Double>,
    previousTimelineRenderMinMax: Pair<Double, Double>?,
    renderMode: LineChartRenderMode,
    animationDuration: Duration,
): LineChartUpdateDecision {
    if (renderMode != LineChartRenderMode.Timeline) {
        return LineChartUpdateDecision(
            normalizationMinMax = null,
            nextTimelineRenderMinMax = null,
            mode = LineChartTransitionMode.Morph,
        )
    }

    val candidateMinMax =
        when (previousRawSeries != null) {
            true -> minMaxForSeries(previousRawSeries, currentRawSeries)
            false -> currentMinMax
        }
    val stabilizedMinMax =
        when (previousTimelineRenderMinMax) {
            null -> candidateMinMax
            else -> expandMinMax(previousTimelineRenderMinMax, candidateMinMax)
        }

    val mode =
        when {
            previousRawSeries != null -> {
                LineChartTransitionMode.TimelineShift(
                    transitionData =
                        TimelineTransitionData(
                            previousSeries = previousRawSeries,
                            currentSeries = currentRawSeries,
                            minMax = stabilizedMinMax,
                        ),
                    animationDuration = animationDuration,
                )
            }

            else -> LineChartTransitionMode.Morph
        }

    return LineChartUpdateDecision(
        normalizationMinMax = stabilizedMinMax,
        nextTimelineRenderMinMax = stabilizedMinMax,
        mode = mode,
    )
}

internal fun normalizeSeriesByMinMax(
    series: List<List<Double>>,
    minMax: Pair<Double, Double>,
): List<List<Float>> {
    val (minValue, maxValue) = minMax
    val range = maxValue - minValue
    return series.map { values ->
        values.map { value ->
            when (range) {
                0.0 -> 0f
                else -> ((value - minValue) / range).toFloat().coerceIn(0f, 1f)
            }
        }
    }
}

private fun minMaxForSeries(
    first: List<List<Double>>,
    second: List<List<Double>>,
): Pair<Double, Double> {
    var minValue = Double.POSITIVE_INFINITY
    var maxValue = Double.NEGATIVE_INFINITY

    fun consume(values: List<List<Double>>) {
        values.forEach { series ->
            series.forEach { value ->
                if (!value.isFinite()) return@forEach
                minValue = minOf(minValue, value)
                maxValue = maxOf(maxValue, value)
            }
        }
    }

    consume(first)
    consume(second)

    return if (!minValue.isFinite() || !maxValue.isFinite()) {
        0.0 to 0.0
    } else {
        minValue to maxValue
    }
}

private fun expandMinMax(
    base: Pair<Double, Double>,
    candidate: Pair<Double, Double>,
): Pair<Double, Double> = minOf(base.first, candidate.first) to maxOf(base.second, candidate.second)

internal fun timelineStep(
    width: Float,
    pointsCount: Int,
): Float {
    if (pointsCount <= 1) return 0f
    return width / (pointsCount - 1)
}
