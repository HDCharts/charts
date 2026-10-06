package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals

class StackedSampleUseCaseTest {
    @Test
    fun stackedBarDeterministic_atAnyCount_endsInQ4Of2025() {
        listOf(8, 64).forEach { points ->
            val sample = stackedBarSampleUseCase().deterministic(points = points)

            assertEquals(expected = points, actual = sample.dataSet.categories.size)
            assertEquals(expected = "Q4 '25", actual = sample.dataSet.categories.last())
        }
    }

    @Test
    fun stackedAreaDeterministic_atAnyCount_endsInDecemberOf2025() {
        listOf(24, 72).forEach { points ->
            val sample = stackedAreaSampleUseCase().deterministic(points = points)

            assertEquals(expected = points, actual = sample.data.categories.size)
            assertEquals(expected = "Dec '25", actual = sample.data.categories.last())
        }
    }
}
