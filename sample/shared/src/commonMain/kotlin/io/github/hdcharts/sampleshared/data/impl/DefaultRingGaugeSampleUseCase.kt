package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.RingGaugeSampleData
import io.github.hdcharts.sampleshared.data.RingGaugeSampleUseCase

internal class DefaultRingGaugeSampleUseCase : RingGaugeSampleUseCase {
    companion object {
        private const val TITLE = "Progress by Ring"
        private const val SIGNED_TITLE = "Temperature (°C)"
        private val RANDOM_RANGE = 10..95
    }

    override fun deterministic(
        rings: Int,
        signed: Boolean,
    ): RingGaugeSampleData {
        val values =
            List(rings) { index ->
                if (signed) 50.0 - 30.0 * index else (100 * (rings - index) / (rings + 1)).toDouble()
            }
        return RingGaugeSampleData(
            data = values.toChartData(categories = ringNames(rings)),
            title = if (signed) SIGNED_TITLE else TITLE,
            rangeMin = if (signed) -20.0 else 0.0,
            rangeMax = if (signed) 40.0 else 100.0,
        )
    }

    override fun random(rings: Int): RingGaugeSampleData =
        RingGaugeSampleData(
            data = List(rings) { RANDOM_RANGE.random().toDouble() }.toChartData(categories = ringNames(rings)),
            title = TITLE,
            rangeMin = 0.0,
            rangeMax = 100.0,
        )

    private fun ringNames(rings: Int): List<String> = List(rings) { index -> "Ring ${index + 1}" }
}
