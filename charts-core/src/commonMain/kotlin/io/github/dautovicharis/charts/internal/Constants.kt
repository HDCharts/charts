package io.github.dautovicharis.charts.internal

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val NO_SELECTION = -1
const val ANIMATION_TARGET = 1.0f

const val DEFAULT_SCALE = 1f
const val MAX_SCALE = 1.05f

val AXIS_LABEL_CHART_GAP: Dp = 10.dp

// Animation duration
const val ANIMATION_DURATION = 200
const val ANIMATION_DURATION_BAR = 500
const val ANIMATION_DURATION_LINE = 1200
const val ANIMATION_DURATION_LINE_CHART = 1700

// Animation duration offset
const val ANIMATION_OFFSET = 50

// Donut chart
const val DONUT_MIN_PERCENTAGE = 0f
const val DONUT_MAX_PERCENTAGE = 70f

object TestTags {
    const val CHART_ERROR = "ChartError"
    const val CHART_TITLE = "ChartTitle"
    const val PIE_CHART = "PieChart"
    const val BAR_CHART = "BarChart"
    const val HISTOGRAM_CHART = "HistogramChart"
    const val BAR_CHART_ZOOM_OUT = "BarChartZoomOut"
    const val BAR_CHART_ZOOM_IN = "BarChartZoomIn"
    const val BAR_CHART_DENSE_EXPAND = "BarChartDenseExpand"
    const val BAR_CHART_DENSE_COLLAPSE = "BarChartDenseCollapse"
    const val BAR_CHART_X_AXIS_LABELS = "BarChartXAxisLabels"
    const val BAR_CHART_Y_AXIS_LABELS = "BarChartYAxisLabels"
    const val BAR_CHART_PLOT = "BarChartPlot"
    const val LINE_CHART_X_AXIS_LABELS = "LineChartXAxisLabels"
    const val LINE_CHART_Y_AXIS_LABELS = "LineChartYAxisLabels"
    const val LINE_CHART_ZOOM_OUT = "LineChartZoomOut"
    const val LINE_CHART_ZOOM_IN = "LineChartZoomIn"
    const val LINE_CHART_DENSE_EXPAND = "LineChartDenseExpand"
    const val LINE_CHART_DENSE_COLLAPSE = "LineChartDenseCollapse"
    const val LINE_CHART_PLOT = "LineChartPlot"
    const val STACKED_BAR_CHART = "StackedBarChart"
    const val STACKED_AREA_CHART = "StackedAreaChart"
    const val STACKED_BAR_CHART_ZOOM_OUT = "StackedBarChartZoomOut"
    const val STACKED_BAR_CHART_ZOOM_IN = "StackedBarChartZoomIn"
    const val STACKED_BAR_CHART_DENSE_EXPAND = "StackedBarChartDenseExpand"
    const val STACKED_BAR_CHART_DENSE_COLLAPSE = "StackedBarChartDenseCollapse"
    const val STACKED_BAR_CHART_X_AXIS_LABELS = "StackedBarChartXAxisLabels"
    const val STACKED_BAR_CHART_Y_AXIS_LABELS = "StackedBarChartYAxisLabels"
    const val STACKED_BAR_CHART_PLOT = "StackedBarChartPlot"
    const val STACKED_AREA_CHART_ZOOM_OUT = "StackedAreaChartZoomOut"
    const val STACKED_AREA_CHART_ZOOM_IN = "StackedAreaChartZoomIn"
    const val STACKED_AREA_CHART_DENSE_EXPAND = "StackedAreaChartDenseExpand"
    const val STACKED_AREA_CHART_DENSE_COLLAPSE = "StackedAreaChartDenseCollapse"
    const val STACKED_AREA_CHART_X_AXIS_LABELS = "StackedAreaChartXAxisLabels"
    const val STACKED_AREA_CHART_Y_AXIS_LABELS = "StackedAreaChartYAxisLabels"
    const val STACKED_AREA_CHART_PLOT = "StackedAreaChartPlot"
    const val LINE_CHART = "LineChart"
    const val RADAR_CHART = "RadarChart"
}
