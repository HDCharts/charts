package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals

private const val SERIES = 4

class RadarSampleUseCaseTest {
    private val useCase = radarSampleUseCase()

    @Test
    fun random_withSeries_keepsDeterministicNames() {
        val fixed = useCase.deterministic(series = SERIES)
        val random = useCase.random(series = SERIES)

        assertEquals(expected = SERIES, actual = fixed.seriesKeys.size)
        assertEquals(expected = fixed.seriesKeys, actual = random.seriesKeys)
        assertEquals(expected = fixed.data.categories, actual = random.data.categories)
    }
}
