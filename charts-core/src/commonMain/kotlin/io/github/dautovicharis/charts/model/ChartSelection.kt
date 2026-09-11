package io.github.dautovicharis.charts.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/**
 * Hoisted selection state for charts that support selecting a single data point
 * (e.g. a pie slice).
 *
 * Create with [rememberChartSelection] for interactive charts, or [staticChartSelection]
 * for an initialized selection used in screenshots and tests. Neither factory disables
 * interaction or animation. The index refers to source data, not an aggregated drawing index;
 * charts are responsible for interpreting it and checking data bounds.
 *
 * @param onSelectionChanged Optional callback invoked after a different index is selected or cleared.
 * @property selectedIndex The currently selected index, or `null` when nothing is selected.
 * @param initialIndex The initially selected index, or `null` for no initial selection.
 */
@Stable
class ChartSelection constructor(
    initialIndex: Int? = null,
    onSelectionChanged: ((Int?) -> Unit)? = null,
) {
    /**
     * The currently selected index, or `null` when nothing is selected.
     */
    var selectedIndex: Int? by mutableStateOf(initialIndex)
        private set

    /**
     * Optional callback invoked after the selection changes. Initialization, repeated
     * selection of the same index, and clearing an already empty selection do not notify.
     */
    var onSelectionChanged: ((Int?) -> Unit)? = onSelectionChanged

    /**
     * Selects the data point at [index].
     */
    fun select(index: Int) {
        if (selectedIndex == index) return
        selectedIndex = index
        onSelectionChanged?.invoke(index)
    }

    /**
     * Clears the current selection.
     */
    fun clear() {
        if (selectedIndex == null) return
        selectedIndex = null
        onSelectionChanged?.invoke(null)
    }
}

/**
 * Creates a [ChartSelection] remembered for the current composition.
 *
 * @param initialIndex The initially selected index, or `null` for no initial selection.
 * Changes to this argument during recomposition do not reset the selection.
 * @param onSelectionChanged Receives actual selection changes using the latest callback
 * from composition, without recreating the state holder. Assigning the holder's
 * [ChartSelection.onSelectionChanged] property explicitly overrides this callback.
 */
@Composable
fun rememberChartSelection(
    initialIndex: Int? = null,
    onSelectionChanged: ((Int?) -> Unit)? = null,
): ChartSelection {
    val currentOnSelectionChanged by rememberUpdatedState(onSelectionChanged)
    return remember {
        ChartSelection(initialIndex = initialIndex) { index -> currentOnSelectionChanged?.invoke(index) }
    }
}

/**
 * Creates a new, unremembered [ChartSelection] initialized to [index], e.g. for
 * screenshots and documentation images. The returned state is still mutable;
 * this factory does not lock selection or disable interaction or animation.
 */
fun staticChartSelection(index: Int): ChartSelection = ChartSelection(initialIndex = index)
