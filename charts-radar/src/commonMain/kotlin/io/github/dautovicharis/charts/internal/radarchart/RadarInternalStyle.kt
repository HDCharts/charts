package io.github.dautovicharis.charts.internal.radarchart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.ChartContainerStyle

@InternalChartsApi
@Immutable
class RadarInternalStyle(
    val chartContainerStyle: ChartContainerStyle,
    val gridColor: Color,
    val gridLineWidth: Float,
    val gridSteps: Int,
    val gridVisible: Boolean,
    val axisLineColor: Color,
    val axisLineWidth: Float,
    val axisVisible: Boolean,
    val axisLabelColor: Color,
    val axisLabelSize: TextUnit,
    val axisLabelPadding: Float,
    val axisLabelVisible: Boolean,
    val categoryLegendVisible: Boolean,
    val categoryColors: List<Color>,
    val categoryPinSize: Float,
    val categoryPinsVisible: Boolean,
    val pointColorSameAsLine: Boolean,
    val pointColor: Color,
    val pointSize: Float,
    val pointVisible: Boolean,
    val lineColor: Color,
    val lineColors: List<Color>,
    val lineWidth: Float,
    val fillAlpha: Float,
    val fillVisible: Boolean,
)
