package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.sampleshared.data.LiveLatencyMultiSeriesWindow
import io.github.hdcharts.sampleshared.data.LiveLatencySingleSeriesWindow
import io.github.hdcharts.sampleshared.data.LiveLatencyTimelineUseCase
import io.github.hdcharts.sampleshared.data.LiveTimelineProfile
import io.github.hdcharts.sampleshared.data.MIN_SCALE_SWITCH_POINTS
import kotlin.math.sin
import kotlin.random.Random

class DefaultLiveLatencyTimelineUseCase : LiveLatencyTimelineUseCase {
    private val generator = LiveLatencyTimelineGenerator()

    override val multiSeriesKeys: List<String> = generator.multiSeriesKeys

    override fun createSingleWindow(
        windowSize: Int,
        endTick: Int?,
        profile: LiveTimelineProfile,
        scaleSwitchPoints: Int,
    ): LiveLatencySingleSeriesWindow =
        generator.createSingleWindow(
            windowSize = windowSize,
            endTick = endTick,
            profile = profile,
            scaleSwitchPoints = scaleSwitchPoints,
        )

    override fun advanceSingleWindow(window: LiveLatencySingleSeriesWindow): LiveLatencySingleSeriesWindow =
        generator.advanceSingleWindow(window)

    override fun toSingleDataSet(window: LiveLatencySingleSeriesWindow): ChartData = generator.toSingleDataSet(window)

    override fun createMultiWindow(
        windowSize: Int,
        endTick: Int?,
    ): LiveLatencyMultiSeriesWindow = generator.createMultiWindow(windowSize = windowSize, endTick = endTick)

    override fun advanceMultiWindow(window: LiveLatencyMultiSeriesWindow): LiveLatencyMultiSeriesWindow =
        generator.advanceMultiWindow(window)

    override fun toMultiDataSet(window: LiveLatencyMultiSeriesWindow): ChartData = generator.toMultiDataSet(window)
}

private class LiveLatencyTimelineGenerator {
    companion object {
        private const val MIN_WINDOW_SIZE = 2
        private const val SECONDS_PER_DAY = 24 * 60 * 60
        private const val BASE_SECOND_OF_DAY = 14 * 60 * 60
        private const val SINGLE_TITLE = "API Gateway P95 Latency"
        private const val SCALE_DROP_TITLE = "Queue Backlog Drain"
        private const val SCALE_DROP_MIN = 1_000_000.0
        private const val SCALE_DROP_MAX = 2_000_000.0
        private const val SCALE_DROP_TAIL_MAX = 100.0
        private const val P50_SERIES_LABEL = "P50 Latency"
        private const val P95_SERIES_LABEL = "P95 Latency"
        private const val P50_MIN = 70.0
        private const val P50_MAX = 190.0
        private const val P95_MAX = 320.0
    }

    val multiSeriesKeys: List<String> = listOf(P50_SERIES_LABEL, P95_SERIES_LABEL)

    fun createSingleWindow(
        windowSize: Int,
        endTick: Int? = null,
        profile: LiveTimelineProfile = LiveTimelineProfile.Latency,
        scaleSwitchPoints: Int = windowSize,
    ): LiveLatencySingleSeriesWindow {
        val safeWindowSize = windowSize.coerceAtLeast(MIN_WINDOW_SIZE)
        val safeScaleSwitchPoints = scaleSwitchPoints.coerceAtLeast(MIN_SCALE_SWITCH_POINTS)
        val resolvedEndTick = resolveEndTick(windowSize = safeWindowSize, endTick = endTick)
        val ticks = (resolvedEndTick - safeWindowSize + 1)..resolvedEndTick
        val values =
            ticks.map { tick ->
                sampleValue(tick = tick, profile = profile, scaleSwitchPoints = safeScaleSwitchPoints)
            }
        val labels = ticks.map(::formatTickLabel)
        return LiveLatencySingleSeriesWindow(
            values = values,
            labels = labels,
            endTick = resolvedEndTick,
            profile = profile,
            scaleSwitchPoints = safeScaleSwitchPoints,
        )
    }

    fun advanceSingleWindow(window: LiveLatencySingleSeriesWindow): LiveLatencySingleSeriesWindow {
        val nextTick = window.endTick + 1
        val nextValue =
            sampleValue(
                tick = nextTick,
                profile = window.profile,
                scaleSwitchPoints = window.scaleSwitchPoints.coerceAtLeast(MIN_SCALE_SWITCH_POINTS),
            )
        return window.copy(
            values = window.values.drop(1) + nextValue,
            labels = window.labels.drop(1) + formatTickLabel(nextTick),
            endTick = nextTick,
        )
    }

