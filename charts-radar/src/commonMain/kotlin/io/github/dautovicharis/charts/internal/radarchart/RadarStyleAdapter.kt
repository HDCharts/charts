package io.github.dautovicharis.charts.internal.radarchart

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.RadarChartStyle

@InternalChartsApi
@Composable
fun RadarChartStyle.toInternal(): RadarInternalStyle =
    RadarInternalStyle(
        chartContainerStyle = chartContainerStyle,
        gridColor = grid.color,
        gridLineWidth = grid.lineWidth,
        gridSteps = grid.steps,
        gridVisible = grid.visible,
        axisLineColor = axes.lineColor,
        axisLineWidth = axes.lineWidth,
        axisVisible = axes.visible,
        axisLabelColor = axes.labelColor,
        axisLabelSize = axes.labelSize,
        axisLabelPadding = axes.labelPadding,
        axisLabelVisible = axes.labelVisible,
        categoryLegendVisible = categories.legendVisible,
        categoryColors = categories.colors,
        categoryPinSize = categories.pinSize,
        categoryPinsVisible = categories.pinsVisible,
        pointColorSameAsLine = points.colorSameAsLine,
        pointColor = points.color,
        pointSize = points.size,
        pointVisible = points.visible,
        lineColor = polygon.lineColor,
        lineColors = polygon.lineColors,
        lineWidth = polygon.lineWidth,
        fillAlpha = polygon.fillAlpha,
        fillVisible = polygon.fillVisible,
    )
