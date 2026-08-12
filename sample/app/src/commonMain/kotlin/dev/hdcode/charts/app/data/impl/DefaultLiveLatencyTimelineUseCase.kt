package dev.hdcode.charts.app.data.impl

import dev.hdcode.charts.app.data.LiveLatencyMultiSeriesWindow
import dev.hdcode.charts.app.data.LiveLatencySingleSeriesWindow
import dev.hdcode.charts.app.data.LiveLatencyTimelineUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import kotlin.math.sin
import kotlin.random.Random

class DefaultLiveLatencyTimelineUseCase : LiveLatencyTimelineUseCase {
    private val generator = LiveLatencyTimelineGenerator()

    override val multiSeriesKeys: List<String> = generator.multiSeriesKeys

    override fun createSingleWindow(
        windowSize: Int,
        endTick: Int?,
    ): LiveLatencySingleSeriesWindow = generator.createSingleWindow(windowSize = windowSize, endTick = endTick)

    override fun advanceSingleWindow(window: LiveLatencySingleSeriesWindow): LiveLatencySingleSeriesWindow =
        generator.advanceSingleWindow(window)

    override fun toSingleDataSet(window: LiveLatencySingleSeriesWindow): ChartDataSet =
        generator.toSingleDataSet(window)

    override fun createMultiWindow(
        windowSize: Int,
        endTick: Int?,
    ): LiveLatencyMultiSeriesWindow = generator.createMultiWindow(windowSize = windowSize, endTick = endTick)

    override fun advanceMultiWindow(window: LiveLatencyMultiSeriesWindow): LiveLatencyMultiSeriesWindow =
        generator.advanceMultiWindow(window)

    override fun toMultiDataSet(window: LiveLatencyMultiSeriesWindow): MultiChartDataSet =
        generator.toMultiDataSet(window)
}

private class LiveLatencyTimelineGenerator {
    companion object {
        private const val MIN_WINDOW_SIZE = 2
        private const val SECONDS_PER_DAY = 24 * 60 * 60
        private const val BASE_SECOND_OF_DAY = 14 * 60 * 60
        private const val SINGLE_TITLE = "API Gateway P95 Latency"
        private const val MULTI_TITLE = "API Latency (P50 vs P95)"
        private const val P50_SERIES_LABEL = "P50 Latency"
        private const val P95_SERIES_LABEL = "P95 Latency"
        private const val VALUE_POSTFIX = " ms"
    }

    val multiSeriesKeys: List<String> = listOf(P50_SERIES_LABEL, P95_SERIES_LABEL)

    fun createSingleWindow(
        windowSize: Int,
        endTick: Int? = null,
    ): LiveLatencySingleSeriesWindow {
        val safeWindowSize = windowSize.coerceAtLeast(MIN_WINDOW_SIZE)
        val resolvedEndTick = resolveEndTick(windowSize = safeWindowSize, endTick = endTick)
        val ticks = (resolvedEndTick - safeWindowSize + 1)..resolvedEndTick
        val values =
            ticks.map { tick ->
                val p50 = sampleP50Latency(tick)
                sampleP95Latency(tick, p50)
            }
        val labels = ticks.map(::formatTickLabel)
        return LiveLatencySingleSeriesWindow(
            values = values,
            labels = labels,
            endTick = resolvedEndTick,
        )
    }

    fun advanceSingleWindow(window: LiveLatencySingleSeriesWindow): LiveLatencySingleSeriesWindow {
        val nextTick = window.endTick + 1
        val p50 = sampleP50Latency(nextTick)
        val nextP95 = sampleP95Latency(nextTick, p50)
        return window.copy(
            values = window.values.drop(1) + nextP95,
            labels = window.labels.drop(1) + formatTickLabel(nextTick),
            endTick = nextTick,
        )
    }

    fun toSingleDataSet(window: LiveLatencySingleSeriesWindow): ChartDataSet =
        window.values.toChartDataSet(
            title = SINGLE_TITLE,
            labels = window.labels,
        )

    fun createMultiWindow(
        windowSize: Int,
        endTick: Int? = null,
    ): LiveLatencyMultiSeriesWindow {
        val safeWindowSize = windowSize.coerceAtLeast(MIN_WINDOW_SIZE)
        val resolvedEndTick = resolveEndTick(windowSize = safeWindowSize, endTick = endTick)
        val ticks = (resolvedEndTick - safeWindowSize + 1)..resolvedEndTick

        val p50Values = mutableListOf<Float>()
        val p95Values = mutableListOf<Float>()
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

    fun toMultiDataSet(window: LiveLatencyMultiSeriesWindow): MultiChartDataSet =
        listOf(
            P50_SERIES_LABEL to window.p50Values,
            P95_SERIES_LABEL to window.p95Values,
        ).toMultiChartDataSet(
            title = MULTI_TITLE,
            categories = window.labels,
            postfix = VALUE_POSTFIX,
        )

    private fun resolveEndTick(
        windowSize: Int,
        endTick: Int?,
    ): Int = (endTick ?: windowSize - 1).coerceAtLeast(windowSize - 1)

    private fun sampleP50Latency(tick: Int): Float {
        val trend = 112.0 + (18.0 * sin(tick / 7.0)) + (8.0 * sin(tick / 2.8))
        val jitter = Random.nextDouble(from = -5.0, until = 5.0)
        return (trend + jitter).toFloat().coerceIn(70f, 190f)
    }

    private fun sampleP95Latency(
        tick: Int,
        p50Latency: Float,
    ): Float {
        val spread = 32.0 + (14.0 * sin(tick / 5.0)) + Random.nextDouble(from = 0.0, until = 15.0)
        return (p50Latency + spread.toFloat()).coerceIn(p50Latency + 8f, 320f)
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
