package io.github.dautovicharis.charts.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * A unified chart data model.
 *
 * @param categories The indexed-dimension labels shared by all series (x-axis values for
 * bar/line charts, slice labels for pie charts, axes for radar charts).
 * @param series The series to render. Pie charts use exactly one series.
 */
@Immutable
data class ChartData(
    val categories: ImmutableList<String>,
    val series: ImmutableList<ChartSeries>,
) {
    /**
     * Convenience constructor accepting mutable lists.
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
 * @param name Optional series name. Required for charts that render a legend with
 * per-series entries.
 * @param values The series values. Mutating the supplied list after construction is
 * unsupported; the values are copied into an immutable list.
 */
@Immutable
data class ChartSeries(
    val name: String? = null,
    val values: ImmutableList<Float>,
) {
    /**
     * Convenience constructor accepting a mutable list of values.
     *
     * @param name Optional series name.
     * @param values The series values; copied into an immutable list.
     */
    constructor(
        name: String? = null,
        values: List<Float>,
    ) : this(
        name = name,
        values = values.toImmutableList(),
    )
}

/**
 * Converts a list of values into single-series [ChartData].
 *
 * @param categories The indexed-dimension labels. Defaults to the string form of each index.
 * @param seriesName Optional series name.
 */
fun List<Float>.toChartData(
    categories: List<String> = indices.map(Int::toString),
    seriesName: String? = null,
): ChartData =
    ChartData(
        categories = categories,
        series = listOf(ChartSeries(name = seriesName, values = this)),
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
