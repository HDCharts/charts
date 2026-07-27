package io.github.dautovicharis.charts.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.RadarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_RADAR
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.common.model.ChartDataType
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.dataSet
import io.github.dautovicharis.charts.model.ChartDataSet
import kotlin.test.Test

class RadarChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withValidData_displaysChart() =
        runComposeUiTest {
            val expectedTitle = dataSet.data.label

            setContent {
                RadarChart(dataSet)
            }

            onNodeWithTag(TestTags.RADAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withInvalidData_displaysError() =
        runComposeUiTest {
            val dataSet =
                ChartDataSet(
                    items = ChartDataType.FloatData(listOf(1f, 2f)),
                    title = TITLE,
                )
            val expectedError = RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_RADAR)

            setContent {
                RadarChart(dataSet)
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun radarChart_withSelectedAxisIndex_displaysSelectedAxisDetails() =
        runComposeUiTest {
            // Arrange
            val selectedAxisIndex = 1
            val expectedTitle = dataSet.data.item.labels[selectedAxisIndex]

            // Act
            setContent {
                RadarChart(
                    dataSet = dataSet,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedAxisIndex = selectedAxisIndex,
                )
            }

            // Assert
            onNodeWithTag(TestTags.RADAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }
}
