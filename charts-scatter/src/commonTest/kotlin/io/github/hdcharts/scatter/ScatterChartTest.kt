package io.github.hdcharts.scatter

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxisLabelDefaults
import io.github.hdcharts.core.style.GridDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScatterChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_displaysNumericPointsAndTitle() =
        runComposeUiTest {
            setContent { ScatterChart(data = sample, title = "Study") }
            onNodeWithTag("ScatterChart").assertIsDisplayed()
            onNodeWithText("Study").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_rejectsNonFiniteCoordinates() =
        runComposeUiTest {
            setContent {
                ScatterChart(
                    data = ScatterData(listOf(ScatterSeries(points = listOf(ScatterPoint(Double.NaN, 1.0))))),
                )
            }
            onNodeWithText(
                "Point coordinates must be finite and sizes must be finite and non-negative.\n",
            ).assertIsDisplayed()
            onNodeWithTag("ScatterChart").assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_acceptsSeriesWithIndependentPointCounts() =
        runComposeUiTest {
            setContent {
                ScatterChart(
                    data =
                        ScatterData(
                            listOf(
                                ScatterSeries("First", listOf(ScatterPoint(1.0, 2.0))),
                                ScatterSeries("Second", listOf(ScatterPoint(2.0, 3.0), ScatterPoint(4.0, 5.0))),
                            ),
                        ),
                )
            }
            onNodeWithTag("ScatterChart").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_emptyDataShowsErrorsWithCallerModifier() =
        runComposeUiTest {
            setContent {
                ScatterChart(
                    data = ScatterData(emptyList()),
                    modifier = Modifier.testTag("ScatterContainer"),
                )
            }
            onNodeWithText("At least one series is required.", substring = true).assertIsDisplayed()
            onNodeWithTag("ScatterContainer").assertIsDisplayed()
            onNodeWithTag("ScatterChart").assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_rejectsInvalidAxisLabelsEvenWhenAxesAreHidden() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    ScatterChart(
                        data = sample,
                        style =
                            ScatterChartDefaults.style(
                                axes =
                                    AxesDefaults.style(
                                        visible = false,
                                        xLabels = AxisLabelDefaults.style(size = 20_000.sp),
                                        yLabels = AxisLabelDefaults.style(maxCount = 1),
                                    ),
                            ),
                    )
                }
            }
            onNodeWithText("X-axis label size must resolve to at most 16384 pixels.", substring = true)
                .assertIsDisplayed()
            onNodeWithText("Y-axis label max count must be in 2..1000.", substring = true).assertIsDisplayed()
            onNodeWithTag("ScatterChart").assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_revalidatesWhenAxisLabelsChange() =
        runComposeUiTest {
            val labelSize = mutableStateOf(12.sp)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    ScatterChart(
                        data = sample,
                        style =
                            ScatterChartDefaults.style(
                                axes =
                                    AxesDefaults.style(
                                        visible = false,
                                        xLabels = AxisLabelDefaults.style(size = labelSize.value),
                                    ),
                            ),
                    )
                }
            }
            onNodeWithTag("ScatterChart").assertIsDisplayed()

            runOnIdle { labelSize.value = 20_000.sp }
            waitForIdle()

            onNodeWithText("X-axis label size must resolve to at most 16384 pixels.", substring = true)
                .assertIsDisplayed()
            onNodeWithTag("ScatterChart").assertDoesNotExist()

            runOnIdle { labelSize.value = 12.sp }
            waitForIdle()

            onNodeWithTag("ScatterChart").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_revalidatesWhenDensityChanges() =
        runComposeUiTest {
            val density = mutableStateOf(Density(1f))
            setContent {
                CompositionLocalProvider(LocalDensity provides density.value) {
                    ScatterChart(
                        data = sample,
                        style =
                            ScatterChartDefaults.style(
                                axes =
                                    AxesDefaults.style(
                                        visible = false,
                                        xLabels = AxisLabelDefaults.style(size = 10_000.sp),
                                    ),
                            ),
                    )
                }
            }
            onNodeWithTag("ScatterChart").assertIsDisplayed()

            runOnIdle { density.value = Density(2f) }
            waitForIdle()

            onNodeWithText("X-axis label size must resolve to at most 16384 pixels.", substring = true)
                .assertIsDisplayed()
            onNodeWithTag("ScatterChart").assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_formatsTheFirstYAxisLabelAndKeepsItAboveTheXAxisLabels() =
        runComposeUiTest {
            setContent {
                ScatterChart(
                    data =
                        ScatterData(
                            listOf(ScatterSeries(points = listOf(ScatterPoint(1.0, 1.8), ScatterPoint(4.5, 4.2)))),
                        ),
                )
            }

            val firstYLabel = onNodeWithText("1.8").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val xLabels = onNodeWithTag("ScatterChartXAxisLabels").fetchSemanticsNode().boundsInRoot
            assertTrue(firstYLabel.bottom <= xLabels.top)
            onNodeWithText("1.7999999999999998").assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_keepsFormattedAxisLabelsVisibleWhenTheGridIsHidden() =
        runComposeUiTest {
            setContent {
                ScatterChart(
                    data = sample,
                    modifier = Modifier.size(width = 320.dp, height = 240.dp),
                    style =
                        ScatterChartDefaults.style(
                            axes =
                                AxesDefaults.style(
                                    grid = GridDefaults.style(visible = false),
                                    xLabels = AxisLabelDefaults.style(size = 18.sp),
                                    yLabels = AxisLabelDefaults.style(size = 18.sp),
                                ),
                        ),
                    xValueFormatter = { "distance: $it" },
                    yValueFormatter = { "cost: $it" },
                )
            }

            onNodeWithTag("ScatterChartXAxisLabels").assertIsDisplayed()
            onNodeWithTag("ScatterChartYAxisLabels").assertIsDisplayed()
            onNodeWithText("distance: 1.0").assertIsDisplayed()
            onNodeWithText("cost: 2.0").assertIsDisplayed()
        }

    @Test
    fun point_keepsOptionalBubbleSize() {
        assertEquals(3.0, ScatterPoint(1.0, 2.0, 3.0).size)
        assertEquals(null, ScatterPoint(1.0, 2.0).size)
    }

    private val sample =
        ScatterData(
            listOf(ScatterSeries("Group", listOf(ScatterPoint(1.0, 2.0), ScatterPoint(3.0, 4.0, 5.0)))),
        )
}
