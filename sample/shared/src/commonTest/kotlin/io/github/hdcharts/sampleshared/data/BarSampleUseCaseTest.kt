package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val POINTS = 30

class BarSampleUseCaseTest {
    private val useCase = barSampleUseCase()

    @Test
    fun deterministic_withPoints_returnsSameBarsOnEveryCall() {
        val first = useCase.deterministic(points = POINTS)
        val second = useCase.deterministic(points = POINTS)

        assertEquals(expected = POINTS, actual = first.categories.size)
        assertEquals(expected = first, actual = second)
    }

    @Test
    fun deterministic_byDefault_hasNoNegativeBars() {
        val values =
            useCase
                .deterministic()
                .series
                .single()
                .values

        assertTrue(actual = values.all { it >= 0 }, message = "Unsigned data covers the all-positive case.")
    }

    @Test
    fun deterministic_whenSigned_hasNegativeAndPositiveBars() {
        val values =
            useCase
                .deterministic(signed = true)
                .series
                .single()
                .values

        assertTrue(
            actual = values.any { it < 0 } && values.any { it > 0 },
            message = "Signed data draws bars on both sides of zero.",
        )
    }
}
