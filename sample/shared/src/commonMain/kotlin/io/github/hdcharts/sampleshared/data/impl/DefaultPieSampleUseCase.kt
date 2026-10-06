package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.PieSampleData
import io.github.hdcharts.sampleshared.data.PieSampleUseCase

internal class DefaultPieSampleUseCase : PieSampleUseCase {
    companion object {
        private const val TITLE = "Share by Segment"
        private val RANDOM_RANGE = 5..45
    }

    override fun deterministic(slices: Int): PieSampleData =
        buildPieSample(slices = slices) { index -> (slices - index).toDouble() }

    override fun random(slices: Int): PieSampleData =
        buildPieSample(slices = slices) { RANDOM_RANGE.random().toDouble() }

    private fun buildPieSample(
        slices: Int,
        value: (index: Int) -> Double,
    ): PieSampleData =
        PieSampleData(
            data =
                List(slices, value).toChartData(
                    categories = List(slices) { index -> "Segment ${index + 1}" },
                ),
            title = TITLE,
        )
}
