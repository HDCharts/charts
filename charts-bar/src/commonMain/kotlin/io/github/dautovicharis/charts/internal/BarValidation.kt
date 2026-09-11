package io.github.dautovicharis.charts.internal

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnitType
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.style.BarChartStyle

@InternalChartsApi
fun validateBarData(
    data: ChartData,
    colorsSize: Int = 0,
): List<String> {
    val validationErrors = mutableListOf<String>()
    if (data.series.size != 1) return listOf("Exactly one series is required; got ${data.series.size}.")
    val points = data.series.single().values
    val pointsSize = points.size

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

    if (data.categories.isNotEmpty() && data.categories.size != pointsSize) {
        validationErrors.add("Category count (${data.categories.size}) must match value count ($pointsSize).")
    }

    points.forEachIndexed { index, value ->
        if (!value.isFinite()) {
            val validationError = ValidationErrors.RULE_DATA_POINT_NOT_NUMBER.format(index)
            validationErrors.add(validationError)
        }
    }
    return validationErrors
}

@InternalChartsApi
fun validateBarStyle(
    style: BarChartStyle,
    density: Density = Density(1f),
): List<String> {
    val errors = mutableListOf<String>()
    val dimensions =
        listOf(
            "bar spacing" to style.bars.space.value,
            "minimum bar width" to style.bars.minBarWidth.value,
            "grid width" to style.grid.lineWidth.value,
            "axis width" to style.axis.lineWidth.value,
            "selection width" to style.selectionLine.width.value,
        )
    dimensions.forEach { (name, value) ->
        val pixels = value * density.density
        if (!pixels.isFinite() || pixels !in 0f..16_384f) errors.add("$name must resolve to 0..16384 pixels.")
    }
    if (!style.bars.alpha.isFinite() || style.bars.alpha !in 0f..1f) errors.add("Bar alpha must be in 0..1.")
    if (style.range.min?.isFinite() == false || style.range.max?.isFinite() == false) {
        errors.add("Range bounds must be finite.")
    }
    if (style.grid.steps !in 0..1000) errors.add("Grid steps must be in 0..1000.")
    listOf(style.axis.xLabels, style.axis.yLabels).forEach { labels ->
        if (labels.size.type != TextUnitType.Sp || !labels.size.value.isFinite() || labels.size.value <= 0f) {
            errors.add("Axis label size must be finite, positive sp.")
        } else if (with(density) { labels.size.toPx() }.let { !it.isFinite() || it > 16_384f }) {
            errors.add("Axis label size must resolve to at most 16384 pixels.")
        }
        if (labels.count !in 2..1000) errors.add("Axis label count must be in 2..1000.")
    }
    return errors
}
