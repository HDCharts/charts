package io.github.dautovicharis.charts.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.BarChartDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarChartDenseDataTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                BarChart(dataSet = largeDataSet())
            }

            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).isDisplayed()
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
            onNodeWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_smallDataset_doesNotShowZoomControls() =
        runComposeUiTest {
            setContent {
                BarChart(dataSet = smallDataSet())
            }

            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_smallDataset_tapUpdatesTitleWithLabelAndValue() =
        runComposeUiTest {
            setContent {
                BarChart(dataSet = smallDataSet())
            }

            val chartSize = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode().size
            val chartBounds = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode().boundsInRoot
            val yAxisBounds = onNodeWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot
            val tapX = (yAxisBounds.right - chartBounds.left) + 20f
            onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
                down(Offset(x = tapX, y = chartSize.height / 2f))
                up()
            }

            waitUntil(timeoutMillis = 3_000L) {
                runCatching {
                    onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Jan 01: -15.0")
                }.isSuccess
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Jan 01: -15.0").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_datasetThatFits_hidesDenseToggle() =
        runComposeUiTest {
            setContent {
                BarChart(
                    dataSet = smallDataSet(points = 8),
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
                    dataSet = largeDataSet(points = 40),
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).isDisplayed()
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withLargeDataset_expandShowsZoomControls() =
        runComposeUiTest {
            setContent {
                BarChart(dataSet = largeDataSet())
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).isDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_OUT).isDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_ZOOM_IN).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withLargeDataset_tapUpdatesTitleWithLabelAndValue() =
        runComposeUiTest {
            val dataSet = largeDataSet()
            setContent {
                BarChart(dataSet = dataSet)
            }

            val chartSize = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode().size
            val chartBounds = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode().boundsInRoot
            val yAxisBounds = onNodeWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot
            val tapX = (yAxisBounds.right - chartBounds.left) + 20f
            onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
                down(Offset(x = tapX, y = chartSize.height / 2f))
                up()
            }

            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != dataSet.data.label
            }
            onNodeWithTag(TestTags.CHART_TITLE).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withZoomControlsHidden_doesNotRenderZoomButtons() =
        runComposeUiTest {
            setContent {
                BarChart(
                    dataSet = largeDataSet(),
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
                    dataSet = largeDataSet(),
                    style = BarChartDefaults.style(xAxisLabelsVisible = false),
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
                    dataSet = largeDataSet(),
                    style = BarChartDefaults.style(yAxisLabelsVisible = false),
                )
            }

            onAllNodesWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_zoomControls_areAbovePlotArea() =
        runComposeUiTest {
            setContent {
                BarChart(dataSet = largeDataSet())
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
                BarChart(dataSet = largeDataSet())
            }

            onAllNodesWithText("Reset").assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_denseExpanded_smallScrollDelta_keepsXAxisLabelCadenceStable() =
        runComposeUiTest {
            setContent {
                BarChart(dataSet = numericLargeDataSet())
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.BAR_CHART_DENSE_COLLAPSE).isDisplayed()

            onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }
            waitForIdle()
            val beforeLabels = visibleBarXAxisNumericLabels()

            val chartSize = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode().size
            val startX = (chartSize.width * 0.7f).coerceIn(2f, (chartSize.width - 2).toFloat())
            val centerY = (chartSize.height / 2f).coerceIn(2f, (chartSize.height - 2).toFloat())
            onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
                down(Offset(x = startX, y = centerY))
                moveBy(Offset(x = -24f, y = 0f))
                up()
            }
            waitForIdle()
            val afterLabels = visibleBarXAxisNumericLabels()

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
            .distinct()
            .sorted()
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

    private fun smallDataSet(points: Int = 12): ChartDataSet {
        val labels = dateLabels(points)
        val values = values(points)
        return values.toChartDataSet(
            title = "Small Bar Chart",
            labels = labels,
        )
    }

    private fun largeDataSet(points: Int = 120): ChartDataSet {
        val labels = dateLabels(points)
        val values = values(points)
        return values.toChartDataSet(
            title = "Large Bar Chart",
            labels = labels,
        )
    }

    private fun numericLargeDataSet(points: Int = 120): ChartDataSet {
        val labels = List(points) { index -> (index + 1).toString() }
        val values = values(points)
        return values.toChartDataSet(
            title = "Numeric Large Bar Chart",
            labels = labels,
        )
    }

    private fun values(points: Int): List<Float> =
        List(points) { index ->
            ((index % 30) - 15).toFloat()
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
