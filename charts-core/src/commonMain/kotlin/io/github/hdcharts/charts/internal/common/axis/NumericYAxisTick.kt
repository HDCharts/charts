package io.github.hdcharts.charts.internal.common.axis

import io.github.hdcharts.charts.internal.InternalChartsApi

@InternalChartsApi
data class NumericYAxisTick(
    val label: String,
    val centerY: Float,
)

@InternalChartsApi
fun buildNumericYAxisTicks(
    minValue: Double,
    maxValue: Double,
    labelCount: Int,
    plotHeightPx: Float,
    verticalInsetPx: Float = 0f,
): List<NumericYAxisTick> {
    if (plotHeightPx <= 0f) return emptyList()
    val safeLabelCount = labelCount.coerceAtLeast(2)
    val steps = (safeLabelCount - 1).coerceAtLeast(1)
    val range = maxValue - minValue
    val safeInset = verticalInsetPx.coerceIn(0f, plotHeightPx / 2f)
    val drawableHeight = (plotHeightPx - (safeInset * 2f)).coerceAtLeast(0f)

    return (0..steps).map { step ->
        val progress = step / steps.toFloat()
        val value = maxValue - range * progress
        NumericYAxisTick(
            label = formatNumericAxisValue(value),
            centerY = safeInset + (drawableHeight * progress),
        )
    }
}
