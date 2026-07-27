package io.github.dautovicharis.charts.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.LineChartRenderMode
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_ITEM_POINTS_SIZE
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.colors
import io.github.dautovicharis.charts.mock.MockTest.invalidMultiDataSet
import io.github.dautovicharis.charts.mock.MockTest.multiDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.LineChartDefaults
import kotlin.test.Test

class MultiLineChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = multiDataSet.data.title

            // Act
            setContent {
                LineChart(multiDataSet)
            }

            // Assert
            onNodeWithTag(TestTags.LINE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withTimelineRenderMode_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = multiDataSet.data.title
            val expectedCurrentValueItem1 = "Item 1 - 45000.57"

            // Act
            setContent {
                LineChart(
                    dataSet = multiDataSet,
                    renderMode = LineChartRenderMode.Timeline,
                )
            }

            // Assert
            onNodeWithTag(TestTags.LINE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
            onAllNodesWithText(expectedCurrentValueItem1).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).isDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withSelectedPointIndex_displaysSelectedPointDetails() =
        runComposeUiTest {
            val selectedIndex = 2
            val expectedTitle = multiDataSet.data.categories[selectedIndex]
            val expectedCurrentValueItem1 =
                "${multiDataSet.data.items[0].label} - ${multiDataSet.data.items[0]
                    .item.labels[selectedIndex]}"

            setContent {
                LineChart(
                    dataSet = multiDataSet,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedPointIndex = selectedIndex,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
            onNodeWithText(expectedCurrentValueItem1).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withLargeDatasetInMorphMode_showsCompactToggleByDefault() =
        runComposeUiTest {
            val points = 120
            val categories = List(points) { index -> "P${index + 1}" }
            val dataSet =
                listOf(
                    "Item 1" to List(points) { index -> (index % 40).toFloat() },
                    "Item 2" to List(points) { index -> ((index % 40) + 10).toFloat() },
                ).toMultiChartDataSet(
                    title = "Dense Multi",
                    categories = categories,
                )

            setContent {
                LineChart(dataSet = dataSet)
            }

            onNodeWithTag(TestTags.LINE_CHART).isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).isDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withoutCategories_hidesXAxisLayerAndShowsYAxisLayer() =
        runComposeUiTest {
            val dataSet =
                listOf(
                    "Item 1" to listOf(10f, 20f, 30f, 40f),
                    "Item 2" to listOf(5f, 15f, 25f, 35f),
                ).toMultiChartDataSet(
                    title = "No Categories",
                )

            setContent {
                LineChart(dataSet = dataSet)
            }

            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withInvalidData_displaysError() =
        runComposeUiTest {
            // Arrange
            val dataSet = invalidMultiDataSet()
            val colors = colors.drop(1)
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

            val expectedColorsSize = dataSet.data.items.size
            val colorsSize = colors.size
            val expectedColorsError = RULE_COLORS_SIZE_MISMATCH.format(colorsSize, expectedColorsSize)

            // Act
            setContent {
                val style =
                    LineChartDefaults.style(
                        lineColors = colors,
                    )
                LineChart(
                    dataSet = dataSet,
                    style = style,
                )
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedPointsErrorFirst}\n").isDisplayed()
            onNodeWithText("${expectedPointsErrorSecond}\n").isDisplayed()
            onNodeWithText("${expectedColorsError}\n").isDisplayed()
        }
}
