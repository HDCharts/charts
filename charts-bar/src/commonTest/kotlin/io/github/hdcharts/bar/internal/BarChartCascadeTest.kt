package io.github.hdcharts.bar.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarChartCascadeTest {
    @Test
    fun progressAt_startAndEnd_areZeroAndOneForEveryBar() {
        val cascade = BarChartCascade(barCount = 30)

        repeat(30) { index ->
            assertEquals(expected = 0f, actual = cascade.progressAt(pointIndex = index, progress = 0f))
            assertEquals(expected = 1f, actual = cascade.progressAt(pointIndex = index, progress = 1f))
        }
    }

    @Test
    fun progressAt_fewBars_laterBarsLagBehind() {
        val cascade = BarChartCascade(barCount = 30)

        val first = cascade.progressAt(pointIndex = 0, progress = 0.3f)
        val last = cascade.progressAt(pointIndex = 29, progress = 0.3f)

        assertTrue(first > last, "The first bar has to lead: first=$first, last=$last")
    }

    @Test
    fun progressAt_manyBars_movesEveryBarTogether() {
        val cascade = BarChartCascade(barCount = 500)

        assertEquals(
            expected = cascade.progressAt(pointIndex = 0, progress = 0.3f),
            actual = cascade.progressAt(pointIndex = 499, progress = 0.3f),
        )
    }
}
