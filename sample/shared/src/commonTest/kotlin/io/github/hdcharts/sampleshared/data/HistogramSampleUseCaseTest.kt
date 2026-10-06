package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals

private const val DENSE_BINS = 150

class HistogramSampleUseCaseTest {
    private val useCase = histogramSampleUseCase()

    @Test
    fun deterministic_withBins_returnsSameCountsOnEveryCall() {
        val first = useCase.deterministic(bins = DENSE_BINS)

        assertEquals(expected = DENSE_BINS, actual = first.categories.size)
        assertEquals(expected = first, actual = useCase.deterministic(bins = DENSE_BINS))
    }
}
