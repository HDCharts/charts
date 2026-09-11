package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.style.ChartContainerStyle

/**
 * Legacy flat bar chart style used by the internal renderer.
 *
 * The v3 public API exposes grouped blocks via [io.github.dautovicharis.charts.style.BarChartStyle].
 * Internal renderers keep this flat shape to minimize churn; the public composable adapts the
 * grouped style into this internal type. Application code should not reference this class.
 */
@InternalChartsApi
@Immutable
class BarChartInternalStyle(
    val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val barColor: Color,
    val barColors: List<Color>,
    val barAlpha: Float,
    val space: Dp,
    val minValue: Double?,
    val maxValue: Double?,
    val minBarWidth: Dp,
    val zoomControlsVisible: Boolean,
    val gridVisible: Boolean,
    val gridSteps: Int,
    val gridColor: Color,
    val gridLineWidth: Float,
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
    val selectionLineVisible: Boolean,
    val selectionLineColor: Color,
    val selectionLineWidth: Float,
)
