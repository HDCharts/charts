package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.common.model.ChartDataType
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.internal.validateBarData
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataValidationBarSingleSeriesTest {
    @Test
    fun validateBarData_tooFewPoints_validationErrorsPresent() {
        val data = listOf(1f).toChartDataSet(title = TITLE).data.item

        val errors = validateBarData(data)

        val expectedError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_BAR)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun validateBarData_invalidColors_validationErrorsPresent() {
        val data = listOf(1f, 2f, 3f).toChartDataSet(title = TITLE).data.item

        val errors = validateBarData(data, colorsSize = 2)

        val expectedError = ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(2, 3)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun validateBarData_nonNumericValue_validationErrorsPresent() {
        val dataSet =
            ChartDataSet(
                items = ChartDataType.StringData(listOf("1.0", "NaN", "3.0")),
                title = TITLE,
            )

        val errors = validateBarData(dataSet.data.item)

        val expectedError = ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(1)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }
}
