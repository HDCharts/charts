package io.github.hdcharts.core.internal

import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertEquals

private const val TITLE = "Sales"
private val FORMATTER = ChartValueFormatter { value -> "#${value.toInt()}" }

@OptIn(InternalChartsApi::class)
class SelectedTitleTest {
    private val oneSeries =
        chartDataOf(
            categories = listOf("Jan", " "),
            ChartSeries(name = "A", values = listOf(1.0, 2.0)),
        )
    private val twoSeries =
        chartDataOf(
            categories = listOf("Jan", " "),
            ChartSeries(name = "A", values = listOf(1.0, 2.0)),
            ChartSeries(name = "B", values = listOf(10.0, 20.0)),
        )

    @Test
    fun selectedTitle_oneSeries_showsCategoryAndValue() {
        assertEquals(
            "Jan: #1",
            selectedTitle(data = oneSeries, selectedIndex = 0, title = TITLE, selectedValueFormatter = FORMATTER),
        )
    }

    @Test
    fun selectedTitle_oneSeriesBlankCategory_showsValue() {
        assertEquals(
            "#2",
            selectedTitle(data = oneSeries, selectedIndex = 1, title = TITLE, selectedValueFormatter = FORMATTER),
        )
    }

    @Test
    fun selectedTitle_severalSeries_showsCategory() {
        assertEquals(
            "Jan",
            selectedTitle(data = twoSeries, selectedIndex = 0, title = TITLE, selectedValueFormatter = FORMATTER),
        )
    }

    @Test
    fun selectedTitle_severalSeriesBlankCategory_showsCallerTitle() {
        assertEquals(
            TITLE,
            selectedTitle(data = twoSeries, selectedIndex = 1, title = TITLE, selectedValueFormatter = FORMATTER),
        )
    }

    @Test
    fun selectedTitle_noSelection_showsCallerTitle() {
        assertEquals(
            TITLE,
            selectedTitle(
                data = oneSeries,
                selectedIndex = NO_SELECTION,
                title = TITLE,
                selectedValueFormatter = FORMATTER,
            ),
        )
    }

    @Test
    fun selectedLegendValues_selection_formatsEachSeries() {
        assertEquals(
            listOf("#2", "#20"),
            selectedLegendValues(data = twoSeries, selectedIndex = 1, selectedValueFormatter = FORMATTER),
        )
    }

    @Test
    fun selectedLegendValues_noSelection_isEmpty() {
        assertEquals(
            emptyList(),
            selectedLegendValues(data = twoSeries, selectedIndex = NO_SELECTION, selectedValueFormatter = FORMATTER),
        )
    }
}
