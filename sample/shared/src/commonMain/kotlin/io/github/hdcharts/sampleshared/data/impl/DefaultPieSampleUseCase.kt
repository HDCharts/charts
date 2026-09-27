package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.charts.model.PieSlice
import io.github.hdcharts.sampleshared.data.PieSampleData
import io.github.hdcharts.sampleshared.data.PieSampleUseCase

internal class DefaultPieSampleUseCase : PieSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Website Traffic by Source"
        private const val MANY_SLICES_TITLE = "App Downloads by Country"
        private val REFRESH_RANGE = 5..45
    }

    // Share of sessions, in percent.
    private val pieDefaultValues = listOf(38.4, 21.7, 14.2, 11.6, 8.3, 5.8)
    private val pieDefaultLabels =
        listOf("Organic Search", "Direct", "Social", "Referral", "Email", "Paid Ads")

    // Share of downloads, in percent. The long tail ends in slices under 2%.
    private val pieManySlicesValues =
        listOf(28.4, 14.1, 9.6, 7.8, 6.9, 6.1, 5.2, 4.7, 4.3, 3.9, 2.8, 2.4, 1.6, 1.2, 0.8)
    private val pieManySlicesLabels =
        listOf(
            "United States",
            "India",
            "Brazil",
            "Germany",
            "United Kingdom",
            "Japan",
            "France",
            "Canada",
            "Mexico",
            "Indonesia",
            "Italy",
            "Spain",
            "Poland",
            "Netherlands",
            "Sweden",
        )

    override fun initialPieSample(): PieSampleData =
        buildPieSample(
            values = pieDefaultValues,
            labels = pieDefaultLabels,
            title = DEFAULT_TITLE,
        )

    override fun initialManySlicesPieSample(): PieSampleData =
        buildPieSample(
            values = pieManySlicesValues,
            labels = pieManySlicesLabels,
            title = MANY_SLICES_TITLE,
        )

    override fun pieRefreshRange(): IntRange = REFRESH_RANGE

    override fun pieSample(
        range: IntRange,
        numOfPoints: IntRange,
    ): PieSampleData {
        val points = numOfPoints.random()
        val values = List(points) { range.random().toDouble() }
        return buildPieSample(
            values = values,
            labels = defaultLabels(points),
            title = DEFAULT_TITLE,
        )
    }

    private fun buildPieSample(
        values: List<Double>,
        labels: List<String>,
        title: String,
    ): PieSampleData {
        val slices =
            values.mapIndexed { index, value ->
                PieSlice(label = labels.getOrNull(index) ?: "Segment ${index + 1}", value = value)
            }
        return PieSampleData(
            slices = slices,
            title = title,
        )
    }

    private fun defaultLabels(points: Int): List<String> {
        if (points <= pieDefaultLabels.size) {
            return pieDefaultLabels.take(points)
        }
        val extrasCount = points - pieDefaultLabels.size
        val extras = List(extrasCount) { index -> "Category ${index + 1}" }
        return pieDefaultLabels + extras
    }
}
