package io.github.hdcharts.charts.internal.common.layout

import androidx.compose.ui.unit.Constraints
import io.github.hdcharts.charts.internal.InternalChartsApi
import kotlin.math.roundToInt

/**
 * Whether Compose can measure a chart canvas [widthPx] wide and [heightPx] tall. Scrolling charts
 * size their canvas to the full content width, which Compose constraints cannot hold past about
 * 262,000 px; width and height share one packed bit budget, so the pair is checked together.
 */
@InternalChartsApi
fun chartCanvasFits(
    widthPx: Float,
    heightPx: Float,
): Boolean {
    if (!widthPx.isFinite() || !heightPx.isFinite() || widthPx < 0f || heightPx < 0f) return false
    return runCatching { Constraints.fixed(widthPx.roundToInt(), heightPx.roundToInt()) }.isSuccess
}
