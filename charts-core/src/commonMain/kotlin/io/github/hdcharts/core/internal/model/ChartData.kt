package io.github.hdcharts.core.internal.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * One series of labels and values, stored as the two columns every reader wants.
 *
 * Drawing reads one column at a time, so labels and points are kept side by side rather than as
 * label/value pairs: a stored pair list would be a third copy of every point that nothing reads.
 *
 * A data class so two models built from equal values are equal. The chart entry seam builds a new
 * model on every data change, so identity equality made every `remember` keyed on a model miss.
 *
 * Labels and points are expected to have the same count. A point without a label reads as blank,
 * which is what an unlabelled point draws as.
 */
data class ChartData(
    val labels: ImmutableList<String>,
    val points: ImmutableList<Double>,
) {
    constructor(
        labels: List<String>,
        points: List<Double>,
    ) : this(
        labels = labels.toImmutableList(),
        points = points.toImmutableList(),
    )
}

/**
 * Pairs these values with [labels], one label per point. A point past the end of [labels] gets a
 * blank label, and labels beyond the last point are dropped.
 */
fun List<Double>.toChartData(labels: List<String> = emptyList()): ChartData =
    ChartData(
        labels = List(size) { index -> labels.getOrNull(index).orEmpty() },
        points = this,
    )
