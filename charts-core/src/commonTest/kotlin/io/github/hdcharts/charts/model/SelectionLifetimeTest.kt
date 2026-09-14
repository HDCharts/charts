package io.github.hdcharts.charts.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SelectionLifetimeTest {
    @Test
    fun autoDeselect_validatesNonNegativeDelay() {
        assertFailsWith<IllegalArgumentException> {
            SelectionLifetime.AutoDeselect(delayMs = -1L)
        }
        SelectionLifetime.AutoDeselect(delayMs = 0L)
        SelectionLifetime.AutoDeselect(delayMs = 3_000L)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dataIdentityChange_clearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 2)
            var data: Any? by mutableStateOf("v1")

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                )
            }

            runOnIdle {
                assertEquals(expected = 2, actual = selection.selectedIndex)
            }

            runOnIdle { data = "v2" }
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dataAndSelectionReplacement_preservesNewInitialSelection() =
        runComposeUiTest {
            val replacement = ChartSelection(initialIndex = 3)
            var selection by mutableStateOf(ChartSelection(initialIndex = 2))
            var data: Any? by mutableStateOf("v1")

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                )
            }

            runOnIdle {
                selection = replacement
                data = "v2"
            }
            runOnIdle {
                assertEquals(expected = 3, actual = replacement.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sameDataInstance_preservesSelection() =
        runComposeUiTest {
            val shared = "stable"
            val selection = ChartSelection(initialIndex = 1)
            var data: Any? by mutableStateOf(shared)

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                )
            }

            runOnIdle { selection.select(4) }
            runOnIdle { data = shared }
            runOnIdle {
                assertEquals(expected = 4, actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun outOfBoundsSelectedIndex_clearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 7)
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 3,
                )
            }

            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun negativeSelectedIndex_clearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection()
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 3,
                )
            }

            runOnIdle { selection.select(-1) }
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun itemCountBecomesZero_clearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 0)
            var itemCount by mutableStateOf(3)
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = itemCount,
                )
            }

            runOnIdle {
                assertEquals(expected = 0, actual = selection.selectedIndex)
                itemCount = 0
            }
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun itemCountMaxValue_skipsBoundsCheck() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 99)
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = Int.MAX_VALUE,
                )
            }

            runOnIdle {
                assertEquals(expected = 99, actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun persistentLifetime_keepsSelectionForever() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 2)
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                    lifetime = SelectionLifetime.Persistent,
                )
            }

            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(60_000L, ignoreFrameDuration = true)
            runOnIdle {
                assertEquals(expected = 2, actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoDeselect_clearsAfterDelay() =
        runComposeUiTest {
            val selection = ChartSelection()
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                    lifetime = SelectionLifetime.AutoDeselect(delayMs = 3_000L),
                )
            }

            runOnIdle { selection.select(1) }
            mainClock.autoAdvance = false

            mainClock.advanceTimeBy(2_000L, ignoreFrameDuration = true)
            runOnIdle {
                assertEquals(expected = 1, actual = selection.selectedIndex)
            }

            mainClock.advanceTimeBy(1_500L, ignoreFrameDuration = true)
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoDeselect_newSelectionRenewsTimer() =
        runComposeUiTest {
            val selection = ChartSelection()
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                    lifetime = SelectionLifetime.AutoDeselect(delayMs = 3_000L),
                )
            }

            runOnIdle { selection.select(0) }
            mainClock.autoAdvance = false

            mainClock.advanceTimeBy(2_000L, ignoreFrameDuration = true)
            runOnIdle {
                runOnIdle { selection.select(2) }
            }

            mainClock.advanceTimeBy(2_000L, ignoreFrameDuration = true)
            runOnIdle {
                assertEquals(expected = 2, actual = selection.selectedIndex)
            }

            mainClock.advanceTimeBy(1_500L, ignoreFrameDuration = true)
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoDeselect_selectionReplacementTransfersTimerOwnership() =
        runComposeUiTest {
            val original = ChartSelection(initialIndex = 1)
            val replacement = ChartSelection(initialIndex = 1)
            var selection by mutableStateOf(original)
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                    lifetime = SelectionLifetime.AutoDeselect(delayMs = 3_000L),
                )
            }

            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(1_000L, ignoreFrameDuration = true)
            runOnIdle { selection = replacement }
            mainClock.advanceTimeByFrame()
            waitForIdle()

            mainClock.advanceTimeBy(2_500L, ignoreFrameDuration = true)
            runOnIdle {
                assertEquals(expected = 1, actual = original.selectedIndex)
                assertEquals(expected = 1, actual = replacement.selectedIndex)
            }

            mainClock.advanceTimeBy(600L, ignoreFrameDuration = true)
            runOnIdle {
                assertNull(actual = replacement.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoDeselect_renew_renewsTimer() =
        runComposeUiTest {
            val selection = ChartSelection()
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                    lifetime = SelectionLifetime.AutoDeselect(delayMs = 3_000L),
                )
            }

            runOnIdle { selection.select(2) }
            mainClock.autoAdvance = false

            mainClock.advanceTimeBy(2_000L, ignoreFrameDuration = true)
            runOnIdle { selection.renew() }

            mainClock.advanceTimeBy(2_000L, ignoreFrameDuration = true)
            runOnIdle {
                assertEquals(expected = 2, actual = selection.selectedIndex)
            }

            mainClock.advanceTimeBy(1_500L, ignoreFrameDuration = true)
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoDeselect_repeatedSelectSameIndex_doesNotRenewTimer() =
        runComposeUiTest {
            val selection = ChartSelection()
            val data = "v1"

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                    lifetime = SelectionLifetime.AutoDeselect(delayMs = 3_000L),
                )
            }

            runOnIdle { selection.select(2) }
            mainClock.autoAdvance = false

            mainClock.advanceTimeBy(2_000L, ignoreFrameDuration = true)
            runOnIdle { selection.select(2) }

            mainClock.advanceTimeBy(1_500L, ignoreFrameDuration = true)
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun autoDeselect_dataChangeBeforeExpiry_preventsTimerClear() =
        runComposeUiTest {
            val notifier = mutableListOf<Int?>()
            val selection = ChartSelection { notifier.add(it) }
            var data: Any? by mutableStateOf<Any?>("v1")

            setContent {
                rememberSelectionLifecycle(
                    selection = selection,
                    data = data,
                    itemCount = 5,
                    lifetime = SelectionLifetime.AutoDeselect(delayMs = 3_000L),
                )
            }

            runOnIdle { selection.select(4) }
            runOnIdle { data = "v2" }

            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(5_000L, ignoreFrameDuration = true)
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
                assertEquals(expected = listOf<Int?>(4, null), actual = notifier)
            }
        }
}
