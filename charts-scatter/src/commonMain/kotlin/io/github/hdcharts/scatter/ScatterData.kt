package io.github.hdcharts.scatter

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** A point positioned by numeric X and Y values, with an optional bubble size. */
@Immutable
data class ScatterPoint(
    val x: Double,
    val y: Double,
    val size: Double? = null,
)

/** One named or unnamed scatter series. */
@Immutable
data class ScatterSeries(
    val name: String? = null,
    val points: ImmutableList<ScatterPoint>,
) {
    constructor(name: String? = null, points: List<ScatterPoint>) : this(name, points.toImmutableList())
}

/** Numeric points grouped into independently colored series. */
@Immutable
data class ScatterData(
    val series: ImmutableList<ScatterSeries>,
) {
    constructor(series: List<ScatterSeries>) : this(series.toImmutableList())
}
