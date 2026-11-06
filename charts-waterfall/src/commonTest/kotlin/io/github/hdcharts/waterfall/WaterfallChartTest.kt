package io.github.hdcharts.waterfall

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxisLabelDefaults
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.waterfall.WaterfallEntry.Change
import io.github.hdcharts.waterfall.WaterfallEntry.Total
import io.github.hdcharts.waterfall.internal.WATERFALL_CHART_TAG
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WaterfallChartTest {
    private val data = WaterfallData(listOf(Change("Sales", 100.0), Change("Costs", -50.0), Total("Profit")))

    @Test
    fun selectionShowsCategoryChangeAndComputedBalance() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 1)
            setContent {
                WaterfallChart(
                    data,
                    title = "Business",
                    selection = selection,
                    animateOnStart = false,
                    valueFormatter = { it.toInt().toString() },
                )
            }
            onNodeWithText("Business · Costs · Change: -50 · Total: 50").assertIsDisplayed()
            runOnIdle { selection.select(2) }
            onNodeWithText("Business · Profit · Total: 50").assertIsDisplayed()
        }

    @Test
    fun tappingAndDraggingFloatingBarsSelectsSourceIndices() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent {
                WaterfallChart(
                    data,
                    modifier = Modifier.size(400.dp, 300.dp),
                    selection = selection,
                    animateOnStart = false,
                    style =
                        WaterfallChartDefaults.style(
                            chartContainerStyle = ChartContainerDefaults.style(contentPadding = 0.dp),
                            axes = AxesDefaults.style(visible = false),
                            legend = LegendDefaults.style(visible = false),
                        ),
                )
            }
            val plot = onNodeWithTag(WATERFALL_CHART_TAG)
            plot.performTouchInput { click(Offset(width / 2f, height * 0.25f)) }
            runOnIdle { assertEquals(1, selection.selectedIndex) }
            plot.performTouchInput {
                down(Offset(width / 2f, height * 0.25f))
                moveTo(Offset(width * 5f / 6f, height * 0.75f))
                up()
            }
            runOnIdle { assertEquals(2, selection.selectedIndex) }
            plot.performTouchInput { click(Offset(width * 5f / 6f, height * 0.1f)) }
            runOnIdle { assertEquals(null, selection.selectedIndex) }
        }

    @Test
    fun disabledInteractionKeepsProgrammaticSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 0)
            setContent {
                WaterfallChart(
                    data,
                    selection = selection,
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }
            onNodeWithTag(WATERFALL_CHART_TAG).performTouchInput { click(center) }
            runOnIdle {
                assertEquals(0, selection.selectedIndex)
                selection.select(2)
            }
            runOnIdle { assertEquals(2, selection.selectedIndex) }
        }

    @Test
    fun changedDataAndOutOfBoundsSelectionClearSelection() =
        runComposeUiTest {
            val state = mutableStateOf(data)
            val selection = ChartSelection(initialIndex = 1)
            setContent { WaterfallChart(state.value, selection = selection, animateOnStart = false) }
            runOnIdle { state.value = WaterfallData(listOf(Total("Opening"))) }
            runOnIdle {
                assertEquals(null, selection.selectedIndex)
                selection.select(10)
            }
            runOnIdle { assertEquals(null, selection.selectedIndex) }
            onNodeWithTag(WATERFALL_CHART_TAG).assertIsDisplayed()
        }

    @Test
    fun emptyDataShowsErrorAndPreservesCallerModifier() =
        runComposeUiTest {
            setContent { WaterfallChart(WaterfallData(emptyList()), modifier = Modifier.testTag("error")) }
            onNodeWithTag("error").assertIsDisplayed()
            onNodeWithText("At least one waterfall entry is required.\n").assertIsDisplayed()
            onNodeWithTag(WATERFALL_CHART_TAG).assertDoesNotExist()
        }

    @Test
    fun nonFiniteChangeAndOverflowAreRejectedBeforeRendering() =
        runComposeUiTest {
            val state = mutableStateOf(WaterfallData(listOf(Change("Invalid", Double.NaN))))
            setContent { WaterfallChart(state.value) }
            onNodeWithText("Change amounts must be finite.\n").assertIsDisplayed()
            runOnIdle {
                state.value =
                    WaterfallData(listOf(Change("A", Double.MAX_VALUE), Change("B", Double.MAX_VALUE)))
            }
            onNodeWithText("Every running total must be finite.\n").assertIsDisplayed()
            onNodeWithTag(WATERFALL_CHART_TAG).assertDoesNotExist()
        }

    @Test
    fun axisLabelsAreValidatedEvenWhenAxesAreHidden() =
        runComposeUiTest {
            setContent {
                WaterfallChart(
                    data,
                    style =
                        WaterfallChartDefaults.style(
                            axes = AxesDefaults.style(visible = false, xLabels = AxisLabelDefaults.style(size = 0.sp)),
                        ),
                )
            }
            onNodeWithTag(WATERFALL_CHART_TAG).assertDoesNotExist()
        }

    @Test
    fun narrowContainerWithLongCategoriesAndNegativeBalanceRenders() =
        runComposeUiTest {
            setContent {
                WaterfallChart(
                    WaterfallData(
                        listOf(
                            Change("Operating expenses for the quarter", -100.0),
                            Total("Remaining balance after all expenses"),
                        ),
                    ),
                    modifier = Modifier.size(140.dp, 240.dp),
                    animateOnStart = false,
                )
            }
            onNodeWithTag(WATERFALL_CHART_TAG).assertIsDisplayed()
        }
}
