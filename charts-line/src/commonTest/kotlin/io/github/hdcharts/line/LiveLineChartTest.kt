package io.github.hdcharts.line

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.toChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class LiveLineChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun liveLineChart_withUnspecifiedAxisLabelSize_displaysValidationError() =
        runComposeUiTest {
            setContent {
                LiveLineChart(
                    data = listOf(10.0, 20.0, 30.0).toChartData(),
                    style =
                        LineChartDefaults.style(
                            axis =
                                LineChartDefaults.axis(
                                    yLabels = LineChartDefaults.yLabels(size = TextUnit.Unspecified),
                                ),
                        ),
                    animateOnStart = false,
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText(
                "Y-axis label size must be a finite, positive sp value.",
                substring = true,
            ).assertIsDisplayed()
        }

    @Test
    fun heldDrag_leavesPlotUnchanged() =
        runComposeUiTest {
            val capture =
                setCapturedContent { modifier ->
                    LiveLineChart(
                        data = listOf(10.0, 40.0, 20.0, 30.0).toChartData(),
                        modifier = modifier.size(width = 240.dp, height = 200.dp),
                        animateOnStart = false,
                    )
                }
            val idle = capture()

            onNodeWithTag(TestTags.LINE_CHART_PLOT).performTouchInput {
                down(center)
                moveBy(Offset(x = -width / 4f, y = 0f))
            }

            assertTrue(idle.buffer.contentEquals(capture().buffer), "A held drag must not draw a selection.")
        }

    @Test
    fun shiftDuration_controlsHowLongTheShiftRuns() =
        runComposeUiTest {
            val data = mutableStateOf(listOf(10.0, 40.0, 20.0, 30.0, 50.0))
            val capture =
                setCapturedContent { modifier ->
                    LiveLineChart(
                        data = data.value.toChartData(),
                        modifier = modifier.size(width = 240.dp, height = 200.dp),
                        shiftDuration = 10.seconds,
                        animateOnStart = false,
                    )
                }
            capture()

            mainClock.autoAdvance = false
            data.value = data.value.drop(1) + 15.0
            // Past the 1700ms morph duration, so only a 10s shift can still be running.
            mainClock.advanceTimeBy(milliseconds = 3_000L)
            val midShift = capture()
            mainClock.advanceTimeBy(milliseconds = 10_000L)
            val settled = capture()

            assertFalse(midShift.buffer.contentEquals(settled.buffer), "The shift must still run after 3s of 10s.")
        }

    @Test
    fun slidingWindow_keepsXLabelsOnTheirSamples() =
        runComposeUiTest {
            val firstSample = mutableStateOf(0)
            setContent {
                LiveLineChart(
                    data = liveWindow(firstSample.value),
                    modifier = Modifier.size(width = 600.dp, height = 300.dp),
                    animateOnStart = false,
                )
            }
            val before = displayedXLabels()

            firstSample.value = 1
            waitForIdle()
            val after = displayedXLabels()

            // Samples 1 to 29 are in both windows; about every other one has room for a label.
            val shared = (1 until WINDOW_SIZE).map { sample -> "S$sample" }.toSet()
            assertTrue(before.size in 2 until WINDOW_SIZE, "labels before: $before")
            assertEquals(expected = before.intersect(shared), actual = after.intersect(shared))
        }

    @Test
    fun slidingWindow_xLabelsMoveWithTheirPoints() =
        runComposeUiTest {
            val firstSample = mutableStateOf(0)
            setContent {
                LiveLineChart(
                    data = liveWindow(firstSample.value),
                    modifier = Modifier.size(width = 600.dp, height = 300.dp),
                    shiftDuration = 10.seconds,
                    animateOnStart = false,
                )
            }
            mainClock.autoAdvance = false
            firstSample.value = 1
            // Halfway through the linear shift, every point is half a step right of its new place.
            mainClock.advanceTimeBy(milliseconds = 5_000L)
            // The new window can change the Y-axis labels and so the plot, so measure it now.
            val plot = onNodeWithTag(TestTags.LINE_CHART_PLOT).fetchSemanticsNode().boundsInRoot
            val step = plot.width / (WINDOW_SIZE - 1)
            val midShift = displayedXLabelCenters()
            mainClock.advanceTimeBy(milliseconds = 10_000L)
            val settled = displayedXLabelCenters()

            assertTrue(midShift.size >= 2 && settled.size >= 2, "mid-shift $midShift, settled $settled")
            midShift.forEach { (label, centerX) ->
                val windowIndex = label.removePrefix("S").toInt() - 1
                assertEquals(
                    expected = plot.left + (windowIndex + 0.5f) * step,
                    actual = centerX,
                    absoluteTolerance = 1.5f,
                    message = label,
                )
            }
            settled.forEach { (label, centerX) ->
                val windowIndex = label.removePrefix("S").toInt() - 1
                assertEquals(
                    expected = plot.left + windowIndex * step,
                    actual = centerX,
                    absoluteTolerance = 1.5f,
                    message = label,
                )
            }
        }

    @Test
    fun slidingWindow_xLabelsNeverStepBackOnTheFrameTheWindowMoves() =
        runComposeUiTest {
            val firstSample = mutableStateOf(0)
            setContent {
                LiveLineChart(
                    data = liveWindow(firstSample.value),
                    modifier = Modifier.size(width = 600.dp, height = 300.dp),
                    shiftDuration = 10.seconds,
                    animateOnStart = false,
                )
            }
            mainClock.autoAdvance = false
            val frames = mutableListOf(displayedXLabelCenters())
            firstSample.value = 1
            repeat(4) {
                mainClock.advanceTimeByFrame()
                frames += displayedXLabelCenters()
            }

            // Labels only slide left, so no frame may draw one further right than the frame before.
            frames.zipWithNext().forEachIndexed { frame, (before, after) ->
                before.keys.intersect(after.keys).forEach { label ->
                    assertTrue(
                        after.getValue(label) <= before.getValue(label) + 1f,
                        "$label moved right on frame ${frame + 1}: $before -> $after",
                    )
                }
            }
        }

    @Test
    fun changingShiftDuration_keepsXLabelsOnTheirSamples() =
        runComposeUiTest {
            val firstSample = mutableStateOf(0)
            val shiftDuration = mutableStateOf(1.seconds)
            setContent {
                LiveLineChart(
                    data = liveWindow(firstSample.value),
                    modifier = Modifier.size(width = 600.dp, height = 300.dp),
                    shiftDuration = shiftDuration.value,
                    animateOnStart = false,
                )
            }
            firstSample.value = 1
            waitForIdle()
            val before = displayedXLabels()

            shiftDuration.value = 2.seconds
            waitForIdle()

            assertEquals(expected = before, actual = displayedXLabels())
        }

    @Test
    fun malformedDataPreservesModifierAndRendersErrorInsteadOfDrawing() =
        runComposeUiTest {
            val invalid =
                chartDataOf(
                    categories = listOf("A", "B"),
                    ChartSeries(name = "First", values = listOf(1.0, 2.0)),
                    ChartSeries(name = "Second", values = listOf(3.0)),
                )
            setContent {
                LiveLineChart(
                    data = invalid,
                    modifier = Modifier.testTag("live-line-container").size(280.dp, 240.dp),
                    animateOnStart = false,
                )
            }

            onNodeWithTag("live-line-container")
                .assertIsDisplayed()
                .assertWidthIsEqualTo(280.dp)
                .assertHeightIsEqualTo(240.dp)
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART).assertCountEquals(0)
            onNodeWithText("Series 1 is not aligned", substring = true).assertIsDisplayed()
        }

    // A window of samples firstSample until firstSample + WINDOW_SIZE; sample s is labeled "S<s>" and
    // keeps its value, so moving the window by one is a timeline shift. Values repeat every 10 samples,
    // so every window spans 0 to 45 and keeps the same Y labels and plot width.
    private fun liveWindow(firstSample: Int) =
        List(WINDOW_SIZE) { index -> ((firstSample + index) * 7 % 10 * 5).toDouble() }
            .toChartData(categories = List(WINDOW_SIZE) { index -> "S${firstSample + index}" })

    private fun ComposeUiTest.displayedXLabelCenters(): Map<String, Float> =
        onAllNodes(hasAnyAncestor(hasTestTag(TestTags.LINE_CHART_X_AXIS_LABELS)) and hasText("S", substring = true))
            .fetchSemanticsNodes()
            .filter { node -> node.layoutInfo.isPlaced }
            .associate { node ->
                node.config[SemanticsProperties.Text].joinToString() to node.boundsInRoot.center.x
            }

    private fun ComposeUiTest.displayedXLabels(): Set<String> = displayedXLabelCenters().keys

    private companion object {
        const val WINDOW_SIZE = 30
    }
}
