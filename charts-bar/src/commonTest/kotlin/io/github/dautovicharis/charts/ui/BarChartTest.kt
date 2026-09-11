package io.github.dautovicharis.charts.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NOT_NUMBER
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.data
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.BarChartDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = TITLE

            // Act
            setContent {
                BarChart(
                    data = data,
                    title = TITLE,
                )
            }

            // Assert
            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withInvalidData_displaysError() =
        runComposeUiTest {
            val invalidData = listOf(1.0).toChartData()
            val expectedError = RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_BAR)

            setContent {
                BarChart(
                    data = invalidData,
                    title = TITLE,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withSelectedBarIndex_displaysSelectedBarDetails() =
        runComposeUiTest {
            // Arrange
            val selectedBarIndex = 1
            val values = data.series.single().values
            val expectedTitle = ChartValueFormatters.Default.format(values[selectedBarIndex])

            // Act
            setContent {
                BarChart(
                    data = data,
                    title = TITLE,
                    selection = staticChartSelection(selectedBarIndex),
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            // Assert
            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .assertIsDisplayed()
            onNodeWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_lastXAxisLabel_hasRightEdgePadding() =
        runComposeUiTest {
            val edgeData =
                listOf(320.0, 280.0, 260.0, 300.0).toChartData(
                    categories = listOf("Region 4", "Region 36", "Region 68", "Region 100"),
                )

            setContent {
                BarChart(
                    data = edgeData,
                    title = "Quarterly Revenue by Region",
                )
            }

            val axisBounds = onNodeWithTag(TestTags.BAR_CHART_X_AXIS_LABELS).fetchSemanticsNode().boundsInRoot
            val lastLabelBounds = onNodeWithText("Region 100").fetchSemanticsNode().boundsInRoot

            assertTrue(lastLabelBounds.right <= axisBounds.right - 1f)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withMatchingBarColors_displaysChart() =
        runComposeUiTest {
            setContent {
                val style =
                    BarChartDefaults.style(
                        bars =
                            BarChartDefaults.bars(
                                colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Cyan),
                            ),
                    )
                BarChart(
                    data = data,
                    title = TITLE,
                    style = style,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withInvalidBarColors_displaysError() =
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
                    BarChartDefaults.style(
                        bars =
                            BarChartDefaults.bars(
                                colors = listOf(Color.Red, Color.Green),
                            ),
                    )
                BarChart(
                    data = data,
                    title = TITLE,
                    style = style,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_emptySeriesAndRaggedCategories_displayErrors() =
        runComposeUiTest {
            val invalidInputs =
                listOf(
                    ChartData(),
                    ChartData(series = listOf(ChartSeries(values = emptyList()))),
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
            setContent { BarChart(data = currentData.value, title = TITLE, animateOnStart = false) }

            invalidInputs.forEach { invalidData ->
                runOnIdle { currentData.value = invalidData }
                onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
                onNodeWithTag(TestTags.BAR_CHART).assertDoesNotExist()
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_nonFiniteValues_displayValidationErrors() =
        runComposeUiTest {
            val currentData = mutableStateOf(listOf(1.0, Double.NaN).toChartData())
            val expectedError = RULE_DATA_POINT_NOT_NUMBER.format(1)
            setContent { BarChart(data = currentData.value, animateOnStart = false) }

            listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { invalidValue ->
                runOnIdle { currentData.value = listOf(1.0, invalidValue).toChartData() }
                onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
                onNodeWithText("${expectedError}\n").assertIsDisplayed()
                onNodeWithTag(TestTags.BAR_CHART).assertDoesNotExist()
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_modifierTagAndSize_arePreservedOnSuccessAndError() =
        runComposeUiTest {
            val currentData = mutableStateOf(data)
            setContent {
                BarChart(
                    data = currentData.value,
                    modifier = Modifier.testTag("bar-container").size(width = 280.dp, height = 240.dp),
                    title = TITLE,
                    animateOnStart = false,
                )
            }

            onNodeWithTag("bar-container")
                .assertIsDisplayed()
                .assertWidthIsEqualTo(280.dp)
                .assertHeightIsEqualTo(240.dp)
            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
            val validBounds = onNodeWithTag("bar-container").fetchSemanticsNode().boundsInRoot

            runOnIdle { currentData.value = listOf(1.0).toChartData() }
            onNodeWithTag("bar-container")
                .assertIsDisplayed()
                .assertWidthIsEqualTo(280.dp)
                .assertHeightIsEqualTo(240.dp)
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            assertEquals(validBounds, onNodeWithTag("bar-container").fetchSemanticsNode().boundsInRoot)

            runOnIdle { currentData.value = data }
            onNodeWithTag(TestTags.BAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_titleOnlyUpdates_withoutCategories_doNotInventLabelsOrClearSelection() =
        runComposeUiTest {
            val unlabeledData = listOf(12.0, 123456.78).toChartData()
            val currentTitle = mutableStateOf("Original")
            val selection = ChartSelection()
            setContent {
                BarChart(
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
    fun barChart_formatterChanges_updateSourceReadoutAndAxisIndependently() =
        runComposeUiTest {
            val preciseData = listOf(0.123456789, 1.0).toChartData(categories = listOf("First", "Last"))
            val selection = ChartSelection(initialIndex = 0)
            val valueFormatter = mutableStateOf(ChartValueFormatters.Default)
            val axisFormatter = mutableStateOf(ChartValueFormatters.Default)
            setContent {
                BarChart(
                    data = preciseData,
                    selection = selection,
                    valueFormatter = valueFormatter.value,
                    axisValueFormatter = axisFormatter.value,
                    style = BarChartDefaults.style(range = BarChartDefaults.range(min = 0.0, max = 1.0)),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("First: 0.12").assertIsDisplayed()
            onNodeWithText("1.0").assertIsDisplayed()
            runOnIdle { valueFormatter.value = ChartValueFormatter { value -> "raw=$value" } }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("First: raw=0.123456789").assertIsDisplayed()
            onNodeWithText("1.0").assertIsDisplayed()
            runOnIdle { axisFormatter.value = ChartValueFormatter { value -> "axis=$value" } }
            onNodeWithText("axis=1.0").assertIsDisplayed()
            onNodeWithText("axis=0.0").assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("First: raw=0.123456789").assertIsDisplayed()
            runOnIdle { valueFormatter.value = ChartValueFormatters.suffix(" units") }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("First: 0.12 units").assertIsDisplayed()
            onNodeWithText("axis=1.0").assertIsDisplayed()
        }
}
