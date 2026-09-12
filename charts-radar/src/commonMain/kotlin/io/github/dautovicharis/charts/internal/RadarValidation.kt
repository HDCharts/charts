package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_RADAR
import io.github.dautovicharis.charts.model.ChartData

@InternalChartsApi
fun validateRadarData(
    data: ChartData,
    paletteSize: Int,
    categoryPaletteSize: Int,
    categoryCount: Int,
): List<String> {
    val errors = mutableListOf<String>()
    if (data.series.isEmpty()) {
        return listOf("At least one radar series is required.")
    }
    val axisCount =
        data.series
            .first()
            .values.size
    if (axisCount < MIN_REQUIRED_RADAR) {
        errors += ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_RADAR)
    }
    if (data.categories.isNotEmpty() && data.categories.size != axisCount) {
        errors +=
            ValidationErrors.RULE_CATEGORIES_SIZE_MISMATCH.format(
                data.categories.size,
                axisCount,
            )
    }
    data.series.forEachIndexed { index, series ->
        if (series.values.size != axisCount) {
            errors +=
                ValidationErrors.RULE_ITEM_POINTS_SIZE.format(
                    index,
                    series.values.size,
                    axisCount,
                )
        }
        series.values.forEachIndexed { pointIndex, value ->
            if (!value.isFinite()) {
                errors +=
                    ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(pointIndex)
            }
        }
    }
    if (paletteSize > 0 && paletteSize != data.series.size) {
        errors +=
            ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(
                paletteSize,
                data.series.size,
            )
    }
    if (categoryPaletteSize > 0 && categoryPaletteSize != categoryCount) {
        errors +=
            ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(
                categoryPaletteSize,
                categoryCount,
            )
    }
    return errors
}
