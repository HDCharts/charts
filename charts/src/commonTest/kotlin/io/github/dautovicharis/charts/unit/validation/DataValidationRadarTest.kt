package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_RADAR
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.internal.validateRadarData
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.colors
import io.github.dautovicharis.charts.mock.MockTest.invalidDataSetCategories
import io.github.dautovicharis.charts.mock.MockTest.mockRadarChartStyle
import io.github.dautovicharis.charts.mock.MockTest.multiDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataValidationRadarTest {
    @Test
    fun `validate radar data with valid data set has no validation errors`() {
        val errors = validateRadarData(multiDataSet.data, mockRadarChartStyle())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate radar data with invalid categories has validation errors`() {
        val dataSet = invalidDataSetCategories()
        val expectedCategoriesSize =
            dataSet.data.items
                .first()
                .item.points.size

        val errors = validateRadarData(dataSet.data, mockRadarChartStyle())

        val expectedError =
            ValidationErrors.RULE_CATEGORIES_SIZE_MISMATCH.format(
                dataSet.data.categories.size,
                expectedCategoriesSize,
            )
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun `validate radar data with invalid colors has validation errors`() {
        val dataSet = multiDataSet
        val style = mockRadarChartStyle(colors.drop(2))

        val errors = validateRadarData(dataSet.data, style)

        val expectedError =
            ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(
                colors.drop(2).size,
                dataSet.data.items.size,
            )
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun `validate radar data with too few points has validation errors`() {
        val dataSet =
            listOf("A" to listOf(1f, 2f))
                .toMultiChartDataSet(title = TITLE)

        val errors = validateRadarData(dataSet.data, mockRadarChartStyle())

        val expectedError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_RADAR)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun `validate radar data with non numeric value has validation errors`() {
        val dataSet =
            listOf("A" to listOf("1.0", "NaN", "3.0"))
                .toMultiChartDataSet(title = TITLE)

        val errors = validateRadarData(dataSet.data, mockRadarChartStyle(colors.take(1)))

        val expectedError =
            ValidationErrors.RULE_ITEM_POINT_NOT_NUMBER.format(0, 1)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }

    @Test
    fun `validate radar data with mismatched item sizes has validation errors`() {
        val dataSet =
            listOf(
                "A" to listOf(1f, 2f, 3f),
                "B" to listOf(1f, 2f),
            ).toMultiChartDataSet(title = TITLE)

        val errors = validateRadarData(dataSet.data, mockRadarChartStyle())

        val expectedError =
            ValidationErrors.RULE_ITEM_POINTS_SIZE.format(1, 2, 3)
        assertTrue(errors.isNotEmpty())
        assertEquals(expectedError, errors.first())
    }
}
