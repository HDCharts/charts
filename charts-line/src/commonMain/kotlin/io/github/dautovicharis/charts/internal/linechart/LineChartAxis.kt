package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.TextUnit
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.axis.AxisXLabelsLayout
import io.github.dautovicharis.charts.internal.common.axis.AxisXLayoutTick
import io.github.dautovicharis.charts.internal.common.axis.AxisYLabelsLayout
import io.github.dautovicharis.charts.internal.common.axis.AxisYLayoutTick
import io.github.dautovicharis.charts.internal.common.axis.buildNumericYAxisTicks
import io.github.dautovicharis.charts.internal.common.axis.resolveAxisLabel
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.model.ChartValueFormatter

internal data class LineAxisTick(
    val label: String,
    val centerX: Float,
)

internal data class LineYAxisTick(
    val label: String,
    val centerY: Float,
)

@Composable
internal fun LineXAxisLabels(
    ticks: List<LineAxisTick>,
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
        modifier = modifier.testTag(TestTags.LINE_CHART_X_AXIS_LABELS),
    )
}

@Composable
internal fun LineYAxisLabels(
    ticks: List<LineYAxisTick>,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    AxisYLabelsLayout(
        ticks = ticks.map { tick -> AxisYLayoutTick(label = tick.label, centerY = tick.centerY) },
        color = color,
        fontSize = fontSize,
        modifier = modifier.testTag(TestTags.LINE_CHART_Y_AXIS_LABELS),
    )
}

internal fun resolveLineXAxisLabels(data: MultiChartData): List<String> =
    when {
        data.hasSingleItem() ->
            data.items
                .firstOrNull()
                ?.item
                ?.labels
                ?.toList()
                ?.takeUnless { labels -> labels.all(String::isBlank) }
                .orEmpty()
        data.hasCategories() -> data.categories.toList()
        else -> emptyList()
    }

internal fun buildLineXAxisTicks(
    labels: List<String>,
    labelIndices: List<Int>,
    pointsCount: Int,
    stepX: Float,
    scrollOffsetPx: Float = 0f,
): List<LineAxisTick> {
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
            LineAxisTick(
                label = resolveAxisLabel(labels = labels, index = safeIndex),
                centerX = centerX,
            )
        }
}

internal fun buildLineYAxisTicks(
    minValue: Double,
    maxValue: Double,
    labelCount: Int,
    plotHeightPx: Float,
    verticalInsetPx: Float = 0f,
    formatter: ChartValueFormatter =
        ChartValueFormatter { value ->
            io.github.dautovicharis.charts.model.ChartValueFormatters.Default
                .format(value)
                .removeSuffix(".0")
        },
): List<LineYAxisTick> =
    buildNumericYAxisTicks(
        minValue = minValue,
        maxValue = maxValue,
        labelCount = labelCount,
        plotHeightPx = plotHeightPx,
        verticalInsetPx = verticalInsetPx,
    ).map { tick ->
        LineYAxisTick(
            label = tick.label.toDoubleOrNull()?.let(formatter::format) ?: tick.label,
            centerY = tick.centerY,
        )
    }
