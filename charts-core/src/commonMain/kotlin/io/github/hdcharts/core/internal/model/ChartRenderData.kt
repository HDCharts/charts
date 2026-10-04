package io.github.hdcharts.core.internal.model

import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import kotlinx.collections.immutable.ImmutableList

/**
 * The render model every chart below the entry seam draws from: the validated [data] plus the
 * chart's [title].
 *
 * It holds the public model rather than a copy of it, so nothing is converted twice and there is no
 * second type named `ChartData` for a reader to confuse with the caller's. [series] and [categories]
 * are the public model's own fields, so code below the seam reads them exactly as a caller does.
 * The one value it works out for itself is [seriesTotals], which three readers would otherwise
 * each walk the data to get.
 *
 * **Its contents are total because of where it comes from, not because it checks.** `ChartEntry`
 * validates, and only data that passes reaches this type: at least one series, the same number of
 * values in every series, every value finite, and categories that are either empty or exactly as
 * long as the values. That is a precondition, so nothing below the seam re-checks it and nothing
 * throws. A style value out of range is clamped and drawn; invalid data is reported through
 * `ChartErrors` and nothing is drawn. See Validation Errors for both halves of that rule.
 *
 * [transposeForStacking] is the one construction that changes the orientation, and it is the one
 * place [categories] and [series] stop meaning what they mean everywhere else.
 */
data class ChartRenderData(
    val data: ChartData,
    val title: String,
) {
    /** One entry per series, in the caller's order. Every series has the same number of values. */
    val series: ImmutableList<ChartSeries> get() = data.series

    /** Labels for the indexed dimension, or empty for a chart whose data has none. */
    val categories: ImmutableList<String> get() = data.categories

    /** Values in the first series, or 0 for a model with no values. */
    fun valueCount(): Int = series.firstOrNull()?.values?.size ?: 0

    fun hasCategories(): Boolean = categories.isNotEmpty()

    /**
     * The sum of each series' values, one entry per series in [series]' order, worked out once.
     *
     * Stacked bar needs that one fact in three places — the axis domain, the height each bar
     * animates to, and the share of a bar that each segment takes — and each of them is a separate
     * walk over the same numbers, the last of them inside the draw loop and so once per frame.
     * Cached here, the three read it instead.
     *
     * **Entry `i` is series `i]'s total, and which series that is depends on the chart.** In a
     * transposed model a series is a bar, so this is the bar total. A chart that draws stacked
     * area needs the same sums read the other way round, per point, and gets them from its own
     * normalizer rather than from here.
     */
    val seriesTotals: List<Double> by lazy { series.map { item -> item.values.sum() } }
}

/**
 * The lowest and highest value across every series, and `0.0 to 0.0` for a model with no values.
 *
 * One pass, with no seed: validation rejects non-finite values before a render model exists, so
 * every value read here is finite.
 */
fun ChartRenderData.minMax(): Pair<Double, Double> {
    var min = Double.POSITIVE_INFINITY
    var max = Double.NEGATIVE_INFINITY
    for (item in series) {
        for (value in item.values) {
            min = minOf(min, value)
            max = maxOf(max, value)
        }
    }
    return if (min.isInfinite() && max.isInfinite()) 0.0 to 0.0 else min to max
}

/**
 * The same data read the other way round: one series per bar rather than one per segment.
 *
 * Every chart's render model is series-major, so a chart that stacks segments transposes once,
 * here, where it stacks, rather than storing a second model shape.
 *
 * **In a transposed model the two fields swap roles, and this is the only function where that is
 * true.** [ChartRenderData.series] holds one entry per bar, so `series[i].name` labels the bar and
 * `series[i].values` are that bar's segment values. [ChartRenderData.categories] holds the segment
 * names, because they label the *points* within each series. Stacked bar is the only reader.
 */
@InternalChartsApi
fun ChartRenderData.transposeForStacking(): ChartRenderData {
    val segmentNames = series.map { it.name.orEmpty() }
    return ChartRenderData(
        data =
            ChartData(
                categories = segmentNames,
                series =
                    List(valueCount()) { barIndex ->
                        ChartSeries(
                            name = categories.getOrNull(barIndex).orEmpty(),
                            values = series.map { item -> item.values[barIndex] },
                        )
                    },
            ),
        title = title,
    )
}
