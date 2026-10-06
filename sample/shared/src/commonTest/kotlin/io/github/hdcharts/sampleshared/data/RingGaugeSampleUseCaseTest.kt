package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val RINGS = 8

class RingGaugeSampleUseCaseTest {
    private val useCase = ringGaugeSampleUseCase()

    @Test
    fun deterministic_withRings_returnsThatManyRings() {
        assertEquals(
            expected = RINGS,
            actual =
                useCase
                    .deterministic(rings = RINGS)
                    .data.categories.size,
        )
    }

    @Test
    fun deterministic_whenSigned_hasValuesPastTheEndAndBelowZero() {
        val sample = useCase.deterministic(signed = true)
        val values =
            sample.data.series
                .single()
                .values

        assertTrue(actual = sample.rangeMin < 0)
        assertTrue(
            actual = values.any { it > sample.rangeMax },
            message = "A value past the end stops at the arc's end.",
        )
        assertTrue(actual = values.any { it < 0 })
    }

    @Test
    fun random_withRings_staysInsideTheRange() {
        val sample = useCase.random(rings = RINGS)
        val values =
            sample.data.series
                .single()
                .values

        assertEquals(expected = RINGS, actual = values.size)
        assertTrue(actual = values.all { it in sample.rangeMin..sample.rangeMax })
    }
}
