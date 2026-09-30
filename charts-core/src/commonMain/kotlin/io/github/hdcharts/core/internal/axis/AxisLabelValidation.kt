package io.github.hdcharts.core.internal.axis

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnitType
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.style.AxisLabelStyle

// Smallest label count a style may set; Y-axis ticks need the two ends of the range.
private const val MIN_AXIS_LABEL_COUNT = 2

// Largest label count a style may set, so a tiny font cannot build thousands of Y ticks.
private const val MAX_AXIS_LABEL_COUNT = 1000

/**
 * Returns the errors of both axis label styles: a size that is not a finite, positive sp value or
 * that resolves to more than [MAX_SIZE_PX] pixels at [density], and a most-label count outside
 * 2..1000. A null count is valid. Every chart with axes validates its labels here before it converts sizes.
 */
@InternalChartsApi
fun validateAxisLabels(
    xLabels: AxisLabelStyle,
    yLabels: AxisLabelStyle,
    density: Density,
): List<String> =
    listOfNotNull(
        axisLabelSizeError(axis = "X", labels = xLabels, density = density),
        axisLabelCountError(axis = "X", labels = xLabels),
        axisLabelSizeError(axis = "Y", labels = yLabels, density = density),
        axisLabelCountError(axis = "Y", labels = yLabels),
    )

private fun axisLabelSizeError(
    axis: String,
    labels: AxisLabelStyle,
    density: Density,
): String? {
    val size = labels.size
    if (size.type != TextUnitType.Sp || !size.value.isFinite() || size.value <= 0f) {
        return "$axis-axis label size must be a finite, positive sp value."
    }
    val sizePx = with(density) { size.toPx() }
    return if (sizePx.isFinite() && sizePx <= MAX_SIZE_PX) {
        null
    } else {
        "$axis-axis label size must resolve to at most ${MAX_SIZE_PX.toInt()} pixels."
    }
}

private fun axisLabelCountError(
    axis: String,
    labels: AxisLabelStyle,
): String? {
    val maxCount = labels.maxCount ?: return null
    if (maxCount in MIN_AXIS_LABEL_COUNT..MAX_AXIS_LABEL_COUNT) return null
    return "$axis-axis label max count must be in $MIN_AXIS_LABEL_COUNT..$MAX_AXIS_LABEL_COUNT."
}
