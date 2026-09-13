package io.github.dautovicharis.charts.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Immutable, indexed data shared by aligned-series charts.
 *
 * Values at the same index in each series share a category. Categories are labels,
 * not numeric coordinates. Replace data to update a chart rather than mutating input lists.
 * Chart-specific validation determines permitted series counts, lengths, and values.
 * Pie charts use their own slice model instead of this table.
 *
 * @param categories The indexed-dimension labels, such as bar labels or radar axes.
 * Empty means no explicit labels; otherwise the count must match each series' value count.
 * @param series The ordered series to render. Series must have aligned value counts.
 */
@Immutable
data class ChartData(
    val categories: ImmutableList<String>,
    val series: ImmutableList<ChartSeries>,
) {
    /**
     * Convenience constructor that defensively copies the supplied lists.
     *
     * @param categories The indexed-dimension labels shared by all series.
     * @param series The series to render.
     */
    constructor(
        categories: List<String> = emptyList(),
        series: List<ChartSeries> = emptyList(),
    ) : this(
        categories = categories.toImmutableList(),
        series = series.toImmutableList(),
    )
}

/**
 * A single named series of values within a [ChartData].
 *
 * @param name Optional display name, not a unique series identifier.
 * @param values The series values. Mutating the supplied list after construction is
 * safe but does not update this series; values are copied into an immutable list.
 * Raw values retain Double precision; drawing coordinates can be normalized separately.
 * Convert other numeric types or parse strings before constructing the series.
 */
@Immutable
data class ChartSeries(
    val name: String? = null,
    val values: ImmutableList<Double>,
) {
    /**
     * Convenience constructor accepting a mutable list of values.
     *
     * @param name Optional series name.
     * @param values The series values; copied into an immutable list.
     */
    constructor(
        name: String? = null,
        values: List<Double>,
    ) : this(
        name = name,
        values = values.toImmutableList(),
    )
}

/**
 * Converts a list of values into single-series [ChartData].
 *
 * @param categories The indexed-dimension labels. Empty by default (no explicit labels).
 * @param seriesName Optional series name.
 */
fun List<Double>.toChartData(
    categories: List<String> = emptyList(),
    seriesName: String? = null,
): ChartData =
    ChartData(
        categories = categories,
        series = listOf(ChartSeries(name = seriesName, values = this)),
    )

/**
 * Builds multi-series [ChartData] from a list of named series.
 *
 * Each pair supplies a series name and its values. For a more explicit
 * construction, use [chartDataOf] with [ChartSeries] instances.
 *
 * @param categories The indexed-dimension labels shared by all series.
 * Empty by default (no explicit labels); otherwise the count must match
 * each series' value count.
 */
fun List<Pair<String, List<Double>>>.toChartData(categories: List<String> = emptyList()): ChartData =
    ChartData(
        categories = categories,
        series = map { (name, values) -> ChartSeries(name = name, values = values) },
    )

/**
 * Builds a [ChartData] from shared [categories] and one or more [series].
 */
fun chartDataOf(
    categories: List<String> = emptyList(),
    vararg series: ChartSeries,
): ChartData =
    ChartData(
        categories = categories,
        series = series.toList(),
    )
