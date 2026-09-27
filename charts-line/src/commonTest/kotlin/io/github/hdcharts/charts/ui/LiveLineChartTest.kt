package io.github.hdcharts.charts.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.LiveLineChart
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.chartDataOf
import io.github.hdcharts.charts.model.toChartData
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class LiveLineChartTest {
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
}
