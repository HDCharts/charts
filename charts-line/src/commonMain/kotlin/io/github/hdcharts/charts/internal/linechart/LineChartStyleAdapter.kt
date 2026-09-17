package io.github.hdcharts.charts.internal.linechart

import androidx.compose.runtime.Composable
import io.github.hdcharts.charts.internal.InternalChartsApi
import io.github.hdcharts.charts.internal.common.layout.wrapContentChartModifier
import io.github.hdcharts.charts.style.LineChartStyle

@InternalChartsApi
@Composable
fun LineChartStyle.toInternal(): LineChartInternalStyle =
    LineChartInternalStyle(
        modifier = wrapContentChartModifier(chartContainerStyle),
        chartContainerStyle = chartContainerStyle,
        dragPointColorSameAsLine = false,
        pointColorSameAsLine = false,
        pointColor = points.color,
        pointVisible = points.visible,
        pointSize = points.size.value,
        lineColor = line.color,
        lineAlpha = line.alpha,
        lineColors = line.colors.toList(),
        bezier = line.bezier,
        lineStrokeWidth = line.strokeWidth.value,
        dragPointSize = selection.size.value,
        dragPointVisible = selection.visible,
        dragActivePointSize = selection.activeSize.value,
        dragPointColor = selection.color,
        axisVisible = axis.visible,
        axisColor = axis.color,
        axisLineWidth = axis.lineWidth.value,
        yAxisLabelsVisible = axis.yLabels.visible,
        yAxisLabelColor = axis.yLabels.color,
        yAxisLabelSize = axis.yLabels.size,
        yAxisLabelCount = axis.yLabels.count,
        xAxisLabelsVisible = axis.xLabels.visible,
        xAxisLabelColor = axis.xLabels.color,
        xAxisLabelSize = axis.xLabels.size,
        xAxisLabelMaxCount = axis.xLabels.count,
        legendVisible = legend.visible,
        zoomControlsVisible = zoomControlsVisible,
    )
