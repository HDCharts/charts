package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_PIE
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.style.PieChartStyle

@InternalChartsApi
fun validatePieData(
    dataSet: ChartDataSet,
    style: PieChartStyle,
): List<String> {
    val validationErrors = mutableListOf<String>()
    val pointsSize = dataSet.data.item.points.size
    val colorsSize = style.pieColors.size

    if (pointsSize < MIN_REQUIRED_PIE) {
        val validationError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_PIE)
        validationErrors.add(validationError)
        return validationErrors
    }

    dataSet.data.item.points.forEachIndexed { index, value ->
        if (value.isNaN()) {
            val validationError = ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(index)
            validationErrors.add(validationError)
        } else if (value < 0) {
            val validationError = ValidationErrors.RULE_DATA_POINT_NEGATIVE.format(index)
            validationErrors.add(validationError)
        }
    }

    if (colorsSize > 0 && colorsSize != pointsSize) {
        val validationError =
            ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(colorsSize, pointsSize)
        validationErrors.add(validationError)
    }
    return validationErrors
}
