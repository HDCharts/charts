package io.github.hdcharts.stackedarea.internal

import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertEquals

class StackedAreaDensityTest {
    @Test
    fun aggregateForCompactDensity_reducesPoints_andPreservesSourceMapping() {
        val render = aggregateForCompactDensity(data = seriesMajorData(points = 10), targetPoints = 5)

        assertEquals(
            expected = 5,
            actual =
                render.data.series
                    .first()
                    .values.size,
        )
        assertEquals(expected = listOf("P1", "P3", "P5", "P7", "P9"), actual = render.data.categories.toList())
        assertEquals(expected = listOf(0, 2, 4, 6, 8), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 0, actual = render.resolveSourceIndex(0))
        assertEquals(expected = 6, actual = render.resolveSourceIndex(3))
        assertEquals(expected = 1, actual = render.resolveRenderIndex(2))
        assertEquals(expected = 3, actual = render.resolveRenderIndex(7))
    }

    @Test
    fun aggregateForCompactDensity_onePointCapacity_createsOneSourceBucket() {
        val render = aggregateForCompactDensity(data = seriesMajorData(points = 10), targetPoints = 1)

        assertEquals(
            expected = 1,
            actual =
                render.data.series
                    .first()
                    .values.size,
        )
        assertEquals(expected = listOf("P5"), actual = render.data.categories.toList())
        assertEquals(expected = listOf(4), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 4, actual = render.resolveSourceIndex(0))
        assertEquals(expected = 0, actual = render.resolveRenderIndex(9))
    }

    @Test
    fun identityRenderData_returnsDirectIndexMapping() {
        val render = identityRenderData(seriesMajorData(points = 4))

        assertEquals(expected = 4, actual = render.sourcePointsCount)
        assertEquals(expected = listOf(0, 1, 2, 3), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 3, actual = render.resolveSourceIndex(3))
        assertEquals(expected = 2, actual = render.resolveRenderIndex(2))
    }

    /** Stacked area draws its series as given, so its render model is the caller's own data. */
    @Test
    fun withoutCategories_aggregating_leavesThemEmptyRatherThanInventingThem() {
        val data = seriesMajorData(points = 10, categories = emptyList())

        val render = aggregateForCompactDensity(data = data, targetPoints = 5)

        assertEquals(
            expected = 5,
            actual =
                render.data.series
                    .first()
                    .values.size,
        )
        assertEquals(expected = emptyList(), actual = render.data.categories.toList())
    }

    /** The caller's data: one series per band, one category per point. */
    private fun seriesMajorData(
        points: Int,
        categories: List<String> = List(points) { index -> "P${index + 1}" },
    ): MultiChartData =
        MultiChartData(
            data =
                chartDataOf(
                    categories = categories,
                    ChartSeries(name = "Series A", values = List(points) { index -> (index + 1).toDouble() }),
                    ChartSeries(name = "Series B", values = List(points) { index -> (index + 10).toDouble() }),
                ),
            title = "",
        )
}
