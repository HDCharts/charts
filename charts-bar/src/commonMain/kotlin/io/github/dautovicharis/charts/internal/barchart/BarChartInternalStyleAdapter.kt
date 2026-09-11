package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.BarChartStyle

/**
 * Adapts the grouped v3 [BarChartStyle] into the flat internal [BarChartInternalStyle] used
 * by the existing bar renderer. This is the boundary between the public v3 API and the
 * internal renderer; a follow-up will refactor the renderer to consume the grouped blocks
 * directly.
 */
@InternalChartsApi
@Composable
fun BarChartStyle.toInternal(): BarChartInternalStyle {
    val density = LocalDensity.current
    return BarChartInternalStyle(
        modifier = chartContainerStyle.fillMaxSizeChartModifier(),
        chartContainerStyle = chartContainerStyle,
        barColor = bars.color,
        barColors = bars.colors,
        barAlpha = bars.alpha,
        space = bars.space,
        minValue = range.min,
        maxValue = range.max,
        minBarWidth = bars.minBarWidth,
        zoomControlsVisible = zoomControlsVisible,
        gridVisible = grid.visible,
        gridSteps = grid.steps,
        gridColor = grid.color,
        gridLineWidth = with(density) { grid.lineWidth.toPx() },
        axisVisible = axis.visible,
        axisColor = axis.color,
        axisLineWidth = with(density) { axis.lineWidth.toPx() },
        yAxisLabelsVisible = axis.yLabels.visible,
        yAxisLabelColor = axis.yLabels.color,
        yAxisLabelSize = axis.yLabels.size,
        yAxisLabelCount = axis.yLabels.count,
        xAxisLabelsVisible = axis.xLabels.visible,
        xAxisLabelColor = axis.xLabels.color,
        xAxisLabelSize = axis.xLabels.size,
        xAxisLabelMaxCount = axis.xLabels.count,
        selectionLineVisible = selectionLine.visible,
        selectionLineColor = selectionLine.color,
        selectionLineWidth = with(density) { selectionLine.width.toPx() },
    )
}
