package io.github.hdcharts.charts.internal.barchart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.charts.internal.AXIS_LABEL_CHART_GAP
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.internal.common.axis.AxisYLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.AxisYLayoutTick
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.style.AxisLabelStyle
import io.github.hdcharts.charts.style.BarChartDefaults

internal data class YAxisTick(
    val label: String,
    val centerY: Float,
)

internal class BarYAxisLayout(
    val ticks: List<YAxisTick>,
    val widthPx: Float,
    val gapPx: Float,
)

/** Y-axis ticks, label width and plot gap, shared by the viewport estimate and the laid-out chart. */
@Composable
internal fun rememberBarYAxisLayout(
    labels: AxisLabelStyle,
    minValue: Double,
    maxValue: Double,
    chartHeightPx: Float,
    formatter: ChartValueFormatter,
    availableWidthPx: Int,
): BarYAxisLayout {
    val density = LocalDensity.current
    val ticks =
        remember(minValue, maxValue, chartHeightPx, labels.count, formatter) {
            buildYAxisTicks(
                minValue = minValue,
                maxValue = maxValue,
                labelCount = labels.count,
                chartHeightPx = chartHeightPx,
                formatter = formatter,
            )
        }
    val widthPx =
        if (labels.visible) {
            barYAxisWidthPx(
                ticks = ticks,
                fontSizePx = with(density) { labels.size.toPx() },
                availableWidthPx = availableWidthPx,
            )
        } else {
            0f
        }
    val gapPx =
        if (labels.visible) {
            with(density) { AXIS_LABEL_CHART_GAP.roundToPx().toFloat() }
                .coerceAtMost((availableWidthPx - widthPx - 1f).coerceAtLeast(0f))
        } else {
            0f
        }
    return BarYAxisLayout(ticks = ticks, widthPx = widthPx, gapPx = gapPx)
}

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
