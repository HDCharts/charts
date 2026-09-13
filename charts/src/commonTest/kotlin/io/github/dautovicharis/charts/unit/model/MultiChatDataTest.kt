package io.github.dautovicharis.charts.unit.model

import io.github.dautovicharis.charts.internal.common.model.ChartDataItem
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.model.minMax
import io.github.dautovicharis.charts.internal.common.model.normalizeByMinMax
import io.github.dautovicharis.charts.internal.common.model.toChartData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MultiChatDataTest {
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
    fun selectionHelpers_preserveSingleAndMultipleSeriesLabelSemantics() {
        val single = data(ChartDataItem("Label1", listOf(1.0, 2.0).toChartData()))
        val multiple =
            data(
                ChartDataItem("Label1", listOf(1.0, 2.0).toChartData()),
                ChartDataItem("Label2", listOf(3.0, 4.0).toChartData()),
                categories = listOf("Jan", "Feb"),
            )

        assertTrue(single.hasSingleItem())
        assertEquals("2.0", single.getLabel(1))
        assertFalse(multiple.hasSingleItem())
        assertTrue(multiple.hasCategories())
        assertEquals("Feb", multiple.getLabel(1))
        assertEquals("Missing Label 3", multiple.getLabel(2))
    }
}
