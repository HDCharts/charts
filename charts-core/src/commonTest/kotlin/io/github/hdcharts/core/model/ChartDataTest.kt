package io.github.hdcharts.core.model

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ChartDataTest {
    private data class Nutrient(
        val name: String,
        val calories: Double,
    )

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
    fun domainObjects_toChartData_mapsValuesAndCategoriesInItemOrder() {
        val nutrients =
            listOf(
                Nutrient("Carbs", 240.0),
                Nutrient("Protein", 180.0),
                Nutrient("Fat", 320.0),
            )

        val data = nutrients.toChartData(value = Nutrient::calories, category = Nutrient::name)

        assertContentEquals(expected = listOf("Carbs", "Protein", "Fat"), actual = data.categories)
        assertContentEquals(expected = listOf(240.0, 180.0, 320.0), actual = data.series.single().values)
        assertNull(actual = data.series.single().name)
    }

    @Test
    fun domainObjects_toChartData_supportsSeriesNameAndDuplicateCategories() {
        val data =
            listOf(Nutrient("Other", 1.0), Nutrient("Other", 2.0)).toChartData(
                value = { it.calories },
                category = { it.name },
                seriesName = "Energy",
            )

        assertContentEquals(expected = listOf("Other", "Other"), actual = data.categories)
        assertContentEquals(expected = listOf(1.0, 2.0), actual = data.series.single().values)
        assertEquals(expected = "Energy", actual = data.series.single().name)
    }

    @Test
    fun domainObjects_toChartData_emptyListProducesEmptySingleSeries() {
        val data =
            emptyList<Nutrient>().toChartData(
                value = { it.calories },
                category = { it.name },
            )

        assertContentEquals(expected = emptyList(), actual = data.categories)
        assertContentEquals(expected = emptyList(), actual = data.series.single().values)
    }

    @Test
    fun domainObjects_toChartData_supportsOtherItemTypesAndPreservesInvalidValues() {
        data class Reading(
            val timestamp: String,
            val value: Double,
        )

        val data =
            listOf(
                Reading("first", Double.NaN),
                Reading("second", 2.0),
            ).toChartData(
                value = { it.value },
                category = { it.timestamp },
            )

        assertContentEquals(expected = listOf("first", "second"), actual = data.categories)
        assertEquals(
            expected = true,
            actual =
                data.series
                    .single()
                    .values
                    .first()
                    .isNaN(),
        )
        assertEquals(expected = 2.0, actual = data.series.single().values[1])
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
    fun toChartData_pairs_buildsMultiSeriesChartData() {
        val data =
            listOf(
                "Revenue" to listOf(10.0, 20.0),
                "Costs" to listOf(4.0, 8.0),
            ).toChartData(categories = listOf("Jan", "Feb"))

        assertContentEquals(actual = data.categories, expected = listOf("Jan", "Feb"))
        assertEquals(actual = data.series.size, expected = 2)
        assertEquals(actual = data.series[0].name, expected = "Revenue")
        assertContentEquals(actual = data.series[0].values, expected = listOf(10.0, 20.0))
        assertEquals(actual = data.series[1].name, expected = "Costs")
        assertContentEquals(actual = data.series[1].values, expected = listOf(4.0, 8.0))
    }

    @Test
    fun toChartData_pairs_emptyCategories_usesEmptyLabels() {
        val data = listOf("Revenue" to listOf(10.0, 20.0)).toChartData()

        assertContentEquals(actual = data.categories, expected = emptyList())
        assertContentEquals(actual = data.series.single().values, expected = listOf(10.0, 20.0))
    }

    @Test
    fun toChartData_pairs_raggedSeriesValues_passThrough() {
        val data =
            listOf(
                "Revenue" to listOf(10.0, 20.0),
                "Costs" to listOf(4.0),
            ).toChartData(categories = listOf("Jan", "Feb"))

        assertContentEquals(actual = data.series[0].values, expected = listOf(10.0, 20.0))
        assertContentEquals(actual = data.series[1].values, expected = listOf(4.0))
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
