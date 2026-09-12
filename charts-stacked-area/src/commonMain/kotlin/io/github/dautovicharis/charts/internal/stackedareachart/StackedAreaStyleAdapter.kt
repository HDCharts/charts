package io.github.dautovicharis.charts.internal.stackedareachart

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.StackedAreaChartStyle

@InternalChartsApi
@Composable
fun StackedAreaChartStyle.toInternal(showXAxisLabels: Boolean = true): StackedAreaInternalStyle =
    StackedAreaInternalStyle(
        modifier = chartContainerStyle.fillMaxSizeChartModifier(),
        chartContainerStyle = chartContainerStyle,
        areaColor = fill.color,
        areaColors = fill.colors.toList(),
        fillAlpha = fill.alpha,
        lineVisible = boundary.visible,
        lineColor = boundary.color,
        lineColors = boundary.colors.toList(),
        lineWidth = boundary.width,
        bezier = boundary.bezier,
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
        selectionLineWidth = selection.width,
    )
