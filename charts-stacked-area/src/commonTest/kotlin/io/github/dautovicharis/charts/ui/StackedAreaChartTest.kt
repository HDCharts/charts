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
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_STACKED_AREA
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.multiDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import kotlin.test.Test
import kotlin.test.assertTrue

class StackedAreaChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = multiDataSet.data.title

            // Act
            setContent {
                StackedAreaChart(multiDataSet)
            }

            // Assert
            onNodeWithTag(TestTags.STACKED_AREA_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withInvalidData_displaysError() =
        runComposeUiTest {
            // Arrange
            val dataSet =
                listOf("Series" to listOf(10f)).toMultiChartDataSet(
                    title = "Stacked Area",
                    categories = listOf("Q1"),
                )
            val expectedError =
                RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_STACKED_AREA)

            // Act
            setContent {
                StackedAreaChart(dataSet)
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withSelectedPointIndex_displaysSelectedPointDetails() =
        runComposeUiTest {
            // Arrange
            val selectedPointIndex = 1
            val expectedTitle = multiDataSet.data.getLabel(selectedPointIndex)

            // Act
            setContent {
                StackedAreaChart(
                    dataSet = multiDataSet,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedPointIndex = selectedPointIndex,
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
                    dataSet = multiDataSet,
                    style = StackedAreaChartDefaults.style(xAxisLabelsVisible = false),
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
                    dataSet = multiDataSet,
                    style = StackedAreaChartDefaults.style(yAxisLabelsVisible = false),
                )
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).isDisplayed()
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withoutCategories_hidesXAxisLayer() =
        runComposeUiTest {
            val dataSet =
                listOf(
                    "Series A" to listOf(10f, 20f, 30f, 25f),
                    "Series B" to listOf(5f, 15f, 20f, 18f),
                ).toMultiChartDataSet(
                    title = "No Categories",
                )

            setContent {
                StackedAreaChart(dataSet = dataSet)
            }

            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(dataSet = denseStackedAreaDataSet())
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
                StackedAreaChart(dataSet = denseStackedAreaDataSet())
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
                    dataSet = denseStackedAreaDataSet(),
                    style = StackedAreaChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_EXPAND).performTouchInput { click() }
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedAreaChart_lastXAxisLabel_hasRightEdgePadding() =
        runComposeUiTest {
            val edgeDataSet =
                listOf(
                    "Q1" to listOf(320f, 280f, 260f, 300f),
                    "Q2" to listOf(180f, 210f, 190f, 220f),
                    "Q3" to listOf(120f, 140f, 130f, 150f),
                ).toMultiChartDataSet(
                    title = "Quarterly Revenue by Region",
                    categories = listOf("Region 4", "Region 36", "Region 68", "Region 100"),
                )

            setContent {
                StackedAreaChart(dataSet = edgeDataSet)
            }

            val axisBounds = onNodeWithTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS).fetchSemanticsNode().boundsInRoot
            val rightMostVisibleLabelBounds = onNodeWithText("Region 68").fetchSemanticsNode().boundsInRoot

            onAllNodesWithText("Region 100").assertCountEquals(0)
            assertTrue(rightMostVisibleLabelBounds.right <= axisBounds.right - 1f)
        }

    private fun denseStackedAreaDataSet(points: Int = 120) =
        listOf(
            "Series A" to List(points) { index -> 40f + (index % 8) },
            "Series B" to List(points) { index -> 25f + (index % 6) },
            "Series C" to List(points) { index -> 15f + (index % 5) },
        ).toMultiChartDataSet(
            title = "Dense Stacked Area",
            categories = List(points) { index -> "P${index + 1}" },
        )
}
