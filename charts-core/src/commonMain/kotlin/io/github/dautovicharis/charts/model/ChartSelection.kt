package io.github.dautovicharis.charts.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Hoisted selection state for charts that support selecting a single data point
 * (e.g. a pie slice).
 *
 * Create with [rememberChartSelection] for interactive charts, or [staticChartSelection]
 * for deterministic preset selections used in screenshots and tests.
 *
 * @param onSelectionChanged Optional callback invoked whenever the selection changes.
 * @property selectedIndex The currently selected index, or `null` when nothing is selected.
 * @param initialIndex The initially selected index, or `null` for no initial selection.
 */
@Stable
class ChartSelection constructor(
    initialIndex: Int? = null,
) {
    /**
     * The currently selected index, or `null` when nothing is selected.
     */
    var selectedIndex: Int? by mutableStateOf(initialIndex)
        private set

    /**
     * Optional callback invoked whenever the selection changes.
     */
    var onSelectionChanged: ((Int?) -> Unit)? = null

    /**
     * Selects the data point at [index].
     */
    fun select(index: Int) {
        selectedIndex = index
        onSelectionChanged?.invoke(index)
    }

    /**
     * Clears the current selection.
     */
    fun clear() {
        selectedIndex = null
        onSelectionChanged?.invoke(null)
    }
}

/**
 * Creates a [ChartSelection] remembered for the current composition.
 *
 * @param initialIndex The initially selected index, or `null` for no initial selection.
 */
@Composable
fun rememberChartSelection(initialIndex: Int? = null): ChartSelection =
    remember { ChartSelection(initialIndex = initialIndex) }

/**
 * Creates a [ChartSelection] fixed to [index]. Used for deterministic rendering,
 * e.g. screenshots and documentation images.
 */
fun staticChartSelection(index: Int): ChartSelection = ChartSelection(initialIndex = index)
