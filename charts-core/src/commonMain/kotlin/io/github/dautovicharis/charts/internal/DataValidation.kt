package io.github.dautovicharis.charts.internal

import io.github.dautovicharis.charts.internal.common.model.MultiChartData

@InternalChartsApi
object ValidationErrors {
    const val RULE_ITEM_POINTS_SIZE: String = "Item at index %d has %d points, expected %d."
    const val RULE_ITEM_POINT_NOT_NUMBER: String =
        "Item at index %d has non-numeric value at index %d."
    const val RULE_CATEGORIES_SIZE_MISMATCH: String =
        "Categories size %d does not match expected %d."
    const val RULE_COLORS_SIZE_MISMATCH: String = "Colors size %d does not match expected %d."
    const val RULE_DATA_POINTS_LESS_THAN_MIN: String =
        "Data points size should be greater than or equal to %d."
    const val RULE_DATA_POINT_NOT_NUMBER: String =
        "Data point at index %d is not a valid number."
    const val RULE_DATA_POINT_NEGATIVE: String =
        "Data point at index %d must be non-negative."
    const val RULE_ITEM_POINT_NEGATIVE: String =
        "Item at index %d has negative value at index %d."

    const val MIN_REQUIRED_PIE: Int = 2
    const val MIN_REQUIRED_LINE: Int = 2
    const val MIN_REQUIRED_STACKED_AREA: Int = 2
    const val MIN_REQUIRED_STACKED_BAR: Int = 1
    const val MIN_REQUIRED_BAR: Int = 2
    const val MIN_REQUIRED_HISTOGRAM: Int = 2
    const val MIN_REQUIRED_RADAR: Int = 3
}

@InternalChartsApi
fun String.format(vararg args: Any?): String {
    if (args.isEmpty()) return this
    return args.fold(this) { formattedString, arg ->
        formattedString.replaceFirst("%d", arg.toString())
    }
}

@InternalChartsApi
fun validateMultiSeriesChartData(
    data: MultiChartData,
    pointsSize: Int,
    minRequiredPointsSize: Int,
    colorsSize: Int,
    expectedColorsSize: Int,
): List<String> {
    val validationErrors = mutableListOf<String>()

    // Rule 1: pointsSize should be greater than minRequiredPointsSize
    if (pointsSize < minRequiredPointsSize) {
        val validationError =
            ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN.format(minRequiredPointsSize)
        validationErrors.add(validationError)
        return validationErrors
    }

    // Rule 2: Each item should have the same number of points
    data.items.forEachIndexed { index, dataItem ->
        if (dataItem.item.points.size != pointsSize) {
            val validationError =
                ValidationErrors.RULE_ITEM_POINTS_SIZE.format(
                    index,
                    dataItem.item.points.size,
                    pointsSize,
                )
            validationErrors.add(validationError)
        }
    }

    // Rule 3: If categories are not empty, it should match pointsSize
    if (data.hasCategories() && data.categories.size != pointsSize) {
        val validationError =
            ValidationErrors.RULE_CATEGORIES_SIZE_MISMATCH.format(
                data.categories.size,
                pointsSize,
            )
        validationErrors.add(validationError)
    }

    // Rule 4: If colors are not empty, it should match expectedColorsSize
    if (colorsSize > 0 && colorsSize != expectedColorsSize) {
        val validationError =
            ValidationErrors.RULE_COLORS_SIZE_MISMATCH.format(colorsSize, expectedColorsSize)
        validationErrors.add(validationError)
    }

    data.items.forEachIndexed { itemIndex, dataItem ->
        dataItem.item.points.forEachIndexed { pointIndex, value ->
            if (value.isNaN()) {
                val validationError =
                    ValidationErrors.RULE_ITEM_POINT_NOT_NUMBER.format(itemIndex, pointIndex)
                validationErrors.add(validationError)
            }
        }
    }
    return validationErrors
}
