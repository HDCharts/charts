package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_LINE
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.linechart.LineChartInternalStyle

@InternalChartsApi
fun validateLineData(
    data: MultiChartData,
    style: LineChartInternalStyle,
): List<String> {
    val firstPointsSize =
        data.items
            .first()
            .item.points.size

    val colorsSize = style.lineColors.size
    val expectedColorsSize = data.items.size

    return validateMultiSeriesChartData(
        data = data,
        pointsSize = firstPointsSize,
        minRequiredPointsSize = MIN_REQUIRED_LINE,
        colorsSize = colorsSize,
        expectedColorsSize = expectedColorsSize,
    )
}
