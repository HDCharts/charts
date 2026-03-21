package io.github.dautovicharis.charts.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.common.model.ChartDataType
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.dataSet
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.BarChartDefaults
import kotlin.test.Test
import kotlin.test.assertTrue

class BarChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = dataSet.data.label

            // Act
            setContent {
                BarChart(dataSet)
            }

            // Assert
            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withInvalidData_displaysError() =
        runComposeUiTest {
            val dataSet =
                ChartDataSet(
                    items = ChartDataType.FloatData(listOf(1f)),
                    title = TITLE,
                )
            val expectedError = RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_BAR)

            setContent {
                BarChart(dataSet)
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withSelectedBarIndex_displaysSelectedBarDetails() =
        runComposeUiTest {
            // Arrange
            val selectedBarIndex = 1
            val expectedLabel = dataSet.data.item.labels[selectedBarIndex]
            val expectedValue = dataSet.data.item.points[selectedBarIndex]
            val expectedTitle = "$expectedLabel: $expectedValue"

            // Act
            setContent {
                BarChart(
                    dataSet = dataSet,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedBarIndex = selectedBarIndex,
                )
            }

            // Assert
            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_lastXAxisLabel_hasRightEdgePadding() =
        runComposeUiTest {
            val edgeDataSet =
                listOf(320f, 280f, 260f, 300f).toChartDataSet(
                    title = "Quarterly Revenue by Region",
                    labels = listOf("Region 4", "Region 36", "Region 68", "Region 100"),
                )

            setContent {
                BarChart(dataSet = edgeDataSet)
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
                        barColors = listOf(Color.Red, Color.Green, Color.Blue, Color.Cyan),
                    )
                BarChart(
                    dataSet = dataSet,
                    style = style,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withInvalidBarColors_displaysError() =
        runComposeUiTest {
            val expectedError =
                RULE_COLORS_SIZE_MISMATCH.format(
                    2,
                    dataSet.data.item.points.size,
                )

            setContent {
                val style =
                    BarChartDefaults.style(
                        barColors = listOf(Color.Red, Color.Green),
                    )
                BarChart(
                    dataSet = dataSet,
                    style = style,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }
}
