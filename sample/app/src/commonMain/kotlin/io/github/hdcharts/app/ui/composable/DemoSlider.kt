package io.github.hdcharts.app.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.sampleshared.theme.Dimens
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Horizontal room kept on both sides of a demo slider.
 *
 * A Material slider centers its thumb on the ends of its track, so half the thumb sits outside the
 * track at either extreme. Without this inset the thumb rests against the edge of the screen, where
 * a finger covers the edge instead of the thumb and the slider is hard to drag away from its
 * minimum or maximum.
 */
private val SLIDER_EDGE_PADDING: Dp = Dimens.md

/** Largest number of intervals a demo slider is divided into, so its tick marks stay readable. */
private const val MAX_SLIDER_INTERVALS = 25

/** Increments a demo slider may snap to, smallest first, so every stop reads as a round number. */
private val SLIDER_INCREMENTS = intArrayOf(1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 500, 1000)

/**
 * Discrete stops a demo slider offers across [range].
 *
 * The stops are whole numbers spaced by the smallest round increment that keeps the slider within
 * [MAX_SLIDER_INTERVALS] intervals, so the demos snap to readable values instead of drifting
 * through every number in a five hundred point range. The last stop is always [IntRange.last], so
 * the maximum stays selectable even when the span is not a multiple of the increment.
 */
internal fun sliderStops(range: IntRange): List<Int> {
    val span = range.last - range.first
    if (span <= 0) return listOf(range.first)

    val increment =
        SLIDER_INCREMENTS.firstOrNull { increment -> span <= increment * MAX_SLIDER_INTERVALS }
            ?: (span / MAX_SLIDER_INTERVALS).coerceAtLeast(1)

    val stops = (range.first..range.last step increment).toMutableList()
    if (stops.last() != range.last) {
        stops += range.last
    }
    return stops
}

/** Index of the stop closest to [value], so a stored value outside the stops still selects one. */
internal fun List<Int>.nearestStopIndex(value: Int): Int =
    indices.minByOrNull { index -> abs(this[index] - value) } ?: 0

/**
 * Labelled slider over the whole numbers in [range].
 *
 * The slider snaps to the stops of [sliderStops] and reports the selected value once the drag ends,
 * so a demo regenerates its data on release instead of on every pixel of movement. [label] renders
 * the value being dragged, which keeps the caption in step with the thumb before the value is
 * committed.
 */
@Composable
fun DemoSlider(
    value: Int,
    range: IntRange,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (Int) -> Unit,
) {
    val stops = remember(range) { sliderStops(range) }
    var draftIndex by
        remember(value, stops) { mutableFloatStateOf(stops.nearestStopIndex(value).toFloat()) }
    val draftValue = stops[draftIndex.roundToInt().coerceIn(stops.indices)]

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        label(draftValue)
        Slider(
            modifier = Modifier.padding(horizontal = SLIDER_EDGE_PADDING),
            value = draftIndex,
            valueRange = 0f..stops.lastIndex.toFloat().coerceAtLeast(1f),
            steps = (stops.size - 2).coerceAtLeast(0),
            onValueChange = { index -> draftIndex = index },
            onValueChangeFinished = { onValueSelected(draftValue) },
        )
    }
}

/**
 * Labelled range slider over the whole numbers in [range].
 *
 * Both thumbs snap to the stops of [sliderStops] and the selected bounds are reported once the drag
 * ends, matching [DemoSlider].
 */
@Composable
fun DemoRangeSlider(
    start: Int,
    end: Int,
    range: IntRange,
    onRangeSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (Int, Int) -> Unit,
) {
    val stops = remember(range) { sliderStops(range) }
    var draftIndices by
        remember(start, end, stops) {
            mutableStateOf(
                stops.nearestStopIndex(start).toFloat()..stops.nearestStopIndex(end).toFloat(),
            )
        }
    val draftStart = stops[draftIndices.start.roundToInt().coerceIn(stops.indices)]
    val draftEnd = stops[draftIndices.endInclusive.roundToInt().coerceIn(stops.indices)]

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        label(draftStart, draftEnd)
        RangeSlider(
            modifier = Modifier.padding(horizontal = SLIDER_EDGE_PADDING),
            value = draftIndices,
            valueRange = 0f..stops.lastIndex.toFloat().coerceAtLeast(1f),
            steps = (stops.size - 2).coerceAtLeast(0),
            onValueChange = { indices -> draftIndices = indices },
            onValueChangeFinished = { onRangeSelected(draftStart, draftEnd) },
        )
    }
}
