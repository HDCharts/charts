package io.github.hdcharts.charts.model

import io.github.hdcharts.charts.internal.InternalChartsApi

/**
 * Background lifecycle policy for a [ChartSelection].
 *
 * The policy governs *automatic* invalidation only. Gestures (tap, drag)
 * and explicit `clear()` calls are always honored. Data changes and
 * out-of-bounds indices are handled by [rememberSelectionLifecycle]
 * independently of this policy.
 */
@InternalChartsApi
sealed interface SelectionLifetime {
    /**
     * Selection persists until explicitly cleared or invalidated by the
     * lifecycle helper. Default for tap-to-pin charts.
     */
    data object Persistent : SelectionLifetime

    /**
     * Selection auto-clears after [delayMs] milliseconds unless renewed
     * by a new selection. Each new selection restarts the timer.
     */
    data class AutoDeselect(
        val delayMs: Long,
    ) : SelectionLifetime {
        init {
            require(delayMs >= 0L) {
                "delayMs must be >= 0, was $delayMs"
            }
        }
    }
}
