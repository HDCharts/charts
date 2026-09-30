package io.github.hdcharts.charts.unit.validation

import io.github.hdcharts.bar.internal.validateBarData
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.format
import io.github.hdcharts.core.model.toChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
}
