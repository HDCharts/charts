package io.github.hdcharts.stackedbar.internal

import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Stacked bar's own conversion is what these exercise: [StackedBarChartSpec.convert] turns the
 * caller's series-major data into the per-bar model the chart draws, so the fixtures below go
 * through the spec rather than restating the transpose.
 */
@OptIn(InternalChartsApi::class)
class StackedBarDensityTest {
    @Test
    fun aggregateForCompactDensity_reducesBars_andPreservesSourceMapping() {
        val data = stackedBarModel(bars = 10)

        val render = aggregateForCompactDensity(data = data, targetBars = 5)

        assertEquals(expected = 5, actual = render.data.series.size)
        assertEquals(expected = 10, actual = render.sourceSize)
        assertEquals(expected = listOf(0, 2, 4, 6, 8), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 0, actual = render.resolveSourceIndex(0))
        assertEquals(expected = 4, actual = render.resolveSourceIndex(2))
        assertEquals(expected = 0, actual = render.resolveRenderIndex(1))
        assertEquals(expected = 2, actual = render.resolveRenderIndex(4))
    }

    @Test
    fun aggregateForCompactDensity_oneBarCapacity_createsOneSourceBucket() {
        val data = stackedBarModel(bars = 10)

        val render = aggregateForCompactDensity(data = data, targetBars = 1)

        assertEquals(expected = 1, actual = render.data.series.size)
        assertEquals(expected = listOf(0..9), actual = render.bucketRanges)
        assertEquals(expected = listOf(4), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 4, actual = render.resolveSourceIndex(0))
        assertEquals(expected = 0, actual = render.resolveRenderIndex(9))
    }

    @Test
    fun identityRenderData_returnsDirectIndexMapping() {
        val data = stackedBarModel(bars = 4)

        val render = identityRenderData(data)

        assertEquals(expected = 4, actual = render.data.series.size)
        assertEquals(expected = listOf(0, 1, 2, 3), actual = render.sourceIndexByRenderIndex)
        assertEquals(expected = 3, actual = render.resolveSourceIndex(3))
        assertEquals(expected = 2, actual = render.resolveRenderIndex(2))
    }

    /** One series per bar, each named by its category, with the segment names as the categories. */
    @Test
    fun convert_transposesIntoOneBarPerCategory() {
        val data =
            StackedBarChartSpec.convert(
                data = seriesMajorData(bars = 2, segmentNames = listOf("S1", "S2")),
                title = "Stacked Bar",
            )

        assertEquals(expected = listOf("Bar 1", "Bar 2"), actual = data.series.map { it.name })
        assertEquals(expected = listOf("S1", "S2"), actual = data.categories.toList())
        assertEquals(expected = listOf(1.0, 2.0), actual = data.series[0].values)
        assertEquals(expected = listOf(2.0, 3.0), actual = data.series[1].values)
    }

    private fun stackedBarModel(bars: Int) =
        StackedBarChartSpec.convert(
            data = seriesMajorData(bars = bars, segmentNames = listOf("S1", "S2")),
            title = "Stacked Bar",
        )

    /** The caller's data: one series per segment, one category per bar. */
    private fun seriesMajorData(
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
}
