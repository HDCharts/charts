package dev.hdcode.charts.app.data

import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet

data class LiveLatencySingleSeriesWindow(
    val values: List<Float>,
    val labels: List<String>,
    val endTick: Int,
)

data class LiveLatencyMultiSeriesWindow(
    val p50Values: List<Float>,
    val p95Values: List<Float>,
    val labels: List<String>,
    val endTick: Int,
)

interface LiveLatencyTimelineUseCase {
    val multiSeriesKeys: List<String>

    fun createSingleWindow(
        windowSize: Int,
        endTick: Int? = null,
    ): LiveLatencySingleSeriesWindow

    fun advanceSingleWindow(window: LiveLatencySingleSeriesWindow): LiveLatencySingleSeriesWindow

    fun toSingleDataSet(window: LiveLatencySingleSeriesWindow): ChartDataSet

    fun createMultiWindow(
        windowSize: Int,
        endTick: Int? = null,
    ): LiveLatencyMultiSeriesWindow

    fun advanceMultiWindow(window: LiveLatencyMultiSeriesWindow): LiveLatencyMultiSeriesWindow

    fun toMultiDataSet(window: LiveLatencyMultiSeriesWindow): MultiChartDataSet
}
