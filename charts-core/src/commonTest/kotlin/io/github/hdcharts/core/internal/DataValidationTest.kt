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

        assertTrue(
            validateSeries(
                data = data,
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = true,
                stacksValues = false,
                colorCount = 0,
                expectedColors = null,
            ).isEmpty(),
        )
    }

    @Test
    fun validateSeries_withoutSeries_reportsOnlyThat() {
        val data = chartDataOf(categories = emptyList())

        assertEquals(
            expected = listOf("At least one series is required."),
            actual =
                validateSeries(
                    data = data,
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = true,
                    stacksValues = false,
                    colorCount = 0,
                    expectedColors = null,
                ),
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
            actual =
                validateSeries(
                    data = data,
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = false,
                    stacksValues = false,
                    colorCount = 0,
                    expectedColors = null,
                ),
        )
    }

    @Test
    fun validateSeries_stackedTotalsThatOverflow_reportsTheFirstIndexWithOtherErrors() {
        val data =
            chartDataOf(
                categories = listOf("A"),
                ChartSeries(name = "One", values = listOf(Double.MAX_VALUE, 1.0, Double.MAX_VALUE)),
                ChartSeries(name = "Two", values = listOf(Double.MAX_VALUE, 2.0, Double.MAX_VALUE)),
            )

        assertEquals(
            expected =
                listOf(
                    "Category count (1) must match value count (3).",
                    "Stacked total at index 0 is not finite.",
                ),
            actual =
                validateSeries(
                    data = data,
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = false,
                    stacksValues = true,
                    colorCount = 0,
                    expectedColors = null,
                ),
        )
    }

    @Test
    fun validateSeries_overflowingTotalsWithoutStacking_returnsNoErrors() {
        val data =
            chartDataOf(
                categories = emptyList(),
                ChartSeries(name = "One", values = listOf(Double.MAX_VALUE, 1.0)),
                ChartSeries(name = "Two", values = listOf(Double.MAX_VALUE, 2.0)),
            )

        assertTrue(
            validateSeries(
                data = data,
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = false,
                stacksValues = false,
                colorCount = 0,
                expectedColors = null,
            ).isEmpty(),
        )
    }

    @Test
    fun validateSeries_allowsNegativeValuesWhenTheyAreAllowed() {
        val data = chartDataOf(categories = emptyList(), ChartSeries(name = "One", values = listOf(-1.0, 2.0)))

        assertTrue(
            validateSeries(
                data = data,
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = true,
                stacksValues = false,
                colorCount = 0,
                expectedColors = null,
            ).isEmpty(),
        )
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
            actual =
                validateSeries(
                    data = data,
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = true,
                    stacksValues = false,
                    colorCount = 3,
                    expectedColors = 2,
                ),
        )
    }

    /** A null expectation is how a chart whose color count depends on the data skips the check. */
    @Test
    fun validateSeries_withANullColorExpectation_skipsTheColorCheck() {
        val data =
            chartDataOf(
                categories = emptyList(),
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        assertTrue(
            validateSeries(
                data = data,
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = true,
                stacksValues = false,
                colorCount = 7,
                expectedColors = null,
            ).isEmpty(),
        )
    }

    @Test
    fun validateSingleSeries_reportsShapeProblemsFirstAndAlone() {
        assertEquals(
            expected = listOf("Exactly one series is required; got 0."),
            actual =
                validateSingleSeries(
                    data = ChartData(),
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = true,
                    colorCount = 0,
                ),
        )
        assertEquals(
            expected = listOf("Exactly one series is required; got 2."),
            actual =
                validateSingleSeries(
                    data =
                        ChartData(
                            series = listOf(ChartSeries(values = listOf(1.0, 2.0)), ChartSeries(values = listOf(3.0))),
                        ),
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = true,
                    colorCount = 0,
                ),
        )
        assertEquals(
            expected = listOf("At least 2 values are required."),
            actual =
                validateSingleSeries(
                    data = listOf(1.0).toChartData(),
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = true,
                    colorCount = 0,
                ),
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
            actual =
                validateSingleSeries(
                    data = data,
                    minValues = ValidationErrors.MIN_VALUES,
                    colorCount = 2,
                    allowNegative = false,
                ),
        )
    }

    @Test
    fun validateSingleSeries_acceptsZerosNegativesAndExtremeFiniteValuesWhenNegativesAreAllowed() {
        assertTrue(
            validateSingleSeries(
                data = listOf(-Double.MAX_VALUE, Double.MAX_VALUE).toChartData(),
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = true,
                colorCount = 0,
            ).isEmpty(),
        )
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
            actual = validateValues(values = listOf(-1.0, Double.NaN), allowNegative = true),
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
