package io.github.hdcharts.scatter

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxisLabelDefaults
import io.github.hdcharts.core.style.GridDefaults
import io.github.hdcharts.scatter.internal.SCATTER_CHART_TAG
import io.github.hdcharts.scatter.internal.SCATTER_CHART_X_AXIS_LABELS_TAG
import io.github.hdcharts.scatter.internal.SCATTER_CHART_Y_AXIS_LABELS_TAG
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScatterChartTest {
    @Test
    fun scatterPointStyle_clampsUnspecifiedMaximumAfterClampingMinimum() {
        val style =
            ScatterPointStyle(
                color = Color.Blue,
                colors = emptyList(),
                alpha = 1f,
                size = 4.dp,
                minSize = 20.dp,
                maxSize = Dp.Unspecified,
            ).clamp(Density(1f))

        assertEquals(expected = 20.dp, actual = style.maxSize)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_displaysNumericPointsAndTitle() =
        runComposeUiTest {
            setContent { ScatterChart(data = sample, title = "Study") }
            onNodeWithTag(SCATTER_CHART_TAG).assertIsDisplayed()
            onNodeWithText("Study").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scatterChart_tappingEmptyPlotSpaceClearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 0)
            setContent {
                ScatterChart(
                    data = ScatterData(listOf(ScatterSeries(points = listOf(ScatterPoint(5.0, 5.0))))),
                    modifier = Modifier.size(width = 320.dp, height = 240.dp),
                    style =
                        ScatterChartDefaults.style(
                            range = ScatterRangeStyle(xMin = 0.0, xMax = 10.0, yMin = 0.0, yMax = 10.0),
                        ),
                    selection = selection,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(SCATTER_CHART_TAG).performTouchInput {
                click(Offset(x = width * 0.9f, y = height * 0.1f))
            }
            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }
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
            onNodeWithTag(SCATTER_CHART_TAG).assertDoesNotExist()
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
            onNodeWithTag(SCATTER_CHART_TAG).assertIsDisplayed()
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
            onNodeWithTag(SCATTER_CHART_TAG).assertDoesNotExist()
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
            onNodeWithTag(SCATTER_CHART_TAG).assertDoesNotExist()
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
            onNodeWithTag(SCATTER_CHART_TAG).assertIsDisplayed()

            runOnIdle { labelSize.value = 20_000.sp }
            waitForIdle()

            onNodeWithText("X-axis label size must resolve to at most 16384 pixels.", substring = true)
                .assertIsDisplayed()
            onNodeWithTag(SCATTER_CHART_TAG).assertDoesNotExist()

            runOnIdle { labelSize.value = 12.sp }
            waitForIdle()

            onNodeWithTag(SCATTER_CHART_TAG).assertIsDisplayed()
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
            onNodeWithTag(SCATTER_CHART_TAG).assertIsDisplayed()

            runOnIdle { density.value = Density(2f) }
            waitForIdle()

            onNodeWithText("X-axis label size must resolve to at most 16384 pixels.", substring = true)
                .assertIsDisplayed()
            onNodeWithTag(SCATTER_CHART_TAG).assertDoesNotExist()
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
            val xLabels = onNodeWithTag(SCATTER_CHART_X_AXIS_LABELS_TAG).fetchSemanticsNode().boundsInRoot
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

            onNodeWithTag(SCATTER_CHART_X_AXIS_LABELS_TAG).assertIsDisplayed()
            onNodeWithTag(SCATTER_CHART_Y_AXIS_LABELS_TAG).assertIsDisplayed()
            onNodeWithText("distance: 1.0").assertIsDisplayed()
            onNodeWithText("cost: 2.0").assertIsDisplayed()
        }

    private val sample =
        ScatterData(
            listOf(ScatterSeries("Group", listOf(ScatterPoint(1.0, 2.0), ScatterPoint(3.0, 4.0, 5.0)))),
        )
}
