package io.github.dautovicharis.charts.unit.model

import io.github.dautovicharis.charts.model.ChartSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChartSelectionTest {
    @Test
    fun initialIndex_defaultsToNull() {
        // Act
        val selection = ChartSelection()

        // Assert
        assertNull(actual = selection.selectedIndex)
    }

    @Test
    fun initialIndex_keepsProvidedValue() {
        // Act
        val selection = ChartSelection(initialIndex = 2)

        // Assert
        assertEquals(expected = 2, actual = selection.selectedIndex)
    }

    @Test
    fun select_updatesIndexAndNotifies() {
        // Arrange
        val selection = ChartSelection()
        val notified = mutableListOf<Int?>()
        selection.onSelectionChanged = { notified.add(it) }

        // Act
        selection.select(3)

        // Assert
        assertEquals(expected = 3, actual = selection.selectedIndex)
        assertEquals(expected = listOf<Int?>(3), actual = notified)
    }

    @Test
    fun select_ignoresDuplicateIndices() {
        // Arrange
        val selection = ChartSelection()
        val notified = mutableListOf<Int?>()
        selection.onSelectionChanged = { notified.add(it) }

        // Act
        selection.select(1)
        selection.select(1)

        // Assert
        assertEquals(expected = listOf<Int?>(1, 1), actual = notified)
    }

    @Test
    fun clear_resetsIndexAndNotifiesNull() {
        // Arrange
        val selection = ChartSelection(initialIndex = 1)
        val notified = mutableListOf<Int?>()
        selection.onSelectionChanged = { notified.add(it) }

        // Act
        selection.clear()

        // Assert
        assertNull(actual = selection.selectedIndex)
        assertEquals(expected = listOf<Int?>(null), actual = notified)
    }
}
