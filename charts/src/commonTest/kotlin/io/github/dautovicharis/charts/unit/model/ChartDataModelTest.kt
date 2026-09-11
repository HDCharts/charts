package io.github.dautovicharis.charts.unit.model

import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.toChartData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ChartDataModelTest {
    @Test
    fun doubleList_toChartData_singleSeriesWithoutCategories() {
        // Act
        val data = listOf(1.0, 2.0, 3.0).toChartData()

        // Assert
        assertContentEquals(actual = data.categories, expected = emptyList())
        assertEquals(actual = data.series.size, expected = 1)
        assertNull(actual = data.series[0].name)
        assertContentEquals(actual = data.series[0].values, expected = listOf(1.0, 2.0, 3.0))
    }

    @Test
    fun doubleList_toChartData_customCategoriesAndSeriesName() {
        // Act
        val data =
            listOf(24.0, 18.0).toChartData(
                categories = listOf("Product A", "Product B"),
                seriesName = "Sales",
            )

        // Assert
        assertContentEquals(actual = data.categories, expected = listOf("Product A", "Product B"))
        assertEquals(actual = data.series[0].name, expected = "Sales")
        assertContentEquals(actual = data.series[0].values, expected = listOf(24.0, 18.0))
    }

    @Test
    fun doubleList_toChartData_preservesPrecision() {
        val values = listOf(16_777_217.0, 1.23456789012345, Double.MAX_VALUE)

        val data = values.toChartData()

        assertContentEquals(expected = values, actual = data.series.single().values)
        assertContentEquals(expected = emptyList(), actual = data.categories)
    }

    @Test
    fun chartDataOf_sharedCategoriesWithMultipleSeries() {
        // Act
        val data =
            chartDataOf(
                categories = listOf("Jan", "Feb", "Mar"),
                ChartSeries(name = "A", values = listOf(1.0, 2.0, 3.0)),
                ChartSeries(name = "B", values = listOf(4.0, 5.0, 6.0)),
            )

        // Assert
        assertContentEquals(actual = data.categories, expected = listOf("Jan", "Feb", "Mar"))
        assertEquals(actual = data.series.size, expected = 2)
        assertEquals(actual = data.series[1].name, expected = "B")
        assertContentEquals(actual = data.series[1].values, expected = listOf(4.0, 5.0, 6.0))
    }

    @Test
    fun chartSeries_mutableList_isCopied() {
        // Arrange
        val mutable = mutableListOf(1.0, 2.0)
        val series = ChartSeries(values = mutable)

        // Act
        mutable.add(3.0)

        // Assert
        assertContentEquals(actual = series.values, expected = listOf(1.0, 2.0))
    }

    @Test
    fun chartData_mutableCategoriesAndSeries_areCopied() {
        val categories = mutableListOf("a", "b")
        val series = mutableListOf(ChartSeries(values = listOf(1.0, 2.0)))
        val data = ChartData(categories = categories, series = series)

        categories[0] = "changed"
        series.clear()

        assertContentEquals(expected = listOf("a", "b"), actual = data.categories)
        assertContentEquals(expected = listOf(1.0, 2.0), actual = data.series.single().values)
    }

    @Test
    fun toChartData_mutableInputs_areCopied() {
        val values = mutableListOf(1.0, 2.0)
        val categories = mutableListOf("a", "b")
        val data = values.toChartData(categories = categories)

        values[0] = 100.0
        categories.clear()

        assertContentEquals(expected = listOf(1.0, 2.0), actual = data.series.single().values)
        assertContentEquals(expected = listOf("a", "b"), actual = data.categories)
    }

    @Test
    fun emptyData_doesNotInventCategoriesOrValues() {
        val data = ChartData()
        val singleSeries = emptyList<Double>().toChartData()

        assertContentEquals(expected = emptyList(), actual = data.categories)
        assertContentEquals(expected = emptyList(), actual = data.series)
        assertContentEquals(expected = emptyList(), actual = singleSeries.categories)
        assertContentEquals(expected = emptyList(), actual = singleSeries.series.single().values)
    }

    @Test
    fun chartData_copy_acceptsImmutableCollections() {
        // Arrange
        val data = listOf(1.0, 2.0).toChartData(categories = listOf("a", "b"))

        // Act
        val copy =
            data.copy(
                categories = kotlinx.collections.immutable.persistentListOf("x", "y"),
            )

        // Assert
        assertContentEquals(actual = copy.categories, expected = listOf("x", "y"))
        assertContentEquals(actual = copy.series[0].values, expected = listOf(1.0, 2.0))
        assertSame(actual = data.series, expected = copy.series)
    }
}
