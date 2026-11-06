package io.github.hdcharts.waterfall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** One labeled step in a [WaterfallChart]. The running balance starts at zero. */
@Immutable
sealed interface WaterfallEntry {
    val category: String

    /** Adds [amount] to the previous balance. Use a change for an opening balance too. */
    @Immutable
    data class Change(
        override val category: String,
        val amount: Double,
    ) : WaterfallEntry

    /** Displays the computed balance from zero, leaving the balance unchanged. */
    @Immutable
    data class Total(
        override val category: String,
    ) : WaterfallEntry
}

/**
 * Ordered changes and computed totals. At least one entry is required by [WaterfallChart].
 * Changes and every intermediate balance must be finite. A total carries no supplied value, so it
 * cannot add the same amount twice. Consecutive totals display the same balance.
 */
@Immutable
data class WaterfallData(
    val entries: ImmutableList<WaterfallEntry>,
) {
    /** Copies entries into an immutable list, including results of domain-model mapping with `map`. */
    constructor(entries: List<WaterfallEntry>) : this(entries.toImmutableList())

    /**
     * Returns the computed balance after the source entry at [index], for use with `ChartSelection`.
     * Uses Double addition in entry order, as the chart does. [index] must be within [entries].
     */
    fun runningTotalAt(index: Int): Double {
        require(index in entries.indices) { "The entry index must be within the data." }
        var balance = 0.0
        for (i in 0..index) {
            val entry = entries[i]
            if (entry is WaterfallEntry.Change) balance += entry.amount
        }
        return balance
    }
}
