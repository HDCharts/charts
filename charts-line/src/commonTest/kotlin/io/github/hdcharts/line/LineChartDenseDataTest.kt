package io.github.hdcharts.line

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.ChartContainerDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LineChartDenseDataTest {
    private companion object {
        const val PLOT_START_PADDING_PX = 12f

        /** How far in from each edge of the plot to look for the line. */
        const val EDGE_PROBE_PX = 3
    }

    @Test
    fun lineChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                LineChart(data = largeDataSet())
            }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @Test
    fun lineChart_smallDataset_doesNotShowZoomControls() =
        runComposeUiTest {
            setContent {
                LineChart(data = smallDataSet())
            }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @Test
    fun lineChart_withZoomControlsHidden_doesNotRenderZoomButtons() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = largeDataSet(),
                    style = LineChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @Test
    fun lineChart_interactionDisabled_hidesDensityToggle() =
        runComposeUiTest {
            setContent {
                LineChart(data = largeDataSet(), interactionEnabled = false)
            }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_DENSE_EXPAND).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_DENSE_COLLAPSE).assertCountEquals(0)
        }

    @Test
    fun lineChart_interactionDisabledWhileExpanded_returnsToCompactMode() =
        runComposeUiTest {
            val interactionEnabled = mutableStateOf(true)
            setContent {
                LineChart(data = largeDataSet(), interactionEnabled = interactionEnabled.value)
            }

            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.LINE_CHART_ZOOM_IN).performTouchInput { click() }
            onNodeWithTag(TestTags.LINE_CHART_PLOT)
                .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))

            runOnIdle { interactionEnabled.value = false }

            onAllNodesWithTag(TestTags.LINE_CHART_DENSE_COLLAPSE).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_PLOT)
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.HorizontalScrollAxisRange))

            runOnIdle { interactionEnabled.value = true }

            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_DENSE_COLLAPSE).assertCountEquals(0)
        }

    @Test
    fun lineChart_compactMode_programmaticSelectionUsesSourceIndex() =
        runComposeUiTest {
            val dataSet = largeDataSet()
            val sourceIndex = 80
            setContent {
                LineChart(
                    data = dataSet,
                    selection = ChartSelection(initialIndex = sourceIndex),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals("${dataSet.categories[sourceIndex]}: ${dataSet.series.single().values[sourceIndex]}")
                .assertIsDisplayed()
        }

    @Test
    fun lineChart_compactMode_dragReportsSourceIndex() =
        runComposeUiTest {
            val reportedSelections = mutableListOf<Int?>()
            val selection = ChartSelection(onSelectionChanged = reportedSelections::add)
            setContent {
                LineChart(
                    data = largeDataSet(),
                    selection = selection,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.LINE_CHART).performTouchInput { swipeLeft() }

            runOnIdle {
                assertTrue(reportedSelections.filterNotNull().any { sourceIndex -> sourceIndex >= 50 })
            }
        }

    @Test
    fun lineChart_scrollThenTap_changesSelectedLabelAtSameViewportX() =
        runComposeUiTest {
            val dataSet = largeDataSet(title = "Dense Line Chart")
            val currentDataSet = mutableStateOf(dataSet)
            setContent {
                LineChart(data = currentDataSet.value)
            }

            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.LINE_CHART_DENSE_COLLAPSE).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_ZOOM_IN).assertIsDisplayed()

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() !=
                    dataSet.series
                        .first()
                        .name
                        .orEmpty()
            }
            val beforeScrollTitle = currentTitle()

            onNodeWithTag(TestTags.LINE_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                val title = currentTitle()
                title != beforeScrollTitle &&
                    title !=
                    dataSet.series
                        .first()
                        .name
                        .orEmpty()
            }
            val afterScrollTitle = currentTitle()

            assertNotEquals(beforeScrollTitle, afterScrollTitle)
            assertNotEquals(
                dataSet.series
                    .first()
                    .name
                    .orEmpty(),
                afterScrollTitle,
            )
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_expandedAndScrolled_keepsDrawingTheLineAcrossTheWholeViewport() =
        runComposeUiTest {
            val capturePixels =
                setCapturedContent { captureModifier ->
                    LineChart(
                        data = largeDataSet(title = "Dense Line Chart"),
                        modifier =
                            captureModifier
                                .testTag("dense-capture")
                                .size(width = 240.dp, height = 200.dp),
                        animateOnStart = false,
                        style =
                            LineChartDefaults.style(
                                chartContainerStyle = ChartContainerDefaults.style(contentPadding = 0.dp),
                                line =
                                    LineChartDefaults.line(
                                        color = Color.Blue,
                                        alpha = 1f,
                                        strokeWidth = 6.dp,
                                        bezier = false,
                                    ),
                                points = LineChartDefaults.points(visible = false),
                                axis = LineChartDefaults.axis(visible = false),
                            ),
                    )
                }

            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()

            val plotBounds = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val captureBounds = onNodeWithTag("dense-capture").fetchSemanticsNode().boundsInRoot
            val plotLeft = (plotBounds.left - captureBounds.left).toInt()

            // The chart draws only the points on screen, so the line has to be found at both ends
            // of the viewport, not only where the series starts.
            onNodeWithTag(TestTags.LINE_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }
            waitForIdle()

            val scrolledPixels = capturePixels()
            val plotWidth = plotBounds.width.toInt()
            val plotHeight = plotBounds.height.toInt()
            val plotTop = (plotBounds.top - captureBounds.top).toInt()

            assertLineAtColumn(
                pixels = scrolledPixels,
                columnX = plotLeft + EDGE_PROBE_PX,
                plotTop = plotTop,
                plotHeight = plotHeight,
                edge = "left",
            )
            assertLineAtColumn(
                pixels = scrolledPixels,
                columnX = plotLeft + plotWidth - EDGE_PROBE_PX,
                plotTop = plotTop,
                plotHeight = plotHeight,
                edge = "right",
            )
        }

    @Test
    fun lineChart_expandedAndScrolled_keepsYAxisLineAtPlotStart() =
        runComposeUiTest {
            val capturePixels =
                setCapturedContent { captureModifier ->
                    LineChart(
                        data = largeDataSet(title = "Dense Line Chart"),
                        modifier =
                            captureModifier
                                .testTag("dense-capture")
                                .size(width = 240.dp, height = 200.dp),
                        animateOnStart = false,
                        style =
                            LineChartDefaults.style(
                                chartContainerStyle = ChartContainerDefaults.style(contentPadding = 0.dp),
                                line = LineChartDefaults.line(color = Color.Blue, alpha = 1f, strokeWidth = 8.dp),
                                points = LineChartDefaults.points(visible = false),
                                axis =
                                    LineChartDefaults.axis(
                                        color = Color.Red,
                                        lineWidth = 4.dp,
                                        xLabels = LineChartDefaults.xLabels(visible = false),
                                    ),
                            ),
                    )
                }

            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.LINE_CHART_DENSE_COLLAPSE).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }
            onNodeWithTag(TestTags.LINE_CHART_PLOT).assert(
                SemanticsMatcher("is scrolled horizontally") { node ->
                    node.config[SemanticsProperties.HorizontalScrollAxisRange].value() > 0f
                },
            )

            val plotBounds = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val captureBounds = onNodeWithTag("dense-capture").fetchSemanticsNode().boundsInRoot
            val axisX = (plotBounds.left - captureBounds.left).toInt() + 1
            val plotTop = (plotBounds.top - captureBounds.top).toInt()
            val pixels = capturePixels()
            val coveredRows =
                (1 until plotBounds.height.toInt() - 1).count { row -> pixels[axisX, plotTop + row] != Color.Red }

            assertEquals(0, coveredRows, "Rows where the Y axis line is covered at the plot start")
        }

    /**
     * Asserts the line is drawn somewhere in the column [columnX], scanning the plot's height.
     *
     * A column rather than a single pixel, because where the line sits vertically depends on the
     * data, and a scan over the whole height rather than a fixed row, because which row it crosses
     * is not part of this behaviour.
     */
    private fun assertLineAtColumn(
        pixels: PixelMap,
        columnX: Int,
        plotTop: Int,
        plotHeight: Int,
        edge: String,
    ) {
        val lineReachesColumn =
            (0 until plotHeight).any { row -> pixels[columnX, plotTop + row] == Color.Blue }
        assertTrue(
            lineReachesColumn,
            "The line must reach the $edge edge of the viewport, or the chart is not drawing the " +
                "points that are on screen. Column: $columnX",
        )
    }

    private fun ComposeUiTest.currentTitle(): String {
        val semanticsNode = onNodeWithTag(TestTags.CHART_TITLE).fetchSemanticsNode()
        return semanticsNode.config[SemanticsProperties.Text]
            .joinToString(separator = "") { item -> item.text }
    }

    private fun ComposeUiTest.tapChartAt(x: Float) {
        val chartNode = onNodeWithTag(TestTags.LINE_CHART).fetchSemanticsNode()
        val size = chartNode.size
        val chartLeft = chartNode.boundsInRoot.left
        val yAxisRight =
            runCatching {
                onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot.right
            }.getOrDefault(chartLeft)
        val plotStartX = (yAxisRight - chartLeft + PLOT_START_PADDING_PX).coerceAtLeast(0f)
        val safeX = (plotStartX + x).coerceIn(1f, (size.width - 1).coerceAtLeast(1).toFloat())
        val safeY = (size.height / 2f).coerceIn(1f, (size.height - 1).coerceAtLeast(1).toFloat())
        onNodeWithTag(TestTags.LINE_CHART).performTouchInput {
            click(Offset(x = safeX, y = safeY))
        }
    }

    private fun smallDataSet(points: Int = 12): ChartData {
        val labels = dateLabels(points)
        val values = values(points)
        return values.toChartData(categories = labels, seriesName = "Small Line Chart")
    }

    private fun largeDataSet(
        points: Int = 120,
        title: String = "Large Line Chart",
    ): ChartData {
        val labels = dateLabels(points)
        val values = values(points)
        return values.toChartData(categories = labels, seriesName = title)
    }

    private fun values(points: Int): List<Double> =
        List(points) { index ->
            ((index % 30) - 10).toDouble()
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
