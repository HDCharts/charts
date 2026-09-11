package io.github.dautovicharis.charts.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.model.toChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class BarChartEdgeInteractionTest {
    private companion object {
        const val PLOT_START_PADDING_PX = 12f
    }

    @Test
    fun barChart_secondTapOnSameBar_togglesSelectionOff() =
        runComposeUiTest {
            val title = "Large Bar Chart"
            val data = largeDataSet()
            setContent {
                BarChart(
                    data = data,
                    title = title,
                )
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() != title
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() == title
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(title)
        }

    @Test
    fun barChart_datasetReload_resetsTitleToNewDatasetTitleAfterSelection() =
        runComposeUiTest {
            val initialTitle = "Initial Bar Chart"
            val reloadedTitle = "Reloaded Bar Chart"
            val initialData = largeDataSet()
            val reloadedData = largeDataSet(valueShift = 7)
            val currentData = mutableStateOf(initialData)
            val currentTitle = mutableStateOf(initialTitle)

            setContent {
                BarChart(
                    data = currentData.value,
                    title = currentTitle.value,
                )
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() != initialTitle
            }
            assertNotEquals(initialTitle, currentChartTitle())

            runOnIdle {
                currentData.value = reloadedData
                currentTitle.value = reloadedTitle
            }
            waitUntil(timeoutMillis = 3_000L) {
                runCatching {
                    onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(reloadedTitle)
                }.isSuccess
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(reloadedTitle)
        }

    @Test
    fun barChart_scrollThenTap_changesSelectedLabelAtSameViewportX() =
        runComposeUiTest {
            val title = "Scrollable Bar Chart"
            val data = largeDataSet()
            setContent {
                BarChart(
                    data = data,
                    title = title,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() != title
            }
            val beforeScrollTitle = currentChartTitle()

            onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                val t = currentChartTitle()
                t != beforeScrollTitle && t != title
            }
            val afterScrollTitle = currentChartTitle()

            assertNotEquals(beforeScrollTitle, afterScrollTitle)
            assertNotEquals(title, afterScrollTitle)
        }

    @Test
    fun barChart_externalSelectAndClear_remainAuthoritativeWithoutLockingTaps() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(onSelectionChanged = { events.add(it) })
            val data = listOf(1.25, 2.5, 3.75, 5.0).toChartData(categories = listOf("A", "B", "C", "D"))
            setContent {
                BarChart(data = data, title = "Bars", selection = selection, animateOnStart = false)
            }

            runOnIdle { selection.select(2) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("C: 3.75").assertIsDisplayed()
            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bars").assertIsDisplayed()
            tapPlotAtFraction(0.125f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("A: 1.25").assertIsDisplayed()
            tapPlotAtFraction(0.375f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B: 2.5").assertIsDisplayed()
            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bars").assertIsDisplayed()
            tapPlotAtFraction(0.375f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B: 2.5").assertIsDisplayed()
            tapPlotAtFraction(0.375f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bars").assertIsDisplayed()
            runOnIdle {
                assertNull(selection.selectedIndex)
                assertEquals(listOf(2, null, 0, 1, null, 1, null), events)
            }
        }

    @Test
    fun barChart_replacingSelectionHolderWithUnchangedData_usesNewPresetAndCallback() =
        runComposeUiTest {
            val oldEvents = mutableListOf<Int?>()
            val newEvents = mutableListOf<Int?>()
            val oldSelection = ChartSelection(initialIndex = 0, onSelectionChanged = { oldEvents.add(it) })
            val newSelection = ChartSelection(initialIndex = 2, onSelectionChanged = { newEvents.add(it) })
            val currentSelection = mutableStateOf(oldSelection)
            val data = listOf(1.25, 2.5, 3.75, 5.0).toChartData(categories = listOf("A", "B", "C", "D"))
            setContent {
                BarChart(data = data, title = "Bars", selection = currentSelection.value, animateOnStart = false)
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("A: 1.25").assertIsDisplayed()
            runOnIdle { currentSelection.value = newSelection }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("C: 3.75").assertIsDisplayed()
            tapPlotAtFraction(0.375f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B: 2.5").assertIsDisplayed()
            tapPlotAtFraction(0.375f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bars").assertIsDisplayed()
            runOnIdle {
                assertEquals(0, oldSelection.selectedIndex)
                assertEquals(emptyList(), oldEvents)
                assertNull(newSelection.selectedIndex)
                assertEquals(listOf(1, null), newEvents)
            }
        }

    @Test
    fun barChart_callbackOnlyUpdate_notifiesLatestCallback() =
        runComposeUiTest {
            val oldEvents = mutableListOf<Int?>()
            val newEvents = mutableListOf<Int?>()
            val callback = mutableStateOf<(Int?) -> Unit>({ oldEvents.add(it) })
            val data = listOf(1.25, 2.5).toChartData(categories = listOf("A", "B"))
            setContent {
                BarChart(
                    data = data,
                    title = "Bars",
                    selection = rememberChartSelection(onSelectionChanged = callback.value),
                    animateOnStart = false,
                )
            }

            tapPlotAtFraction(0.25f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("A: 1.25").assertIsDisplayed()
            runOnIdle { callback.value = { newEvents.add(it) } }
            tapPlotAtFraction(0.25f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bars").assertIsDisplayed()
            runOnIdle {
                assertEquals(listOf<Int?>(0), oldEvents)
                assertEquals(listOf<Int?>(null), newEvents)
            }
        }

    @Test
    fun barChart_reloadAndShrink_clearHoistedSelectionAndNotifyNull() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(initialIndex = 3, onSelectionChanged = { events.add(it) })
            val currentData = mutableStateOf(listOf(1.0, 2.0, 3.0, 4.0).toChartData())
            setContent {
                BarChart(data = currentData.value, title = "Bars", selection = selection, animateOnStart = false)
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("4.0").assertIsDisplayed()
            runOnIdle {
                assertEquals(emptyList(), events)
                currentData.value = listOf(10.0, 20.0, 30.0, 40.0).toChartData()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bars").assertIsDisplayed()
            runOnIdle {
                assertNull(selection.selectedIndex)
                assertEquals(listOf<Int?>(null), events)
                selection.select(3)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("40.0").assertIsDisplayed()
            runOnIdle { currentData.value = listOf(7.0, 8.0).toChartData() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bars").assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
            runOnIdle {
                assertNull(selection.selectedIndex)
                assertEquals(listOf(null, 3, null), events)
            }
        }

    private fun ComposeUiTest.tapPlotAtFraction(fraction: Float) {
        onNodeWithTag(TestTags.BAR_CHART_PLOT).performTouchInput {
            click(Offset(x = width * fraction, y = height / 2f))
        }
    }

    private fun ComposeUiTest.currentChartTitle(): String {
        val semanticsNode = onNodeWithTag(TestTags.CHART_TITLE).fetchSemanticsNode()
        return semanticsNode.config[SemanticsProperties.Text]
            .joinToString(separator = "") { item -> item.text }
    }

    private fun ComposeUiTest.tapChartAt(x: Float) {
        val chartNode = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode()
        val size = chartNode.size
        val chartLeft = chartNode.boundsInRoot.left
        val yAxisRight =
            runCatching {
                onNodeWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot.right
            }.getOrDefault(chartLeft)
        val plotStartX = (yAxisRight - chartLeft + PLOT_START_PADDING_PX).coerceAtLeast(0f)
        val safeX = (plotStartX + x).coerceIn(1f, (size.width - 1).coerceAtLeast(1).toFloat())
        val safeY = (size.height / 2f).coerceIn(1f, (size.height - 1).coerceAtLeast(1).toFloat())
        onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
            click(Offset(x = safeX, y = safeY))
        }
    }

    private fun largeDataSet(
        points: Int = 120,
        valueShift: Int = 0,
    ): ChartData {
        val labels = dateLabels(points)
        val values =
            List(points) { index ->
                (((index + valueShift) % 30) - 15).toDouble()
            }
        return values.toChartData(categories = labels)
    }

    private fun dateLabels(points: Int): List<String> {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
        val monthLengths = listOf(31, 28, 31, 30, 31, 30)

        var month = 0
        var day = 1
        return List(points) {
            val label = "${monthNames[month]} ${day.toString().padStart(2, '0')}"
            day++
            if (day > monthLengths[month]) {
                day = 1
                month = (month + 1) % monthNames.size
            }
            label
        }
    }
}
