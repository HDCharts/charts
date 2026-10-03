package io.github.hdcharts.core.internal.model

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MultiChartDataTest {
    private fun data(
        vararg items: ChartDataItem,
        categories: List<String> = emptyList(),
    ) = MultiChartData(items = items.toList(), categories = categories, title = "Title")

    @Test
    fun minMax_returnsCorrectMinMaxValues() {
        val multiChartData =
            data(
                ChartDataItem("Label1", listOf(1.0, 2.0, 3.0).toChartData()),
                ChartDataItem("Label2", listOf(0.5, 1.0, 1.5).toChartData()),
                ChartDataItem("Label3", listOf(0.5, 1.0, 5.0).toChartData()),
            )

        assertEquals(0.5 to 5.0, multiChartData.minMax())
    }

    @Test
    fun normalizeByMinMax_returnsNormalizedValues() {
        val multiChartData =
            data(
                ChartDataItem("Series1", listOf(0.0, 5.0).toChartData()),
                ChartDataItem("Series2", listOf(2.5, 10.0).toChartData()),
            )

        assertContentEquals(
            listOf(listOf(0f, 0.5f), listOf(0.25f, 1f)),
            multiChartData.normalizeByMinMax(multiChartData.minMax(), zeroRangeValue = 0f),
        )
    }

    @Test
    fun normalizeByMinMax_whenZeroRange_usesZeroRangeValue() {
        val multiChartData =
            data(
                ChartDataItem("Series1", listOf(3.0, 3.0).toChartData()),
                ChartDataItem("Series2", listOf(3.0, 3.0).toChartData()),
            )

        assertContentEquals(
            listOf(listOf(1f, 1f), listOf(1f, 1f)),
            multiChartData.normalizeByMinMax(multiChartData.minMax(), zeroRangeValue = 1f),
        )
    }

    @Test
    fun getFirstPointsSize_returnsFirstPointsSize() {
        val multiChartData =
            data(
                ChartDataItem("Label1", listOf(1.0, 2.0, 3.0).toChartData()),
                ChartDataItem("Label2", listOf(0.5, 1.0, 1.5).toChartData()),
            )

        assertEquals(3, multiChartData.getFirstPointsSize())
    }

    @Test
    fun getFirstPointsSize_withNoItems_isZeroRatherThanThrowing() {
        assertEquals(0, data().getFirstPointsSize())
    }

    @Test
    fun minMax_withNoItems_isZeroRatherThanThrowing() {
        assertEquals(0.0 to 0.0, data().minMax())
    }

    @Test
    fun minMax_skipsAnEmptySeriesAndBoundsTheRest() {
        val multiChartData =
            data(
                ChartDataItem("Label1", emptyList<Double>().toChartData()),
                ChartDataItem("Label2", listOf(2.0, 4.0).toChartData()),
            )

        assertEquals(2.0 to 4.0, multiChartData.minMax())
    }

    @Test
    fun minMax_withNoPointsAtAll_isZeroRatherThanThrowing() {
        val multiChartData = data(ChartDataItem("Label1", emptyList<Double>().toChartData()))

        assertEquals(0.0 to 0.0, multiChartData.minMax())
    }

    @Test
    fun selectionHelpers_reportSingleAndMultipleSeriesShape() {
        val single = data(ChartDataItem("Label1", listOf(1.0, 2.0).toChartData()))
        val multiple =
            data(
                ChartDataItem("Label1", listOf(1.0, 2.0).toChartData()),
                ChartDataItem("Label2", listOf(3.0, 4.0).toChartData()),
                categories = listOf("Jan", "Feb"),
            )

        assertTrue(single.hasSingleItem())
        assertFalse(single.hasCategories())
        assertFalse(multiple.hasSingleItem())
        assertTrue(multiple.hasCategories())
    }

    /**
     * Two models built from equal values are equal, so a `remember` keyed on one does not recompute
     * when the caller passes freshly allocated but identical data.
     */
    @Test
    fun modelsWithEqualContent_areEqual() {
        val first =
            data(
                ChartDataItem("Label1", listOf(1.0, 2.0).toChartData()),
                categories = listOf("Jan", "Feb"),
            )
        val second =
            data(
                ChartDataItem("Label1", listOf(1.0, 2.0).toChartData()),
                categories = listOf("Jan", "Feb"),
            )
        val different =
            data(
                ChartDataItem("Label1", listOf(1.0, 9.0).toChartData()),
                categories = listOf("Jan", "Feb"),
            )

        assertEquals(expected = first, actual = second)
        assertEquals(expected = first.hashCode(), actual = second.hashCode())
        assertFalse(first == different)
    }
}
