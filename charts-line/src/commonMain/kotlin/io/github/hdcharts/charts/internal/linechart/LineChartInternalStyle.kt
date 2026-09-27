package io.github.hdcharts.charts.internal.linechart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.charts.style.ChartContainerStyle
import io.github.hdcharts.charts.style.LineVisualStyle

/** Flat renderer style retained temporarily behind the grouped v3 line API. */
@Immutable
internal class LineChartInternalStyle(
    val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val pointColor: Color,
    val pointVisible: Boolean,
    val pointSize: Dp,
    val line: LineVisualStyle,
    val dragPointSize: Dp,
    val dragPointVisible: Boolean,
    val dragActivePointSize: Dp,
    val dragPointColor: Color,
    val axisVisible: Boolean,
    val axisColor: Color,
    val axisLineWidth: Dp,
    val minValue: Double?,
    val maxValue: Double?,
    val yAxisLabelsVisible: Boolean,
    val yAxisLabelColor: Color,
    val yAxisLabelSize: TextUnit,
    val yAxisLabelCount: Int,
    val xAxisLabelsVisible: Boolean,
    val xAxisLabelColor: Color,
    val xAxisLabelSize: TextUnit,
    val xAxisLabelMaxCount: Int,
    val legendVisible: Boolean,
    val zoomControlsVisible: Boolean,
)
