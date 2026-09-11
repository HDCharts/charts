package io.github.dautovicharis.charts.ui

import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_HISTOGRAM
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NEGATIVE
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NOT_NUMBER
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals

class HistogramChartTest {
    private val data: ChartData =
        listOf(3.0, 7.0, 10.0, 6.0)
            .toChartData(
                categories = listOf("0-10", "10-20", "20-30", "30-40"),
            )
    private val title = "Histogram"

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withValidData_displaysChart() =
        runComposeUiTest {
            setContent {
                HistogramChart(
                    data = data,
                    title = title,
                )
            }

            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(title)
                .assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withNegativeData_displaysError() =
        runComposeUiTest {
            val negativeData =
                listOf(2.0, -1.0, 4.0).toChartData(
                    categories = listOf("0-10", "10-20", "20-30"),
                )
            val expectedError = RULE_DATA_POINT_NEGATIVE.format(1)

            setContent {
                HistogramChart(
                    data = negativeData,
                    title = title,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withSelectedBarIndex_displaysSelectedBarDetails() =
        runComposeUiTest {
            val selectedBarIndex = 2
            val categories = data.categories
            val values = data.series.single().values
            val expectedLabel = categories[selectedBarIndex]
            val expectedValue = ChartValueFormatters.Default.format(values[selectedBarIndex])
            val expectedTitle = "$expectedLabel: $expectedValue"

            setContent {
                HistogramChart(
                    data = data,
                    title = title,
                    selection = staticChartSelection(selectedBarIndex),
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withInvalidBarColors_displaysError() =
        runComposeUiTest {
            val pointsSize =
                data.series
                    .single()
                    .values.size
            val expectedError =
                RULE_COLORS_SIZE_MISMATCH.format(
                    2,
                    pointsSize,
                )

            setContent {
                val style =
                    HistogramChartDefaults.style(
                        bars =
                            HistogramChartDefaults.bars(
                                colors = listOf(Color.Red, Color.Green),
                            ),
                    )
                HistogramChart(
                    data = data,
                    title = title,
                    style = style,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_emptySeriesAndRaggedCategories_displayErrors() =
        runComposeUiTest {
            val invalidInputs =
                listOf(
                    ChartData(),
                    ChartData(
                        series = listOf(ChartSeries(values = listOf(1.0, 2.0)), ChartSeries(values = listOf(3.0, 4.0))),
                    ),
                    ChartData(
                        series = listOf(ChartSeries(values = listOf(1.0, 2.0)), ChartSeries(values = listOf(3.0))),
                    ),
                    listOf(1.0, 2.0).toChartData(categories = listOf("Only one")),
                    listOf(1.0, 2.0).toChartData(categories = listOf("One", "Two", "Extra")),
                )
            val currentData = mutableStateOf(invalidInputs.first())
            setContent { HistogramChart(data = currentData.value, title = title, animateOnStart = false) }

            invalidInputs.forEach { invalidData ->
                runOnIdle { currentData.value = invalidData }
                onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
                onNodeWithTag(TestTags.BAR_CHART_PLOT).assertDoesNotExist()
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_emptyOrSingleBin_displaysMinimumSizeError() =
        runComposeUiTest {
            val currentData = mutableStateOf(emptyList<Double>().toChartData())
            val expectedError = RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_HISTOGRAM)
            setContent { HistogramChart(data = currentData.value, animateOnStart = false) }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
            runOnIdle { currentData.value = listOf(1.0).toChartData() }
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_nonFiniteValues_displayValidationErrors() =
        runComposeUiTest {
            val currentData = mutableStateOf(listOf(1.0, Double.NaN).toChartData())
            val expectedError = RULE_DATA_POINT_NOT_NUMBER.format(1)
            setContent { HistogramChart(data = currentData.value, animateOnStart = false) }

            listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { invalidValue ->
                runOnIdle { currentData.value = listOf(1.0, invalidValue).toChartData() }
                onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
                onNodeWithText(expectedError, substring = true).assertIsDisplayed()
                onNodeWithTag(TestTags.BAR_CHART_PLOT).assertDoesNotExist()
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_modifierTagAndSize_arePreservedOnSuccessAndError() =
        runComposeUiTest {
            val currentData = mutableStateOf(data)
            setContent {
                HistogramChart(
                    data = currentData.value,
                    modifier = Modifier.testTag("histogram-container").size(width = 280.dp, height = 240.dp),
                    title = title,
                    animateOnStart = false,
                )
            }

            onNodeWithTag("histogram-container")
                .assertIsDisplayed()
                .assertWidthIsEqualTo(280.dp)
                .assertHeightIsEqualTo(240.dp)
            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            val validBounds = onNodeWithTag("histogram-container").fetchSemanticsNode().boundsInRoot
            runOnIdle { currentData.value = listOf(1.0, -1.0).toChartData() }
            onNodeWithTag("histogram-container")
                .assertIsDisplayed()
                .assertWidthIsEqualTo(280.dp)
                .assertHeightIsEqualTo(240.dp)
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            assertEquals(validBounds, onNodeWithTag("histogram-container").fetchSemanticsNode().boundsInRoot)
            runOnIdle { currentData.value = data }
            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_titleOnlyUpdates_withoutCategories_doNotInventLabelsOrClearSelection() =
        runComposeUiTest {
            val unlabeledData = listOf(0.0, 123456.78).toChartData()
            val currentTitle = mutableStateOf("Original")
            val selection = ChartSelection()
            setContent {
                HistogramChart(
                    data = unlabeledData,
                    title = currentTitle.value,
                    selection = selection,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).assertDoesNotExist()
            runOnIdle { currentTitle.value = "Updated" }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Updated").assertIsDisplayed()
            runOnIdle { selection.select(1) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("123456.78").assertIsDisplayed()
            runOnIdle { currentTitle.value = "Latest" }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("123456.78").assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).assertDoesNotExist()
            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Latest").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_formatterChanges_updateFractionalSourceReadoutAndAxisIndependently() =
        runComposeUiTest {
            val preciseData = listOf(0.123456789, 1.0).toChartData(categories = listOf("0-10", "10+"))
            val selection = ChartSelection(initialIndex = 0)
            val valueFormatter = mutableStateOf(ChartValueFormatters.Default)
            val axisFormatter = mutableStateOf(ChartValueFormatters.Default)
            setContent {
                HistogramChart(
                    data = preciseData,
                    selection = selection,
                    valueFormatter = valueFormatter.value,
                    axisValueFormatter = axisFormatter.value,
                    style = HistogramChartDefaults.style(range = BarChartDefaults.range(min = 0.0, max = 1.0)),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10: 0.12").assertIsDisplayed()
            onNodeWithText("1.0").assertIsDisplayed()
            runOnIdle { valueFormatter.value = ChartValueFormatter { value -> "raw=$value" } }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10: raw=0.123456789").assertIsDisplayed()
            onNodeWithText("1.0").assertIsDisplayed()
            runOnIdle { axisFormatter.value = ChartValueFormatter { value -> "axis=$value" } }
            onNodeWithText("axis=1.0").assertIsDisplayed()
            onNodeWithText("axis=0.0").assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10: raw=0.123456789").assertIsDisplayed()
            runOnIdle { valueFormatter.value = ChartValueFormatters.suffix(" counts") }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("0-10: 0.12 counts").assertIsDisplayed()
            onNodeWithText("axis=1.0").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_denseCustomBarColors_keepEveryBinAdjacentWithoutAggregation() =
        runComposeUiTest {
            val colors = List(60) { if (it % 2 == 0) Color.Red else Color.Blue }
            lateinit var captureLayer: GraphicsLayer
            lateinit var captureScope: CoroutineScope
            val capturedPixels = mutableStateOf<PixelMap?>(null)
            setContent {
                captureLayer = rememberGraphicsLayer()
                captureScope = rememberCoroutineScope()
                HistogramChart(
                    data = List(60) { 0.75 }.toChartData(),
                    modifier =
                        Modifier
                            .testTag("histogram-capture")
                            .size(240.dp)
                            .drawWithContent {
                                captureLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(captureLayer)
                            },
                    animateOnStart = false,
                    style =
                        HistogramChartDefaults.style(
                            chartContainerStyle =
                                ChartContainerDefaults.style(
                                    outerPadding = 0.dp,
                                    innerPadding = 0.dp,
                                ),
                            bars = HistogramChartDefaults.bars(colors = colors, alpha = 1f),
                            range = BarChartDefaults.range(min = 0.0, max = 1.0),
                            grid = BarChartDefaults.grid(visible = false),
                            axis =
                                BarChartDefaults.axis(
                                    visible = false,
                                    yLabels = BarChartDefaults.yLabels(visible = false),
                                ),
                        ),
                )
            }

            onNodeWithTag(TestTags.HISTOGRAM_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).assertIsDisplayed()
            runOnIdle { captureScope.launch { capturedPixels.value = captureLayer.toImageBitmap().toPixelMap() } }
            waitUntil(timeoutMillis = 3_000L) { capturedPixels.value != null }
            val pixels = checkNotNull(capturedPixels.value)
            val plotBounds = onNodeWithTag(TestTags.BAR_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val captureBounds = onNodeWithTag("histogram-capture").fetchSemanticsNode().boundsInRoot
            val y = (plotBounds.center.y - captureBounds.top).toInt()
            colors.forEachIndexed { index, color ->
                val centerX = (pixels.width * (index + 0.5) / colors.size).toInt()
                assertEquals(color, pixels[centerX, y], "Source bin $index must retain its own color in fit mode")
            }
            assertEquals(Color.Blue, pixels[pixels.width / 2 - 1, y])
            assertEquals(Color.Red, pixels[pixels.width / 2 + 1, y])
        }
}
