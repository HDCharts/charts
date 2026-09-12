package io.github.dautovicharis.charts.internal.barstackedchart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.ChartContainerStyle

@InternalChartsApi
@Immutable
class StackedBarInternalStyle(
    val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val barColor: Color,
    val barAlpha: Float,
    val space: Dp,
    val barColors: List<Color>,
    val minBarWidth: Dp,
    val zoomControlsVisible: Boolean,
    val yAxisLabelsVisible: Boolean,
    val yAxisLabelColor: Color,
    val yAxisLabelSize: TextUnit,
    val yAxisLabelCount: Int,
    val xAxisLabelsVisible: Boolean,
    val xAxisLabelColor: Color,
    val xAxisLabelSize: TextUnit,
    val xAxisLabelMaxCount: Int,
    val selectionLineVisible: Boolean,
    val selectionLineColor: Color,
    val selectionLineWidth: Float,
)
