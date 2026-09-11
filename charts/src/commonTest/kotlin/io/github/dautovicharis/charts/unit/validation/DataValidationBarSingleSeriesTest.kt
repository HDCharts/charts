package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.internal.validateBarData
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.toChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataValidationBarSingleSeriesTest {
    @Test
    fun validateBarData_tooFewPoints_validationErrorsPresent() {
        val data = listOf(1.0).toChartData()

        val errors = validateBarData(data)

        val expectedError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_BAR)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun validateBarData_invalidColors_validationErrorsPresent() {
        val data = listOf(1.0, 2.0, 3.0).toChartData()

        val errors = validateBarData(data, colorsSize = 2)

        val expectedError = ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(2, 3)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun validateBarData_nonNumericValue_validationErrorsPresent() {
        val data = listOf(1.0, Double.NaN, 3.0).toChartData()

        val errors = validateBarData(data)

        val expectedError = ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(1)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun malformedSeriesCategoriesAndInfinities_areValidationErrors() {
        val inputs =
            listOf(
                ChartData(),
                ChartData(series = listOf(ChartSeries(values = emptyList()))),
                ChartData(
                    series = listOf(ChartSeries(values = listOf(1.0, 2.0)), ChartSeries(values = listOf(3.0, 4.0))),
                ),
                listOf(1.0, 2.0).toChartData(categories = listOf("A")),
                listOf(1.0, Double.POSITIVE_INFINITY).toChartData(),
                listOf(1.0, Double.NEGATIVE_INFINITY).toChartData(),
            )
        inputs.forEach { assertTrue(validateBarData(it).isNotEmpty()) }
        assertTrue(validateBarData(listOf(-Double.MAX_VALUE, Double.MAX_VALUE).toChartData()).isEmpty())
        assertTrue(validateBarData(listOf(0.0, 0.0).toChartData()).isEmpty())
    }
}
