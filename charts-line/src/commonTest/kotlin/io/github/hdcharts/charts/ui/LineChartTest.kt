package io.github.hdcharts.charts.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.mock.MockTest.TITLE
import io.github.hdcharts.charts.mock.MockTest.dataSet
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.LineChartDefaults
import kotlin.test.Test
import kotlin.test.assertTrue

class LineChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Act
            setContent {
                LineChart(data = dataSet)
            }

            // Assert
            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onAllNodesWithTag(TestTags.CHART_TITLE).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_insideVerticalScroll_hasNonZeroPlotHeight() =
        runComposeUiTest {
            setContent {
                Column(
                    modifier =
                        Modifier
                            .width(240.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    LineChart(
                        data = dataSet,
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds =
                onNodeWithTag(TestTags.LINE_CHART)
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertTrue(chartBounds.height > 0f, "Chart bounds must have positive height: $chartBounds")
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withTimelineRenderMode_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedLegendCurrentValue = "$TITLE - 40.0"

            // Act
            setContent {
                LineChart(
                    data = dataSet,
                    renderMode = LineChartRenderMode.Timeline,
                )
            }

            // Assert
            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onAllNodesWithTag(TestTags.CHART_TITLE).assertCountEquals(0)
            onAllNodesWithText(expectedLegendCurrentValue).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withLargeDatasetInTimelineMode_hidesZoomControls() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = largeDataSet(),
                    renderMode = LineChartRenderMode.Timeline,
                )
            }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withXAxisLabelsHidden_hidesXAxisLayerOnly() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = dataSet,
                    style =
                        LineChartDefaults.style(
                            axis = LineChartDefaults.axis(xLabels = LineChartDefaults.xLabels(visible = false)),
                        ),
                )
            }

            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withYAxisLabelsHidden_hidesYAxisLayerOnly() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = dataSet,
                    style =
                        LineChartDefaults.style(
                            axis = LineChartDefaults.axis(yLabels = LineChartDefaults.yLabels(visible = false)),
                        ),
                )
            }

            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_lastXAxisLabel_hasRightEdgePadding() =
        runComposeUiTest {
            val edgeDataSet =
                listOf(20f, 28f, 23f, 30f)
                    .map { it.toDouble() }
                    .toChartData(
                        seriesName = "Quarterly Revenue by Region",
                        categories = listOf("Region 4", "Region 36", "Region 68", "Region 100"),
                    )

            setContent {
                LineChart(data = edgeDataSet)
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
            val dataSet = ChartData(series = listOf(ChartSeries(name = TITLE, values = listOf(1.0))))

            setContent {
                LineChart(data = dataSet)
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("At least two line values are required.", substring = true).assertIsDisplayed()
        }

    private fun largeDataSet(points: Int = 120): ChartData {
        val labels = List(points) { index -> "Point ${index + 1}" }
        val values = List(points) { index -> (index % 25).toDouble() }
        return values.toChartData(categories = labels, seriesName = "Large Line Chart")
    }
}
