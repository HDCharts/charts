package io.github.hdcharts.core.internal

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.toChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataValidationTest {
    @Test
    fun validateSeries_withValidData_returnsNoErrors() {
        val data =
            chartDataOf(
                categories = listOf("A", "B"),
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        assertTrue(validateSeries(data = data, minValues = ValidationErrors.MIN_VALUES).isEmpty())
    }

    @Test
    fun validateSeries_withoutSeries_reportsOnlyThat() {
        val data = chartDataOf(categories = emptyList())

        assertEquals(
            expected = listOf("At least one series is required."),
            actual = validateSeries(data = data, minValues = ValidationErrors.MIN_VALUES),
        )
    }

    @Test
    fun validateSeries_reportsEveryDataProblem() {
        val data =
            chartDataOf(
                categories = listOf("A", "B", "C"),
                ChartSeries(name = "One", values = listOf(1.0)),
                ChartSeries(name = "Two", values = listOf(Double.NaN, -1.0)),
            )

        assertEquals(
            expected =
                listOf(
                    "At least 2 values are required.",
                    "Category count (3) must match value count (1).",
                    "Series 1 is not aligned with the first series.",
                    "Series 1 contains a non-finite value.",
                    "Series 1 contains a negative value.",
                ),
            actual = validateSeries(data = data, minValues = ValidationErrors.MIN_VALUES, allowNegative = false),
        )
    }

    @Test
    fun validateSeries_allowsNegativeValuesByDefault() {
        val data = chartDataOf(categories = emptyList(), ChartSeries(name = "One", values = listOf(-1.0, 2.0)))

        assertTrue(validateSeries(data = data, minValues = ValidationErrors.MIN_VALUES).isEmpty())
    }

    @Test
    fun messages_nameTheProblemAndTheValues() {
        assertEquals(expected = "Exactly one series is required; got 2.", actual = ValidationErrors.exactlyOneSeries(2))
        assertEquals(
            expected = "Color count (2) must match value count (3).",
            actual = ValidationErrors.colorCountMismatch(colors = 2, expected = 3, target = "value"),
        )
        assertEquals(expected = "Value at index 1 is not finite.", actual = ValidationErrors.nonFiniteValue(1))
        assertEquals(expected = "Value at index 1 is negative.", actual = ValidationErrors.negativeValue(1))
    }

    @Test
    fun validateSeries_withColorCountMismatch_reportsColorError() {
        val data =
            chartDataOf(
                categories = emptyList(),
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        assertEquals(
            expected = listOf("Color count (3) must match series count (2)."),
            actual = validateSeries(data = data, minValues = ValidationErrors.MIN_VALUES, colorCount = 3),
        )
    }

    @Test
    fun validateSingleSeries_reportsShapeProblemsFirstAndAlone() {
        assertEquals(
            expected = listOf("Exactly one series is required; got 0."),
            actual = validateSingleSeries(data = ChartData()),
        )
        assertEquals(
            expected = listOf("Exactly one series is required; got 2."),
            actual =
                validateSingleSeries(
                    data =
                        ChartData(
                            series = listOf(ChartSeries(values = listOf(1.0, 2.0)), ChartSeries(values = listOf(3.0))),
                        ),
                ),
        )
        assertEquals(
            expected = listOf("At least 2 values are required."),
            actual = validateSingleSeries(data = listOf(1.0).toChartData()),
        )
    }

    @Test
    fun validateSingleSeries_reportsColorsCategoriesAndEachBadValue() {
        val data = listOf(1.0, Double.NaN, -3.0).toChartData(categories = listOf("A", "B"))

        assertEquals(
            expected =
                listOf(
                    "Color count (2) must match value count (3).",
                    "Category count (2) must match value count (3).",
                    "Value at index 1 is not finite.",
                    "Value at index 2 is negative.",
                ),
            actual = validateSingleSeries(data = data, colorCount = 2, allowNegative = false),
        )
    }

    @Test
    fun validateSingleSeries_acceptsZerosNegativesAndExtremeFiniteValuesByDefault() {
        assertTrue(validateSingleSeries(data = listOf(0.0, 0.0).toChartData()).isEmpty())
        assertTrue(validateSingleSeries(data = listOf(-Double.MAX_VALUE, Double.MAX_VALUE).toChartData()).isEmpty())
    }

    @Test
    fun validateValues_reportsEveryNonFiniteValueAndNegativesWhenNotAllowed() {
        val values = listOf(1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -2.0, 0.0)

        assertEquals(
            expected =
                listOf(
                    "Value at index 1 is not finite.",
                    "Value at index 2 is not finite.",
                    "Value at index 3 is not finite.",
                    "Value at index 4 is negative.",
                ),
            actual = validateValues(values = values, allowNegative = false),
        )
        assertEquals(
            expected = listOf("Value at index 1 is not finite."),
            actual = validateValues(values = listOf(-1.0, Double.NaN)),
        )
    }

    @Test
    fun validateRange_rejectsOnlyNonFiniteBounds() {
        assertTrue(validateRange(min = null, max = null).isEmpty())
        assertTrue(validateRange(min = -1.0, max = 1.0).isEmpty())
        assertEquals(
            expected = listOf("Range bounds must be finite."),
            actual = validateRange(min = 0.0, max = Double.POSITIVE_INFINITY),
        )
        assertEquals(
            expected = listOf("Range bounds must be finite."),
            actual = validateRange(min = Double.NaN, max = null),
        )
    }
}
