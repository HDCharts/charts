package io.github.hdcharts.waterfall.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.model.normalizeValue
import io.github.hdcharts.core.internal.model.resolveOptionalRange
import io.github.hdcharts.core.style.BarRangeStyle
import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry

/** Source order is preserved for selection. A total starts at zero and keeps the balance. */
internal data class WaterfallBar(
    val start: Double,
    val end: Double,
)

internal fun waterfallBars(data: WaterfallData): List<WaterfallBar> {
    var balance = 0.0
    return data.entries.map { entry ->
        when (entry) {
            is WaterfallEntry.Change -> {
                val start = balance
                balance += entry.amount
                WaterfallBar(start, balance)
            }
            is WaterfallEntry.Total -> WaterfallBar(0.0, balance)
        }
    }
}

internal fun waterfallRange(
    bars: List<WaterfallBar>,
    range: BarRangeStyle,
): Pair<Double, Double> {
    val min = minOf(0.0, bars.minOf { minOf(it.start, it.end) })
    val max = maxOf(0.0, bars.maxOf { maxOf(it.start, it.end) })
    // All-zero datasets still need a scale and a visible zero baseline.
    return resolveOptionalRange(min, if (min == max) 1.0 else max, range.min, range.max)
}

/** Pixel geometry shared by the bars, connectors, axis labels and pointer input. */
internal data class WaterfallPlotLayout(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val count: Int,
    val space: Float,
    val maxBarWidth: Float,
) {
    val slotWidth: Float get() = width / count

    fun y(
        value: Double,
        range: Pair<Double, Double>,
    ): Float = top + (1f - normalizeValue(value, range.first, range.second)) * height

    fun bounds(
        index: Int,
        bar: WaterfallBar,
        range: Pair<Double, Double>,
        progress: Float = 1f,
    ): Rect {
        val barWidth = minOf(maxBarWidth, slotWidth - minOf(space, slotWidth / 2f))
        val x = left + slotWidth * (index + 0.5f) - barWidth / 2f
        val start = y(bar.start * progress, range)
        val end = y(bar.end * progress, range)
        // Zero changes remain visible at the balance, including at the plot's bottom edge.
        val barTop = minOf(start, end).coerceAtMost(top + height - 1f)
        return Rect(x, barTop, x + barWidth, maxOf(maxOf(start, end), barTop + 1f))
    }
}

internal fun waterfallHitIndex(
    position: Offset,
    bounds: List<Rect>,
    verticalSlop: Float,
): Int =
    bounds
        .indexOfFirst { rect ->
            rect.width > 0f &&
                position.x >= rect.left &&
                position.x <= rect.right &&
                position.y >= rect.top - verticalSlop &&
                position.y <= rect.bottom + verticalSlop
        }.let { if (it < 0) NO_SELECTION else it }
