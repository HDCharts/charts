package io.github.hdcharts.stackedarea

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.staticChartSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StackedAreaChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_longEdgeXAxisLabels_stayInsideChartBounds() =
        runComposeUiTest {
            val categories = listOf("Region 1", "Region 36", "Region 68", "Region 100")
            setContent {
                Box(modifier = Modifier.size(width = 400.dp, height = 300.dp).testTag("chart-bounds")) {
                    StackedAreaChart(
                        data =
                            chartDataOf(
                                categories = categories,
                                ChartSeries(name = "S1", values = listOf(20.0, 28.0, 23.0, 30.0)),
                                ChartSeries(name = "S2", values = listOf(5.0, 6.0, 7.0, 8.0)),
                            ),
                        modifier = Modifier.fillMaxSize(),
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds = onNodeWithTag("chart-bounds").fetchSemanticsNode().boundsInRoot
            val firstLabelBounds = onNodeWithText("Region 1").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val lastLabelBounds = onNodeWithText("Region 100").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val plotBounds = onNodeWithTag(TestTags.STACKED_AREA_CHART_PLOT).fetchSemanticsNode().boundsInRoot

            assertTrue(
                lastLabelBounds.right <= chartBounds.right,
                "last label ends at ${lastLabelBounds.right}, chart at ${chartBounds.right}",
            )
            assertTrue(firstLabelBounds.left >= chartBounds.left, "first label starts at ${firstLabelBounds.left}")
            assertEquals(expected = plotBounds.right, actual = lastLabelBounds.center.x, absoluteTolerance = 1.5f)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_dragBesidePoint_selectsNearestPoint() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent {
                StackedAreaChart(
                    data =
                        chartDataOf(
                            categories = listOf("A", "B", "C", "D", "E"),
                            ChartSeries(name = "S1", values = listOf(10.0, 20.0, 30.0, 40.0, 50.0)),
                            ChartSeries(name = "S2", values = listOf(5.0, 6.0, 7.0, 8.0, 9.0)),
                        ),
                    selection = selection,
                    modifier = Modifier.size(width = 400.dp, height = 300.dp),
                    animateOnStart = false,
                )
            }
            val plot = onNodeWithTag(TestTags.STACKED_AREA_CHART_PLOT)
            val widthPx =
                plot
                    .fetchSemanticsNode()
                    .size.width
                    .toFloat()
            val stepPx = widthPx / 4f

            plot.performTouchInput {
                down(Offset(0f, centerY))
                moveTo(Offset(stepPx - 2f, centerY))
            }
            val besidePointOne = runOnIdle { selection.selectedIndex }
            plot.performTouchInput { moveTo(Offset(widthPx - 1f, centerY)) }
            val onLastPixel = runOnIdle { selection.selectedIndex }
            plot.performTouchInput { up() }

            assertEquals(expected = 1, actual = besidePointOne)
            assertEquals(expected = 4, actual = onLastPixel)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withEmAxisLabelSize_displaysValidationError() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    style =
                        StackedAreaChartDefaults.style(
                            axis =
                                StackedAreaChartDefaults.axis(
                                    yLabels = StackedAreaChartDefaults.yLabels(size = 1.em),
                                ),
                        ),
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText(
                "Y-axis label size must be a finite, positive sp value.",
                substring = true,
            ).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Act
            setContent {
                StackedAreaChart(data = multiDataSet)
            }

            // Assert
            onNodeWithTag(TestTags.STACKED_AREA_CHART).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_insideVerticalScroll_hasNonZeroPlotHeight() =
        runComposeUiTest {
            setContent {
                Column(
                    modifier =
                        Modifier
                            .width(240.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    StackedAreaChart(
                        data = multiDataSet,
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds =
                onNodeWithTag(TestTags.STACKED_AREA_CHART)
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertTrue(chartBounds.height > 0f, "Chart bounds must have positive height: $chartBounds")
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withInvalidData_displaysError() =
        runComposeUiTest {
            // Arrange
            val data =
                chartDataOf(
                    categories = listOf("Q1"),
                    ChartSeries(name = "Series A", values = listOf(10.0, 20.0)),
                    ChartSeries(name = "Series B", values = listOf(8.0)),
                )

            // Act
            setContent {
                StackedAreaChart(data = data)
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("Series 1 is not aligned with the first series.\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withSelectedPointIndex_displaysSelectedPointDetails() =
        runComposeUiTest {
            // Arrange
            val selectedPointIndex = 1
            val expectedTitle = multiDataSet.categories[selectedPointIndex]

            // Act
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    selection = staticChartSelection(selectedPointIndex),
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            // Assert
            onNodeWithTag(TestTags.STACKED_AREA_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withXAxisLabelsHidden_doesNotRenderXAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    style =
                        StackedAreaChartDefaults.style(
                            axis =
                                StackedAreaChartDefaults.axis(
                                    xLabels = StackedAreaChartDefaults.xLabels(visible = false),
                                ),
                        ),
                )
            }

            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withYAxisLabelsHidden_doesNotRenderYAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    style =
                        StackedAreaChartDefaults.style(
                            axis =
                                StackedAreaChartDefaults.axis(
                                    yLabels = StackedAreaChartDefaults.yLabels(visible = false),
                                ),
                        ),
                )
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).isDisplayed()
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withoutCategories_hidesXAxisLayer() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = emptyList(),
                    ChartSeries(name = "Series A", values = listOf(10.0, 20.0, 30.0, 25.0)),
                    ChartSeries(name = "Series B", values = listOf(5.0, 15.0, 20.0, 18.0)),
                )

            setContent {
                StackedAreaChart(data = data, title = "No Categories")
            }

            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withBlankCategoriesAndSelection_hidesXAxisAndKeepsTitleAndLegend() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = listOf("", " ", "", ""),
                    ChartSeries(name = "Series A", values = listOf(10.0, 20.0, 30.0, 25.0)),
                    ChartSeries(name = "Series B", values = listOf(5.0, 15.0, 20.0, 18.0)),
                )

            setContent {
                StackedAreaChart(
                    data = data,
                    title = "Blank Categories",
                    selection = staticChartSelection(index = 1),
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Blank Categories")
            onNodeWithText("Series A", substring = true).assertIsDisplayed()
            onNodeWithText("Series B", substring = true).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withLabelsBlankAfterCompactAggregation_hidesXAxisLayer() =
        runComposeUiTest {
            val points = 120
            val data =
                chartDataOf(
                    categories = List(points) { index -> if (index == 0) "Start" else "" },
                    ChartSeries(name = "Series A", values = List(points) { index -> 40.0 + (index % 8) }),
                    ChartSeries(name = "Series B", values = List(points) { index -> 25.0 + (index % 6) }),
                )

            setContent {
                StackedAreaChart(data = data, animateOnStart = false)
            }

            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(data = denseStackedAreaData(), title = "Dense Stacked Area")
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_EXPAND).isDisplayed()
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withLargeDataset_expandShowsZoomControls() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(data = denseStackedAreaData(), title = "Dense Stacked Area")
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_COLLAPSE).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_ZOOM_OUT).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_ZOOM_IN).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withZoomControlsHidden_doesNotRenderZoomButtons() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = denseStackedAreaData(),
                    title = "Dense Stacked Area",
                    style = StackedAreaChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_EXPAND).performTouchInput { click() }
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withInvalidColors_displaysError() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    style =
                        StackedAreaChartDefaults.style(
                            fill = StackedAreaChartDefaults.fill(colors = listOf(seriesColors[0], seriesColors[1])),
                        ),
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("Color count (2) must match series count (4).\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withInvalidNumericStyleValues_drawsClampedChart() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    style =
                        StackedAreaChartDefaults.style(
                            fill = StackedAreaChartDefaults.fill(alpha = Float.NaN),
                            selection =
                                StackedAreaChartDefaults.selection(
                                    width = (-1).dp,
                                    unselectedAlpha = -0.1f,
                                ),
                        ),
                    selection = staticChartSelection(1),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_PLOT).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withNegativeData_displaysError() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = listOf("Q1", "Q2"),
                    ChartSeries(name = "Series A", values = listOf(10.0, -2.0)),
                    ChartSeries(name = "Series B", values = listOf(5.0, 8.0)),
                )

            setContent {
                StackedAreaChart(data = data)
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("Series 0 contains a negative value.\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_lastXAxisLabel_centeredOnLastPoint() =
        runComposeUiTest {
            val edgeData =
                chartDataOf(
                    categories = listOf("Region 4", "Region 36", "Region 68", "Region 100"),
                    ChartSeries(name = "Q1", values = listOf(320.0, 280.0, 260.0, 300.0)),
                    ChartSeries(name = "Q2", values = listOf(180.0, 210.0, 190.0, 220.0)),
                    ChartSeries(name = "Q3", values = listOf(120.0, 140.0, 130.0, 150.0)),
                )

            setContent {
                StackedAreaChart(data = edgeData, title = "Quarterly Revenue by Region")
            }

            val plotBounds = onNodeWithTag(TestTags.STACKED_AREA_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val lastLabelBounds = onNodeWithText("Region 100").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

            assertEquals(expected = plotBounds.right, actual = lastLabelBounds.center.x, absoluteTolerance = 1.5f)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_xAxisLabels_centeredOnTheirPoints() =
        runComposeUiTest {
            val categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

            setContent {
                StackedAreaChart(
                    data =
                        chartDataOf(
                            categories = categories,
                            ChartSeries(name = "Web", values = listOf(12.0, 18.0, 15.0, 22.0, 19.0)),
                            ChartSeries(name = "App", values = listOf(8.0, 9.0, 11.0, 10.0, 12.0)),
                        ),
                    modifier = Modifier.size(width = 600.dp, height = 400.dp),
                )
            }

            val plotBounds = onNodeWithTag(TestTags.STACKED_AREA_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val pointStep = plotBounds.width / (categories.size - 1)
            categories.forEachIndexed { index, label ->
                val labelBounds = onNodeWithText(label).assertIsDisplayed().fetchSemanticsNode().boundsInRoot

                assertEquals(
                    expected = plotBounds.left + pointStep * index,
                    actual = labelBounds.center.x,
                    absoluteTolerance = 1.5f,
                    message = label,
                )
            }
        }

    private val seriesColors =
        listOf(
            androidx.compose.ui.graphics.Color.Red,
            androidx.compose.ui.graphics.Color.Green,
            androidx.compose.ui.graphics.Color.Blue,
            androidx.compose.ui.graphics.Color.Yellow,
        )

    private fun denseStackedAreaData(points: Int = 120) =
        chartDataOf(
            categories = List(points) { index -> "P${index + 1}" },
            ChartSeries(name = "Series A", values = List(points) { index -> 40.0 + (index % 8) }),
            ChartSeries(name = "Series B", values = List(points) { index -> 25.0 + (index % 6) }),
            ChartSeries(name = "Series C", values = List(points) { index -> 15.0 + (index % 5) }),
        )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_formatters_formatSelectedLegendValuesAndYAxisLabels() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    selection = staticChartSelection(0),
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedValueFormatter = ChartValueFormatter { value -> "#${value.toInt()}" },
                    axisValueFormatter = ChartValueFormatter { value -> "y=${value.toInt()}" },
                )
            }

            onNodeWithText("Item 3 - #4000").assertIsDisplayed()
            onNodeWithText("y=0").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_hiddenLegend_hidesSeriesNames() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(
                    data = multiDataSet,
                    style = StackedAreaChartDefaults.style(legend = StackedAreaChartDefaults.legend(visible = false)),
                    animateOnStart = false,
                )
            }

            onAllNodesWithText("Item 3").assertCountEquals(0)
        }

    private companion object {
        val multiDataSet =
            chartDataOf(
                categories = listOf("Jan", "Feb", "Mar", "Apr"),
                ChartSeries(name = "Item 1", values = listOf(26000.68, 28000.34, 32000.57, 45000.57)),
                ChartSeries(name = "Item 2", values = listOf(26000.68, 28000.34, 32000.57, 45000.57)),
                ChartSeries(name = "Item 3", values = listOf(4000.87, 5000.58, 30245.81, 135000.58)),
                ChartSeries(name = "Item 4", values = listOf(1000.87, 9000.58, 16544.81, 100444.87)),
            )
    }
}
