package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.charts.model.ChartData

enum class LiveTimelineProfile {
    Latency,
    ScaleDrop,
}

const val MIN_SCALE_SWITCH_POINTS = 2

data class LiveLatencySingleSeriesWindow(
    val values: List<Double>,
    val labels: List<String>,
    val endTick: Int,
    val profile: LiveTimelineProfile = LiveTimelineProfile.Latency,
    val scaleSwitchPoints: Int = MIN_SCALE_SWITCH_POINTS,
)

data class LiveLatencyMultiSeriesWindow(
    val p50Values: List<Double>,
    val p95Values: List<Double>,
    val labels: List<String>,
    val endTick: Int,
)

interface LiveLatencyTimelineUseCase {
    val multiSeriesKeys: List<String>

    fun createSingleWindow(
        windowSize: Int,
        endTick: Int? = null,
        profile: LiveTimelineProfile = LiveTimelineProfile.Latency,
        scaleSwitchPoints: Int = windowSize,
    ): LiveLatencySingleSeriesWindow

    fun advanceSingleWindow(window: LiveLatencySingleSeriesWindow): LiveLatencySingleSeriesWindow

    fun toSingleDataSet(window: LiveLatencySingleSeriesWindow): ChartData

    fun createMultiWindow(
        windowSize: Int,
        endTick: Int? = null,
    ): LiveLatencyMultiSeriesWindow

    fun advanceMultiWindow(window: LiveLatencyMultiSeriesWindow): LiveLatencyMultiSeriesWindow

    fun toMultiDataSet(window: LiveLatencyMultiSeriesWindow): ChartData
}
