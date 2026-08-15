package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_PIE
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NEGATIVE
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NOT_NUMBER
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.internal.validatePieData
import io.github.dautovicharis.charts.model.PieSlice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataValidationPieTest {
    @Test
    fun validatePieData_validSlices_noValidationErrors() {
        val slices =
            listOf(
                PieSlice(label = "A", value = 1f),
                PieSlice(label = "B", value = 2f),
            )

        val validationErrors = validatePieData(slices)

        assertTrue(validationErrors.isEmpty())
    }

    @Test
    fun validatePieData_tooFewSlices_validationErrorsPresent() {
        val slices = listOf(PieSlice(label = "A", value = 10f))

        val validationErrors = validatePieData(slices)

        val expectedError =
            RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_PIE)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(validationErrors.first(), expectedError)
    }

    @Test
    fun validatePieData_nanValue_validationErrorsPresent() {
        val slices =
            listOf(
                PieSlice(label = "A", value = 1f),
                PieSlice(label = "B", value = Float.NaN),
                PieSlice(label = "C", value = 3f),
            )

        val validationErrors = validatePieData(slices)

        val expectedError = RULE_DATA_POINT_NOT_NUMBER.format(1)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(validationErrors.first(), expectedError)
    }

    @Test
    fun validatePieData_negativeValue_validationErrorsPresent() {
        val slices =
            listOf(
                PieSlice(label = "A", value = 1f),
                PieSlice(label = "B", value = -2f),
                PieSlice(label = "C", value = 3f),
            )

        val validationErrors = validatePieData(slices)

        val expectedError = RULE_DATA_POINT_NEGATIVE.format(1)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(validationErrors.first(), expectedError)
    }
}
