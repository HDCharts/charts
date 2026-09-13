package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.PieSampleData
import dev.hdcode.charts.sampleshared.data.PieSampleUseCase
import io.github.dautovicharis.charts.model.PieSlice

internal class DefaultPieSampleUseCase : PieSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Household Energy"
        private const val CUSTOM_TITLE = "Monthly Budget Allocation"
        private val REFRESH_RANGE = 5..45
    }

    private val pieDefaultValues = listOf(32.0, 21.0, 24.0, 14.0, 9.0)
    private val pieDefaultLabels =
        listOf("Heating", "Cooling", "Appliances", "Water Heating", "Lighting")
    private val pieCustomValues = listOf(35.0, 20.0, 12.0, 8.0, 18.0, 7.0)
    private val pieCustomLabels =
        listOf("Housing", "Food", "Transport", "Healthcare", "Savings", "Leisure")

    override fun initialPieSample(): PieSampleData =
        buildPieSample(
            values = pieDefaultValues,
            labels = pieDefaultLabels,
            title = DEFAULT_TITLE,
        )

    override fun initialPieCustomSample(): PieSampleData =
        buildPieSample(
            values = pieCustomValues,
            labels = pieCustomLabels,
            title = CUSTOM_TITLE,
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

    override fun pieCustomSample(range: IntRange): PieSampleData {
        val values = List(pieCustomLabels.size) { range.random().toDouble() }
        return buildPieSample(
            values = values,
            labels = pieCustomLabels,
            title = CUSTOM_TITLE,
        )
    }

    private fun buildPieSample(
        values: List<Double>,
        labels: List<String>,
        title: String = DEFAULT_TITLE,
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
