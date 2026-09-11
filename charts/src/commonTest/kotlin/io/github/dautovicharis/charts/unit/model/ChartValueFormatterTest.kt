package io.github.dautovicharis.charts.unit.model

import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.ChartValueFormatters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChartValueFormatterTest {
    @Test
    fun default_roundsToTwoDecimals() {
        // Act
        val formatter = ChartValueFormatters.Default

        // Assert
        assertEquals(expected = "41.7", actual = formatter.format(41.7))
        assertEquals(expected = "-41.7", actual = formatter.format(-41.7))
        assertEquals(expected = "1.5", actual = formatter.format(1.5))
        assertEquals(expected = "3.0", actual = formatter.format(3.0))
        assertEquals(expected = "3.0", actual = formatter.format(2.999))
        assertEquals(expected = "1.23", actual = formatter.format(1.234))
        assertEquals(expected = "-1.24", actual = formatter.format(-1.236))
        assertEquals(expected = "10.0", actual = formatter.format(9.999))
    }

    @Test
    fun default_normalizesNearZeroToZero() {
        // Act
        val formatter = ChartValueFormatters.Default

        // Assert
        for (value in listOf(0.0, -0.0, 0.004, -0.004, Double.MIN_VALUE, -Double.MIN_VALUE)) {
            assertEquals(expected = "0.0", actual = formatter.format(value), message = "value=$value")
        }
        assertEquals(expected = "0.01", actual = formatter.format(0.01))
        assertEquals(expected = "0.01", actual = formatter.format(0.005))
        assertEquals(expected = "-0.01", actual = formatter.format(-0.005))
    }

    @Test
    fun default_roundsDecimalTiesAwayFromZero() {
        assertEquals(expected = "0.13", actual = ChartValueFormatters.Default.format(0.125))
        assertEquals(expected = "-0.13", actual = ChartValueFormatters.Default.format(-0.125))
        assertEquals(expected = "1.01", actual = ChartValueFormatters.Default.format(1.005))
        assertEquals(expected = "-1.01", actual = ChartValueFormatters.Default.format(-1.005))
    }

    @Test
    fun default_doesNotSaturateLargeValues() {
        assertEquals(expected = "21474836.48", actual = ChartValueFormatters.Default.format(21_474_836.48))
        assertEquals(expected = "-21474836.48", actual = ChartValueFormatters.Default.format(-21_474_836.48))
        assertEquals(expected = "100000000000000000000.0", actual = ChartValueFormatters.Default.format(1.0e20))
    }

    @Test
    fun prefix_prependsBeforeDefaultFormat() {
        // Act
        val formatter = ChartValueFormatters.prefix("$")

        // Assert
        assertEquals(expected = "$41.7", actual = formatter.format(41.7))
        assertEquals(expected = "$-41.7", actual = formatter.format(-41.7))
    }

    @Test
    fun suffix_appendsAfterDefaultFormat() {
        // Act
        val formatter = ChartValueFormatters.suffix("%")

        // Assert
        assertEquals(expected = "41.7%", actual = formatter.format(41.7))
        assertEquals(expected = "-41.7%", actual = formatter.format(-41.7))
    }

    @Test
    fun fixed_roundsToGivenPrecision() {
        // Assert
        assertEquals(expected = "4", actual = ChartValueFormatters.fixed(0).format(3.7))
        assertEquals(expected = "-4", actual = ChartValueFormatters.fixed(0).format(-3.7))
        assertEquals(expected = "3.7", actual = ChartValueFormatters.fixed(1).format(3.7))
        assertEquals(expected = "1.235", actual = ChartValueFormatters.fixed(3).format(1.23456))
        assertEquals(expected = "-1.235", actual = ChartValueFormatters.fixed(3).format(-1.23456))
    }

    @Test
    fun fixed_padsExactlyToPrecision() {
        assertEquals(expected = "3", actual = ChartValueFormatters.fixed(0).format(3.0))
        assertEquals(expected = "3.0", actual = ChartValueFormatters.fixed(1).format(3.0))
        assertEquals(expected = "3.00", actual = ChartValueFormatters.fixed(2).format(3.0))
        assertEquals(expected = "1.50", actual = ChartValueFormatters.fixed(2).format(1.5))
        assertEquals(expected = "-1.50", actual = ChartValueFormatters.fixed(2).format(-1.5))
        assertEquals(expected = "1.000000000000000", actual = ChartValueFormatters.fixed(15).format(1.0))
    }

    @Test
    fun fixed_roundsDecimalTiesAwayFromZero() {
        assertEquals(expected = "2", actual = ChartValueFormatters.fixed(0).format(1.5))
        assertEquals(expected = "-2", actual = ChartValueFormatters.fixed(0).format(-1.5))
        assertEquals(expected = "0.13", actual = ChartValueFormatters.fixed(2).format(0.125))
        assertEquals(expected = "-0.13", actual = ChartValueFormatters.fixed(2).format(-0.125))
        assertEquals(expected = "2.68", actual = ChartValueFormatters.fixed(2).format(2.675))
        assertEquals(expected = "-2.68", actual = ChartValueFormatters.fixed(2).format(-2.675))
    }

    @Test
    fun fixed_respectsValuesAdjacentToATie() {
        val formatter = ChartValueFormatters.fixed(2)
        assertEquals(expected = "0.12", actual = formatter.format(0.12499999999999999))
        assertEquals(expected = "-0.12", actual = formatter.format(-0.12499999999999999))
        assertEquals(expected = "0.13", actual = formatter.format(0.12500000000000003))
        assertEquals(expected = "-0.13", actual = formatter.format(-0.12500000000000003))
    }

    @Test
    fun fixed_carriesAcrossDecimalAndIntegerDigits() {
        assertEquals(expected = "10.00", actual = ChartValueFormatters.fixed(2).format(9.999))
        assertEquals(expected = "-10.00", actual = ChartValueFormatters.fixed(2).format(-9.999))
        assertEquals(expected = "1.00", actual = ChartValueFormatters.fixed(2).format(0.999))
        assertEquals(expected = "100", actual = ChartValueFormatters.fixed(0).format(99.5))
        assertEquals(expected = "-100", actual = ChartValueFormatters.fixed(0).format(-99.5))
    }

    @Test
    fun fixed_suppressesNegativeZeroAtEveryPrecision() {
        for (precision in 0..15) {
            val formatter = ChartValueFormatters.fixed(precision)
            val expected = if (precision == 0) "0" else "0." + "0".repeat(precision)
            for (value in listOf(0.0, -0.0, 1.0e-20, -1.0e-20, Double.MIN_VALUE, -Double.MIN_VALUE)) {
                assertEquals(
                    expected = expected,
                    actual = formatter.format(value),
                    message = "precision=$precision, value=$value",
                )
            }
        }
        assertEquals(expected = "0", actual = ChartValueFormatters.fixed(0).format(-0.49))
        assertEquals(expected = "0.00", actual = ChartValueFormatters.fixed(2).format(-0.004))
    }

    @Test
    fun fixed_expandsScientificNotation() {
        assertEquals(expected = "125000000000000000000.00", actual = ChartValueFormatters.fixed(2).format(1.25e20))
        assertEquals(expected = "-125000000000000000000", actual = ChartValueFormatters.fixed(0).format(-1.25e20))
        assertEquals(expected = "0.0000001250", actual = ChartValueFormatters.fixed(10).format(1.25e-7))
        assertEquals(expected = "-0.0000001250", actual = ChartValueFormatters.fixed(10).format(-1.25e-7))
        assertEquals(expected = "0.000000", actual = ChartValueFormatters.fixed(6).format(1.25e-7))
    }

    @Test
    fun fixed_roundsTinyValuesAtMaximumPrecision() {
        val formatter = ChartValueFormatters.fixed(15)
        assertEquals(expected = "0.000000000000005", actual = formatter.format(5.0e-15))
        assertEquals(expected = "0.000000000000001", actual = formatter.format(5.0e-16))
        assertEquals(expected = "-0.000000000000001", actual = formatter.format(-5.0e-16))
        assertEquals(expected = "0.000000000000000", actual = formatter.format(4.0e-16))
        assertEquals(expected = "0.000000000000000", actual = formatter.format(-4.0e-16))
    }

    @Test
    fun formatters_preserveDoublePrecision() {
        assertEquals(expected = "16777217.13", actual = ChartValueFormatters.Default.format(16_777_217.125))
        assertEquals(expected = "16777217.125", actual = ChartValueFormatters.fixed(3).format(16_777_217.125))
        assertEquals(
            expected = "9007199254740991",
            actual = ChartValueFormatters.fixed(0).format(9_007_199_254_740_991.0),
        )
        assertEquals(expected = "1.234567890123456", actual = ChartValueFormatters.fixed(15).format(1.234567890123456))
        assertEquals(
            expected = "0.100000001490116",
            actual = ChartValueFormatters.fixed(15).format(0.10000000149011612),
        )
    }

    @Test
    fun formatters_expandMaximumDoubleWithoutOverflow() {
        // The decimal source is 1.7976931348623157E308, not the exact binary integer.
        val integer = "17976931348623157" + "0".repeat(292)
        assertEquals(expected = "$integer.0", actual = ChartValueFormatters.Default.format(Double.MAX_VALUE))
        assertEquals(expected = "-$integer.0", actual = ChartValueFormatters.Default.format(-Double.MAX_VALUE))
        for (precision in listOf(0, 2, 15)) {
            val expected = if (precision == 0) integer else "$integer." + "0".repeat(precision)
            val formatter = ChartValueFormatters.fixed(precision)
            assertEquals(
                expected = expected,
                actual = formatter.format(Double.MAX_VALUE),
                message = "precision=$precision",
            )
            assertEquals(
                expected = "-$expected",
                actual = formatter.format(-Double.MAX_VALUE),
                message = "precision=$precision",
            )
        }
    }

    @Test
    fun formatters_useExplicitNonfiniteStrings() {
        val formatters =
            listOf(
                ChartValueFormatters.Default,
                ChartValueFormatters.fixed(0),
                ChartValueFormatters.fixed(2),
                ChartValueFormatters.fixed(15),
            )
        for (formatter in formatters) {
            assertEquals(expected = "NaN", actual = formatter.format(Double.NaN))
            assertEquals(expected = "Infinity", actual = formatter.format(Double.POSITIVE_INFINITY))
            assertEquals(expected = "-Infinity", actual = formatter.format(Double.NEGATIVE_INFINITY))
        }
    }

    @Test
    fun fixed_outOfRangePrecision_throws() {
        // Assert
        for (precision in listOf(Int.MIN_VALUE, -1, 16, Int.MAX_VALUE)) {
            assertFailsWith<IllegalArgumentException> {
                ChartValueFormatters.fixed(precision)
            }
        }
    }

    @Test
    fun customFormatter_receivesDoubleWithoutNarrowing() {
        val formatter = ChartValueFormatter { value: Double -> "value=${value - 16_777_217.0}" }
        assertEquals(expected = "value=0.125", actual = formatter.format(16_777_217.125))
    }
}
