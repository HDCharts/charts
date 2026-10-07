package io.github.hdcharts.core.internal.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ChartMorphStateTest {
    @Test
    fun morphInterruptedHalfway_freezesTheDrawnValuesAndRestartsProgress() =
        runComposeUiTest {
            val state = ChartMorphState(initialValues = listOf(listOf(0f)))
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
                message = "The new morph must freeze the value on screen, or the chart jumps on the frame it starts.",
            )
            assertTrue(
                restartedProgress < 0.05f,
                "Progress must restart near 0, not carry over the interrupted morph: $restartedProgress",
            )
        }

    @Test
    fun morphWithPointProgress_interrupted_freezesTheDrawnValues() =
        runComposeUiTest {
            // The second point lags behind the first, as a cascaded bar does.
            val state =
                ChartMorphState(
                    initialValues = listOf(listOf(0f, 0f)),
                    pointProgress = ChartPointProgress(::squaredPastFirstPoint),
                )
            val target = mutableStateOf(listOf(listOf(0f, 0f)))
            mainClock.autoAdvance = false

            setContent {
                LaunchedEffect(target.value) {
                    state.animateTo(
                        targets = target.value,
                        animationSpec = TweenSpec(durationMillis = 1_000, easing = LinearEasing),
                    )
                }
            }

            target.value = listOf(listOf(1f, 1f))
            mainClock.advanceTimeBy(milliseconds = 500L)
            val drawnHalfway = state.drawnValueAt(seriesIndex = 0, pointIndex = 1, progress = state.progress.value)
            assertTrue(drawnHalfway in 0.15f..0.35f, "The lagging point has to be part-way through: $drawnHalfway")

            target.value = listOf(listOf(0f, 0f))
            mainClock.advanceTimeByFrame()

            assertEquals(
                expected = drawnHalfway,
                actual = state.drawnValueAt(seriesIndex = 0, pointIndex = 1, progress = state.progress.value),
                absoluteTolerance = 0.05f,
                message = "The new morph must freeze each point at its own progress, or the point jumps.",
            )
        }

    @Test
    fun drawnValueAt_matchesTheValueBlendIntoWritesMidMorph() =
        runComposeUiTest {
            val state = ChartMorphState(initialValues = listOf(listOf(0.25f, 0.75f)))
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

    @Test
    fun blendInto_morphHalfway_halvesTheDistanceFromStartToTarget() {
        val buffer = FloatArray(4)

        val count =
            blendInto(
                into = buffer,
                from = listOf(0f, 0.5f, 1f),
                to = listOf(1f, 1f, 0f),
                progress = 0.5f,
            )

        assertEquals(expected = 3, actual = count)
        assertEquals(expected = 0.5f, actual = buffer[0])
        assertEquals(expected = 0.75f, actual = buffer[1])
        assertEquals(expected = 0.5f, actual = buffer[2])
    }

    @Test
    fun blendInto_atTarget_writesTheTargetValues() {
        val buffer = FloatArray(3)

        val count =
            blendInto(
                into = buffer,
                from = listOf(0f, 0f, 0f),
                to = listOf(0.25f, 0.5f, 1f),
                progress = 1f,
            )

        assertEquals(expected = 3, actual = count)
        assertContentEquals(expected = floatArrayOf(0.25f, 0.5f, 1f), actual = buffer)
    }

    @Test
    fun blendInto_smallerBuffer_writesOnlyWhatFits() {
        val buffer = FloatArray(2)

        val count =
            blendInto(
                into = buffer,
                from = emptyList(),
                to = listOf(1f, 2f, 3f, 4f),
                progress = 1f,
            )

        assertEquals(expected = 2, actual = count)
        assertContentEquals(expected = floatArrayOf(1f, 2f), actual = buffer)
    }

    private fun squaredPastFirstPoint(
        pointIndex: Int,
        progress: Float,
    ): Float = if (pointIndex == 0) progress else progress * progress
}
