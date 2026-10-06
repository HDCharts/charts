package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.RadarSampleData
import io.github.hdcharts.sampleshared.data.RadarSampleUseCase

internal class DefaultRadarSampleUseCase : RadarSampleUseCase {
    companion object {
        private const val TITLE = "Score by Axis"
        private const val AXES = 6
        private val RANDOM_RANGE = 30..100
    }

    // The radar scales from the lowest to the highest value, so the scores spread from 40 to 100.
    override fun deterministic(series: Int): RadarSampleData =
        buildRadarSample(series = series) { seriesIndex, axis -> 40.0 + (axis * 3 + seriesIndex * 5) % 7 * 10 }

    override fun random(series: Int): RadarSampleData =
        buildRadarSample(series = series) { _, _ -> RANDOM_RANGE.random().toDouble() }

    private fun buildRadarSample(
        series: Int,
        score: (seriesIndex: Int, axis: Int) -> Double,
    ): RadarSampleData {
        val items =
            List(series) { seriesIndex ->
                "Series ${seriesIndex + 1}" to
                    List(AXES) { axis -> score(seriesIndex, axis) }
            }
        return RadarSampleData(
            data = items.toChartData(categories = List(AXES) { axis -> "Axis ${axis + 1}" }),
            seriesKeys = items.map { it.first },
            title = TITLE,
        )
    }
}
