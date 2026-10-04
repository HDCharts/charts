package io.github.hdcharts.core.internal

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
