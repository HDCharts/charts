package io.github.hdcharts.app.ui.composable

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val MAX_READABLE_STOPS = 27

class DemoSliderTest {
    @Test
    fun sliderStops_forAWholeIncrementRange_spacesStopsEvenly() {
        assertEquals(
            expected = listOf(0, 100, 200, 300, 400, 500),
            actual = sliderStops(0..2000).take(6),
        )
    }

    @Test
    fun sliderStops_forEveryDemoRange_staysReadableAndReachesBothEnds() {
        val demoRanges =
            listOf(
                1..500,
                2..500,
                10..500,
                -500..500,
                0..500,
                50..400,
                0..2000,
                200..2000,
                10..120,
                2..120,
            )

        demoRanges.forEach { range ->
            val stops = sliderStops(range)

            assertEquals(
                expected = range.first,
                actual = stops.first(),
                message = "The minimum of $range must stay selectable.",
            )
            assertEquals(
                expected = range.last,
                actual = stops.last(),
                message = "The maximum of $range must stay selectable.",
            )
            assertTrue(
                stops.size <= MAX_READABLE_STOPS,
                "A slider over $range draws a tick per stop, but it has ${stops.size} of them.",
            )
            assertTrue(
                stops.zipWithNext().all { (current, next) -> next > current },
                "The stops of $range must increase, but they are $stops.",
            )
        }
    }

    @Test
    fun sliderStops_forAnEmptyRange_returnsTheSingleValue() {
        assertEquals(expected = listOf(7), actual = sliderStops(7..7))
    }

    @Test
    fun nearestStopIndex_forAValueBetweenStops_picksTheClosestStop() {
        val stops = sliderStops(0..500)

        assertEquals(expected = 0, actual = stops.nearestStopIndex(-40))
        assertEquals(expected = 1, actual = stops.nearestStopIndex(23))
        assertEquals(expected = stops.lastIndex, actual = stops.nearestStopIndex(9_000))
    }
}
