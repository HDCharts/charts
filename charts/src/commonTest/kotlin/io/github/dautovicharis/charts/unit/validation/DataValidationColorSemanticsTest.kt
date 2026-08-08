package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.validateBarData
import io.github.dautovicharis.charts.internal.validateLineData
import io.github.dautovicharis.charts.mock.MockTest.asymmetricMultiDataSet
import io.github.dautovicharis.charts.mock.MockTest.colorsAsymmetric
import io.github.dautovicharis.charts.mock.MockTest.mockLineChartStyle
import io.github.dautovicharis.charts.mock.MockTest.mockStackedBarChartStyle
import kotlin.test.Test
import kotlin.test.assertTrue

class DataValidationColorSemanticsTest {
    @Test
    fun lineChart_invalidColors_validationErrorsPresent() {
        val dataSet = asymmetricMultiDataSet
        val style = mockLineChartStyle(colorsAsymmetric.take(3))

        assertTrue(validateLineData(dataSet.data, style).isEmpty())
    }

    @Test
    fun stackedBarChart_invalidColors_validationErrorsPresent() {
        val dataSet = asymmetricMultiDataSet
        val style = mockStackedBarChartStyle(colorsAsymmetric.take(5))

        assertTrue(validateBarData(dataSet.data, style).isEmpty())
    }
}
