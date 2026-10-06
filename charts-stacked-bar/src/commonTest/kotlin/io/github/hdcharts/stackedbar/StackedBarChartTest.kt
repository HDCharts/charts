package io.github.hdcharts.stackedbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.staticChartSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StackedBarChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_expandedPastLayoutLimits_displaysErrorInsteadOfCrashing() =
        runComposeUiTest {
            // 14,000 bars of at least 10 dp plus 10 dp spacing need 279,990 px or more, past the 262,143 px
            // Compose can measure.
            val bars = 14_000
            val data =
                chartDataOf(
                    categories = List(bars) { index -> "B$index" },
                    ChartSeries(name = "S1", values = List(bars) { index -> 1.0 + index % 3 }),
                    ChartSeries(name = "S2", values = List(bars) { index -> 2.0 + index % 5 }),
                )
            setContent {
                StackedBarChart(
                    data = data,
                    modifier = Modifier.size(width = 400.dp, height = 300.dp),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).performTouchInput { click() }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Chart exceeds layout limits.", substring = true).assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_COLLAPSE).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withEmAxisLabelSize_displaysValidationError() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = listOf("Bar 1", "Bar 2"),
                    ChartSeries(name = "S1", values = listOf(10.0, 2.0)),
                    ChartSeries(name = "S2", values = listOf(5.0, 8.0)),
                )

            setContent {
                StackedBarChart(
                    data = data,
                    style =
                        StackedBarChartDefaults.style(
                            axis = StackedBarChartDefaults.axis(xLabels = StackedBarChartDefaults.xLabels(size = 1.em)),
                        ),
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText(
                "X-axis label size must be a finite, positive sp value.",
                substring = true,
            ).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = "Quarterly Revenue by Region"
            val data =
                transposeStackedBars(
                    rows =
                        listOf(
                            "North America" to listOf(320f, 340f, 360f, 390f),
                            "Europe" to listOf(260f, 280f, 240f, 260f),
                            "Asia Pacific" to listOf(220f, 210f, 230f, 250f),
                        ),
                    segmentNames = listOf("Q1", "Q2", "Q3", "Q4"),
                )

            // Act
            setContent {
                StackedBarChart(data = data, title = expectedTitle)
            }

            // Assert
            onNodeWithTag(TestTags.STACKED_BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_insideVerticalScroll_hasNonZeroPlotHeight() =
        runComposeUiTest {
            setContent {
                Column(
                    modifier =
                        Modifier
                            .width(240.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    StackedBarChart(
                        data = validData(),
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds =
                onNodeWithTag(TestTags.STACKED_BAR_CHART)
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertTrue(chartBounds.height > 0f, "Chart bounds must have positive height: $chartBounds")
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withInvalidData_displaysError() =
        runComposeUiTest {
            // Arrange
            val data =
                chartDataOf(
                    categories = listOf("Bar 1", "Bar 2", "Bar 3"),
                    ChartSeries(name = "S1", values = listOf(10.0, 20.0, 30.0)),
                    ChartSeries(name = "S2", values = listOf(12.0, 22.0)),
                    ChartSeries(name = "S3", values = listOf(14.0, 24.0, 34.0)),
                )

            // Act
            setContent {
                StackedBarChart(data = data)
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Series 1 is not aligned with the first series.\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withInvalidColors_displaysError() =
        runComposeUiTest {
            // Arrange
            val data =
                transposeStackedBars(
                    rows =
                        listOf(
                            "Bar 1" to listOf(10f, 20f, 30f),
                            "Bar 2" to listOf(12f, 22f, 32f),
                            "Bar 3" to listOf(14f, 24f, 34f),
                        ),
                    segmentNames = listOf("S1", "S2", "S3"),
                )

            // Act
            setContent {
                StackedBarChart(
                    data = data,
                    style =
                        StackedBarChartDefaults.style(
                            segments = StackedBarChartDefaults.segments(colors = colors.take(2)),
                        ),
                )
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Color count (2) must match series count (3).\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withInvalidNumericStyleValues_drawsClampedChart() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = listOf("Bar 1", "Bar 2"),
                    ChartSeries(name = "S1", values = listOf(10.0, 2.0)),
                    ChartSeries(name = "S2", values = listOf(5.0, 8.0)),
                )

            setContent {
                StackedBarChart(
                    data = data,
                    style =
                        StackedBarChartDefaults.style(
                            segments = StackedBarChartDefaults.segments(alpha = 2f),
                            layout = StackedBarChartDefaults.layout(space = (-1).dp),
                            selection =
                                StackedBarChartDefaults.selection(
                                    width = Dp.Unspecified,
                                    unselectedAlpha = Float.NaN,
                                ),
                        ),
                    selection = staticChartSelection(1),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withNegativeData_displaysError() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = listOf("Bar 1", "Bar 2"),
                    ChartSeries(name = "S1", values = listOf(10.0, -2.0)),
                    ChartSeries(name = "S2", values = listOf(5.0, 8.0)),
                )

            setContent {
                StackedBarChart(data = data)
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Series 0 contains a negative value.\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withSelection_displaysSelectedBarDetails() =
        runComposeUiTest {
            // Arrange
            val selectedIndex = 1
            val data =
                transposeStackedBars(
                    rows =
                        listOf(
                            "Bar 1" to listOf(10f, 20f, 30f),
                            "Bar 2" to listOf(12f, 22f, 32f),
                            "Bar 3" to listOf(14f, 24f, 34f),
                        ),
                    segmentNames = listOf("S1", "S2", "S3"),
                )
            val expectedTitle = "Bar 2"

            // Act
            setContent {
                StackedBarChart(
                    data = data,
                    title = expectedTitle,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selection = staticChartSelection(selectedIndex),
                )
            }

            // Assert
            onNodeWithTag(TestTags.STACKED_BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_selectedCategoryBlank_showsTheCallerTitle() =
        runComposeUiTest {
            // Arrange
            val data =
                chartDataOf(
                    categories = listOf("Bar 1", "   ", "Bar 3"),
                    ChartSeries(name = "S1", values = listOf(10.0, 20.0, 30.0)),
                    ChartSeries(name = "S2", values = listOf(5.0, 15.0, 25.0)),
                )
            val expectedTitle = "Totals"

            // Act
            setContent {
                StackedBarChart(
                    data = data,
                    title = expectedTitle,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selection = staticChartSelection(1),
                )
            }

            // Assert
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withXAxisLabelsHidden_doesNotRenderXAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    data = validData(),
                    style =
                        StackedBarChartDefaults.style(
                            axis =
                                StackedBarChartDefaults.axis(
                                    xLabels = StackedBarChartDefaults.xLabels(visible = false),
                                ),
                        ),
                )
            }

            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withoutCategories_doesNotRenderXAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedBarChart(data = ChartData(categories = emptyList(), series = validData().series))
            }

            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withBlankCategoryLabels_doesNotRenderXAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedBarChart(data = ChartData(categories = listOf("", " ", ""), series = validData().series))
            }

            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withYAxisLabelsHidden_doesNotRenderYAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    data = validData(),
                    style =
                        StackedBarChartDefaults.style(
                            axis =
                                StackedBarChartDefaults.axis(
                                    yLabels = StackedBarChartDefaults.yLabels(visible = false),
                                ),
                        ),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    data = denseStackedBarDataSet(),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).assertIsDisplayed()
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withLargeDataset_expandShowsZoomControls() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    data = denseStackedBarDataSet(),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_COLLAPSE).assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withThreeItems_displaysAllXAxisLabels() =
        runComposeUiTest {
            val dataSet =
                listOf(
                    "North America" to listOf(320f, 340f, 360f, 390f),
                    "Europe" to listOf(260f, 280f, 240f, 260f),
                    "Asia Pacific" to listOf(220f, 210f, 230f, 250f),
                )

            setContent {
                StackedBarChart(
                    data = transposeStackedBars(dataSet, listOf("Q1", "Q2", "Q3", "Q4")),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onNodeWithText("North America").assertIsDisplayed()
            onNodeWithText("Europe").assertIsDisplayed()
            onNodeWithText("Asia Pacific").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_lastXAxisLabel_centeredUnderLastBar() =
        runComposeUiTest {
            val dataSet =
                listOf(
                    "Region 1" to listOf(320f, 340f, 360f, 390f),
                    "Region 2" to listOf(260f, 280f, 240f, 260f),
                    "Region 3" to listOf(220f, 210f, 230f, 250f),
                    "Region 4" to listOf(210f, 220f, 240f, 260f),
                )

            setContent {
                StackedBarChart(
                    data = transposeStackedBars(dataSet, listOf("S1", "S2", "S3")),
                    style = StackedBarChartDefaults.style(layout = StackedBarChartDefaults.layout(space = 0.dp)),
                )
            }

            val plotBounds = onNodeWithTag(TestTags.STACKED_BAR_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val barWidth = plotBounds.width / dataSet.size
            val lastLabelBounds = onNodeWithText("Region 4").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

            assertEquals(
                expected = plotBounds.right - barWidth / 2f,
                actual = lastLabelBounds.center.x,
                absoluteTolerance = 1.5f,
            )
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_xAxisLabels_centeredUnderBars() =
        runComposeUiTest {
            val categories = listOf("Q1 '24", "Q2 '24", "Q3 '24", "Q4 '24", "Q1 '25", "Q2 '25", "Q3 '25", "Q4 '25")

            setContent {
                StackedBarChart(
                    data =
                        transposeStackedBars(
                            rows = categories.mapIndexed { index, label -> label to listOf(20f + index, 10f + index) },
                            segmentNames = listOf("Hardware", "Services"),
                        ),
                    modifier = Modifier.size(width = 800.dp, height = 400.dp),
                    style = StackedBarChartDefaults.style(layout = StackedBarChartDefaults.layout(space = 0.dp)),
                )
            }

            val plotBounds = onNodeWithTag(TestTags.STACKED_BAR_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val barWidth = plotBounds.width / categories.size
            categories.forEachIndexed { index, label ->
                val labelBounds = onNodeWithText(label).assertIsDisplayed().fetchSemanticsNode().boundsInRoot

                assertEquals(
                    expected = plotBounds.left + barWidth * (index + 0.5f),
                    actual = labelBounds.center.x,
                    absoluteTolerance = 1.5f,
                    message = label,
                )
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withZoomControlsHidden_doesNotRenderZoomButtons() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    data = denseStackedBarDataSet(),
                    style = StackedBarChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    private fun validData() =
        transposeStackedBars(
            rows =
                listOf(
                    "Bar 1" to listOf(10f, 20f, 30f),
                    "Bar 2" to listOf(12f, 22f, 32f),
                    "Bar 3" to listOf(14f, 24f, 34f),
                ),
            segmentNames = listOf("S1", "S2", "S3"),
        )

    private companion object {
        val colors = listOf(Color.Red, Color.Green, Color.Cyan, Color.Black)
    }
}
