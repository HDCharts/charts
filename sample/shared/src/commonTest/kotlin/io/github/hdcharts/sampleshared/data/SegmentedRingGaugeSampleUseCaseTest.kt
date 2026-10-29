package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SegmentedRingGaugeSampleUseCaseTest {
    @Test
    fun deterministic_returnsDistinctContributionsWithinTheCalorieCapacity() {
        val sample = segmentedRingGaugeSampleUseCase().deterministic()
        val values =
            sample.data.series
                .single()
                .values

        assertEquals(expected = "Daily Nutrition", actual = sample.title)
        assertEquals(expected = listOf("Carbs", "Protein", "Fat"), actual = sample.data.categories)
        assertEquals(expected = 740.0, actual = values.sum())
        assertTrue(actual = values.sum() < sample.capacity)
    }
}
