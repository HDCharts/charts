package io.github.dautovicharis.charts.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class HistogramChartInteractionTest {
    @Test
    fun histogramChart_oversizedExpandedCanvas_displaysErrorAndCanReturnToFit() =
        runComposeUiTest {
            val bins = List(17) { it.toDouble() }.toChartData()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    HistogramChart(
                        data = bins,
                        title = "Bins",
                        modifier = Modifier.size(300.dp),
                        style =
                            HistogramChartDefaults.style(
                                bars = HistogramChartDefaults.bars(minBarWidth = 16_384.dp),
                            ),
                        animateOnStart = false,
                    )
                }
            }
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).performTouchInput { click() }
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
            onNodeWithTag(TestTags.BAR_CHART_PLOT).assertIsDisplayed()
        }

    private val data =
        listOf(1.25, 2.5, 3.75, 5.0).toChartData(categories = listOf("0-10", "10-20", "20-30", "30+"))
    private val denseData =
        (List(59) { it + 0.125 } + 59.123456789).toChartData(
            categories = List(59) { "${it * 10}-${(it + 1) * 10}ms" } + "590ms+",
        )

    @Test
    fun histogramChart_externalSelectAndClear_remainAuthoritativeWithoutLockingTaps() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(onSelectionChanged = { events.add(it) })
            setContent {
                HistogramChart(data = data, title = "Bins", selection = selection, animateOnStart = false)
            }

            runOnIdle { selection.select(2) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("20-30: 3.75").assertIsDisplayed()
            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bins").assertIsDisplayed()
            tapFitBin(index = 0, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10: 1.25").assertIsDisplayed()
            tapFitBin(index = 1, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("10-20: 2.5").assertIsDisplayed()
            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bins").assertIsDisplayed()
            tapFitBin(index = 1, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("10-20: 2.5").assertIsDisplayed()
            tapFitBin(index = 1, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bins").assertIsDisplayed()
            runOnIdle {
                assertNull(selection.selectedIndex)
                assertEquals(listOf(2, null, 0, 1, null, 1, null), events)
            }
        }

    @Test
    fun histogramChart_replacingSelectionHolderWithUnchangedData_usesNewPresetAndCallback() =
        runComposeUiTest {
            val oldEvents = mutableListOf<Int?>()
            val newEvents = mutableListOf<Int?>()
            val oldSelection = ChartSelection(initialIndex = 0, onSelectionChanged = { oldEvents.add(it) })
            val newSelection = ChartSelection(initialIndex = 2, onSelectionChanged = { newEvents.add(it) })
            val currentSelection = mutableStateOf(oldSelection)
            setContent {
                HistogramChart(data = data, title = "Bins", selection = currentSelection.value, animateOnStart = false)
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10: 1.25").assertIsDisplayed()
            runOnIdle { currentSelection.value = newSelection }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("20-30: 3.75").assertIsDisplayed()
            tapFitBin(index = 1, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("10-20: 2.5").assertIsDisplayed()
            tapFitBin(index = 1, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bins").assertIsDisplayed()
            runOnIdle {
                assertEquals(0, oldSelection.selectedIndex)
                assertEquals(emptyList(), oldEvents)
                assertNull(newSelection.selectedIndex)
                assertEquals(listOf(1, null), newEvents)
            }
        }

    @Test
    fun histogramChart_callbackOnlyUpdate_notifiesLatestCallback() =
        runComposeUiTest {
            val oldEvents = mutableListOf<Int?>()
            val newEvents = mutableListOf<Int?>()
            val callback = mutableStateOf<(Int?) -> Unit>({ oldEvents.add(it) })
            setContent {
                HistogramChart(
                    data = data,
                    title = "Bins",
                    selection = rememberChartSelection(onSelectionChanged = callback.value),
                    animateOnStart = false,
                )
            }

            tapFitBin(index = 0, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10: 1.25").assertIsDisplayed()
            runOnIdle { callback.value = { newEvents.add(it) } }
            tapFitBin(index = 0, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bins").assertIsDisplayed()
            runOnIdle {
                assertEquals(listOf<Int?>(0), oldEvents)
                assertEquals(listOf<Int?>(null), newEvents)
            }
        }

    @Test
    fun histogramChart_reloadAndShrink_clearHoistedSelectionAndNotifyNull() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(initialIndex = 3, onSelectionChanged = { events.add(it) })
            val currentData = mutableStateOf(data)
            setContent {
                HistogramChart(data = currentData.value, title = "Bins", selection = selection, animateOnStart = false)
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("30+: 5.0").assertIsDisplayed()
            runOnIdle {
                assertEquals(emptyList(), events)
                currentData.value = listOf(10.0, 20.0, 30.0, 40.0).toChartData(categories = data.categories)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bins").assertIsDisplayed()
            runOnIdle {
                assertNull(selection.selectedIndex)
                assertEquals(listOf<Int?>(null), events)
                selection.select(3)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("30+: 40.0").assertIsDisplayed()
            runOnIdle { currentData.value = listOf(0.0, 0.5).toChartData(categories = listOf("0-10", "10+")) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Bins").assertIsDisplayed()
            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            runOnIdle {
                assertNull(selection.selectedIndex)
                assertEquals(listOf(null, 3, null), events)
            }
        }

    @Test
    fun histogramChart_denseFit_presetsAndTapsUseExactBinsIncludingFinalOpenLabel() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(initialIndex = 59, onSelectionChanged = { events.add(it) })
            val chartWidth = mutableStateOf(300.dp)
            setContent {
                HistogramChart(
                    data = denseData,
                    title = "Latency",
                    modifier = Modifier.size(width = chartWidth.value, height = 300.dp),
                    selection = selection,
                    valueFormatter = ChartValueFormatter { it.toString() },
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("590ms+: 59.123456789").assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertIsDisplayed()
            runOnIdle {
                assertEquals(emptyList(), events)
                selection.select(0)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10ms: 0.125").assertIsDisplayed()
            runOnIdle { selection.select(17) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("170-180ms: 17.125").assertIsDisplayed()
            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Latency").assertIsDisplayed()
            tapFitBin(index = 17, binCount = 60)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("170-180ms: 17.125").assertIsDisplayed()
            tapFitBin(index = 17, binCount = 60)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Latency").assertIsDisplayed()
            tapFitBin(index = 59, binCount = 60)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("590ms+: 59.123456789").assertIsDisplayed()
            runOnIdle { assertEquals(listOf(0, 17, null, 17, null, 59), events) }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("590ms+: 59.123456789").assertIsDisplayed()
            val plot = onNodeWithTag(TestTags.BAR_CHART_PLOT)
            val beforeZoomMax =
                plot
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.HorizontalScrollAxisRange]
                    .maxValue()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).performTouchInput { click() }
            val afterZoomMax =
                plot
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.HorizontalScrollAxisRange]
                    .maxValue()
            assertTrue(afterZoomMax > beforeZoomMax)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("590ms+: 59.123456789").assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).performTouchInput { click() }
            runOnIdle { chartWidth.value = 220.dp }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("590ms+: 59.123456789").assertIsDisplayed()
            runOnIdle {
                assertEquals(59, selection.selectedIndex)
                assertEquals(listOf(0, 17, null, 17, null, 59), events)
            }
        }

    @Test
    fun histogramChart_fractionalAndZeroBins_remainSelectableIncludingAllZeroReload() =
        runComposeUiTest {
            val currentData =
                mutableStateOf(listOf(0.0, 0.125, 0.5, 0.0).toChartData(categories = listOf("A", "B", "C", "D+")))
            val selection = ChartSelection(initialIndex = 0)
            setContent {
                HistogramChart(
                    data = currentData.value,
                    title = "Counts",
                    selection = selection,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("A: 0.0").assertIsDisplayed()
            tapFitBin(index = 1, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B: 0.13").assertIsDisplayed()
            tapFitBin(index = 2, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("C: 0.5").assertIsDisplayed()
            tapFitBin(index = 3, binCount = 4)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("D+: 0.0").assertIsDisplayed()
            runOnIdle { currentData.value = listOf(0.0, 0.0).toChartData() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Counts").assertIsDisplayed()
            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
            tapFitBin(index = 1, binCount = 2)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0.0").assertIsDisplayed()
            runOnIdle { assertEquals(1, selection.selectedIndex) }
        }

    @Test
    fun histogramChart_disabledInteraction_hidesControlsAndBlocksExpandedScrollPinchAndTaps() =
        runComposeUiTest {
            val enabled = mutableStateOf(false)
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(initialIndex = 59, onSelectionChanged = { events.add(it) })
            setContent {
                HistogramChart(
                    data = denseData,
                    title = "Disabled",
                    modifier = Modifier.size(300.dp),
                    selection = selection,
                    interactionEnabled = enabled.value,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("590ms+: 59.12").assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertDoesNotExist()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).assertDoesNotExist()
            runOnIdle { enabled.value = true }
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).assertIsDisplayed()
            runOnIdle { enabled.value = false }
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertDoesNotExist()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).assertDoesNotExist()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).assertDoesNotExist()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertDoesNotExist()

            val plot = onNodeWithTag(TestTags.BAR_CHART_PLOT)
            val beforeRange = plot.fetchSemanticsNode().config.getOrNull(SemanticsProperties.HorizontalScrollAxisRange)
            val beforeOffset = beforeRange?.value() ?: 0f
            val beforeMax = beforeRange?.maxValue() ?: 0f
            plot.performTouchInput { swipeLeft() }
            plot.performTouchInput {
                down(0, Offset(width * 0.4f, height / 2f))
                down(1, Offset(width * 0.6f, height / 2f))
                moveTo(0, Offset(width * 0.3f, height / 2f))
                moveTo(1, Offset(width * 0.7f, height / 2f))
                moveTo(0, Offset(width * 0.1f, height / 2f))
                moveTo(1, Offset(width * 0.9f, height / 2f))
                up(0)
                up(1)
            }
            plot.performTouchInput { click(center) }
            waitForIdle()
            val afterRange = plot.fetchSemanticsNode().config.getOrNull(SemanticsProperties.HorizontalScrollAxisRange)
            assertEquals(beforeOffset, afterRange?.value() ?: 0f)
            assertEquals(beforeMax, afterRange?.maxValue() ?: 0f)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("590ms+: 59.12").assertIsDisplayed()
            runOnIdle {
                assertEquals(59, selection.selectedIndex)
                assertEquals(emptyList(), events)
                selection.select(1)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("10-20ms: 1.13").assertIsDisplayed()
        }

    private fun ComposeUiTest.tapFitBin(
        index: Int,
        binCount: Int,
    ) {
        onNodeWithTag(TestTags.BAR_CHART_PLOT).performTouchInput {
            click(Offset(x = width * (index + 0.5f) / binCount, y = height / 2f))
        }
    }
}
