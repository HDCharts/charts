package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val POINTS = 90

class LineSampleUseCaseTest {
    private val useCase = lineSampleUseCase()

    @Test
    fun deterministic_withPoints_returnsSameLineOnEveryCall() {
        val first = useCase.deterministic(points = POINTS)

        assertEquals(expected = POINTS, actual = first.categories.size)
        assertEquals(expected = first, actual = useCase.deterministic(points = POINTS))
    }

    @Test
    fun deterministic_whenSigned_crossesZero() {
        val values =
            useCase
                .deterministic(signed = true)
                .series
                .single()
                .values

        assertTrue(actual = values.any { it < 0 } && values.any { it > 0 })
    }
}
