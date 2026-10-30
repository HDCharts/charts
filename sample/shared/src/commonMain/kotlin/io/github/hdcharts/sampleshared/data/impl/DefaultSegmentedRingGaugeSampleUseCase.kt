package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.SegmentedRingGaugeSampleData
import io.github.hdcharts.sampleshared.data.SegmentedRingGaugeSampleUseCase

internal class DefaultSegmentedRingGaugeSampleUseCase : SegmentedRingGaugeSampleUseCase {
    override fun deterministic() =
        SegmentedRingGaugeSampleData(
            data =
                listOf(240.0, 180.0, 320.0).toChartData(
                    categories = listOf("Carbs", "Protein", "Fat"),
                ),
            title = "Daily Nutrition",
            capacity = 2_000.0,
            centerSupportingLabel = "of 2000 kcal",
        )
}
