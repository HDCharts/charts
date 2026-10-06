package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals

private const val SLICES = 4

class PieSampleUseCaseTest {
    private val useCase = pieSampleUseCase()

    @Test
    fun deterministic_withSlices_returnsSameSlicesOnEveryCall() {
        val first = useCase.deterministic(slices = SLICES)
        val second = useCase.deterministic(slices = SLICES)

        assertEquals(expected = SLICES, actual = first.data.categories.size)
        assertEquals(expected = first, actual = second)
    }

    @Test
    fun random_withSlices_keepsDeterministicLabels() {
        val fixed = useCase.deterministic(slices = SLICES)
        val random = useCase.random(slices = SLICES)

        assertEquals(
            expected = fixed.data.categories,
            actual = random.data.categories,
            message = "A refresh changes the values only, so the slices keep their names.",
        )
    }
}
