package io.github.hdcharts.gauge

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.gauge.internal.RING_GAUGE_CHART_TAG
import io.github.hdcharts.gauge.internal.RingGaugeLayout
import io.github.hdcharts.gauge.internal.ringGaugeLayout
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RingGaugeChartTest {
    private val values = listOf(25.0, 39.0)
    private val gaugeData = values.toChartData(categories = LABELS)

    @Test
    fun ringGaugeChart_withValidData_displaysChartAndTitle() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(data = gaugeData, title = TITLE)
            }

            onNodeWithTag(RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    /** The legend names the rings, like pie names its slices, and does not change on selection. */
    @Test
    fun ringGaugeChart_withCategories_namesEachRingInTheLegend() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = gaugeData,
                    valueFormatter = ChartValueFormatters.suffix("%"),
                    selection = staticChartSelection(1),
                    interactionEnabled = false,
                )
            }

            onNodeWithText(LABELS[0]).assertIsDisplayed()
            onNodeWithText(LABELS[1]).assertIsDisplayed()
            onNodeWithText("${LABELS[1]} - 39.0%").assertDoesNotExist()
        }

    /** One ring needs no key, so a single named value draws without a legend. */
    @Test
    fun ringGaugeChart_withSingleNamedValue_hidesTheLegend() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(data = listOf(72.0).toChartData(categories = listOf(LABELS[0])))
            }

            onNodeWithTag(RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithText(LABELS[0]).assertDoesNotExist()
        }

    @Test
    fun ringGaugeChart_withSingleValue_displaysChart() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(data = listOf(72.0).toChartData())
            }

            onNodeWithTag(RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @Test
    fun ringGaugeChart_withEmptySeries_displaysError() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(data = emptyList<Double>().toChartData())
            }

            assertError(ValidationErrors.tooFewValues(min = ValidationErrors.MIN_RING_GAUGE_VALUES))
        }

    @Test
    fun ringGaugeChart_withTwoSeries_displaysError() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data =
                        chartDataOf(
                            series =
                                arrayOf(
                                    ChartSeries(name = "One", values = values),
                                    ChartSeries(name = "Two", values = values),
                                ),
                        ),
                )
            }

            assertError(ValidationErrors.exactlyOneSeries(count = 2))
        }

    @Test
    fun ringGaugeChart_withNonFiniteValue_displaysError() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(data = listOf(25.0, Double.NaN).toChartData())
            }

            assertError(ValidationErrors.nonFiniteValue(index = 1))
        }

    @Test
    fun ringGaugeChart_withNonFiniteRange_displaysError() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = gaugeData,
                    style =
                        RingGaugeChartDefaults.style(
                            range = RingGaugeChartDefaults.range(max = Double.POSITIVE_INFINITY),
                        ),
                )
            }

            assertError(ValidationErrors.nonFiniteRange())
        }

    @Test
    fun ringGaugeChart_withColorCountMismatch_displaysError() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = gaugeData,
                    style =
                        RingGaugeChartDefaults.style(
                            rings = RingGaugeChartDefaults.rings(colors = listOf(Color.Red)),
                        ),
                )
            }

            assertError(ValidationErrors.colorCountMismatch(colors = 1, expected = 2, target = "value"))
        }

    @Test
    fun ringGaugeChart_withInvalidData_forwardsModifierToTheError() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = emptyList<Double>().toChartData(),
                    modifier = Modifier.testTag(CONTAINER_TAG),
                )
            }

            onNodeWithTag(CONTAINER_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
        }

    /** A ring gauge is one series, so the title carries the selected value. */
    @Test
    fun ringGaugeChart_selection_showsCategoryAndValueInTheTitle() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = gaugeData,
                    title = TITLE,
                    valueFormatter = ChartValueFormatters.suffix("%"),
                    selection = staticChartSelection(1),
                    interactionEnabled = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("${LABELS[1]}: 39.0%")
        }

    /** Without categories the title shows the value alone. */
    @Test
    fun ringGaugeChart_selectionWithoutCategories_showsTheValueInTheTitle() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = values.toChartData(),
                    title = TITLE,
                    valueFormatter = ChartValueFormatters.suffix("%"),
                    selection = staticChartSelection(1),
                    interactionEnabled = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("39.0%")
        }

    /** A selection past the last ring is ignored, so the title stays plain. */
    @Test
    fun ringGaugeChart_selectionOutsideTheRings_showsThePlainTitle() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = gaugeData,
                    title = TITLE,
                    selection = staticChartSelection(5),
                    interactionEnabled = false,
                )
            }

            onNodeWithTag(RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    /** Dropping the selected ring clears the selection instead of drawing past the last ring. */
    @Test
    fun ringGaugeChart_selectedRingRemoved_showsThePlainTitle() =
        runComposeUiTest {
            var data by mutableStateOf(listOf(25.0, 39.0, 52.0).toChartData())
            setContent {
                RingGaugeChart(
                    data = data,
                    title = TITLE,
                    selection = staticChartSelection(2),
                    interactionEnabled = false,
                )
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("52.0")

            data = values.toChartData()
            waitForIdle()

            onNodeWithTag(RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    @Test
    fun ringGaugeChart_tapOnRing_selectsItAndTapOutsideClears() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = gaugeData,
                    modifier = Modifier.size(width = 320.dp, height = 260.dp),
                    style = unlabeledStyle(),
                    title = TITLE,
                    animateOnStart = false,
                )
            }

            tapRing(index = 1)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("${LABELS[1]}: 39.0")
            tapRing(index = 0)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("${LABELS[0]}: 25.0")

            onNodeWithTag(RING_GAUGE_CHART_TAG).performTouchInput {
                down(Offset(width / 2f, height - 1f))
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)

            tapRing(index = 1)
            tapHole()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    @Test
    fun ringGaugeChart_withInteractionDisabled_ignoresTaps() =
        runComposeUiTest {
            setContent {
                RingGaugeChart(
                    data = gaugeData,
                    modifier = Modifier.size(width = 320.dp, height = 260.dp),
                    style = unlabeledStyle(),
                    title = TITLE,
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            tapRing(index = 0)
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    @Composable
    private fun unlabeledStyle(): RingGaugeChartStyle =
        RingGaugeChartDefaults.style(labels = RingGaugeChartDefaults.labels(visible = false))

    /** Taps the top of ring [index], using the layout the chart draws with no range labels. */
    private fun ComposeUiTest.tapRing(index: Int) {
        val node = onNodeWithTag(RING_GAUGE_CHART_TAG)
        val layout = unlabeledLayout(node.fetchSemanticsNode().size)
        val ring = layout.rings[index]
        node.performTouchInput {
            down(Offset(layout.center.x, layout.center.y - ring.centerRadius))
            up()
        }
    }

    /** The layout the chart draws with no range labels. */
    private fun ComposeUiTest.unlabeledLayout(size: IntSize): RingGaugeLayout =
        with(density) {
            ringGaugeLayout(
                size = Size(size.width.toFloat(), size.height.toFloat()),
                ringCount = values.size,
                maxRingWidth = StyleDefaults.ringGaugeWidth.toPx(),
                spacing = StyleDefaults.ringGaugeSpacing.toPx(),
                labelBand = 0f,
            )
        }

    /** Taps inside the hole, just above the baseline. */
    private fun ComposeUiTest.tapHole() {
        val node = onNodeWithTag(RING_GAUGE_CHART_TAG)
        val layout = unlabeledLayout(node.fetchSemanticsNode().size)
        node.performTouchInput {
            down(Offset(layout.center.x, layout.center.y - 1f))
            up()
        }
    }

    private fun ComposeUiTest.assertError(error: String) {
        onNodeWithTag(RING_GAUGE_CHART_TAG).assertDoesNotExist()
        onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
        onNodeWithText("${error}\n").assertIsDisplayed()
    }

    private companion object {
        const val TITLE = "Ring Gauge"
        const val CONTAINER_TAG = "ring-gauge-container"
        val LABELS = listOf("Last year", "This year")
    }
}
