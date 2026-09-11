package io.github.dautovicharis.charts.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINT_NEGATIVE
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import kotlin.test.Test

class HistogramChartTest {
    private val data: ChartData =
        listOf(3.0, 7.0, 10.0, 6.0)
            .toChartData(
                categories = listOf("0-10", "10-20", "20-30", "30-40"),
            )
    private val title = "Histogram"

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withValidData_displaysChart() =
        runComposeUiTest {
            setContent {
                HistogramChart(
                    data = data,
                    title = title,
                )
            }

            onNodeWithTag(TestTags.HISTOGRAM_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE)
                .assertTextEquals(title)
                .isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun histogramChart_withNegativeData_displaysError() =
        runComposeUiTest {
            val negativeData =
                listOf(2.0, -1.0, 4.0).toChartData(
                    categories = listOf("0-10", "10-20", "20-30"),
                )
            val expectedError = RULE_DATA_POINT_NEGATIVE.format(1)

            setContent {
                HistogramChart(
                    data = negativeData,
                    title = title,
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
            val categories = data.categories
            val values = data.series.single().values
            val expectedLabel = categories[selectedBarIndex]
            val expectedValue = values[selectedBarIndex].toFloat()
            val expectedTitle = "$expectedLabel: $expectedValue"

            setContent {
                HistogramChart(
                    data = data,
                    title = title,
                    selection = staticChartSelection(selectedBarIndex),
                    interactionEnabled = false,
                    animateOnStart = false,
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
                    HistogramChartDefaults.style(
                        bars =
                            BarChartDefaults.bars(
                                colors = listOf(Color.Red, Color.Green),
                            ),
                    )
                HistogramChart(
                    data = data,
                    title = title,
                    style = style,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }
}
