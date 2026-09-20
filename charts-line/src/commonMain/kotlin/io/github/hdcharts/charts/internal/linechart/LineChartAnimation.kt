package io.github.hdcharts.charts.internal.linechart

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.internal.AnimationSpec
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

/**
 * Decides how the next line chart update is rendered.
 *
 * Timeline updates shift the previous window towards the current one. Both windows are normalized
 * with the current data range so the drawn line and the y-axis labels always describe the same
 * range, and the line follows the data down once large values leave the timeline window.
 *
 * Shifting only describes a window that advanced by one point. Data that was replaced in place
 * morphs instead, because sliding unrelated values across the plot and swapping them at the end
 * would animate a change that never happened.
 */
internal fun decideLineChartUpdate(
    previousRawSeries: List<List<Double>>?,
    currentRawSeries: List<List<Double>>,
    currentMinMax: Pair<Double, Double>,
    renderMode: LineChartRenderMode,
    animationDuration: Duration,
): LineChartTransitionMode {
    if (renderMode != LineChartRenderMode.Timeline || previousRawSeries == null) {
        return LineChartTransitionMode.Morph
    }

    if (!isTimelineAdvance(previous = previousRawSeries, current = currentRawSeries)) {
        return LineChartTransitionMode.Morph
    }

    return LineChartTransitionMode.TimelineShift(
        transitionData =
            TimelineTransitionData(
                previousSeries = previousRawSeries,
                currentSeries = currentRawSeries,
                minMax = currentMinMax,
            ),
        animationDuration = animationDuration,
    )
}

/**
 * Animation spec the line chart moves each point with when an update is not a timeline shift.
 *
 * A timeline update arrives once per update interval, so it settles within [animationDuration] like
 * a shift does. The longer default spec would be cancelled by the next update before it arrives,
 * leaving the line trailing the data it is supposed to be showing.
 */
internal fun lineChartValueAnimationSpec(
    renderMode: LineChartRenderMode,
    animationDuration: Duration,
): TweenSpec<Float> =
    when (renderMode) {
        LineChartRenderMode.Timeline ->
            TweenSpec(
                durationMillis = animationDuration.toTimelineDurationMillis(),
                delay = 0,
                easing = LinearEasing,
            )

        else -> AnimationSpec.lineChart()
    }

/**
 * Reports whether [current] is [previous] advanced by exactly one point in every series.
 *
 * A live timeline window drops its oldest point and appends a new one, so every remaining value is
 * carried over unchanged. Any other update replaced the window contents and cannot be drawn as a
 * shift.
 */
private fun isTimelineAdvance(
    previous: List<List<Double>>,
    current: List<List<Double>>,
): Boolean {
    if (previous.size != current.size) return false

    return previous.indices.all { index ->
        val previousValues = previous[index]
        val currentValues = current[index]
        when {
            previousValues.size != currentValues.size -> false
            previousValues.size < 2 -> false
            else ->
                previousValues.subList(1, previousValues.size) ==
                    currentValues.subList(0, currentValues.size - 1)
        }
    }
}

/**
 * Builds the normalized values drawn for each series while a timeline shift animates.
 *
 * The drawn window is the previous window followed by the newest point, so the line slides one step
 * to the left and ends on the current window.
 *
 * Only the oldest point can fall outside [minMax]: every other drawn value belongs to the current
 * window. That point is drawn flat against the plot edge for the length of the shift, because the
 * renderer keeps every value inside the plot. The artifact covers one horizontal step and only
 * appears when the leaving value is outside the current range.
 */
internal fun timelineShiftValues(
    previousSeries: List<List<Double>>,
    currentSeries: List<List<Double>>,
    minMax: Pair<Double, Double>,
): List<List<Float>> {
    val normalizedPrevious = normalizeSeriesByMinMax(series = previousSeries, minMax = minMax)
    val normalizedCurrent = normalizeSeriesByMinMax(series = currentSeries, minMax = minMax)

    return normalizedPrevious.mapIndexed { index, previousValues ->
        val newestValue = normalizedCurrent.getOrNull(index)?.lastOrNull()
        when {
            previousValues.isEmpty() || newestValue == null -> emptyList()
            else -> previousValues + newestValue
        }
    }
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

internal fun timelineStep(
    width: Float,
    pointsCount: Int,
): Float {
    if (pointsCount <= 1) return 0f
    return width / (pointsCount - 1)
}
