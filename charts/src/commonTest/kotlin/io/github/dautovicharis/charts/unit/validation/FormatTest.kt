package io.github.dautovicharis.charts.unit.validation

import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_ITEM_POINTS_SIZE
import io.github.dautovicharis.charts.internal.format
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {
    @Test
    fun `format with no args returns original string`() {
        assertEquals("Points: %d", "Points: %d".format())
    }

    @Test
    fun `format with single arg replaces first placeholder`() {
        assertEquals("Item 3", "Item %d".format(3))
    }

    @Test
    fun `format with fewer args than placeholders leaves remaining placeholders`() {
        val result = "Item %d has %d points".format(1)
        assertEquals("Item 1 has %d points", result)
    }

    @Test
    fun `format with more args than placeholders ignores extra args`() {
        val result = "Value %d".format(1, 2, 3)
        assertEquals("Value 1", result)
    }

    @Test
    fun `format with multiple placeholders replaces in order`() {
        val result = RULE_ITEM_POINTS_SIZE.format(2, 4, 5)
        assertEquals("Item at index 2 has 4 points, expected 5.", result)
    }
}
