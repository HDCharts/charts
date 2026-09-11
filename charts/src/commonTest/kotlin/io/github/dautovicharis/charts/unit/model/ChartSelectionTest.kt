package io.github.dautovicharis.charts.unit.model

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.model.staticChartSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

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
        assertEquals(expected = listOf<Int?>(1), actual = notified)
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

    @Test
    fun initializationAndRepeatedClear_doNotNotify() {
        val notified = mutableListOf<Int?>()
        val selection = ChartSelection { notified.add(it) }

        selection.clear()
        assertEquals(expected = emptyList(), actual = notified)

        selection.select(1)
        selection.clear()
        selection.clear()
        assertEquals(expected = listOf(1, null), actual = notified)
    }

    @Test
    fun callback_observesUpdatedState() {
        val selection = ChartSelection(initialIndex = 1)
        val observed = mutableListOf<Int?>()
        selection.onSelectionChanged = { index ->
            assertEquals(expected = index, actual = selection.selectedIndex)
            observed.add(index)
        }

        selection.select(2)
        selection.clear()

        assertEquals(expected = listOf(2, null), actual = observed)
    }

    @Test
    fun callbackReplacementAndRemoval_applyWithoutNotification() {
        val oldNotifications = mutableListOf<Int?>()
        val newNotifications = mutableListOf<Int?>()
        val selection = ChartSelection(initialIndex = 1) { oldNotifications.add(it) }
        assertEquals(expected = emptyList(), actual = oldNotifications)

        selection.onSelectionChanged = { newNotifications.add(it) }
        selection.select(2)
        selection.onSelectionChanged = null
        selection.clear()

        assertEquals(expected = emptyList(), actual = oldNotifications)
        assertEquals(expected = listOf<Int?>(2), actual = newNotifications)
        assertNull(selection.selectedIndex)
    }

    @Test
    fun staticSelection_isInitializedButStillMutable() {
        val selection = staticChartSelection(2)
        assertEquals(expected = 2, actual = selection.selectedIndex)

        selection.select(3)
        assertEquals(expected = 3, actual = selection.selectedIndex)

        selection.clear()
        assertNull(selection.selectedIndex)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberedSelection_keepsIdentityAndUsesLatestCallback() =
        runComposeUiTest {
            val oldNotifications = mutableListOf<Int?>()
            val newNotifications = mutableListOf<Int?>()
            var initialIndex by mutableStateOf<Int?>(null)
            var callback by mutableStateOf<((Int?) -> Unit)?>({ oldNotifications.add(it) })
            lateinit var selection: ChartSelection
            lateinit var originalSelection: ChartSelection

            setContent {
                val remembered = rememberChartSelection(initialIndex, callback)
                SideEffect { selection = remembered }
            }

            runOnIdle {
                originalSelection = selection
                selection.select(1)
                initialIndex = 10
                callback = { newNotifications.add(it) }
            }
            runOnIdle {
                assertSame(originalSelection, selection)
                assertEquals(expected = 1, actual = selection.selectedIndex)
                selection.select(2)
                callback = null
            }
            runOnIdle {
                assertSame(originalSelection, selection)
                selection.clear()
                assertEquals(expected = listOf<Int?>(1), actual = oldNotifications)
                assertEquals(expected = listOf<Int?>(2), actual = newNotifications)
            }
        }
}
