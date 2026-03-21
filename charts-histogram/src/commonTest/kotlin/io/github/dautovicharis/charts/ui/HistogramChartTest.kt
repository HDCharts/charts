package io.github.dautovicharis.charts.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NEGATIVE
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import kotlin.test.Test

class HistogramChartTest {
    private val dataSet =
        listOf(3f, 7f, 10f, 6f)
            .toChartDataSet(
                title = "Histogram",
                labels = listOf("0-10", "10-20", "20-30", "30-40"),
            )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withValidData_displaysChart() =
        runComposeUiTest {
            setContent {
                HistogramChart(dataSet)
            }

            onNodeWithTag(TestTags.HISTOGRAM_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(dataSet.data.label)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withNegativeData_displaysError() =
        runComposeUiTest {
            val expectedError = RULE_DATA_POINT_NEGATIVE.format(1)

            setContent {
                HistogramChart(
                    dataSet =
                        listOf(2f, -1f, 4f).toChartDataSet(
                            title = "Histogram",
                            labels = listOf("0-10", "10-20", "20-30"),
                        ),
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withSelectedBarIndex_displaysSelectedBarDetails() =
        runComposeUiTest {
            val selectedBarIndex = 2
            val expectedLabel = dataSet.data.item.labels[selectedBarIndex]
            val expectedValue = dataSet.data.item.points[selectedBarIndex]
            val expectedTitle = "$expectedLabel: $expectedValue"

            setContent {
                HistogramChart(
                    dataSet = dataSet,
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedBarIndex = selectedBarIndex,
                )
            }

            onNodeWithTag(TestTags.HISTOGRAM_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(expectedTitle)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withInvalidBarColors_displaysError() =
        runComposeUiTest {
            val expectedError =
                RULE_COLORS_SIZE_MISMATCH.format(
                    2,
                    dataSet.data.item.points.size,
                )

            setContent {
                val style = HistogramChartDefaults.style(barColors = listOf(Color.Red, Color.Green))
                HistogramChart(
                    dataSet = dataSet,
                    style = style,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }
}
