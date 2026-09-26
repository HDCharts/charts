package io.github.hdcharts.charts.internal.radarchart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.charts.internal.InternalChartsApi
import io.github.hdcharts.charts.style.ChartContainerStyle

@InternalChartsApi
@Immutable
class RadarInternalStyle(
    val chartContainerStyle: ChartContainerStyle,
    val gridColor: Color,
    val gridLineWidth: Dp,
    val gridSteps: Int,
    val gridVisible: Boolean,
    val axisLineColor: Color,
    val axisLineWidth: Dp,
    val axisVisible: Boolean,
    val axisLabelColor: Color,
    val axisLabelSize: TextUnit,
    val axisLabelPadding: Dp,
    val axisLabelVisible: Boolean,
    val categoryLegendVisible: Boolean,
    val categoryColors: List<Color>,
    val categoryPinSize: Dp,
    val categoryPinsVisible: Boolean,
    val pointColorSameAsLine: Boolean,
    val pointColor: Color,
    val pointSize: Dp,
    val pointVisible: Boolean,
    val lineColor: Color,
    val lineColors: List<Color>,
    val lineWidth: Dp,
    val fillAlpha: Float,
    val fillVisible: Boolean,
)
