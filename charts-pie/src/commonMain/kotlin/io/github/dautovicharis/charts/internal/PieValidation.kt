package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_PIE
import io.github.dautovicharis.charts.model.PieSlice

@InternalChartsApi
fun validatePieData(data: List<PieSlice>): List<String> {
    val validationErrors = mutableListOf<String>()
    val pointsSize = data.size

    if (pointsSize < MIN_REQUIRED_PIE) {
        validationErrors +=
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_PIE)
        return validationErrors
    }

    data.forEachIndexed { index, slice ->
        if (slice.value.isNaN()) {
            validationErrors += ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(index)
        } else if (!slice.value.isFinite()) {
            validationErrors += ValidationErrors.RULE_DATA_POINT_NOT_FINITE.format(index)
        } else if (slice.value < 0) {
            validationErrors += ValidationErrors.RULE_DATA_POINT_NEGATIVE.format(index)
        }
    }
    return validationErrors
}
