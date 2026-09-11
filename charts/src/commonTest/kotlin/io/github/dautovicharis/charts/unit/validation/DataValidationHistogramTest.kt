package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.internal.validateHistogramData
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.toChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataValidationHistogramTest {
    @Test
    fun validateHistogramData_validDataSet_noValidationErrors() {
        val data = listOf(1.0, 2.0, 3.0).toChartData()
        assertTrue(validateHistogramData(data).isEmpty())
    }

    @Test
    fun validateHistogramData_tooFewPoints_validationErrorsPresent() {
        val data = listOf(1.0).toChartData()

        val errors = validateHistogramData(data)

        val expectedError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_BAR)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun validateHistogramData_negativeValue_validationErrorsPresent() {
        val data = listOf(1.0, -2.0, 3.0).toChartData()

        val errors = validateHistogramData(data)

        val expectedError = ValidationErrors.RULE_DATA_POINT_NEGATIVE.format(1)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun validateHistogramData_invalidColors_validationErrorsPresent() {
        val data = listOf(1.0, 2.0, 3.0).toChartData()

        val errors = validateHistogramData(data, colorsSize = 2)

        val expectedError = ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(2, 3)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun validateHistogramData_nonNumericValue_validationErrorsPresent() {
        val data = listOf(1.0, Double.NaN, 3.0).toChartData()

        val errors = validateHistogramData(data)

        val expectedError = ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(1)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun histogramShapeAndFiniteValues_areValidatedWithoutThrowing() {
        assertTrue(validateHistogramData(ChartData()).isNotEmpty())
        assertTrue(validateHistogramData(listOf(0.0, 0.5).toChartData(categories = listOf("0-10", "300ms+"))).isEmpty())
        assertTrue(validateHistogramData(listOf(0.0, 0.0).toChartData()).isEmpty())
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertTrue(validateHistogramData(listOf(1.0, value).toChartData()).isNotEmpty())
        }
        assertTrue(validateHistogramData(listOf(1.0, 2.0).toChartData(categories = listOf("A"))).isNotEmpty())
    }
}
