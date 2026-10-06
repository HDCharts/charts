package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.RingGaugeSampleData
import io.github.hdcharts.sampleshared.data.RingGaugeSampleUseCase

internal class DefaultRingGaugeSampleUseCase : RingGaugeSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Quarterly Targets"
        private const val SINGLE_RING_TITLE = "Storage Used"
        private const val MANY_RINGS_TITLE = "Sprint Progress by Team"
        private const val SIGNED_RANGE_TITLE = "Temperature (°C)"
        private const val PERCENT_MIN = 0.0
        private const val PERCENT_MAX = 100.0
        private const val TEMPERATURE_MIN = -20.0
        private const val TEMPERATURE_MAX = 40.0
    }

    // Share of each quarterly target reached, in percent.
    private val defaultValues = listOf(62.0, 39.0, 25.0)
    private val defaultLabels = listOf("Revenue", "Signups", "Retention")

    // Share of planned work done, in percent.
    private val manyRingsValues = listOf(92.0, 81.0, 74.0, 66.0, 58.0, 47.0, 35.0, 22.0)
    private val manyRingsLabels =
        listOf("Payments", "Search", "Mobile", "Web", "Platform", "Data", "Growth", "Support")

    // The sauna reading is past the 40 °C end of the range.
    private val temperatureValues = listOf(-8.0, 21.0, 74.0)
    private val temperatureLabels = listOf("Outside", "Inside", "Sauna")

    override fun initialRingGaugeSample(): RingGaugeSampleData =
        RingGaugeSampleData(
            data = defaultValues.toChartData(categories = defaultLabels),
            title = DEFAULT_TITLE,
            rangeMin = PERCENT_MIN,
            rangeMax = PERCENT_MAX,
        )

    override fun initialSingleRingGaugeSample(): RingGaugeSampleData =
        RingGaugeSampleData(
            data = listOf(72.0).toChartData(),
            title = SINGLE_RING_TITLE,
            rangeMin = PERCENT_MIN,
            rangeMax = PERCENT_MAX,
        )

    override fun initialManyRingsGaugeSample(): RingGaugeSampleData =
        RingGaugeSampleData(
            data = manyRingsValues.toChartData(categories = manyRingsLabels),
            title = MANY_RINGS_TITLE,
            rangeMin = PERCENT_MIN,
            rangeMax = PERCENT_MAX,
        )

    override fun initialSignedRangeRingGaugeSample(): RingGaugeSampleData =
        RingGaugeSampleData(
            data = temperatureValues.toChartData(categories = temperatureLabels),
            title = SIGNED_RANGE_TITLE,
            rangeMin = TEMPERATURE_MIN,
            rangeMax = TEMPERATURE_MAX,
        )
}
