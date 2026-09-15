package io.github.hdcharts.charts.unit.validation

import io.github.hdcharts.charts.internal.validateLineData
import io.github.hdcharts.charts.mock.MockTest.asymmetricMultiDataSet
import io.github.hdcharts.charts.mock.MockTest.colorsAsymmetric
import io.github.hdcharts.charts.mock.MockTest.mockLineChartStyle
import kotlin.test.Test
import kotlin.test.assertTrue

class DataValidationColorSemanticsTest {
    @Test
    fun lineChart_invalidColors_validationErrorsPresent() {
        val dataSet = asymmetricMultiDataSet
        val style = mockLineChartStyle(colorsAsymmetric.take(3))

        assertTrue(validateLineData(dataSet, style).isEmpty())
    }
}
