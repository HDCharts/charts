package io.github.hdcharts.core.internal.axis

import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.model.ChartValueFormatter

// Most ticks when the style leaves the label count unset.
private const val DEFAULT_Y_AXIS_MAX_TICK_COUNT = 5

// Fewest ticks: the two ends of the range.
private const val MIN_Y_AXIS_TICK_COUNT = 2

/**
 * Y-axis tick count for ticks spread over [spanPx]: at most [maxCount], or five when it is null, and
 * no more than fit [yAxisLabelMinSpacingPx] apart at [fontSizePx], but always the two ends.
 */
@InternalChartsApi
fun yAxisTickCount(
    maxCount: Int?,
    spanPx: Float,
    fontSizePx: Float,
): Int {
    val spacingPx = yAxisLabelMinSpacingPx(fontSizePx)
    // A span that is not a number leaves no room between the two ends.
    val safeSpanPx = if (spanPx.isNaN()) 0f else spanPx.coerceAtLeast(0f)
    val fittingCount =
        if (spacingPx > 0f) {
            (safeSpanPx / spacingPx).toInt().coerceAtMost(Int.MAX_VALUE - 1) + 1
        } else {
            Int.MAX_VALUE
        }
    return (maxCount ?: DEFAULT_Y_AXIS_MAX_TICK_COUNT).coerceAtMost(fittingCount).coerceAtLeast(MIN_Y_AXIS_TICK_COUNT)
}

/**
 * Builds [labelCount] evenly spaced Y ticks from [maxValue] at the top to [minValue] at the bottom,
 * labeled by [formatter]. [verticalInsetPx] keeps the first and last tick away from the plot edges.
 */
@InternalChartsApi
fun buildNumericYAxisTicks(
    minValue: Double,
    maxValue: Double,
    labelCount: Int,
    plotHeightPx: Float,
    verticalInsetPx: Float,
    formatter: ChartValueFormatter,
): List<AxisYLayoutTick> {
    if (plotHeightPx <= 0f) return emptyList()
    val steps = labelCount.coerceAtLeast(2) - 1
    val safeInset = verticalInsetPx.coerceIn(0f, plotHeightPx / 2f)
    val drawableHeight = (plotHeightPx - (safeInset * 2f)).coerceAtLeast(0f)

    return (0..steps).map { step ->
        val progress = step.toDouble() / steps
        // A convex combination avoids overflowing (max - min) for extreme signed data. On a flat range its
        // rounding can miss the value by one double and flip a label, so the value is used as is.
        val value = if (minValue == maxValue) maxValue else maxValue * (1.0 - progress) + minValue * progress
        AxisYLayoutTick(
            label = formatter.format(value),
            centerY = safeInset + drawableHeight * progress.toFloat(),
        )
    }
}
