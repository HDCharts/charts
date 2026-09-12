package io.github.dautovicharis.charts.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.mock.MockTest.multiDataSet
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import kotlin.test.Test
import kotlin.test.assertTrue

class StackedAreaChartTest {
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
            onNodeWithText("Fill color count must match series count (4).\n").isDisplayed()
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
            onNodeWithText("Series 0 contains a negative or non-finite contribution.\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_lastXAxisLabel_hasRightEdgePadding() =
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

            val axisBounds = onNodeWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).fetchSemanticsNode().boundsInRoot
            val rightMostVisibleLabelBounds = onNodeWithText("Region 68").fetchSemanticsNode().boundsInRoot

            onAllNodesWithText("Region 100").assertCountEquals(0)
            assertTrue(rightMostVisibleLabelBounds.right <= axisBounds.right - 1f)
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
}
