package io.github.hdcharts.core.internal

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * The title a chart header shows: the selected category while it is selected, otherwise the caller's
 * [title]. A selected category that is missing or blank falls back to [title] rather than leaving the
 * header empty, so every chart names itself the same way.
 */
@InternalChartsApi
fun selectedCategoryTitle(
    categories: List<String>,
    selectedIndex: Int,
    title: String?,
): String = categories.getOrNull(selectedIndex)?.takeIf { it.isNotBlank() } ?: title.orEmpty()

/**
 * The title of a chart whose legend carries several series' values: one series has no legend to
 * carry its value, so the title shows it; several series leave the title to [selectedCategoryTitle].
 */
@InternalChartsApi
fun selectedTitle(
    data: ChartData,
    selectedIndex: Int,
    title: String?,
    valueFormatter: ChartValueFormatter,
): String {
    val singleSeries = data.series.singleOrNull()
    if (singleSeries == null || selectedIndex == NO_SELECTION) {
        return selectedCategoryTitle(categories = data.categories, selectedIndex = selectedIndex, title = title)
    }
    return selectedValueTitle(
        category = data.categories.getOrNull(selectedIndex),
        value = valueFormatter.format(singleSeries.values[selectedIndex]),
    )
}

/** Each series' formatted value at [selectedIndex], for the legend; empty without a selection. */
@InternalChartsApi
fun selectedLegendValues(
    data: ChartData,
    selectedIndex: Int,
    valueFormatter: ChartValueFormatter,
): ImmutableList<String> =
    if (selectedIndex == NO_SELECTION) {
        persistentListOf()
    } else {
        data.series.map { valueFormatter.format(it.values[selectedIndex]) }.toImmutableList()
    }

/** `Category: value`, or the value without a category. */
private fun selectedValueTitle(
    category: String?,
    value: String,
): String = if (category.isNullOrBlank()) value else "$category: $value"
