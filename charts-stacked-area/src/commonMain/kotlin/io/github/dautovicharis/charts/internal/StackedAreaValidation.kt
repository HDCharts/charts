package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_STACKED_AREA
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.stackedareachart.StackedAreaInternalStyle

@InternalChartsApi
fun validateStackedAreaData(
    data: MultiChartData,
    style: StackedAreaInternalStyle,
): List<String> {
    val firstPointsSize =
        data.items
            .first()
            .item.points.size
    val expectedColorsSize = data.items.size
    val validationErrors =
        validateMultiSeriesChartData(
            data = data,
            pointsSize = firstPointsSize,
            minRequiredPointsSize = MIN_REQUIRED_STACKED_AREA,
            colorsSize = style.areaColors.size,
            expectedColorsSize = expectedColorsSize,
        ).toMutableList()

    if (style.lineColors.isNotEmpty() && style.lineColors.size != expectedColorsSize) {
        validationErrors +=
            ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(
                style.lineColors.size,
                expectedColorsSize,
            )
    }

    data.items.forEachIndexed { itemIndex, dataItem ->
        dataItem.item.points.forEachIndexed { pointIndex, value ->
            if (value < 0) {
                validationErrors +=
                    ValidationErrors.RULE_ITEM_POINT_NEGATIVE.format(
                        itemIndex,
                        pointIndex,
                    )
            }
        }
    }

    return validationErrors
}
