package io.github.hdcharts.charts.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.mock.MockTest.TITLE
import io.github.hdcharts.charts.mock.MockTest.data
import io.github.hdcharts.charts.model.ChartSelection
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.chartDataOf
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.RadarChartDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
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
                        data = data,
                        title = TITLE,
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds = onNodeWithTag(TestTags.RADAR_CHART).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val legendBounds = onNodeWithText("Categories").fetchSemanticsNode().boundsInRoot
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
            onNodeWithText("Data points size should be greater than or equal to 3.\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withSelectedAxisIndex_displaysSelectedAxisDetails() =
        runComposeUiTest {
            val selectedAxisIndex = 1
            val expectedTitle = data.categories[selectedAxisIndex]

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
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("B").isDisplayed()

            chart.performTouchInput {
                down(Offset(x = width / 2f, y = 8f))
                moveTo(Offset(x = width / 2f, y = height - 8f))
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("C").isDisplayed()

            runOnIdle {
                assertEquals(2, selection.selectedIndex)
                assertEquals(listOf<Int?>(1, 2), events)
                selection.clear()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE).isDisplayed()
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
}
