package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors
import io.github.dautovicharis.charts.internal.common.model.ChartDataItem
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.internal.validateBarData
import io.github.dautovicharis.charts.internal.validateLineData
import io.github.dautovicharis.charts.mock.MockTest
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.model.toChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.dautovicharis.charts.internal.common.model.toChartData as toInternalChartData

class DataValidationStringDataTest {
    @Test
    fun validateBarData_explicitlyParsedInvalidValue_validationErrorsPresent() {
        // Arrange
        val data =
            listOf("1.0", "invalid", "3.0")
                .map { it.toDoubleOrNull() ?: Double.NaN }
                .toChartData()

        // Act
        val validationErrors = validateBarData(data)

        // Assert
        val expectedError =
            ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(1)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(expectedError, validationErrors.first())
    }

    @Test
    fun validateLineData_stringDataWithInvalidValue_validationErrorsPresent() {
        // Arrange
        val dataSet =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem(
                            label = "Series",
                            item = listOf("2.0", "invalid").toInternalChartData(),
                        ),
                    ),
                title = TITLE,
            )
        val lineChartStyle =
            MockTest.mockLineChartStyle(lineColors = listOf(MockTest.colors.first()))

        // Act
        val validationErrors = validateLineData(dataSet, lineChartStyle)

        // Assert
        val expectedError =
            ValidationErrors.RULE_ITEM_POINT_NOT_NUMBER.format(0, 1)
        assertTrue(validationErrors.isNotEmpty())
        assertEquals(expectedError, validationErrors.first())
    }
}
