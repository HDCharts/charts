package io.github.dautovicharis.charts.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_BAR
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.data
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.BarChartDefaults
import kotlin.test.Test
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
            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
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

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun barChart_withSelectedBarIndex_displaysSelectedBarDetails() =
        runComposeUiTest {
            // Arrange
            val selectedBarIndex = 1
            val values = data.series.single().values
            val expectedValue = values[selectedBarIndex].toFloat()
            val expectedTitle = "$TITLE${selectedBarIndex + 1}: $expectedValue"

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
            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
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

            onNodeWithTag(TestTags.BAR_CHART).isDisplayed()
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

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }
}
