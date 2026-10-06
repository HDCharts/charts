package io.github.hdcharts.core.internal

import io.github.hdcharts.core.model.ChartData

/** Data error messages shared by every chart, so the same rule reads the same way everywhere. */
@InternalChartsApi
object ValidationErrors {
    /** Fewest values a chart needs to draw. */
    const val MIN_VALUES: Int = 2

    /** Fewest values a radar chart needs to draw a polygon. */
    const val MIN_RADAR_VALUES: Int = 3

    /** Fewest values a ring gauge needs: one ring is a valid gauge. */
    const val MIN_RING_GAUGE_VALUES: Int = 1

    fun noSeries(): String = "At least one series is required."

    fun exactlyOneSeries(count: Int): String = "Exactly one series is required; got $count."

    fun tooFewValues(min: Int): String =
        if (min == 1) "At least one value is required." else "At least $min values are required."

    fun categoryCountMismatch(
        categories: Int,
        values: Int,
    ): String = "Category count ($categories) must match value count ($values)."

    /** [target] names what the colors are matched to, such as "value" or "series". */
    fun colorCountMismatch(
        colors: Int,
        expected: Int,
        target: String,
    ): String = "Color count ($colors) must match $target count ($expected)."

    fun seriesNotAligned(series: Int): String = "Series $series is not aligned with the first series."

    fun nonFiniteSeriesValue(series: Int): String = "Series $series contains a non-finite value."

    fun negativeSeriesValue(series: Int): String = "Series $series contains a negative value."

    fun nonFiniteValue(index: Int): String = "Value at index $index is not finite."

    fun negativeValue(index: Int): String = "Value at index $index is negative."

    fun missingAxisLabels(): String = "Axis label styles are missing."

    fun nonFiniteRange(): String = "Range bounds must be finite."
}

/**
 * Returns the errors of charts that draw aligned series: no series, fewer than [minValues] values, a
 * category count that does not match, misaligned series, non-finite values, and [colorCount] colors
 * that do not match [expectedColors]. With [allowNegative] set to `false`, negative values are
 * errors.
 *
 * [expectedColors] is null when the chart's policy skips the color check, which is how a chart whose
 * color count depends on the data says so.
 */
@InternalChartsApi
fun validateSeries(
    data: ChartData,
    minValues: Int,
    allowNegative: Boolean,
    colorCount: Int,
    expectedColors: Int?,
): List<String> {
    if (data.series.isEmpty()) return listOf(ValidationErrors.noSeries())
    val errors = mutableListOf<String>()
    val valueCount =
        data.series
            .first()
            .values.size
    if (valueCount < minValues) errors += ValidationErrors.tooFewValues(min = minValues)
    if (data.categories.isNotEmpty() && data.categories.size != valueCount) {
        errors += ValidationErrors.categoryCountMismatch(categories = data.categories.size, values = valueCount)
    }
    data.series.forEachIndexed { index, series ->
        if (series.values.size != valueCount) errors += ValidationErrors.seriesNotAligned(series = index)
        if (series.values.any { !it.isFinite() }) errors += ValidationErrors.nonFiniteSeriesValue(series = index)
        if (!allowNegative && series.values.any { it < 0.0 }) {
            errors += ValidationErrors.negativeSeriesValue(series = index)
        }
    }
    errors += validateColorCount(colors = colorCount, expected = expectedColors, target = "series")
    return errors
}

/**
 * Returns the errors of charts that draw one series: not exactly one series, fewer than [minValues]
 * values, [colorCount] colors that do not match the value count, a category count that does not
 * match, and each bad value. With [allowNegative] set to `false`, negative values are errors.
 */
@InternalChartsApi
fun validateSingleSeries(
    data: ChartData,
    minValues: Int,
    allowNegative: Boolean,
    colorCount: Int,
): List<String> {
    if (data.series.size != 1) return listOf(ValidationErrors.exactlyOneSeries(count = data.series.size))
    val values = data.series.single().values
    if (values.size < minValues) {
        return listOf(ValidationErrors.tooFewValues(min = minValues))
    }
    val errors = validateColorCount(colors = colorCount, expected = values.size, target = "value").toMutableList()
    if (data.categories.isNotEmpty() && data.categories.size != values.size) {
        errors += ValidationErrors.categoryCountMismatch(categories = data.categories.size, values = values.size)
    }
    errors += validateValues(values = values, allowNegative = allowNegative)
    return errors
}

/** Returns an error for each value that is not finite or, with [allowNegative] set to `false`, negative. */
@InternalChartsApi
fun validateValues(
    values: List<Double>,
    allowNegative: Boolean,
): List<String> =
    values.mapIndexedNotNull { index, value ->
        when {
            !value.isFinite() -> ValidationErrors.nonFiniteValue(index = index)
            !allowNegative && value < 0.0 -> ValidationErrors.negativeValue(index = index)
            else -> null
        }
    }

/**
 * Returns an error when [colors] is set and does not match [expected]; [target] names what they
 * color. A null [expected] is a chart that skips the check, and [colors] of zero is a style that sets
 * none.
 */
@InternalChartsApi
fun validateColorCount(
    colors: Int,
    expected: Int?,
    target: String,
): List<String> =
    if (expected != null && colors > 0 && colors != expected) {
        listOf(ValidationErrors.colorCountMismatch(colors = colors, expected = expected, target = target))
    } else {
        emptyList()
    }

/** Returns an error when a fixed range bound is not finite. */
@InternalChartsApi
fun validateRange(
    min: Double?,
    max: Double?,
): List<String> =
    if (min?.isFinite() == false || max?.isFinite() == false) listOf(ValidationErrors.nonFiniteRange()) else emptyList()
