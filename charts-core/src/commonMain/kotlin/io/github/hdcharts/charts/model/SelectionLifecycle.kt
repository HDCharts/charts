package io.github.hdcharts.charts.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.hdcharts.charts.internal.InternalChartsApi
import kotlinx.coroutines.delay

/**
 * Drives the background lifecycle of a [ChartSelection]:
 *
 *  - Clears the selection when [data] reference changes between recompositions.
 *    The first composition never clears selection, so an `initialIndex` provided
 *    to [rememberChartSelection] is preserved.
 *  - Clears the selection when [itemCount] becomes zero, or when the currently
 *    selected index is no longer in `0 until itemCount`. The bounds check
 *    re-runs whenever the index or item count changes, so external
 *    `selection.select(...)` calls that fall out of range are caught
 *    immediately.
 *  - When [lifetime] is [SelectionLifetime.AutoDeselect], renews the
 *    auto-clear timer on each new selection.
 *
 * The helper never overrides gesture-driven selection or explicit `clear()`
 * calls beyond the rules above. Density-mode (compact vs scrollable) reshuffles
 * should be reported through [itemCount] when the source range changes; if
 * the chart reshuffles `renderData` while preserving source indices, pass
 * `itemCount = Int.MAX_VALUE` to skip the bounds check.
 *
 * @param selection The hoisted selection holder.
 * @param data Stable identity key for the source data. Changing this
 *   reference clears selection.
 * @param itemCount Number of selectable items in the *source* data, or
 *   `Int.MAX_VALUE` to skip the bounds check.
 * @param lifetime Background policy. Defaults to
 *   [SelectionLifetime.Persistent].
 * @param autoDeselectTrigger Restarts [SelectionLifetime.AutoDeselect] when
 *   its value changes. Pass `null` until automatic cleanup is armed.
 */
@InternalChartsApi
@Composable
fun rememberSelectionLifecycle(
    selection: ChartSelection,
    data: Any?,
    itemCount: Int = Int.MAX_VALUE,
    lifetime: SelectionLifetime = SelectionLifetime.Persistent,
    autoDeselectTrigger: Any? = selection.version,
) {
    var observedData by remember(selection) { mutableStateOf<Any?>(data) }

    LaunchedEffect(selection, data) {
        if (observedData != data && selection.selectedIndex != null) {
            selection.clear()
        }
        observedData = data
    }

    val selectedIndex = selection.selectedIndex
    LaunchedEffect(selection, itemCount, selectedIndex) {
        if (itemCount <= 0) {
            if (selection.selectedIndex != null) {
                selection.clear()
            }
            return@LaunchedEffect
        }
        val current = selection.selectedIndex
        if (current != null && (current < 0 || current >= itemCount)) {
            selection.clear()
        }
    }

    when (lifetime) {
        SelectionLifetime.Persistent -> Unit
        is SelectionLifetime.AutoDeselect -> {
            LaunchedEffect(selection, autoDeselectTrigger, lifetime.delayMs) {
                if (autoDeselectTrigger == null) return@LaunchedEffect
                val pending = selection.selectedIndex ?: return@LaunchedEffect
                delay(lifetime.delayMs)
                if (selection.selectedIndex == pending) {
                    selection.clear()
                }
            }
        }
    }
}
