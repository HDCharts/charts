package io.github.hdcharts.charts.internal.stackedareachart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.internal.common.axis.AxisXLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.AxisXLayoutTick
import io.github.hdcharts.charts.internal.common.axis.AxisYLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.AxisYLayoutTick
import io.github.hdcharts.charts.internal.common.axis.buildNumericYAxisTicks
import io.github.hdcharts.charts.internal.common.axis.resolveAxisLabel

internal data class StackedAreaXAxisTick(
    val label: String,
    val centerX: Float,
)

internal data class StackedAreaYAxisTick(
    val label: String,
    val centerY: Float,
)

@Composable
internal fun StackedAreaXAxisLabels(
    ticks: List<StackedAreaXAxisTick>,
    color: Color,
    fontSize: TextUnit,
    tiltDegrees: Float,
    modifier: Modifier = Modifier,
) {
    AxisXLabelsLayout(
        ticks = ticks.map { tick -> AxisXLayoutTick(label = tick.label, centerX = tick.centerX) },
        color = color,
        fontSize = fontSize,
        tiltDegrees = tiltDegrees,
        modifier = modifier.testTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS),
    )
}

@Composable
internal fun StackedAreaYAxisLabels(
    ticks: List<StackedAreaYAxisTick>,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    AxisYLabelsLayout(
        ticks = ticks.map { tick -> AxisYLayoutTick(label = tick.label, centerY = tick.centerY) },
        color = color,
        fontSize = fontSize,
        modifier = modifier.testTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS),
    )
}

internal fun buildStackedAreaXAxisTicks(
    labels: List<String>,
    labelIndices: List<Int>,
    pointsCount: Int,
    stepX: Float,
    scrollOffsetPx: Float = 0f,
): List<StackedAreaXAxisTick> {
    if (pointsCount <= 0 || stepX <= 0f || labelIndices.isEmpty()) return emptyList()
    val safePointsCount = pointsCount.coerceAtLeast(1)
    val lastPointIndex = (safePointsCount - 1).coerceAtLeast(0)

    return labelIndices
        .distinct()
        .sorted()
        .map { index ->
            val safeIndex = index.coerceIn(0, lastPointIndex)
            val centerX =
                if (lastPointIndex == 0) {
                    -scrollOffsetPx
                } else {
                    (safeIndex * stepX) - scrollOffsetPx
                }
            StackedAreaXAxisTick(
                label = resolveAxisLabel(labels = labels, index = safeIndex),
                centerX = centerX,
            )
        }
}

internal fun buildStackedAreaYAxisTicks(
    minValue: Double,
    maxValue: Double,
    labelCount: Int,
    plotHeightPx: Float,
): List<StackedAreaYAxisTick> =
    buildNumericYAxisTicks(
        minValue = minValue,
        maxValue = maxValue,
        labelCount = labelCount,
        plotHeightPx = plotHeightPx,
        verticalInsetPx = 0f,
    ).map { tick ->
        StackedAreaYAxisTick(
            label = tick.label,
            centerY = tick.centerY,
        )
    }
