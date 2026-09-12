package io.github.dautovicharis.charts.ui

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
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.mock.MockTest.colors
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.StackedBarChartDefaults
import kotlin.test.Test
import kotlin.test.assertTrue

class StackedBarChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = "Quarterly Revenue by Region"
            val data =
                stackedData(
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
            onNodeWithText("Segment 1 is not aligned with the first segment.\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withInvalidColors_displaysError() =
        runComposeUiTest {
            // Arrange
            val data =
                stackedData(
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
            onNodeWithText("Segment color count must match segment count (3).\n").assertIsDisplayed()
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
            onNodeWithText("Segment 0 contains a negative or non-finite contribution.\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stackedBarChart_withSelection_displaysSelectedBarDetails() =
        runComposeUiTest {
            // Arrange
            val selectedIndex = 1
            val data =
                stackedData(
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
                    data = transpose(dataSet, listOf("Q1", "Q2", "Q3", "Q4")),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onNodeWithText("North America").assertIsDisplayed()
            onNodeWithText("Europe").assertIsDisplayed()
            onNodeWithText("Asia Pacific").assertIsDisplayed()
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
                )

            setContent {
                StackedBarChart(
                    data = transpose(dataSet, listOf("S1", "S2", "S3")),
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
                    data = denseStackedBarDataSet(),
                    style = StackedBarChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    private fun validData() =
        stackedData(
            rows =
                listOf(
                    "Bar 1" to listOf(10f, 20f, 30f),
                    "Bar 2" to listOf(12f, 22f, 32f),
                    "Bar 3" to listOf(14f, 24f, 34f),
                ),
            segmentNames = listOf("S1", "S2", "S3"),
        )

    private fun denseStackedBarDataSet(bars: Int = 120) =
        List(bars) { index ->
            "Bar ${index + 1}" to
                listOf(
                    50f + (index % 9),
                    30f + (index % 7),
                    20f + (index % 5),
                    10f + (index % 3),
                )
        }.let { rows -> stackedData(rows, listOf("S1", "S2", "S3", "S4")) }

    private fun stackedData(
        rows: List<Pair<String, List<Float>>>,
        segmentNames: List<String>,
    ) = chartDataOf(
        categories = rows.map { (barLabel, _) -> barLabel },
        *segmentNames
            .mapIndexed { segmentIndex, segmentName ->
                ChartSeries(
                    name = segmentName,
                    values = rows.map { (_, values) -> values.getOrNull(segmentIndex)?.toDouble() ?: Double.NaN },
                )
            }.toTypedArray(),
    )

    private fun transpose(
        rows: List<Pair<String, List<Float>>>,
        segmentNames: List<String>,
    ) = stackedData(rows, segmentNames)
}
