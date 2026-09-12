package io.github.dautovicharis.charts.internal.barstackedchart

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.StackedBarChartStyle

@InternalChartsApi
@Composable
fun StackedBarChartStyle.toInternal(showXAxisLabels: Boolean = true): StackedBarInternalStyle =
    StackedBarInternalStyle(
        modifier = chartContainerStyle.fillMaxSizeChartModifier(),
        chartContainerStyle = chartContainerStyle,
        barColor = segments.color,
        barAlpha = segments.alpha,
        space = layout.space,
        barColors = segments.colors.toList(),
        minBarWidth = layout.minBarWidth,
        zoomControlsVisible = zoomControlsVisible,
        yAxisLabelsVisible = axis.yLabels.visible,
        yAxisLabelColor = axis.yLabels.color,
        yAxisLabelSize = axis.yLabels.size,
        yAxisLabelCount = axis.yLabels.count,
        xAxisLabelsVisible = axis.xLabels.visible && showXAxisLabels,
        xAxisLabelColor = axis.xLabels.color,
        xAxisLabelSize = axis.xLabels.size,
        xAxisLabelMaxCount = axis.xLabels.count,
        selectionLineVisible = selection.visible,
        selectionLineColor = selection.color,
        selectionLineWidth = selection.width.value,
    )
