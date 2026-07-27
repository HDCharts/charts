package io.github.dautovicharis.charts.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_ITEM_POINTS_SIZE
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.colors
import io.github.dautovicharis.charts.mock.MockTest.invalidMultiDataSet
import io.github.dautovicharis.charts.mock.MockTest.multiDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.StackedBarChartDefaults
import kotlin.test.Test
import kotlin.test.assertTrue

class StackedBarChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = multiDataSet.data.title

            // Act
            setContent {
                StackedBarChart(multiDataSet)
            }

            // Assert
            onNodeWithTag(TestTags.STACKED_BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withInvalidData_displaysError() =
        runComposeUiTest {
            // Arrange
            val dataSet = invalidMultiDataSet()
            val firstIndex = 1
            val thirdIndex = 3

            val pointsSizeFirst =
                dataSet.data.items[firstIndex]
                    .item.points.size
            val pointsSizeThird =
                dataSet.data.items[thirdIndex]
                    .item.points.size
            val expectedPointsSize =
                dataSet.data.items
                    .first()
                    .item.points.size

            val expectedPointsErrorFirst = RULE_ITEM_POINTS_SIZE.format(firstIndex, pointsSizeFirst, expectedPointsSize)
            val expectedPointsErrorSecond =
                RULE_ITEM_POINTS_SIZE.format(
                    thirdIndex,
                    pointsSizeThird,
                    expectedPointsSize,
                )

            // Act
            setContent {
                val style = StackedBarChartDefaults.style()
                StackedBarChart(
                    dataSet = dataSet,
                    style = style,
                )
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedPointsErrorFirst}\n").isDisplayed()
            onNodeWithText("${expectedPointsErrorSecond}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withInvalidColors_displaysError() =
        runComposeUiTest {
            // Arrange
            val dataSet = multiDataSet
            val colors = colors.drop(1)

            val expectedColorsSize = dataSet.data.items.size
            val colorsSize = colors.size
            val expectedColorsError = RULE_COLORS_SIZE_MISMATCH.format(colorsSize, expectedColorsSize)

            // Act
            setContent {
                val style = StackedBarChartDefaults.style(barColors = colors)
                StackedBarChart(
                    dataSet = dataSet,
                    style = style,
                )
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedColorsError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withSelectedBarIndex_displaysSelectedBarDetails() =
        runComposeUiTest {
            // Arrange
            val selectedBarIndex = 1
            val expectedTitle = multiDataSet.data.items[selectedBarIndex].label

            // Act
            setContent {
                StackedBarChart(
                    dataSet = multiDataSet,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedBarIndex = selectedBarIndex,
                )
            }

            // Assert
            onNodeWithTag(TestTags.STACKED_BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withXAxisLabelsHidden_doesNotRenderXAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    dataSet = multiDataSet,
                    style = StackedBarChartDefaults.style(xAxisLabelsVisible = false),
                )
            }

            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withYAxisLabelsHidden_doesNotRenderYAxisLayer() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    dataSet = multiDataSet,
                    style = StackedBarChartDefaults.style(yAxisLabelsVisible = false),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).isDisplayed()
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    dataSet = denseStackedBarDataSet(),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).isDisplayed()
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withLargeDataset_expandShowsZoomControls() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    dataSet = denseStackedBarDataSet(),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_COLLAPSE).isDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).isDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).isDisplayed()
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
                ).toMultiChartDataSet(
                    title = "Quarterly Revenue by Region",
                    categories = listOf("Q1", "Q2", "Q3", "Q4"),
                )

            setContent {
                StackedBarChart(
                    dataSet = dataSet,
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithText("North America").isDisplayed()
            onNodeWithText("Europe").isDisplayed()
            onNodeWithText("Asia Pacific").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_lastXAxisLabel_hasRightEdgePadding() =
        runComposeUiTest {
            val dataSet =
                listOf(
                    "Region 1" to listOf(320f, 340f, 360f, 390f),
                    "Region 2" to listOf(260f, 280f, 240f, 260f),
                    "Region 3" to listOf(220f, 210f, 230f, 250f),
                    "Region 4" to listOf(210f, 220f, 240f, 260f),
                ).toMultiChartDataSet(
                    title = "Quarterly Revenue by Region",
                    categories = listOf("Q1", "Q2", "Q3", "Q4"),
                )

            setContent {
                StackedBarChart(
                    dataSet = dataSet,
                )
            }

            val axisBounds =
                onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS)
                    .fetchSemanticsNode()
                    .boundsInRoot
            val lastLabelBounds = onNodeWithText("Region 4").fetchSemanticsNode().boundsInRoot

            assertTrue(lastLabelBounds.right <= axisBounds.right - 1f)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withZoomControlsHidden_doesNotRenderZoomButtons() =
        runComposeUiTest {
            setContent {
                StackedBarChart(
                    dataSet = denseStackedBarDataSet(),
                    style = StackedBarChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    private fun denseStackedBarDataSet(bars: Int = 120) =
        List(bars) { index ->
            "Bar ${index + 1}" to
                listOf(
                    50f + (index % 9),
                    30f + (index % 7),
                    20f + (index % 5),
                    10f + (index % 3),
                )
        }.toMultiChartDataSet(
            title = "Dense Stacked Bar",
            categories = listOf("S1", "S2", "S3", "S4"),
        )
}
