package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.common.model.ChartData

@InternalChartsApi
fun validateBarData(
    data: ChartData,
    colorsSize: Int = 0,
): List<String> {
    val validationErrors = mutableListOf<String>()
    val pointsSize = data.points.size

    if (pointsSize < MIN_REQUIRED_BAR) {
        val validationError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_BAR)
        validationErrors.add(validationError)
        return validationErrors
    }

    if (colorsSize > 0 && colorsSize != pointsSize) {
        val validationError =
            RULE_COLORS_SIZE_MISMATCH.format(colorsSize, pointsSize)
        validationErrors.add(validationError)
    }

    data.points.forEachIndexed { index, value ->
        if (value.isNaN()) {
            val validationError = ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(index)
            validationErrors.add(validationError)
        }
    }
    return validationErrors
}
