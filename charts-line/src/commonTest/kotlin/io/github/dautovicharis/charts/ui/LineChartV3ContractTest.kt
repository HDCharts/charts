package io.github.dautovicharis.charts.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.toChartData
import kotlin.test.Test
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class LineChartV3ContractTest {
    @Test
    fun externalSelectionAndDataReplacement_areAuthoritative() =
        runComposeUiTest {
            val selection = ChartSelection()
            val currentData = mutableStateOf(data(categories = listOf("A", "B", "C")))
            setContent {
                LineChart(
                    data = currentData.value,
                    title = "Line",
                    selection = selection,
                    animateOnStart = false,
                )
            }

            runOnIdle { selection.select(1) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B: 20.0").assertIsDisplayed()
            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Line").assertIsDisplayed()

            runOnIdle {
                selection.select(1)
                currentData.value = data(categories = listOf("X", "Y", "Z"))
            }
            waitForIdle()
            runOnIdle { assertNull(selection.selectedIndex) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Line").assertIsDisplayed()
        }

    @Test
    fun noCategories_selectionUsesFormattedRawValueAndDoesNotCreateXAxisLabels() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 1)
            setContent {
                LineChart(
                    data = listOf(0.123456789, 42.0).toChartData(),
                    selection = selection,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("42.0").assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
        }

    @Test
    fun formattersUpdateSelectedReadoutAndAxisIndependently() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 0)
            val valueFormatter = mutableStateOf(ChartValueFormatters.Default)
            val axisFormatter = mutableStateOf(ChartValueFormatters.Default)
            val preciseData = listOf(0.123456789, 1.0).toChartData(categories = listOf("First", "Last"))
            setContent {
                LineChart(
                    data = preciseData,
                    selection = selection,
                    valueFormatter = valueFormatter.value,
                    axisValueFormatter = axisFormatter.value,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("First: 0.12").assertIsDisplayed()
            runOnIdle { valueFormatter.value = ChartValueFormatters.suffix(" units") }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("First: 0.12 units").assertIsDisplayed()
            runOnIdle { axisFormatter.value = { value -> "axis=$value" } }
            onNodeWithText("axis=1.0").assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("First: 0.12 units").assertIsDisplayed()
        }

    @Test
    fun malformedDataPreservesModifierAndRendersErrorInsteadOfDrawing() =
        runComposeUiTest {
            val invalid =
                chartDataOf(
                    categories = listOf("A", "B"),
                    ChartSeries(name = "First", values = listOf(1.0, 2.0)),
                    ChartSeries(name = "Second", values = listOf(3.0)),
                )
            setContent {
                LineChart(
                    data = invalid,
                    modifier = Modifier.testTag("line-container").size(280.dp, 240.dp),
                    animateOnStart = false,
                )
            }

            onNodeWithTag("line-container")
                .assertIsDisplayed()
                .assertWidthIsEqualTo(280.dp)
                .assertHeightIsEqualTo(240.dp)
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART).assertCountEquals(0)
            onNodeWithText("Series 1 is not aligned", substring = true).assertIsDisplayed()
        }

    private fun data(categories: List<String>): ChartData =
        listOf(10.0, 20.0, 30.0).toChartData(categories = categories, seriesName = "Series")
}
