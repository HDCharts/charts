package io.github.dautovicharis.charts.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.LineChartRenderMode
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.mock.MockTest.colors
import io.github.dautovicharis.charts.mock.MockTest.invalidMultiDataSet
import io.github.dautovicharis.charts.mock.MockTest.multiDataSet
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.LineChartDefaults
import kotlin.test.Test

class MultiLineChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = "Title"

            // Act
            setContent {
                LineChart(data = multiDataSet, title = expectedTitle)
            }

            // Assert
            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withTimelineRenderMode_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = "Title"
            val expectedCurrentValueItem1 = "Item 1 - \$45000.57"

            // Act
            setContent {
                LineChart(
                    data = multiDataSet,
                    title = expectedTitle,
                    valueFormatter = ChartValueFormatters.prefix("$"),
                    renderMode = LineChartRenderMode.Timeline,
                )
            }

            // Assert
            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
            onAllNodesWithText(expectedCurrentValueItem1).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withSelectedPointIndex_displaysSelectedPointDetails() =
        runComposeUiTest {
            val selectedIndex = 2
            val expectedTitle = multiDataSet.categories[selectedIndex]
            setContent {
                LineChart(
                    data = multiDataSet,
                    title = "Title",
                    valueFormatter = ChartValueFormatters.prefix("$"),
                    interactionEnabled = false,
                    animateOnStart = false,
                    selection = staticChartSelection(selectedIndex),
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
            // The selected axis title is the stable public readout contract; legend layout is
            // rendered by the shared FlowRow and is covered by screenshot/platform tests.
            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withLargeDatasetInMorphMode_showsCompactToggleByDefault() =
        runComposeUiTest {
            val points = 120
            val categories = List(points) { index -> "P${index + 1}" }
            val dataSet =
                listOf(
                    ChartSeries(name = "Item 1", values = List(points) { index -> (index % 40).toDouble() }),
                    ChartSeries(name = "Item 2", values = List(points) { index -> ((index % 40) + 10).toDouble() }),
                ).let { series ->
                    chartDataOf(
                        categories = categories,
                        *series.toTypedArray(),
                    )
                }

            setContent {
                LineChart(data = dataSet, title = "Dense Multi")
            }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withoutCategories_hidesXAxisLayerAndShowsYAxisLayer() =
        runComposeUiTest {
            val dataSet =
                listOf(
                    ChartSeries(name = "Item 1", values = listOf(10.0, 20.0, 30.0, 40.0)),
                    ChartSeries(name = "Item 2", values = listOf(5.0, 15.0, 25.0, 35.0)),
                ).let { series ->
                    chartDataOf(categories = emptyList(), *series.toTypedArray())
                }

            setContent {
                LineChart(data = dataSet, title = "No Categories")
            }

            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiLineChart_withInvalidData_displaysError() =
        runComposeUiTest {
            // Arrange
            val dataSet = invalidMultiDataSet()
            val colors = colors.drop(1)
            // Act
            setContent {
                val style =
                    LineChartDefaults.style(
                        line = LineChartDefaults.line(colors = colors),
                    )
                LineChart(
                    data = dataSet,
                    title = "Title",
                    style = style,
                )
            }

            // Assert
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Category count", substring = true).assertIsDisplayed()
            onNodeWithText("Series 1 is not aligned", substring = true).assertIsDisplayed()
            onNodeWithText("Line color count", substring = true).assertIsDisplayed()
        }
}
