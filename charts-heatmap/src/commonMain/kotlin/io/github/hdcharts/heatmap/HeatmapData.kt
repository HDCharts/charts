package io.github.hdcharts.heatmap

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * A matrix of values on two category axes, such as activity per weekday and hour.
 *
 * [values] holds one list per row label, in [rows] order, and each list holds one value per column
 * label, in [columns] order: `values[row][column]`. A `null` value is a missing cell. It draws empty,
 * cannot be selected, and is never treated as zero.
 *
 * @property columns Column labels, shown along the X axis from left to right.
 * @property rows Row labels, shown along the Y axis from top to bottom.
 * @property values Row-major cell values; `null` marks a missing cell.
 */
@Immutable
data class HeatmapData(
    val columns: ImmutableList<String>,
    val rows: ImmutableList<String>,
    val values: ImmutableList<ImmutableList<Double?>>,
) {
    /** Copies the supplied labels and values into immutable lists. */
    constructor(
        columns: List<String>,
        rows: List<String>,
        values: List<List<Double?>>,
    ) : this(
        columns = columns.toImmutableList(),
        rows = rows.toImmutableList(),
        values = values.map { row -> row.toImmutableList() }.toImmutableList(),
    )

    /**
     * Returns the selection index of the cell at [row] and [column]: `row * columns.size + column`.
     * Pass it to a `ChartSelection` to select that cell.
     */
    fun cellIndex(
        row: Int,
        column: Int,
    ): Int = row * columns.size + column
}
