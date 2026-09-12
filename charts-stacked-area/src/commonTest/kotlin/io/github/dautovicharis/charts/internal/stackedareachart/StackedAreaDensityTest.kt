package io.github.dautovicharis.charts.internal.stackedareachart

import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertEquals

class StackedAreaDensityTest {
    @Test
    fun aggregateForCompactDensity_reducesPoints_andPreservesSourceMapping() {
        val points = 10
        val data =
            chartDataOf(
                categories = List(points) { index -> "P${index + 1}" },
                ChartSeries(name = "Series A", values = List(points) { index -> (index + 1).toDouble() }),
                ChartSeries(name = "Series B", values = List(points) { index -> (index + 10).toDouble() }),
            )

        val render = aggregateForCompactDensity(data = toInternalData(data), targetPoints = 5)

        assertEquals(
            expected = 5,
            actual =
                render.data.items
                    .first()
                    .item.points.size,
        )
        assertEquals(expected = listOf("P1", "P3", "P5", "P7", "P9"), actual = render.data.categories)
        assertEquals(expected = listOf(0, 2, 4, 6, 8), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 0, actual = render.resolveSourceIndex(0))
        assertEquals(expected = 6, actual = render.resolveSourceIndex(3))
        assertEquals(expected = 1, actual = render.resolveRenderIndex(2))
        assertEquals(expected = 3, actual = render.resolveRenderIndex(7))
    }

    @Test
    fun identityRenderData_returnsDirectIndexMapping() {
        val points = 4
        val data =
            chartDataOf(
                categories = List(points) { index -> "P${index + 1}" },
                ChartSeries(name = "Series A", values = List(points) { index -> (index + 1).toDouble() }),
                ChartSeries(name = "Series B", values = List(points) { index -> (index + 10).toDouble() }),
            )

        val render = identityRenderData(toInternalData(data))

        assertEquals(expected = 4, actual = render.sourcePointsCount)
        assertEquals(expected = listOf(0, 1, 2, 3), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 3, actual = render.resolveSourceIndex(3))
        assertEquals(expected = 2, actual = render.resolveRenderIndex(2))
    }

    private fun toInternalData(data: io.github.dautovicharis.charts.model.ChartData) =
        io.github.dautovicharis.charts.internal.common.model.MultiChartData(
            items =
                data.series.map { series ->
                    io.github.dautovicharis.charts.internal.common.model.ChartDataItem(
                        label = series.name.orEmpty(),
                        item =
                            io.github.dautovicharis.charts.internal.common.model.ChartData(
                                series.values.mapIndexed { index, value ->
                                    data.categories.getOrNull(index).orEmpty() to value
                                },
                            ),
                    )
                },
            categories = data.categories,
            title = "",
        )
}
