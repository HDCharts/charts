package io.github.hdcharts.charts.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.LiveLineChart
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.mock.MockTest.TITLE
import io.github.hdcharts.charts.mock.MockTest.dataSet
import io.github.hdcharts.charts.mock.MockTest.multiDataSet
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSelection
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.model.chartDataOf
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.LineChartDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LineChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_longEdgeXAxisLabels_stayInsideChartBounds() =
        runComposeUiTest {
            val categories = listOf("Region 1", "Region 36", "Region 68", "Region 100")
            setContent {
                Box(modifier = Modifier.size(width = 400.dp, height = 300.dp).testTag("chart-bounds")) {
                    LineChart(
                        data = listOf(20.0, 28.0, 23.0, 30.0).toChartData(categories = categories),
                        modifier = Modifier.fillMaxSize(),
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds = onNodeWithTag("chart-bounds").fetchSemanticsNode().boundsInRoot
            val firstLabelBounds = onNodeWithText("Region 1").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val lastLabelBounds = onNodeWithText("Region 100").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val plotBounds = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot

            assertTrue(
                lastLabelBounds.right <= chartBounds.right,
                "last label ends at ${lastLabelBounds.right}, chart at ${chartBounds.right}",
            )
            assertTrue(firstLabelBounds.left >= chartBounds.left, "first label starts at ${firstLabelBounds.left}")
            assertEquals(expected = plotBounds.right, actual = lastLabelBounds.center.x, absoluteTolerance = 1.5f)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_dragBesidePoint_selectsNearestPoint() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent {
                LineChart(
                    data = listOf(10.0, 20.0, 30.0, 40.0, 50.0).toChartData(),
                    selection = selection,
                    modifier = Modifier.size(width = 400.dp, height = 300.dp),
                    animateOnStart = false,
                )
            }
            val plot = onNodeWithTag(TestTags.LINE_CHART_PLOT)
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
    fun lineChart_expandedPastLayoutLimits_displaysErrorInsteadOfCrashing() =
        runComposeUiTest {
            // 22,000 points at the 12 px dense step need 263,988 px, past the 262,143 px Compose can measure.
            setContent {
                LineChart(
                    data = List(22_000) { index -> (index % 17).toDouble() }.toChartData(),
                    modifier = Modifier.size(width = 400.dp, height = 300.dp),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).performTouchInput { click() }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Chart exceeds layout limits.", substring = true).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_DENSE_COLLAPSE).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withEmAxisLabelSize_displaysValidationError() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = listOf(10.0, 20.0, 30.0).toChartData(),
                    style =
                        LineChartDefaults.style(
                            axis = LineChartDefaults.axis(xLabels = LineChartDefaults.xLabels(size = 1.em)),
                        ),
                    animateOnStart = false,
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
    fun lineChart_withFixedRange_appliesIndependentMinAndMaxOverrides() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = listOf(10.0, 20.0, 30.0).toChartData(),
                    style = LineChartDefaults.style(range = LineChartDefaults.range(min = 0.0, max = 100.0)),
                    animateOnStart = false,
                )
            }

            onNodeWithText("100").assertIsDisplayed()
            onNodeWithText("0").assertIsDisplayed()
            onAllNodesWithText("30").assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withNonFiniteRangeBound_displaysValidationError() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = listOf(10.0, 20.0, 30.0).toChartData(),
                    style = LineChartDefaults.style(range = LineChartDefaults.range(min = Double.NaN)),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Range bounds must be finite.", substring = true).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withUnspecifiedPointSize_displaysValidationError() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = listOf(10.0, 20.0, 30.0).toChartData(),
                    style = LineChartDefaults.style(points = LineChartDefaults.points(size = Dp.Unspecified)),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Point size must resolve to 0..16384 pixels.", substring = true).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_rangeOnlyStyleChange_movesLineWithoutDataChange() =
        runComposeUiTest {
            val rangeMin = mutableStateOf<Double?>(null)
            val capturePixels =
                setCapturedContent { captureModifier ->
                    LineChart(
                        data = listOf(25.0, 75.0).toChartData(),
                        modifier =
                            captureModifier
                                .testTag("line-capture")
                                .size(width = 200.dp, height = 300.dp),
                        animateOnStart = false,
                        style =
                            LineChartDefaults.style(
                                chartContainerStyle = ChartContainerDefaults.style(contentPadding = 0.dp),
                                line =
                                    LineChartDefaults.line(
                                        color = Color.Blue,
                                        alpha = 1f,
                                        strokeWidth = 10.dp,
                                        bezier = false,
                                    ),
                                range = LineChartDefaults.range(min = rangeMin.value),
                            ),
                    )
                }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()

            val plotBounds = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val captureBounds = onNodeWithTag("line-capture").fetchSemanticsNode().boundsInRoot
            val sampleX = (plotBounds.left - captureBounds.left + 2f).toInt()
            // The line's vertical inset is a small fixed pixel margin, not density-scaled, so a
            // value at the bottom of the range hugs within a few px of the true bottom edge.
            val sampleY = (plotBounds.top - captureBounds.top + plotBounds.height - 3f).toInt()

            // The first value normalizes to the bottom of the plot under the default, data-derived range.
            val beforePixels = capturePixels()
            assertEquals(Color.Blue, beforePixels[sampleX, sampleY])

            // Only the style's fixed range changes; the data instance is untouched.
            runOnIdle { rangeMin.value = 0.0 }
            val afterPixels = capturePixels()
            assertNotEquals(
                Color.Blue,
                afterPixels[sampleX, sampleY],
                "A range-only style change must re-normalize and redraw the line, not leave it at the stale position.",
            )
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
    fun liveLineChart_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedLegendCurrentValue = "$TITLE - 40.0"

            // Act
            setContent {
                LiveLineChart(data = dataSet)
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
    fun lineChart_multiSeriesWithHiddenLegend_doesNotRenderLegend() =
        runComposeUiTest {
            setContent {
                LineChart(
                    data = multiDataSet,
                    style = LineChartDefaults.style(legend = LineChartDefaults.legend(visible = false)),
                )
            }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onAllNodesWithText("Item 1").assertCountEquals(0)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_multiSeriesWithDefaultLegend_rendersLegend() =
        runComposeUiTest {
            setContent {
                LineChart(data = multiDataSet)
            }

            onNodeWithTag(TestTags.LINE_CHART).assertIsDisplayed()
            onNodeWithText("Item 1").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun liveLineChart_withLargeDataset_hidesZoomControls() =
        runComposeUiTest {
            setContent {
                LiveLineChart(data = largeDataSet())
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
    fun lineChart_lastXAxisLabel_centeredOnLastPoint() =
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

            val plotBounds = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val lastLabelBounds = onNodeWithText("Region 100").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

            assertEquals(expected = plotBounds.right, actual = lastLabelBounds.center.x, absoluteTolerance = 1.5f)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_xAxisLabels_centeredOnTheirPoints() =
        runComposeUiTest {
            val categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

            setContent {
                LineChart(
                    data =
                        listOf(12.0, 18.0, 15.0, 22.0, 19.0)
                            .toChartData(seriesName = "Visits", categories = categories),
                    modifier = Modifier.size(width = 600.dp, height = 400.dp),
                )
            }

            val plotBounds = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lineChart_withBlankCategoriesAndSelection_titleShowsValueOnly() =
        runComposeUiTest {
            val dataSet =
                chartDataOf(
                    categories = listOf("", "", ""),
                    ChartSeries(values = listOf(10.0, 20.0, 30.0)),
                )

            setContent {
                LineChart(
                    data = dataSet,
                    valueFormatter = ChartValueFormatter { value -> "v${value.toInt()}" },
                    selection = staticChartSelection(index = 1),
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("v20")
        }

    private fun largeDataSet(points: Int = 120): ChartData {
        val labels = List(points) { index -> "Point ${index + 1}" }
        val values = List(points) { index -> (index % 25).toDouble() }
        return values.toChartData(categories = labels, seriesName = "Large Line Chart")
    }
}
