package io.github.hdcharts.charts.internal

import io.github.hdcharts.charts.internal.ValidationErrors.MIN_REQUIRED_STACKED_BAR
import io.github.hdcharts.charts.internal.barstackedchart.StackedBarInternalStyle
import io.github.hdcharts.charts.internal.common.model.MultiChartData

@InternalChartsApi
fun validateBarData(
    data: MultiChartData,
    style: StackedBarInternalStyle,
): List<String> {
    val firstPointsSize =
        data.items
            .first()
            .item.points.size
    val colorsSize = style.barColors.size

    return validateMultiSeriesChartData(
        data = data,
        pointsSize = firstPointsSize,
        minRequiredPointsSize = MIN_REQUIRED_STACKED_BAR,
        colorsSize = colorsSize,
        expectedColorsSize = firstPointsSize,
    )
}
