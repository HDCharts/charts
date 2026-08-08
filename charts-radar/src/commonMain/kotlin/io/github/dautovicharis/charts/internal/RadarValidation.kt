package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_RADAR
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.style.RadarChartStyle

@InternalChartsApi
fun validateRadarData(
    data: MultiChartData,
    style: RadarChartStyle,
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
        minRequiredPointsSize = MIN_REQUIRED_RADAR,
        colorsSize = colorsSize,
        expectedColorsSize = expectedColorsSize,
    )
}