    fun toSingleDataSet(window: LiveLatencySingleSeriesWindow): ChartData =
        window.values.toChartData(
            categories = window.labels,
            seriesName =
                when (window.profile) {
                    LiveTimelineProfile.Latency -> SINGLE_TITLE
                    LiveTimelineProfile.ScaleDrop -> SCALE_DROP_TITLE
                },
        )

    fun createMultiWindow(
        windowSize: Int,
        endTick: Int? = null,
    ): LiveLatencyMultiSeriesWindow {
        val safeWindowSize = windowSize.coerceAtLeast(MIN_WINDOW_SIZE)
        val resolvedEndTick = resolveEndTick(windowSize = safeWindowSize, endTick = endTick)
        val ticks = (resolvedEndTick - safeWindowSize + 1)..resolvedEndTick

        val p50Values = mutableListOf<Double>()
        val p95Values = mutableListOf<Double>()
        ticks.forEach { tick ->
            val p50 = sampleP50Latency(tick)
            val p95 = sampleP95Latency(tick, p50)
            p50Values += p50
            p95Values += p95
        }

        return LiveLatencyMultiSeriesWindow(
            p50Values = p50Values,
            p95Values = p95Values,
            labels = ticks.map(::formatTickLabel),
            endTick = resolvedEndTick,
        )
    }

    fun advanceMultiWindow(window: LiveLatencyMultiSeriesWindow): LiveLatencyMultiSeriesWindow {
        val nextTick = window.endTick + 1
        val nextP50 = sampleP50Latency(nextTick)
        val nextP95 = sampleP95Latency(nextTick, nextP50)
        return window.copy(
            p50Values = window.p50Values.drop(1) + nextP50,
            p95Values = window.p95Values.drop(1) + nextP95,
            labels = window.labels.drop(1) + formatTickLabel(nextTick),
            endTick = nextTick,
        )
    }

    fun toMultiDataSet(window: LiveLatencyMultiSeriesWindow): ChartData =
        listOf(
            P50_SERIES_LABEL to window.p50Values,
            P95_SERIES_LABEL to window.p95Values,
        ).toChartData(categories = window.labels)

    private fun resolveEndTick(
        windowSize: Int,
        endTick: Int?,
    ): Int = (endTick ?: windowSize - 1).coerceAtLeast(windowSize - 1)

    private fun sampleValue(
        tick: Int,
        profile: LiveTimelineProfile,
        scaleSwitchPoints: Int,
    ): Double =
        when (profile) {
            LiveTimelineProfile.Latency -> sampleP95Latency(tick, sampleP50Latency(tick))
            LiveTimelineProfile.ScaleDrop -> sampleScaleDropValue(tick = tick, scaleSwitchPoints = scaleSwitchPoints)
        }

    private fun sampleScaleDropValue(
        tick: Int,
        scaleSwitchPoints: Int,
    ): Double =
        when ((tick / scaleSwitchPoints) % 2) {
            0 -> Random.nextDouble(from = SCALE_DROP_MIN, until = SCALE_DROP_MAX)
            else -> Random.nextDouble(from = 0.0, until = SCALE_DROP_TAIL_MAX)
        }

    private fun sampleP50Latency(tick: Int): Double {
        val trend = 112.0 + (18.0 * sin(tick / 7.0)) + (8.0 * sin(tick / 2.8))
        val jitter = Random.nextDouble(from = -5.0, until = 5.0)
        return (trend + jitter).coerceIn(P50_MIN, P50_MAX)
    }

    private fun sampleP95Latency(
        tick: Int,
        p50Latency: Double,
    ): Double {
        val spread = 32.0 + (14.0 * sin(tick / 5.0)) + Random.nextDouble(from = 0.0, until = 15.0)
        return (p50Latency + spread).coerceIn(p50Latency + 8.0, P95_MAX)
    }

    private fun formatTickLabel(tick: Int): String {
        val absoluteSecond = BASE_SECOND_OF_DAY + tick
        val normalized = ((absoluteSecond % SECONDS_PER_DAY) + SECONDS_PER_DAY) % SECONDS_PER_DAY
        val hours = normalized / 3600
        val minutes = (normalized % 3600) / 60
        val seconds = normalized % 60
        return "${twoDigits(hours)}:${twoDigits(minutes)}:${twoDigits(seconds)}"
    }

    private fun twoDigits(value: Int): String =
        if (value < 10) {
            "0$value"
        } else {
            value.toString()
        }
}
