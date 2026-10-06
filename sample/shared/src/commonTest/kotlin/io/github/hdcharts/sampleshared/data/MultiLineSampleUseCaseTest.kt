package io.github.hdcharts.sampleshared.data

import kotlin.test.Test
import kotlin.test.assertEquals

private const val DAYS = 365

class MultiLineSampleUseCaseTest {
    private val useCase = multiLineSampleUseCase()

    @Test
    fun deterministic_byDefault_returnsTheMonths() {
        assertEquals(
            expected = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
            actual = useCase.deterministic().dataSet.categories,
        )
    }

    @Test
    fun deterministic_withDays_returnsSameDaysOnEveryCall() {
        val first = useCase.deterministic(points = DAYS)

        assertEquals(expected = DAYS, actual = first.dataSet.categories.size)
        assertEquals(expected = first, actual = useCase.deterministic(points = DAYS))
    }
}
