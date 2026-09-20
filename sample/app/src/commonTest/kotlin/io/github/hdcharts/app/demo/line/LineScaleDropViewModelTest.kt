package io.github.hdcharts.app.demo.line

import io.github.hdcharts.app.demo.timeline.LiveTimelineDefaults
import io.github.hdcharts.sampleshared.data.impl.DefaultLiveLatencyTimelineUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val TICKS_TO_ADVANCE = 4

@OptIn(ExperimentalCoroutinesApi::class)
class LineScaleDropViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onEnterDemo_afterUserPaused_keepsStreamingStopped() =
        runViewModelTest { viewModel ->
            viewModel.onEnterDemo()
            viewModel.togglePlaying()
            assertFalse(viewModel.uiState.value.isPlaying, "The demo must be paused before re-entering it.")

            // Leaving and re-entering happens on a preset switch and on an Android configuration change.
            viewModel.onLeaveDemo()
            viewModel.onEnterDemo()

            val pausedDataSet = viewModel.uiState.value.dataSet
            advanceTimeBy(tickDuration(ticks = TICKS_TO_ADVANCE))

            assertFalse(
                viewModel.uiState.value.isPlaying,
                "Re-entering the demo must not override the pause the user chose.",
            )
            assertEquals(
                expected = pausedDataSet,
                actual = viewModel.uiState.value.dataSet,
                message = "A paused demo must not stream new points after it is re-entered.",
            )
        }

    @Test
    fun onEnterDemo_whilePlaying_streamsNewPoints() =
        runViewModelTest { viewModel ->
            val initialDataSet = viewModel.uiState.value.dataSet

            viewModel.onEnterDemo()
            advanceTimeBy(tickDuration(ticks = TICKS_TO_ADVANCE))

            assertTrue(viewModel.uiState.value.isPlaying)
            assertTrue(
                viewModel.uiState.value.dataSet != initialDataSet,
                "A playing demo must stream new points.",
            )
        }

    @Test
    fun onLeaveDemo_stopsStreamingWithoutChangingThePlayState() =
        runViewModelTest { viewModel ->
            viewModel.onEnterDemo()
            viewModel.onLeaveDemo()

            val leftDataSet = viewModel.uiState.value.dataSet
            advanceTimeBy(tickDuration(ticks = TICKS_TO_ADVANCE))

            assertTrue(
                viewModel.uiState.value.isPlaying,
                "Leaving the demo must not read as the user pausing it.",
            )
            assertEquals(
                expected = leftDataSet,
                actual = viewModel.uiState.value.dataSet,
                message = "A demo that is not on screen must not stream new points.",
            )
        }

    @Test
    fun updateScaleSwitchPoints_clampsToTheSupportedRange() {
        val viewModel = createViewModel()

        viewModel.updateScaleSwitchPoints(Int.MAX_VALUE)
        assertEquals(
            expected = LineScaleDropViewModel.SWITCH_POINTS_RANGE.last,
            actual = viewModel.uiState.value.controlsState.scaleSwitchPoints,
        )

        viewModel.updateScaleSwitchPoints(Int.MIN_VALUE)
        assertEquals(
            expected = LineScaleDropViewModel.SWITCH_POINTS_RANGE.first,
            actual = viewModel.uiState.value.controlsState.scaleSwitchPoints,
        )
    }

    /**
     * Runs [body] against a view model whose streaming loop is always stopped afterwards.
     *
     * [runTest] advances the scheduler until it is idle once the body returns, and the streaming
     * loop delays forever, so a leaked stream hangs the suite instead of failing it.
     */
    private fun runViewModelTest(body: TestScope.(LineScaleDropViewModel) -> Unit) =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            try {
                body(viewModel)
            } finally {
                viewModel.onLeaveDemo()
            }
        }

    private fun createViewModel() =
        LineScaleDropViewModel(liveLatencyTimelineUseCase = DefaultLiveLatencyTimelineUseCase())

    private fun tickDuration(ticks: Int): Long = LiveTimelineDefaults.DEFAULT_UPDATE_INTERVAL_MS.toLong() * ticks
}
