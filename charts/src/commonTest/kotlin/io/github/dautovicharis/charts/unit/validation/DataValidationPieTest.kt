package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_PIE
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NEGATIVE
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NOT_FINITE
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
                PieSlice(label = "A", value = 1.0),
                PieSlice(label = "B", value = 2.0),
            )

        val validationErrors = validatePieData(slices)

        assertTrue(validationErrors.isEmpty())
    }

    @Test
    fun validatePieData_tooFewSlices_validationErrorsPresent() {
        val slices = listOf(PieSlice(label = "A", value = 10.0))

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
                PieSlice(label = "A", value = 1.0),
                PieSlice(label = "B", value = Double.NaN),
                PieSlice(label = "C", value = 3.0),
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
                PieSlice(label = "A", value = 1.0),
                PieSlice(label = "B", value = -2.0),
                PieSlice(label = "C", value = 3.0),
            )

        val validationErrors = validatePieData(slices)

        val expectedError = RULE_DATA_POINT_NEGATIVE.format(1)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(validationErrors.first(), expectedError)
    }

    @Test
    fun validatePieData_positiveInfinityValue_validationErrorsPresent() {
        val slices =
            listOf(
                PieSlice(label = "A", value = 1.0),
                PieSlice(label = "B", value = Double.POSITIVE_INFINITY),
            )

        val validationErrors = validatePieData(slices)

        val expectedError = RULE_DATA_POINT_NOT_FINITE.format(1)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(expectedError, validationErrors.first())
    }

    @Test
    fun validatePieData_negativeInfinityValue_validationErrorsPresent() {
        val slices =
            listOf(
                PieSlice(label = "A", value = 1.0),
                PieSlice(label = "B", value = Double.NEGATIVE_INFINITY),
            )

        val validationErrors = validatePieData(slices)

        val expectedError = RULE_DATA_POINT_NOT_FINITE.format(1)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(expectedError, validationErrors.first())
    }

    @Test
    fun validatePieData_allZeroValues_noValidationErrors() {
        val slices =
            listOf(
                PieSlice(label = "A", value = 0.0),
                PieSlice(label = "B", value = 0.0),
            )

        val validationErrors = validatePieData(slices)

        assertTrue(validationErrors.isEmpty())
    }
}
