package io.github.hdcharts.radar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.staticChartSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RadarChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withValidData_displaysChart() =
        runComposeUiTest {
            setContent {
                RadarChart(
                    data = data,
                    title = TITLE,
                )
            }

            onNodeWithTag(TestTags.RADAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(TITLE)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_insideVerticalScroll_displaysChart() =
        runComposeUiTest {
            setContent {
                Column(
                    modifier =
                        Modifier
                            .width(240.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    RadarChart(
                        data = multiSeriesData,
                        title = TITLE,
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds = onNodeWithTag(TestTags.RADAR_CHART).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val legendBounds = onNodeWithText("Alpha").fetchSemanticsNode().boundsInRoot
            assertTrue(
                chartBounds.bottom <= legendBounds.top,
                "Chart bounds $chartBounds overlap legend bounds $legendBounds",
            )
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withInvalidData_displaysError() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = listOf("A", "B"),
                    ChartSeries(name = "Series", values = listOf(1.0, 2.0)),
                )

            setContent {
                RadarChart(data = data, title = TITLE)
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("At least 3 values are required.\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withInvalidNumericStyleValues_drawsClampedChart() =
        runComposeUiTest {
            setContent {
                RadarChart(
                    data = multiSeriesData,
                    title = TITLE,
                    style =
                        RadarChartDefaults.style(
                            grid = RadarChartDefaults.grid(steps = Int.MAX_VALUE),
                            polygon = RadarChartDefaults.polygon(fillAlpha = Float.NaN),
                            points = RadarChartDefaults.points(size = (-1).dp),
                            selection =
                                RadarChartDefaults.selection(
                                    unselectedAlpha = Float.NaN,
                                    unfocusedSeriesAlpha = 2f,
                                ),
                        ),
                    selection = staticChartSelection(1),
                    seriesSelection = staticChartSelection(0),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.RADAR_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withSelectedAxisIndex_displaysSelectedAxisDetails() =
        runComposeUiTest {
            val selectedAxisIndex = 1
            // A single series has no legend, so the title carries the value.
            val expectedTitle = "${data.categories[selectedAxisIndex]}: 20.0"

            setContent {
                RadarChart(
                    data = data,
                    title = TITLE,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selection = staticChartSelection(selectedAxisIndex),
                )
            }

            onNodeWithTag(TestTags.RADAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_dragSelection_persistsAfterReleaseAndCanBeChanged() =
        runComposeUiTest {
            val events = mutableListOf<Int?>()
            val selection = ChartSelection(onSelectionChanged = { events.add(it) })
            setContent {
                RadarChart(
                    data = data,
                    modifier = Modifier.size(240.dp),
                    title = TITLE,
                    selection = selection,
                    animateOnStart = false,
                )
            }

            val chart = onNodeWithTag(TestTags.RADAR_CHART)
            chart.performTouchInput {
                down(Offset(x = 8f, y = height / 2f))
                moveTo(Offset(x = width - 8f, y = height / 2f))
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B: 20.0").isDisplayed()

            chart.performTouchInput {
                down(Offset(x = width / 2f, y = 8f))
                moveTo(Offset(x = width / 2f, y = height - 8f))
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("C: 30.0").isDisplayed()

            runOnIdle {
                assertEquals(2, selection.selectedIndex)
                assertEquals(listOf<Int?>(1, 2), events)
                selection.clear()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_tapAfterDrag_clearsTheSelectedAxis() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent {
                RadarChart(
                    data = data,
                    modifier = Modifier.size(240.dp),
                    title = TITLE,
                    selection = selection,
                    animateOnStart = false,
                )
            }

            val chart = onNodeWithTag(TestTags.RADAR_CHART)
            chart.performTouchInput {
                down(Offset(x = 8f, y = height / 2f))
                moveTo(Offset(x = width - 8f, y = height / 2f))
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B: 20.0").isDisplayed()

            chart.performTouchInput { click(center) }
            runOnIdle { assertNull(selection.selectedIndex) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_tapOnSeriesOutline_focusesSeriesAndTapOnEmptySpaceClears() =
        runComposeUiTest {
            val seriesSelection = ChartSelection()
            setContent {
                RadarChart(
                    data = multiSeriesData,
                    modifier = Modifier.size(240.dp),
                    seriesSelection = seriesSelection,
                    animateOnStart = false,
                )
            }

            val chart = onNodeWithTag(TestTags.RADAR_CHART)
            // Beta peaks on the top axis, where Alpha sits at the center. The web shrinks to leave
            // room for the axis labels, so the peak sits between the top edge and the center.
            chart.performTouchInput { click(Offset(x = width / 2f, y = height / 4f)) }
            runOnIdle { assertEquals(expected = 1, actual = seriesSelection.selectedIndex) }

            chart.performTouchInput { click(Offset(x = 2f, y = 2f)) }
            runOnIdle { assertNull(seriesSelection.selectedIndex) }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withInteractionDisabled_ignoresSeriesTap() =
        runComposeUiTest {
            val seriesSelection = ChartSelection()
            setContent {
                RadarChart(
                    data = multiSeriesData,
                    modifier = Modifier.size(240.dp),
                    seriesSelection = seriesSelection,
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            // Beta peaks on the top axis, where Alpha sits at the center.
            onNodeWithTag(TestTags.RADAR_CHART)
                .performTouchInput { click(Offset(x = width / 2f, y = height / 4f)) }
            runOnIdle { assertNull(seriesSelection.selectedIndex) }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withoutCategories_hidesAxisLabels() =
        runComposeUiTest {
            val data =
                chartDataOf(
                    categories = emptyList(),
                    ChartSeries(
                        name = "Series",
                        values = listOf(10.0, 20.0, 30.0, 40.0, 50.0, 60.0),
                    ),
                )
            setContent {
                RadarChart(
                    data = data,
                    title = TITLE,
                    style =
                        RadarChartDefaults.style(
                            axes = RadarChartDefaults.axes(labelVisible = true),
                        ),
                )
            }

            onNodeWithTag(TestTags.RADAR_CHART).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_valueFormatter_formatsSelectedLegendValues() =
        runComposeUiTest {
            setContent {
                RadarChart(
                    data = multiSeriesData,
                    selection = staticChartSelection(0),
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedValueFormatter = ChartValueFormatter { value -> "#${value.toInt()}" },
                )
            }

            onNodeWithText("Alpha - #10").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_hiddenLegend_hidesSeriesNames() =
        runComposeUiTest {
            setContent {
                RadarChart(
                    data = multiSeriesData,
                    style = RadarChartDefaults.style(legend = RadarChartDefaults.legend(visible = false)),
                    animateOnStart = false,
                )
            }

            onAllNodesWithText("Alpha").assertCountEquals(0)
        }

    private companion object {
        const val TITLE = "Title"

        val data: ChartData =
            chartDataOf(
                categories = listOf("A", "B", "C", "D"),
                ChartSeries(name = "Series", values = listOf(10.0, 20.0, 30.0, 40.0)),
            )

        val multiSeriesData: ChartData =
            chartDataOf(
                categories = listOf("A", "B", "C", "D"),
                ChartSeries(name = "Alpha", values = listOf(10.0, 20.0, 30.0, 40.0)),
                ChartSeries(name = "Beta", values = listOf(40.0, 30.0, 20.0, 10.0)),
            )
    }
}
