package io.github.dautovicharis.charts.internal.barstackedchart

import io.github.dautovicharis.charts.internal.common.model.ChartDataItem
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.model.toChartData
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertEquals

class StackedBarDensityTest {
    @Test
    fun aggregateForCompactDensity_reducesBars_andPreservesSourceMapping() {
        val data =
            toInternal(
                chartData(
                    bars = 10,
                    segmentNames = listOf("S1", "S2"),
                ),
            )

        val render = aggregateForCompactDensity(data = data, targetBars = 5)

        assertEquals(expected = 5, actual = render.data.items.size)
        assertEquals(expected = 10, actual = render.sourceSize)
        assertEquals(expected = listOf(0, 2, 4, 6, 8), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 0, actual = render.resolveSourceIndex(0))
        assertEquals(expected = 4, actual = render.resolveSourceIndex(2))
        assertEquals(expected = 0, actual = render.resolveRenderIndex(1))
        assertEquals(expected = 2, actual = render.resolveRenderIndex(4))
    }

    @Test
    fun identityRenderData_returnsDirectIndexMapping() {
        val data =
            toInternal(
                chartData(
                    bars = 4,
                    segmentNames = listOf("S1", "S2"),
                ),
            )

        val render = identityRenderData(data)

        assertEquals(expected = 4, actual = render.data.items.size)
        assertEquals(expected = listOf(0, 1, 2, 3), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 3, actual = render.resolveSourceIndex(3))
        assertEquals(expected = 2, actual = render.resolveRenderIndex(2))
    }

    private fun chartData(
        bars: Int,
        segmentNames: List<String>,
    ): ChartData =
        chartDataOf(
            categories = List(bars) { index -> "Bar ${index + 1}" },
            *segmentNames
                .mapIndexed { segmentIndex, segmentName ->
                    ChartSeries(
                        name = segmentName,
                        values = List(bars) { index -> (index + segmentIndex + 1).toDouble() },
                    )
                }.toTypedArray(),
        )

    private fun toInternal(data: ChartData): MultiChartData {
        val segmentNames = data.series.map { series -> series.name.orEmpty() }
        return MultiChartData(
            items =
                data.categories.mapIndexed { barIndex, barLabel ->
                    ChartDataItem(
                        label = barLabel,
                        item =
                            data.series
                                .map { series -> series.values[barIndex] }
                                .toChartData(labels = segmentNames),
                    )
                },
            categories = segmentNames,
            title = "Stacked Bar",
        )
    }
}
