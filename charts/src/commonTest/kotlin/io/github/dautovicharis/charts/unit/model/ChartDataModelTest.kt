package io.github.dautovicharis.charts.unit.model

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
    fun floatList_toChartData_singleSeriesWithIndexCategories() {
        // Act
        val data = listOf(1.0f, 2.0f, 3.0f).toChartData()

        // Assert
        assertContentEquals(actual = data.categories, expected = listOf("0", "1", "2"))
        assertEquals(actual = data.series.size, expected = 1)
        assertNull(actual = data.series[0].name)
        assertContentEquals(actual = data.series[0].values, expected = listOf(1.0f, 2.0f, 3.0f))
    }

    @Test
    fun floatList_toChartData_customCategoriesAndSeriesName() {
        // Act
        val data =
            listOf(24.0f, 18.0f).toChartData(
                categories = listOf("Product A", "Product B"),
                seriesName = "Sales",
            )

        // Assert
        assertContentEquals(actual = data.categories, expected = listOf("Product A", "Product B"))
        assertEquals(actual = data.series[0].name, expected = "Sales")
        assertContentEquals(actual = data.series[0].values, expected = listOf(24.0f, 18.0f))
    }

    @Test
    fun chartDataOf_sharedCategoriesWithMultipleSeries() {
        // Act
        val data =
            chartDataOf(
                categories = listOf("Jan", "Feb", "Mar"),
                ChartSeries(name = "A", values = listOf(1.0f, 2.0f, 3.0f)),
                ChartSeries(name = "B", values = listOf(4.0f, 5.0f, 6.0f)),
            )

        // Assert
        assertContentEquals(actual = data.categories, expected = listOf("Jan", "Feb", "Mar"))
        assertEquals(actual = data.series.size, expected = 2)
        assertEquals(actual = data.series[1].name, expected = "B")
        assertContentEquals(actual = data.series[1].values, expected = listOf(4.0f, 5.0f, 6.0f))
    }

    @Test
    fun chartSeries_mutableList_isCopied() {
        // Arrange
        val mutable = mutableListOf(1.0f, 2.0f)
        val series = ChartSeries(values = mutable)

        // Act
        mutable.add(3.0f)

        // Assert
        assertContentEquals(actual = series.values, expected = listOf(1.0f, 2.0f))
    }

    @Test
    fun chartData_copy_acceptsImmutableCollections() {
        // Arrange
        val data = listOf(1.0f, 2.0f).toChartData(categories = listOf("a", "b"))

        // Act
        val copy =
            data.copy(
                categories = kotlinx.collections.immutable.persistentListOf("x", "y"),
            )

        // Assert
        assertContentEquals(actual = copy.categories, expected = listOf("x", "y"))
        assertContentEquals(actual = copy.series[0].values, expected = listOf(1.0f, 2.0f))
        assertSame(actual = data.series, expected = copy.series)
    }
}
