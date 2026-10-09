package io.github.hdcharts.line.internal

import io.github.hdcharts.core.internal.model.normalizeValue

/** The two windows either side of a live shift, and the range they are drawn against. */
internal data class LineChartTimelineShiftData(
    val previousSeries: List<List<Double>>,
    val currentSeries: List<List<Double>>,
    val minMax: Pair<Double, Double>,
) {
    /**
     * Normalized values drawn for each series while the shift animates.
     *
     * The shift data is rebuilt once per update, so the values are normalized here instead of on
     * every animation frame.
     */
    val drawValues: List<List<Float>> =
        timelineShiftValues(
            previousSeries = previousSeries,
            currentSeries = currentSeries,
            minMax = minMax,
        )
}

/**
 * Counts the points a live timeline window has dropped since its data was last replaced, so X-axis
 * labels can stay on their points while the window slides.
 */
internal class TimelineWindowCounter {
    private var previousSeries: List<List<Double>>? = null
    private var droppedPoints = 0L

    /** Takes [series] as the next window and returns how many points it has dropped in total. */
    fun next(series: List<List<Double>>): Long {
        // Composition can be discarded and run again for the same window, which would count the drop
        // twice. remember rebuilds the window list, so the window is compared by value and the count
        // this window already reached is returned again.
        if (series == previousSeries) return droppedPoints

        val previous = previousSeries
        val isAdvance = previous != null && isTimelineAdvance(previous, series)
        droppedPoints = if (isAdvance) droppedPoints + 1 else 0L
        previousSeries = series

        return droppedPoints
    }
}

/**
 * Reports whether [current] is [previous] advanced by exactly one point in every series.
 *
 * A live timeline window drops its oldest point and appends a new one, so every remaining value is
 * carried over unchanged. Any other update replaced the window contents and cannot be drawn as a
 * shift.
 */
internal fun isTimelineAdvance(
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
            else -> {
                // Compared by index: a sublist comparison would allocate two views per series on
                // every update.
                var carried = true
                for (pointIndex in 1 until previousValues.size) {
                    if (previousValues[pointIndex] != currentValues[pointIndex - 1]) {
                        carried = false
                        break
                    }
                }
                carried
            }
        }
    }
}

/**
 * Builds the normalized values drawn for each series while a timeline shift animates.
 *
 * The drawn window is the previous window followed by the newest point, so the line slides one step
 * to the left and ends on the current window.
 *
 * When [minMax] is fully data-derived, only the oldest point can fall outside it: every other
 * drawn value belongs to the current window, and that point is drawn flat against the plot edge
 * for the length of the shift because the renderer keeps every value inside the plot. That
 * artifact covers one horizontal step and only appears when the leaving value is outside the
 * current range. If [minMax] instead comes from a fixed range narrower than the live data, more
 * than one drawn point can sit outside it for as long as the data keeps exceeding those bounds.
 */
internal fun timelineShiftValues(
    previousSeries: List<List<Double>>,
    currentSeries: List<List<Double>>,
    minMax: Pair<Double, Double>,
): List<List<Float>> {
    val (minValue, maxValue) = minMax

    return previousSeries.mapIndexed { index, previousValues ->
        // Only the newest point of the current window is drawn, so only that one is normalized.
        val newestValue =
            currentSeries
                .getOrNull(index)
                ?.lastOrNull()
                ?.let { value -> normalizeValue(value, minValue, maxValue) }
        when {
            previousValues.isEmpty() || newestValue == null -> emptyList()
            else ->
                List(previousValues.size + 1) { position ->
                    when {
                        position < previousValues.size -> normalizeValue(previousValues[position], minValue, maxValue)
                        else -> newestValue
                    }
                }
        }
    }
}

/** The distance the line covers in one shift. */
internal fun timelineStep(
    width: Float,
    pointsCount: Int,
): Float {
    if (pointsCount <= 1) return 0f
    return width / (pointsCount - 1)
}

/** Copies [source] into [into] scaled by [scaleBy] and reports how many values it copied. */
internal fun copyInto(
    source: List<Float>,
    into: FloatArray,
    scaleBy: Float = 1f,
): Int {
    val count = minOf(into.size, source.size)
    for (index in 0 until count) {
        into[index] = source[index] * scaleBy
    }
    return count
}
