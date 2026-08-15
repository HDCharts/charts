package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_PIE
import io.github.dautovicharis.charts.model.PieSlice

@InternalChartsApi
fun validatePieData(data: List<PieSlice>): List<String> {
    val validationErrors = mutableListOf<String>()
    val pointsSize = data.size

    if (pointsSize < MIN_REQUIRED_PIE) {
        val validationError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_PIE)
        validationErrors.add(validationError)
        return validationErrors
    }

    data.forEachIndexed { index, slice ->
        if (slice.value.isNaN()) {
            val validationError = ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(index)
            validationErrors.add(validationError)
        } else if (slice.value < 0) {
            val validationError = ValidationErrors.RULE_DATA_POINT_NEGATIVE.format(index)
            validationErrors.add(validationError)
        }
    }
    return validationErrors
}
