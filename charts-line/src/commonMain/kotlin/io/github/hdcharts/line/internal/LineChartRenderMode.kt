package io.github.hdcharts.line.internal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import io.github.hdcharts.core.internal.AnimationSpec
import kotlin.time.Duration

/**
 * The shared contract the line chart's animations agree on: how a chart is rendered, how one update
 * moves between two data sets, and how long each move takes.
 *
 * The animations themselves live in their own files, one per kind of movement:
 * [LineChartRevealWindow] for the reveal on first display, [LineChartMorphAnimation] for values
 * moving between two data sets, and [LineChartTimelineShiftData] for a live window sliding.
 */
internal const val MIN_TIMELINE_DURATION_MS = 1

/** How a line chart renders an update: values morph in place, or a live window slides sideways. */
internal sealed interface LineChartRenderMode {
    data object Morph : LineChartRenderMode

    data class Timeline(
        val shiftDuration: Duration,
    ) : LineChartRenderMode
}

/** The movement one update makes. */
internal sealed interface LineChartTransitionMode {
    data object Morph : LineChartTransitionMode

    data class Timeline(
        val shiftData: LineChartTimelineShiftData,
    ) : LineChartTransitionMode
}

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
 *
 * [isAdvance] comes from the timeline window counter, which already compared the window against the
 * one before it, so the comparison is not repeated here. The counter remembers the window pair from
 * the last data change, so a re-run of this decision for a window the chart has already drawn -
 * because the range or the shift duration changed - keeps [isAdvance] from that pair. Only a window
 * the chart has not drawn yet can shift.
 */
internal fun decideLineChartUpdate(
    previousRawSeries: List<List<Double>>?,
    currentRawSeries: List<List<Double>>,
    currentMinMax: Pair<Double, Double>,
    renderMode: LineChartRenderMode,
    isAdvance: Boolean,
): LineChartTransitionMode {
    if (
        renderMode !is LineChartRenderMode.Timeline ||
        previousRawSeries == null ||
        !isAdvance ||
        previousRawSeries === currentRawSeries
    ) {
        return LineChartTransitionMode.Morph
    }

    return LineChartTransitionMode.Timeline(
        shiftData =
            LineChartTimelineShiftData(
                previousSeries = previousRawSeries,
                currentSeries = currentRawSeries,
                minMax = currentMinMax,
            ),
    )
}

/** Spec for both timeline shifts and point morphs; Timeline uses shiftDuration for both. */
internal fun lineChartValueAnimationSpec(renderMode: LineChartRenderMode): TweenSpec<Float> =
    when (renderMode) {
        is LineChartRenderMode.Timeline ->
            TweenSpec(
                durationMillis = renderMode.shiftDuration.toTimelineDurationMillis(),
                delay = 0,
                easing = LinearEasing,
            )

        LineChartRenderMode.Morph -> AnimationSpec.lineChart()
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
