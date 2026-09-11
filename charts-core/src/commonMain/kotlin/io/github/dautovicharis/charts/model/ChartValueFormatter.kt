package io.github.dautovicharis.charts.model

import kotlin.math.abs

/**
 * Formats a raw chart value for display.
 *
 * Used for axis labels, tooltips, and chart-specific value readouts.
 * Implementations should be deterministic for a given input.
 */
fun interface ChartValueFormatter {
    fun format(value: Double): String
}

/**
 * Standard [ChartValueFormatter] factories.
 *
 * Finite values are rounded half away from zero using the decimal representation
 * returned by `abs(value).toString()`, not the exact binary fraction. Scientific
 * notation is expanded, and negative zero is suppressed. Nonfinite values are
 * displayed as `NaN`, `Infinity`, and `-Infinity`.
 *
 * This assumes the platform's Double string uses decimal digits, an optional dot,
 * and an optional `e`/`E` exponent. Last-digit differences between platform string
 * representations can affect rounding at a boundary.
 */
object ChartValueFormatters {
    /**
     * Rounds to two decimal places, then trims trailing zeros while keeping `.0`
     * for integers, including zero. For example: `3.0`, `41.7`, and `0.0`.
     */
    val Default: ChartValueFormatter = ChartValueFormatter(::formatDefaultValue)

    /**
     * Formats a value with the given [prefix] prepended, e.g. `prefix("?")`.
     */
    fun prefix(prefix: String): ChartValueFormatter = ChartValueFormatter { value -> "$prefix${Default.format(value)}" }

    /**
     * Formats a value with the given [suffix] appended, e.g. `suffix("%")`.
     */
    fun suffix(suffix: String): ChartValueFormatter = ChartValueFormatter { value -> "${Default.format(value)}$suffix" }

    /**
     * Rounds and pads finite values to exactly [precision] decimal places, with no
     * decimal point when [precision] is zero. Precision must be in `0..15`.
     *
     * @throws IllegalArgumentException if [precision] is outside `0..15`.
     */
    fun fixed(precision: Int): ChartValueFormatter {
        require(precision in 0..15) { "precision must be in 0..15, was $precision" }
        return ChartValueFormatter { value -> formatFixedValue(value, precision) }
    }

    private fun formatDefaultValue(value: Double): String {
        val trimmed = formatFixedValue(value, 2).trimEnd('0')
        return if (trimmed.endsWith('.')) "${trimmed}0" else trimmed
    }

    private fun formatFixedValue(
        value: Double,
        precision: Int,
    ): String {
        if (value.isNaN()) return "NaN"
        if (value == Double.POSITIVE_INFINITY) return "Infinity"
        if (value == Double.NEGATIVE_INFINITY) return "-Infinity"

        val source = abs(value).toString()
        val exponentIndex = source.indexOf('e', ignoreCase = true)
        val mantissa = if (exponentIndex < 0) source else source.substring(0, exponentIndex)
        val exponent = if (exponentIndex < 0) 0 else source.substring(exponentIndex + 1).toInt()
        val decimalPoint = mantissa.indexOf('.').let { if (it < 0) mantissa.length else it }
        val digits = mantissa.replace(".", "")

        // Keep only digits through the rounding place; even MAX_VALUE needs at most
        // 309 integer + 15 fractional digits. Tiny exponents need no zero expansion.
        val retainedLength = decimalPoint + exponent + precision
        val rounded =
            StringBuilder(
                if (retainedLength <= 0) "0" else digits.take(retainedLength).padEnd(retainedLength, '0'),
            )
        if (retainedLength >= 0 && (digits.getOrNull(retainedLength) ?: '0') >= '5') {
            var index = rounded.lastIndex
            while (index >= 0 && rounded[index] == '9') {
                rounded[index] = '0'
                index--
            }
            if (index < 0) rounded.insert(0, '1') else rounded[index] = rounded[index] + 1
        }

        val padded = rounded.toString().padStart(precision + 1, '0')
        val magnitude = if (precision == 0) padded else padded.dropLast(precision) + "." + padded.takeLast(precision)
        return if (value < 0.0 && padded.any { it != '0' }) "-$magnitude" else magnitude
    }
}
