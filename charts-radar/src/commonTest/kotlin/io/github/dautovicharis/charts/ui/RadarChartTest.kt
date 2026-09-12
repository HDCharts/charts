package io.github.dautovicharis.charts.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.RadarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.data
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.RadarChartDefaults
import kotlin.test.Test

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
