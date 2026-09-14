package io.github.hdcharts.charts.internal.barchart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.internal.common.axis.AxisXLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.AxisXLayoutTick
import io.github.hdcharts.charts.internal.common.model.ChartData

internal data class AxisTick(
    val label: String,
    val centerX: Float,
)

@Composable
internal fun BarXAxisLabels(
    ticks: List<AxisTick>,
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
        modifier = modifier.testTag(TestTags.BAR_CHART_X_AXIS_LABELS),
    )
}

internal fun buildAxisTicks(
    chartData: ChartData,
    labelIndices: List<Int>,
    barWidthPx: Float,
    unitWidthPx: Float,
    scrollOffsetPx: Float,
): List<AxisTick> =
    labelIndices.map { index ->
        AxisTick(
            label = resolveAxisLabel(labels = chartData.labels, index = index),
            centerX = index * unitWidthPx + barWidthPx / 2f - scrollOffsetPx,
        )
    }
