package io.github.hdcharts.charts.internal.linechart

import androidx.compose.runtime.Composable
import io.github.hdcharts.charts.internal.common.layout.wrapContentChartModifier
import io.github.hdcharts.charts.style.LineChartStyle

@Composable
internal fun LineChartStyle.toInternal(): LineChartInternalStyle =
    LineChartInternalStyle(
        modifier = wrapContentChartModifier(chartContainerStyle),
        chartContainerStyle = chartContainerStyle,
        pointColor = points.color,
        pointVisible = points.visible,
        pointSize = points.size,
        line = line,
        dragPointSize = selection.size,
        dragPointVisible = selection.visible,
        dragActivePointSize = selection.activeSize,
        dragPointColor = selection.color,
        axisVisible = axis.visible,
        axisColor = axis.color,
        axisLineWidth = axis.lineWidth,
        minValue = range.min,
        maxValue = range.max,
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
