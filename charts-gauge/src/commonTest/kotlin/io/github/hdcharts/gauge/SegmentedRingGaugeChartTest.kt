package io.github.hdcharts.gauge

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.ANIMATION_DURATION_LINE
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.gauge.internal.SEGMENTED_RING_GAUGE_CHART_TAG
import io.github.hdcharts.gauge.internal.gapAngleFor
import io.github.hdcharts.gauge.internal.segmentGeometries
import io.github.hdcharts.gauge.internal.segmentedRingGaugeLayout
import io.github.hdcharts.gauge.internal.toRadians
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SegmentedRingGaugeChartTest {
    private val values = listOf(240.0, 180.0, 320.0)
    private val categories = listOf("Carbs", "Protein", "Fat")

    @Test
    fun segmentedRingGaugeChart_defaultTotal_countsUpToTheFinalValue() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                SegmentedRingGaugeChart(data = values.toChartData(), capacity = 2_000.0)
            }

            onNodeWithText("0").assertIsDisplayed()
            mainClock.advanceTimeBy(ANIMATION_DURATION_LINE / 2L)
            onNodeWithText("0").assertDoesNotExist()
            onNodeWithText("740").assertDoesNotExist()

            mainClock.advanceTimeBy(ANIMATION_DURATION_LINE.toLong())
            onNodeWithText("740").assertIsDisplayed()
        }

    @Test
    fun segmentedRingGaugeChart_customCenterLabel_staysUnchangedDuringAnimation() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                SegmentedRingGaugeChart(
                    data = values.toChartData(),
                    capacity = 2_000.0,
                    centerLabel = "Daily total",
                )
            }

            onNodeWithText("Daily total").assertIsDisplayed()
            mainClock.advanceTimeBy(ANIMATION_DURATION_LINE / 2L)
            onNodeWithText("Daily total").assertIsDisplayed()
            mainClock.advanceTimeBy(ANIMATION_DURATION_LINE.toLong())
            onNodeWithText("Daily total").assertIsDisplayed()
        }

    @Test
    fun segmentedRingGaugeChart_withoutAnimation_displaysTheExactFormattedTotalImmediately() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                SegmentedRingGaugeChart(
                    data = listOf(120.5).toChartData(),
                    capacity = 200.0,
                    centerValueFormatter = ChartValueFormatter { "total=$it" },
                    animateOnStart = false,
                )
            }

            onNodeWithText("total=120.5").assertIsDisplayed()
        }

    @Test
    fun segmentedRingGaugeChart_fractionalTotal_formatsRoundedStepsAndTheExactFinalValue() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                SegmentedRingGaugeChart(
                    data = listOf(120.5).toChartData(),
                    capacity = 200.0,
                    centerValueFormatter = ChartValueFormatter { "total=$it" },
                )
            }

            onNodeWithText("total=0.0").assertIsDisplayed()
            mainClock.advanceTimeBy(ANIMATION_DURATION_LINE / 2L)
            val intermediateText =
                onNodeWithText("total=", substring = true)
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.Text]
                    .single()
                    .text
            val intermediateValue = intermediateText.removePrefix("total=").toDouble()
            assertTrue(intermediateValue > 0.0 && intermediateValue < 120.5)
            assertEquals(round(intermediateValue), intermediateValue)

            mainClock.advanceTimeBy(ANIMATION_DURATION_LINE.toLong())
            onNodeWithText("total=120.5").assertIsDisplayed()
        }

    @Test
    fun segmentedRingGaugeChart_displaysTheRingTotalSupportingTextAndLegend() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = values.toChartData(categories = categories),
                    capacity = 2_000.0,
                    centerSupportingLabel = "of 2000 kcal",
                    animateOnStart = false,
                )
            }

            onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithText("740").assertIsDisplayed()
            onNodeWithText("of 2000 kcal").assertIsDisplayed()
            categories.forEach { onNodeWithText(it).assertIsDisplayed() }
        }

    @Test
    fun segmentedRingGaugeChart_selectionUsesTheCategoryInTheTitle() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = values.toChartData(categories = categories),
                    capacity = 2_000.0,
                    title = "Calories",
                    selection = staticChartSelection(1),
                    interactionEnabled = false,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Protein: 180.0")
            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(
                SEGMENTED_RING_GAUGE_SELECTION_AUTO_DESELECT_TIMEOUT_MS + 500L,
                ignoreFrameDuration = true,
            )
            waitForIdle()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Protein: 180.0")
        }

    @Test
    fun segmentedRingGaugeChart_withTapSelection_autoDeselectsAfterTimeout() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = values.toChartData(categories = categories),
                    capacity = 2_000.0,
                    title = "Calories",
                    animateOnStart = false,
                )
            }

            waitForIdle()
            val position = segmentPosition(index = 0)
            onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG).performTouchInput {
                down(position)
                up()
            }
            waitForIdle()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Carbs: 240.0")

            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(
                SEGMENTED_RING_GAUGE_SELECTION_AUTO_DESELECT_TIMEOUT_MS + 500L,
                ignoreFrameDuration = true,
            )
            waitForIdle()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Calories")
        }

    @Test
    fun segmentedRingGaugeChart_repeatedTapRenewsAutoDeselectTimeout() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = values.toChartData(categories = categories),
                    capacity = 2_000.0,
                    title = "Calories",
                    animateOnStart = false,
                )
            }

            waitForIdle()
            mainClock.autoAdvance = false
            val chart = onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG)
            val position = segmentPosition(index = 0)

            fun tapCarbs() {
                chart.performTouchInput {
                    down(position)
                    up()
                }
            }

            tapCarbs()
            mainClock.advanceTimeBy(2_500L, ignoreFrameDuration = true)
            tapCarbs()
            mainClock.advanceTimeBy(1_000L, ignoreFrameDuration = true)
            waitForIdle()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Carbs: 240.0")

            mainClock.advanceTimeBy(2_500L, ignoreFrameDuration = true)
            waitForIdle()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals("Calories")
        }

    @Test
    fun segmentedRingGaugeChart_withTotalOverCapacity_displaysAnError() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(data = values.toChartData(), capacity = 700.0)
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("Segment total (740.0) must not exceed capacity (700.0).\n").assertIsDisplayed()
        }

    @Test
    fun segmentedRingGaugeChart_roundingAtCapacity_doesNotDisplayAnError() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = listOf(0.1, 0.2).toChartData(),
                    capacity = 0.3,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @Test
    fun segmentedRingGaugeChart_repeatedRoundingAtCapacity_doesNotDisplayAnError() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = List(300) { 0.1 }.toChartData(),
                    capacity = 30.0,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @Test
    fun segmentedRingGaugeChart_zeroCornerRadius_isAcceptedByPublicConfiguration() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = values.toChartData(),
                    capacity = 2_000.0,
                    style =
                        SegmentedRingGaugeChartDefaults.style(
                            segments = SegmentedRingGaugeChartDefaults.segments(cornerRadius = 0.dp),
                        ),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @Test
    fun segmentedRingGaugeChart_sharedAndCapacityErrors_areDisplayedTogether() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = listOf(2.0, -1.0).toChartData(),
                    capacity = 0.5,
                )
            }

            onNodeWithText("Value at index 1 is negative.", substring = true).assertIsDisplayed()
            onNodeWithText("Segment total (1.0) must not exceed capacity (0.5).", substring = true)
                .assertIsDisplayed()
        }

    @Test
    fun segmentedRingGaugeChart_changingCapacity_recomputesCapacityValidation() =
        runComposeUiTest {
            val capacity = androidx.compose.runtime.mutableStateOf(0.5)
            setContent {
                SegmentedRingGaugeChart(
                    data = listOf(0.4, 0.2).toChartData(),
                    capacity = capacity.value,
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            runOnIdle { capacity.value = 1.0 }
            waitForIdle()
            onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @Test
    fun segmentedRingGaugeChart_withInvalidCapacity_forwardsModifierToTheError() =
        runComposeUiTest {
            setContent {
                SegmentedRingGaugeChart(
                    data = values.toChartData(),
                    capacity = 0.0,
                    modifier = Modifier.testTag("segmented-gauge-container"),
                )
            }

            onNodeWithTag("segmented-gauge-container").assertIsDisplayed()
            onNodeWithText("Capacity must be greater than zero.\n").assertIsDisplayed()
        }

    private fun ComposeUiTest.segmentPosition(index: Int): Offset {
        val nodeSize = onNodeWithTag(SEGMENTED_RING_GAUGE_CHART_TAG).fetchSemanticsNode().size
        val size = Size(nodeSize.width.toFloat(), nodeSize.height.toFloat())
        val overflowInset = size.minDimension * (1.05f - 1f) / 2f
        val layout =
            with(density) {
                segmentedRingGaugeLayout(
                    size = size,
                    width = StyleDefaults.segmentedRingGaugeWidth.toPx(),
                    overflowInset = overflowInset,
                )
            }
        val geometries =
            segmentGeometries(
                values = values,
                capacity = 2_000.0,
                gapAngle =
                    gapAngleFor(
                        gap = with(density) { StyleDefaults.segmentedRingGaugeSpacing.toPx() },
                        radius = layout.radius,
                    ),
                startAngle = SegmentedRingGaugeStartPosition.TopLeft.angle,
                direction = SegmentedRingGaugeDirection.CounterClockwise,
            )
        val geometry = geometries[index]
        val angle = (geometry.startAngle + geometry.sweepAngle / 2f).toRadians()
        return Offset(
            x = layout.center.x + layout.radius * cos(angle),
            y = layout.center.y + layout.radius * sin(angle),
        )
    }
}
