package io.github.hdcharts.gauge

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.gauge.internal.SEGMENTED_RING_GAUGE_CHART_TAG
import io.github.hdcharts.gauge.internal.gapAngleFor
import io.github.hdcharts.gauge.internal.segmentGeometries
import io.github.hdcharts.gauge.internal.segmentedRingGaugeLayout
import io.github.hdcharts.gauge.internal.toRadians
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SegmentedRingGaugeChartTest {
    private val values = listOf(240.0, 180.0, 320.0)
    private val categories = listOf("Carbs", "Protein", "Fat")

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
