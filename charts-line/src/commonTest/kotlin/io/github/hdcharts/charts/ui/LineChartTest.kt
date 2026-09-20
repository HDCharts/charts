package io.github.hdcharts.charts.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
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
import io.github.hdcharts.charts.mock.MockTest.multiDataSet
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.LineChartDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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
    fun lineChart_rangeOnlyStyleChange_movesLineWithoutDataChange() =
        runComposeUiTest {
            val rangeMin = mutableStateOf<Double?>(null)
            lateinit var captureLayer: GraphicsLayer
            lateinit var captureScope: CoroutineScope
            val capturedPixels = mutableStateOf<PixelMap?>(null)
            setContent {
                captureLayer = rememberGraphicsLayer()
                captureScope = rememberCoroutineScope()
                LineChart(
                    data = listOf(25.0, 75.0).toChartData(),
                    modifier =
                        Modifier
                            .testTag("line-capture")
                            .size(width = 200.dp, height = 300.dp)
                            .drawWithContent {
                                captureLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(captureLayer)
                            },
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

            fun capturePixels(): PixelMap {
                capturedPixels.value = null
                runOnIdle { captureScope.launch { capturedPixels.value = captureLayer.toImageBitmap().toPixelMap() } }
                waitUntil(timeoutMillis = 3_000L) { capturedPixels.value != null }
                return checkNotNull(capturedPixels.value)
            }

            val plotBounds = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val captureBounds = onNodeWithTag("line-capture").fetchSemanticsNode().boundsInRoot
            val sampleX = (plotBounds.left - captureBounds.left + 2f).toInt()
            val sampleY = (plotBounds.top - captureBounds.top + plotBounds.height * 0.95f).toInt()

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
