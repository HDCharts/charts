package io.github.hdcharts.charts.internal.common.model

import io.github.hdcharts.charts.internal.NO_SELECTION
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class MultiChartData(
    val items: ImmutableList<ChartDataItem>,
    val categories: ImmutableList<String> = persistentListOf(),
    val title: String,
) {
    constructor(
        items: List<ChartDataItem>,
        categories: List<String> = emptyList(),
        title: String,
    ) : this(
        items = items.toImmutableList(),
        categories = categories.toImmutableList(),
        title = title,
    )

    fun getFirstPointsSize(): Int =
        items
            .first()
            .item.points.size

    fun hasSingleItem(): Boolean = items.size == 1

    fun hasCategories(): Boolean = categories.isNotEmpty()

    fun getLabel(index: Int): String {
        if (index == NO_SELECTION) return title

        return if (hasSingleItem()) {
            items.first().item.labels[index]
        } else {
            if (hasCategories()) {
                categories.getOrNull(index) ?: "Missing Label ${index + 1}"
            } else {
                title
            }
        }
    }
}

fun MultiChartData.minMax(): Pair<Double, Double> {
    val first = this.items.first()
    var min = first.item.points.min()
    var max = first.item.points.max()

    for (data in this.items) {
        val currentMin = data.item.points.minOrNull() ?: continue
        val currentMax = data.item.points.maxOrNull() ?: continue

        min = minOf(min, currentMin)
        max = maxOf(max, currentMax)
    }

    return min to max
}
