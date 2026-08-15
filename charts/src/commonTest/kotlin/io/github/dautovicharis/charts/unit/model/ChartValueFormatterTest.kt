package io.github.dautovicharis.charts.unit.model

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
        assertEquals(expected = "41.7", actual = formatter.format(41.7f))
        assertEquals(expected = "1.5", actual = formatter.format(1.5f))
        assertEquals(expected = "3.0", actual = formatter.format(2.999f))
    }

    @Test
    fun default_normalizesNearZeroToZero() {
        // Act
        val formatter = ChartValueFormatters.Default

        // Assert
        assertEquals(expected = "0.0", actual = formatter.format(0.004f))
        assertEquals(expected = "0.0", actual = formatter.format(-0.004f))
        assertEquals(expected = "0.01", actual = formatter.format(0.01f))
    }

    @Test
    fun prefix_prependsBeforeDefaultFormat() {
        // Act
        val formatter = ChartValueFormatters.prefix("$")

        // Assert
        assertEquals(expected = "$41.7", actual = formatter.format(41.7f))
    }

    @Test
    fun suffix_appendsAfterDefaultFormat() {
        // Act
        val formatter = ChartValueFormatters.suffix("%")

        // Assert
        assertEquals(expected = "41.7%", actual = formatter.format(41.7f))
    }

    @Test
    fun fixed_roundsToGivenPrecision() {
        // Assert
        assertEquals(expected = "4.0", actual = ChartValueFormatters.fixed(0).format(3.7f))
        assertEquals(expected = "3.7", actual = ChartValueFormatters.fixed(1).format(3.7f))
        assertEquals(expected = "1.235", actual = ChartValueFormatters.fixed(3).format(1.23456f))
        assertEquals(expected = "2.0", actual = ChartValueFormatters.fixed(0).format(1.5f))
    }

    @Test
    fun fixed_negativePrecision_throws() {
        // Assert
        assertFailsWith<IllegalArgumentException> {
            ChartValueFormatters.fixed(-1)
        }
    }
}
