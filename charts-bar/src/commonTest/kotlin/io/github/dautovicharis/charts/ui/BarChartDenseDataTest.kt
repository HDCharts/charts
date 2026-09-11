package io.github.dautovicharis.charts.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarChartDenseDataTest {
    private companion object {
        const val DEFAULT_TITLE = "Default Bar Chart"
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(),
                    title = DEFAULT_TITLE,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertIsDisplayed()
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
            onNodeWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_smallDataset_doesNotShowZoomControls() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = smallDataSet(),
                    title = DEFAULT_TITLE,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_smallDataset_tapUpdatesTitleWithLabelAndValue() =
        runComposeUiTest {
            val title = "Small Bar Chart"
            setContent {
                BarChart(
                    data = smallDataSet(),
                    title = title,
                    animateOnStart = false,
                )
            }

            val plot = onNodeWithTag(TestTags.BAR_CHART_PLOT)
            val plotSize = plot.fetchSemanticsNode().size
            plot.performTouchInput {
                click(Offset(x = 20f, y = plotSize.height / 2f))
            }

            waitUntil(timeoutMillis = 3_000L) {
                runCatching {
                    onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Jan 01: -15.0")
                }.isSuccess
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Jan 01: -15.0").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_datasetThatFits_hidesDenseToggle() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = smallDataSet(points = 8),
                    title = DEFAULT_TITLE,
                )
            }

            onAllNodesWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_datasetThatDoesNotFit_showsCompactToggle() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(points = 40),
                    title = DEFAULT_TITLE,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertIsDisplayed()
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withLargeDataset_expandShowsZoomControls() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(),
                    title = DEFAULT_TITLE,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withLargeDataset_tapUpdatesTitleWithLabelAndValue() =
        runComposeUiTest {
            val data = largeDataSet()
            setContent {
                BarChart(
                    data = data,
                    title = DEFAULT_TITLE,
                    animateOnStart = false,
                )
            }

            val plot = onNodeWithTag(TestTags.BAR_CHART_PLOT)
            val plotSize = plot.fetchSemanticsNode().size
            plot.performTouchInput {
                click(Offset(x = 20f, y = plotSize.height / 2f))
            }

            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != DEFAULT_TITLE
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withZoomControlsHidden_doesNotRenderZoomButtons() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(),
                    title = DEFAULT_TITLE,
                    style = BarChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withXAxisLabelsHidden_doesNotRenderXAxisLayer() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(),
                    title = DEFAULT_TITLE,
                    style =
                        BarChartDefaults.style(
                            axis =
                                BarChartDefaults.axis(
                                    xLabels = BarChartDefaults.xLabels(visible = false),
                                ),
                        ),
                )
            }

            onAllNodesWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withYAxisLabelsHidden_doesNotRenderYAxisLayer() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(),
                    title = DEFAULT_TITLE,
                    style =
                        BarChartDefaults.style(
                            axis =
                                BarChartDefaults.axis(
                                    yLabels = BarChartDefaults.yLabels(visible = false),
                                ),
                        ),
                )
            }

            onAllNodesWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_zoomControls_areAbovePlotArea() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(),
                    title = DEFAULT_TITLE,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            val zoomBounds = onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).fetchSemanticsNode().boundsInRoot
            val chartBounds = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode().boundsInRoot
            assertTrue(zoomBounds.bottom <= chartBounds.top)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_resetControl_isNotRendered() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = largeDataSet(),
                    title = DEFAULT_TITLE,
                )
            }

            onAllNodesWithText("Reset").assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_denseExpanded_smallScrollDelta_keepsXAxisLabelCadenceStable() =
        runComposeUiTest {
            setContent {
                BarChart(
                    data = numericLargeDataSet(),
                    title = DEFAULT_TITLE,
                    modifier = Modifier.size(300.dp),
                    style = BarChartDefaults.style(bars = BarChartDefaults.bars(minBarWidth = 20.dp)),
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).assertIsDisplayed()

            val plot = onNodeWithTag(TestTags.BAR_CHART_PLOT)
            plot.performSemanticsAction(SemanticsActions.ScrollBy) { it(200f, 0f) }
            waitForIdle()
            val beforeLabels = visibleBarXAxisNumericLabels()
            val beforeOffset = plot.fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange].value()
            plot.performSemanticsAction(SemanticsActions.ScrollBy) { it(4f, 0f) }
            waitForIdle()
            val afterLabels = visibleBarXAxisNumericLabels()
            val afterOffset = plot.fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange].value()
            assertTrue(afterOffset > beforeOffset)

            assertTrue(beforeLabels.isNotEmpty())
            assertTrue(afterLabels.isNotEmpty())
            assertTrue(beforeLabels.zipWithNext { previous, next -> next > previous }.all { it })
            assertTrue(afterLabels.zipWithNext { previous, next -> next > previous }.all { it })
            assertEquals(beforeLabels.size, afterLabels.size)

            if (beforeLabels.size >= 2 && afterLabels.size >= 2) {
                val beforeStride = beforeLabels[1] - beforeLabels[0]
                val afterStride = afterLabels[1] - afterLabels[0]
                assertEquals(beforeStride, afterStride)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_compactTaps_reportBucketCenterSourceIndicesAndToggleOff() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(onSelectionChanged = { events.add(it) })
            val data = List(12) { it + 1.0 }.toChartData(categories = List(12) { "B$it" })
            setContent {
                BarChart(
                    data = data,
                    title = "Buckets",
                    modifier = Modifier.size(300.dp),
                    selection = selection,
                    animateOnStart = false,
                    style =
                        BarChartDefaults.style(
                            chartContainerStyle =
                                ChartContainerDefaults.style(
                                    outerPadding = 0.dp,
                                    innerPadding = 0.dp,
                                ),
                            bars = BarChartDefaults.bars(space = 0.dp, minBarWidth = 100.dp),
                            axis = BarChartDefaults.axis(yLabels = BarChartDefaults.yLabels(visible = false)),
                        ),
                )
            }

            // Three rendered buckets cover 0..3, 4..7, and 8..11, with lower-middle source indices.
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertIsDisplayed()
            val plot = onNodeWithTag(TestTags.BAR_CHART_PLOT)
            plot.performTouchInput { click(center) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B5: 6.0").assertIsDisplayed()
            plot.performTouchInput { click(center) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Buckets").assertIsDisplayed()
            plot.performTouchInput { click(Offset(width / 6f, height / 2f)) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B1: 2.0").assertIsDisplayed()
            plot.performTouchInput { click(Offset(width * 5f / 6f, height / 2f)) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B9: 10.0").assertIsDisplayed()
            runOnIdle {
                assertEquals(9, selection.selectedIndex)
                assertEquals(listOf(5, null, 1, 9), events)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_lastSourcePreset_highlightsItsBucketAndSurvivesExpandFitAndResize() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(initialIndex = 11, onSelectionChanged = { events.add(it) })
            val chartWidth = mutableStateOf(300.dp)
            val values = List(11) { it + 1.0 } + 12.3456789
            val data = values.toChartData(categories = List(12) { "B$it" })
            lateinit var captureLayer: GraphicsLayer
            lateinit var captureScope: CoroutineScope
            val capturedPixels = mutableStateOf<PixelMap?>(null)
            setContent {
                captureLayer = rememberGraphicsLayer()
                captureScope = rememberCoroutineScope()
                BarChart(
                    data = data,
                    title = "Buckets",
                    modifier =
                        Modifier
                            .testTag("bar-capture")
                            .size(width = chartWidth.value, height = 300.dp)
                            .drawWithContent {
                                captureLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(captureLayer)
                            },
                    selection = selection,
                    valueFormatter = ChartValueFormatter { it.toString() },
                    animateOnStart = false,
                    style =
                        BarChartDefaults.style(
                            chartContainerStyle =
                                ChartContainerDefaults.style(
                                    outerPadding = 0.dp,
                                    innerPadding = 0.dp,
                                ),
                            bars =
                                BarChartDefaults.bars(
                                    color = Color.Blue,
                                    alpha = 1f,
                                    space = 0.dp,
                                    minBarWidth = 100.dp,
                                ),
                            range = BarChartDefaults.range(min = 0.0, max = 20.0),
                            grid = BarChartDefaults.grid(visible = false),
                            axis =
                                BarChartDefaults.axis(
                                    visible = false,
                                    xLabels = BarChartDefaults.xLabels(visible = false),
                                    yLabels = BarChartDefaults.yLabels(visible = false),
                                ),
                            selectionLine = BarChartDefaults.selectionLine(color = Color.Magenta, width = 4.dp),
                        ),
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B11: 12.3456789").assertIsDisplayed()
            runOnIdle { captureScope.launch { capturedPixels.value = captureLayer.toImageBitmap().toPixelMap() } }
            waitUntil(timeoutMillis = 3_000L) { capturedPixels.value != null }
            val compactPixels = checkNotNull(capturedPixels.value)
            val compactBounds = onNodeWithTag(TestTags.BAR_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val captureBounds = onNodeWithTag("bar-capture").fetchSemanticsNode().boundsInRoot
            assertEquals(
                Color.Magenta,
                compactPixels[
                    (compactBounds.left - captureBounds.left + compactBounds.width * 5 / 6).toInt(),
                    (compactBounds.top - captureBounds.top + compactBounds.height / 8).toInt(),
                ],
            )

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B11: 12.3456789").assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).performTouchInput { click() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B11: 12.3456789").assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).performTouchInput { click() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B11: 12.3456789").assertIsDisplayed()
            runOnIdle { chartWidth.value = 200.dp }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B11: 12.3456789").assertIsDisplayed()
            runOnIdle {
                capturedPixels.value = null
                captureScope.launch { capturedPixels.value = captureLayer.toImageBitmap().toPixelMap() }
            }
            waitUntil(timeoutMillis = 3_000L) { capturedPixels.value != null }
            val resizedPixels = checkNotNull(capturedPixels.value)
            val resizedBounds = onNodeWithTag(TestTags.BAR_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val resizedCaptureBounds = onNodeWithTag("bar-capture").fetchSemanticsNode().boundsInRoot
            assertEquals(
                Color.Magenta,
                resizedPixels[
                    (resizedBounds.left - resizedCaptureBounds.left + resizedBounds.width * 3 / 4).toInt(),
                    (resizedBounds.top - resizedCaptureBounds.top + resizedBounds.height / 8).toInt(),
                ],
            )
            runOnIdle {
                assertEquals(11, selection.selectedIndex)
                assertEquals(emptyList(), events)
            }

            onNodeWithTag(TestTags.BAR_CHART_PLOT).performTouchInput {
                click(Offset(width * 3f / 4f, height / 2f))
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Buckets").assertIsDisplayed()
            runOnIdle { assertEquals(listOf<Int?>(null), events) }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_disabledInteraction_hidesControlsAndBlocksExpandedScrollPinchAndTaps() =
        runComposeUiTest {
            val enabled = mutableStateOf(false)
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(initialIndex = 119, onSelectionChanged = { events.add(it) })
            val data = List(120) { it + 1.0 }.toChartData(categories = List(120) { "B$it" })
            setContent {
                BarChart(
                    data = data,
                    title = "Disabled",
                    modifier = Modifier.size(300.dp),
                    selection = selection,
                    interactionEnabled = enabled.value,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B119: 120.0").assertIsDisplayed()
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
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B119: 120.0").assertIsDisplayed()
            runOnIdle {
                assertEquals(119, selection.selectedIndex)
                assertEquals(emptyList(), events)
                selection.select(1)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B1: 2.0").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    private fun ComposeUiTest.currentTitle(): String {
        val semanticsNode = onNodeWithTag(TestTags.CHART_TITLE).fetchSemanticsNode()
        return semanticsNode.config[SemanticsProperties.Text]
            .joinToString(separator = "") { item -> item.text }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun ComposeUiTest.visibleBarXAxisNumericLabels(): List<Int> {
        val axisNode = onNodeWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).fetchSemanticsNode()
        return collectSemanticsTexts(axisNode)
            .mapNotNull { text -> text.trim().toIntOrNull() }
    }

    private fun collectSemanticsTexts(node: SemanticsNode): List<String> {
        val ownText =
            runCatching {
                node.config[SemanticsProperties.Text]
                    .joinToString(separator = "") { item -> item.text }
                    .trim()
            }.getOrDefault("")
        val childrenTexts = node.children.flatMap(::collectSemanticsTexts)
        return if (ownText.isNotEmpty()) {
            listOf(ownText) + childrenTexts
        } else {
            childrenTexts
        }
    }

    private fun smallDataSet(points: Int = 12): ChartData {
        val labels = dateLabels(points)
        return values(points).toChartData(categories = labels)
    }

    private fun largeDataSet(points: Int = 120): ChartData {
        val labels = dateLabels(points)
        return values(points).toChartData(categories = labels)
    }

    private fun numericLargeDataSet(points: Int = 120): ChartData {
        val labels = List(points) { index -> (index + 1).toString() }
        return values(points).toChartData(categories = labels)
    }

    private fun values(points: Int): List<Double> =
        List(points) { index ->
            ((index % 30) - 15).toDouble()
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
