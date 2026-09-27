package io.github.hdcharts.charts.internal.barstackedchart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.charts.internal.AXIS_LABEL_CHART_GAP
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.internal.common.axis.AxisXLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.AxisXLayoutTick
import io.github.hdcharts.charts.internal.common.axis.AxisYLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.AxisYLayoutTick
import io.github.hdcharts.charts.internal.common.axis.buildNumericYAxisTicks
import io.github.hdcharts.charts.internal.common.axis.estimateYAxisLabelWidthPx
import io.github.hdcharts.charts.internal.common.axis.resolveAxisLabel
import io.github.hdcharts.charts.style.AxisLabelStyle

internal data class StackedBarXAxisTick(
    val label: String,
    val centerX: Float,
)

internal data class StackedBarYAxisTick(
    val label: String,
    val centerY: Float,
)

internal class StackedBarYAxisLayout(
    val ticks: List<StackedBarYAxisTick>,
    val widthPx: Float,
    val gapPx: Float,
)

/** Y-axis ticks, label width and plot gap, shared by the viewport estimate and the laid-out chart. */
@Composable
internal fun rememberStackedBarYAxisLayout(
    labels: AxisLabelStyle,
    minValue: Double,
    maxValue: Double,
    chartHeightPx: Float,
): StackedBarYAxisLayout {
    val density = LocalDensity.current
    val ticks =
        remember(minValue, maxValue, chartHeightPx, labels.count) {
            buildStackedBarYAxisTicks(
                minValue = minValue,
                maxValue = maxValue,
                labelCount = labels.count,
                chartHeightPx = chartHeightPx,
            )
        }
    val widthPx =
        if (labels.visible) {
            estimateYAxisLabelWidthPx(
                labels = ticks.map { tick -> tick.label },
                fontSizePx = with(density) { labels.size.toPx() },
            )
        } else {
            0f
        }
    val gapPx = if (labels.visible) with(density) { AXIS_LABEL_CHART_GAP.toPx() } else 0f
    return StackedBarYAxisLayout(ticks = ticks, widthPx = widthPx, gapPx = gapPx)
}

@Composable
internal fun StackedBarXAxisLabels(
    ticks: List<StackedBarXAxisTick>,
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
        modifier = modifier.testTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS),
    )
}

@Composable
internal fun StackedBarYAxisLabels(
    ticks: List<StackedBarYAxisTick>,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    AxisYLabelsLayout(
        ticks = ticks.map { tick -> AxisYLayoutTick(label = tick.label, centerY = tick.centerY) },
        color = color,
        fontSize = fontSize,
        modifier = modifier.testTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS),
    )
}

internal fun buildStackedBarXAxisTicks(
    labels: List<String>,
    labelIndices: List<Int>,
    barWidthPx: Float,
    unitWidthPx: Float,
    scrollOffsetPx: Float,
): List<StackedBarXAxisTick> =
    labelIndices.map { index ->
        StackedBarXAxisTick(
            label = resolveAxisLabel(labels = labels, index = index),
            centerX = index * unitWidthPx + barWidthPx / 2f - scrollOffsetPx,
        )
    }

internal fun buildStackedBarYAxisTicks(
    minValue: Double,
    maxValue: Double,
    labelCount: Int,
    chartHeightPx: Float,
): List<StackedBarYAxisTick> =
    buildNumericYAxisTicks(
        minValue = minValue,
        maxValue = maxValue,
        labelCount = labelCount,
        plotHeightPx = chartHeightPx,
        verticalInsetPx = 0f,
    ).map { tick ->
        StackedBarYAxisTick(
            label = tick.label,
            centerY = tick.centerY,
        )
    }
