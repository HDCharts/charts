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
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_LINE
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.common.model.ChartDataType
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.dataSet
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.LineChartDefaults
import kotlin.test.Test
import kotlin.test.assertTrue

class LineChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = dataSet.data.label

            // Act
            setContent {
                LineChart(dataSet)
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
    fun lineChart_withTimelineRenderMode_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = dataSet.data.label
            val expectedLegendCurrentValue = "${dataSet.data.label} - 40"

            // Act
            setContent {
                LineChart(
                    dataSet = dataSet,
                    renderMode = LineChartRenderMode.Timeline,
                )
            }

            // Assert
            onNodeWithTag(TestTags.LINE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
            onAllNodesWithText(expectedLegendCurrentValue).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).isDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withLargeDatasetInTimelineMode_hidesZoomControls() =
        runComposeUiTest {
            setContent {
                LineChart(
                    dataSet = largeDataSet(),
                    renderMode = LineChartRenderMode.Timeline,
                )
            }

            onNodeWithTag(TestTags.LINE_CHART).isDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withXAxisLabelsHidden_hidesXAxisLayerOnly() =
        runComposeUiTest {
            setContent {
                LineChart(
                    dataSet = dataSet,
                    style = LineChartDefaults.style(xAxisLabelsVisible = false),
                )
            }

            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withYAxisLabelsHidden_hidesYAxisLayerOnly() =
        runComposeUiTest {
            setContent {
                LineChart(
                    dataSet = dataSet,
                    style = LineChartDefaults.style(yAxisLabelsVisible = false),
                )
            }

            onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).isDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_lastXAxisLabel_hasRightEdgePadding() =
        runComposeUiTest {
            val edgeDataSet =
                listOf(20f, 28f, 23f, 30f).toChartDataSet(
                    title = "Quarterly Revenue by Region",
                    labels = listOf("Region 4", "Region 36", "Region 68", "Region 100"),
                )

            setContent {
                LineChart(dataSet = edgeDataSet)
            }

            val axisBounds = onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).fetchSemanticsNode().boundsInRoot
            val rightMostVisibleLabelBounds = onNodeWithText("Region 68").fetchSemanticsNode().boundsInRoot

            onAllNodesWithText("Region 100").assertCountEquals(0)
            assertTrue(rightMostVisibleLabelBounds.right <= axisBounds.right - 1f)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withInvalidData_displaysError() =
        runComposeUiTest {
            val dataSet =
                ChartDataSet(
                    items = ChartDataType.FloatData(listOf(1f)),
                    title = TITLE,
                )
            val expectedError = RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_LINE)

            setContent {
                LineChart(dataSet)
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    private fun largeDataSet(points: Int = 120): ChartDataSet {
        val labels = List(points) { index -> "Point ${index + 1}" }
        val values = List(points) { index -> (index % 25).toFloat() }
        return values.toChartDataSet(
            title = "Large Line Chart",
            labels = labels,
        )
    }
}
