package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Public style alias for histogram charts.
 */
typealias HistogramChartStyle = BarChartStyle

/**
 * Defaults for [HistogramChartStyle].
 *
 * Histogram defaults enforce contiguous bins and zero baseline.
 */
object HistogramChartDefaults {
    @Composable
    fun style(
        barColor: Color = MaterialTheme.colorScheme.primary,
        barColors: List<Color> = emptyList(),
        barAlpha: Float = defaultChartAlpha(),
        space: Dp = 0.dp,
        minValue: Float? = 0f,
        maxValue: Float? = null,
        minBarWidth: Dp = 10.dp,
        zoomControlsVisible: Boolean = true,
        gridVisible: Boolean = true,
        gridSteps: Int = 4,
        gridColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
        gridLineWidth: Float = 1f,
        axisVisible: Boolean = true,
        axisColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        axisLineWidth: Float = 1f,
        xAxisLabelsVisible: Boolean = true,
        xAxisLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        xAxisLabelSize: TextUnit = 11.sp,
        xAxisLabelMaxCount: Int = 6,
        selectionLineVisible: Boolean = true,
        selectionLineColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        selectionLineWidth: Float = 1f,
        chartViewStyle: ChartViewStyle = ChartViewDefaults.style(),
        yAxisLabelsVisible: Boolean = true,
        yAxisLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        yAxisLabelSize: TextUnit = 11.sp,
        yAxisLabelCount: Int = 5,
    ): HistogramChartStyle =
        BarChartDefaults.style(
            barColor = barColor,
            barColors = barColors,
            barAlpha = barAlpha,
            space = space,
            minValue = minValue,
            maxValue = maxValue,
            minBarWidth = minBarWidth,
            zoomControlsVisible = zoomControlsVisible,
            gridVisible = gridVisible,
            gridSteps = gridSteps,
            gridColor = gridColor,
            gridLineWidth = gridLineWidth,
            axisVisible = axisVisible,
            axisColor = axisColor,
            axisLineWidth = axisLineWidth,
            xAxisLabelsVisible = xAxisLabelsVisible,
            xAxisLabelColor = xAxisLabelColor,
            xAxisLabelSize = xAxisLabelSize,
            xAxisLabelMaxCount = xAxisLabelMaxCount,
            selectionLineVisible = selectionLineVisible,
            selectionLineColor = selectionLineColor,
            selectionLineWidth = selectionLineWidth,
            chartViewStyle = chartViewStyle,
            yAxisLabelsVisible = yAxisLabelsVisible,
            yAxisLabelColor = yAxisLabelColor,
            yAxisLabelSize = yAxisLabelSize,
            yAxisLabelCount = yAxisLabelCount,
        )
}
