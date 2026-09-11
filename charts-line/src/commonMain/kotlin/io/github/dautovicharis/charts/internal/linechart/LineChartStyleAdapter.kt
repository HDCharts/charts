package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.LineChartStyle

@InternalChartsApi
@Composable
fun LineChartStyle.toInternal(modifier: Modifier = Modifier): LineChartInternalStyle =
    LineChartInternalStyle(
        modifier = modifier.then(chartContainerStyle.wrapContentChartModifier()),
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
        zoomControlsVisible = zoomControlsVisible,
    )
