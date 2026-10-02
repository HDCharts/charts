package io.github.hdcharts.line.internal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LineChartMorphAnimationTest {
    @Test
    fun morphInterruptedHalfway_freezesTheDrawnValuesAndRestartsProgress() =
        runComposeUiTest {
            val state = LineChartMorphState(initialValues = listOf(listOf(0f)))
            val target = mutableStateOf(listOf(listOf(0f)))
            mainClock.autoAdvance = false

            setContent {
                LaunchedEffect(target.value) {
                    state.animateTo(
                        targets = target.value,
                        animationSpec = TweenSpec(durationMillis = 1_000, easing = LinearEasing),
                    )
                }
            }

            target.value = listOf(listOf(1f))
            mainClock.advanceTimeBy(milliseconds = 500L)
            val drawnHalfway = state.drawnValueAt(seriesIndex = 0, pointIndex = 0, progress = state.progress.value)
            assertTrue(drawnHalfway in 0.4f..0.6f, "The morph has to be part-way through: $drawnHalfway")

            // A new update arrives while the morph is still running.
            target.value = listOf(listOf(0f))
            mainClock.advanceTimeByFrame()
            val restartedProgress = state.progress.value

            // The next morph starts from the value the chart was drawing, not from the first one.
            assertEquals(
                expected = drawnHalfway,
                actual = state.drawnValueAt(seriesIndex = 0, pointIndex = 0, progress = restartedProgress),
                absoluteTolerance = 0.1f,
                message = "The new morph must freeze the value on screen, or the line jumps on the frame it starts.",
            )
            assertTrue(
                restartedProgress < 0.05f,
                "Progress must restart near 0, not carry over the interrupted morph: $restartedProgress",
            )
        }

    @Test
    fun drawnValueAt_matchesTheValueBlendIntoWritesMidMorph() =
        runComposeUiTest {
            val state = LineChartMorphState(initialValues = listOf(listOf(0.25f, 0.75f)))
            val target = mutableStateOf(listOf(listOf(1f, 0f)))
            mainClock.autoAdvance = false

            setContent {
                LaunchedEffect(target.value) {
                    state.animateTo(
                        targets = target.value,
                        animationSpec = TweenSpec(durationMillis = 1_000, easing = LinearEasing),
                    )
                }
            }

            mainClock.advanceTimeBy(milliseconds = 500L)
            val progress = state.progress.value
            val buffer = FloatArray(2)
            val written =
                blendInto(
                    into = buffer,
                    from = state.from[0],
                    to = state.to[0],
                    progress = progress,
                )

            // The selection marker and the path have to read the same blend, or they disagree mid-morph.
            assertEquals(expected = 2, actual = written)
            assertEquals(
                expected = buffer[0],
                actual = state.drawnValueAt(seriesIndex = 0, pointIndex = 0, progress = progress),
            )
            assertEquals(
                expected = buffer[1],
                actual = state.drawnValueAt(seriesIndex = 0, pointIndex = 1, progress = progress),
            )
        }
}
