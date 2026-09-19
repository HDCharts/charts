package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.sampleshared.data.impl.DefaultLiveLatencyTimelineUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val WINDOW_SIZE = 40
private const val LARGE_VALUE_FLOOR = 1_000_000.0
private const val SMALL_VALUE_CEILING = 100.0

class LiveLatencyScaleDropTest {
    private val useCase = DefaultLiveLatencyTimelineUseCase()

    @Test
    fun createSingleWindow_forScaleDropProfile_defaultsPhaseToWindowSize() {
        val window =
            useCase.createSingleWindow(
                windowSize = WINDOW_SIZE,
                profile = LiveTimelineProfile.ScaleDrop,
            )

        assertEquals(
            expected = WINDOW_SIZE,
            actual = window.scaleSwitchPoints,
            message =
                "A phase shorter than the window keeps both magnitudes on screen at all times, which " +
                    "pins the range and hides the rescaling the profile exists to show.",
        )
    }

    @Test
    fun advanceSingleWindow_forScaleDropProfile_eventuallyHoldsOneMagnitude() {
        var window =
            useCase.createSingleWindow(
                windowSize = WINDOW_SIZE,
                profile = LiveTimelineProfile.ScaleDrop,
            )
        repeat(WINDOW_SIZE) { window = useCase.advanceSingleWindow(window) }

        val values = window.values
        val isSingleMagnitude =
            values.all { value -> value >= LARGE_VALUE_FLOOR } ||
                values.all { value -> value < SMALL_VALUE_CEILING }

        assertTrue(
            isSingleMagnitude,
            "After a full window of updates the window must hold a single magnitude so the chart " +
                "rescales, but it spans ${values.min()}..${values.max()}.",
        )
    }

    @Test
    fun createSingleWindow_forScaleDropProfile_clampsPhaseToSupportedMinimum() {
        val window =
            useCase.createSingleWindow(
                windowSize = WINDOW_SIZE,
                profile = LiveTimelineProfile.ScaleDrop,
                scaleSwitchPoints = 0,
            )

        assertEquals(expected = MIN_SCALE_SWITCH_POINTS, actual = window.scaleSwitchPoints)
    }

    @Test
    fun createSingleWindow_forLatencyProfile_staysInTheLatencyRange() {
        val window = useCase.createSingleWindow(windowSize = WINDOW_SIZE)

        assertEquals(expected = WINDOW_SIZE, actual = window.values.size)
        assertTrue(
            window.values.all { value -> value < LARGE_VALUE_FLOOR },
            "The latency profile must ignore the scale drop magnitudes.",
        )
    }
}
