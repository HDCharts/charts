package io.github.dautovicharis.charts.model

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Formats a raw chart value for display.
 *
 * Used for axis labels, tooltips, and chart-specific value readouts.
 * Implementations must be deterministic across all platforms.
 */
fun interface ChartValueFormatter {
    fun format(value: Float): String
}

/**
 * Standard [ChartValueFormatter] factories.
 */
object ChartValueFormatters {
    /**
     * Default format: values are rounded to two decimals, near-zero values are displayed
     * as `0`, and the result matches the platform `toString` of the rounded double.
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
     * Formats a value rounded to the given number of [precision] decimal places.
     */
    fun fixed(precision: Int): ChartValueFormatter {
        require(precision >= 0) { "precision must be non-negative, was $precision" }
        val factor = 10.0.pow(precision)
        return ChartValueFormatter { value ->
            val rounded = (value.toDouble() * factor).roundToInt() / factor
            rounded.toString()
        }
    }

    // Avoid displaying "-0.0" for tiny values after rounding to two decimals.
    private const val NEAR_ZERO_DISPLAY_EPSILON = 0.005

    private fun formatDefaultValue(value: Float): String {
        val rounded = (value.toDouble() * 100.0).roundToInt() / 100.0
        val normalized = if (abs(rounded) < NEAR_ZERO_DISPLAY_EPSILON) 0.0 else rounded
        return normalized.toString()
    }
}
