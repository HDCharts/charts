package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.TextUnit
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.axis.AxisYLabelsLayout
import io.github.dautovicharis.charts.internal.common.axis.AxisYLayoutTick
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.style.BarChartDefaults

internal data class YAxisTick(
    val label: String,
    val centerY: Float,
)

@Composable
internal fun BarYAxisLabels(
    ticks: List<YAxisTick>,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    AxisYLabelsLayout(
        ticks = ticks.map { tick -> AxisYLayoutTick(label = tick.label, centerY = tick.centerY) },
        color = color,
        fontSize = fontSize,
        modifier = modifier.testTag(TestTags.BAR_CHART_Y_AXIS_LABELS),
    )
}

internal fun buildYAxisTicks(
    minValue: Double,
    maxValue: Double,
    labelCount: Int,
    chartHeightPx: Float,
    formatter: ChartValueFormatter = BarChartDefaults.axisValueFormatter,
): List<YAxisTick> {
    if (chartHeightPx <= 0f) return emptyList()
    val steps = labelCount.coerceAtLeast(2) - 1
    return (0..steps).map { step ->
        val progress = step.toDouble() / steps
        // A convex combination avoids overflowing (max - min) for extreme signed data.
        val value = maxValue * (1.0 - progress) + minValue * progress
        YAxisTick(
            label = formatter.format(value),
            centerY = chartHeightPx * progress.toFloat(),
        )
    }
}

internal fun formatAxisValue(value: Double): String = BarChartDefaults.axisValueFormatter.format(value)
