package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.ChartContainerStyle

/** Flat renderer style retained temporarily behind the grouped v3 line API. */
@InternalChartsApi
@Immutable
class LineChartInternalStyle(
    val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val dragPointColorSameAsLine: Boolean,
    val pointColorSameAsLine: Boolean,
    val pointColor: Color,
    val pointVisible: Boolean,
    val pointSize: Float,
    val lineColor: Color,
    val lineAlpha: Float,
    val lineColors: List<Color>,
    val bezier: Boolean,
    val lineStrokeWidth: Float,
    val dragPointSize: Float,
    val dragPointVisible: Boolean,
    val dragActivePointSize: Float,
    val dragPointColor: Color,
    val axisVisible: Boolean,
    val axisColor: Color,
    val axisLineWidth: Float,
    val yAxisLabelsVisible: Boolean,
    val yAxisLabelColor: Color,
    val yAxisLabelSize: TextUnit,
    val yAxisLabelCount: Int,
    val xAxisLabelsVisible: Boolean,
    val xAxisLabelColor: Color,
    val xAxisLabelSize: TextUnit,
    val xAxisLabelMaxCount: Int,
    val zoomControlsVisible: Boolean,
)
