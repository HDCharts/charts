package io.github.hdcharts.core.internal.model

import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.model.ChartData
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

    /** Points per item, or 0 for a model with no items. Every item has the same count. */
    fun getFirstPointsSize(): Int =
        items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: 0

    fun hasSingleItem(): Boolean = items.size == 1

    fun hasCategories(): Boolean = categories.isNotEmpty()
}

/**
 * The lowest and highest value across every item, and `0.0 to 0.0` for a model with no points.
 *
 * Validation rejects non-finite values before a render model exists, so a single pass over the
 * points bounds the model.
 */
fun MultiChartData.minMax(): Pair<Double, Double> {
    var min = Double.POSITIVE_INFINITY
    var max = Double.NEGATIVE_INFINITY
    for (item in items) {
        for (value in item.item.points) {
            min = minOf(min, value)
            max = maxOf(max, value)
        }
    }
    return if (min.isInfinite() && max.isInfinite()) 0.0 to 0.0 else min to max
}

/**
 * Where a render model's point labels come from.
 *
 * Declared rather than inferred, because the answer differs per chart and the reason is a chart
 * decision: where the chart reads its X-axis text and its compact-mode labels from.
 */
@InternalChartsApi
enum class PointLabels {
    /** No point carries a label and the model keeps no categories. */
    NONE,

    /**
     * Every point carries its category, and the model keeps the categories too. A chart that reads
     * one set for drawing and the other for compact-mode aggregation needs both, which is why the
     * labels appear twice rather than being resolved at draw time.
     */
    CATEGORIES,

    /**
     * A single series carries its categories as point labels; several series keep them on the model
     * and their points unlabelled. Line is the case: its X-axis labels are one series' point labels
     * when there is one series, and a shared category axis when there are several.
     */
    CATEGORIES_WHEN_SINGLE_SERIES,
}

/**
 * Builds the render model every Cartesian and polar chart draws from: one item per series, each
 * holding its point labels and values.
 *
 * Everything below the seam relies on the model this returns being total: at least one item, the
 * same number of points in every item, every value finite, and a label or a blank for every index.
 *
 * That is a **precondition, not a check**. Validation has already rejected data that breaks it, so
 * the factory does not re-check and does not throw — a chart never crashes a user's app over its
 * data. A style value out of range is clamped and drawn; invalid data is reported through
 * `ChartErrors` and nothing is drawn. See Data Policy for both halves of that rule.
 *
 * The ordering is what guarantees it: `ChartEntry` validates, and only a passing validation reaches
 * this function. `ChartPolicyConformanceTest` and `RenderModelFactoryTest` pin that.
 *
 * [PointLabels] says where the labels go. A chart that stacks segments transposes the result with
 * [transposeForStacking].
 */
@InternalChartsApi
fun toRenderModel(
    data: ChartData,
    title: String?,
    labels: PointLabels,
): MultiChartData {
    val pointsCarryCategories =
        labels == PointLabels.CATEGORIES ||
            (labels == PointLabels.CATEGORIES_WHEN_SINGLE_SERIES && data.series.size == 1)

    return MultiChartData(
        items =
            data.series.map { series ->
                ChartDataItem(
                    label = series.name.orEmpty(),
                    item =
                        series.values.toChartData(
                            labels = if (pointsCarryCategories) data.categories else emptyList(),
                        ),
                )
            },
        categories =
            when (labels) {
                PointLabels.NONE -> emptyList()
                PointLabels.CATEGORIES -> data.categories
                PointLabels.CATEGORIES_WHEN_SINGLE_SERIES ->
                    if (data.series.size > 1) data.categories else emptyList()
            },
        title = title.orEmpty(),
    )
}

/**
 * The same data read the other way round: one item per stacked segment rather than one per bar.
 *
 * Every chart's render model is series-major, so a chart that stacks segments transposes once, here,
 * where it stacks, rather than storing a second model shape. Categories become the segment labels
 * and each item's points become one bar's segment values.
 */
@InternalChartsApi
fun MultiChartData.transposeForStacking(): MultiChartData {
    val segmentLabels = items.map { it.label }
    return MultiChartData(
        items =
            List(getFirstPointsSize()) { barIndex ->
                ChartDataItem(
                    label = categories.getOrNull(barIndex).orEmpty(),
                    item =
                        ChartData(
                            labels = segmentLabels,
                            points = items.map { segment -> segment.item.points[barIndex] },
                        ),
                )
            },
        categories = segmentLabels,
        title = title,
    )
}
