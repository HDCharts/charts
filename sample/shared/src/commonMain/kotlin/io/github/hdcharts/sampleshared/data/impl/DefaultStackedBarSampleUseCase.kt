package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.StackedBarSampleData
import io.github.hdcharts.sampleshared.data.StackedBarSampleUseCase
import kotlin.random.Random

internal class DefaultStackedBarSampleUseCase : StackedBarSampleUseCase {
    companion object {
        private const val TITLE = "Quarterly Revenue by Product"
        private const val RANDOM_START_YEAR = 2024
        private const val YEAR_AFTER_LAST = 2026
        private const val QUARTERS_PER_YEAR = 4

        // Shifts the yearly cycle so it peaks in Q4.
        private const val Q4_PEAK_PHASE = -2.0
    }

    // Revenue in $K per quarter: each product's first and last quarter, curve, Q4 lift, and noise.
    // Subscriptions grow fastest; hardware shrinks but lifts every Q4.
    private val products =
        listOf(
            ProductShape("Subscriptions", start = 20.0, end = 350.0, curve = 1.6, q4Lift = 8.0, noise = 6.0),
            ProductShape("Hardware", start = 260.0, end = 190.0, curve = 1.0, q4Lift = 40.0, noise = 8.0),
            ProductShape("Services", start = 55.0, end = 130.0, curve = 1.0, q4Lift = 14.0, noise = 5.0),
            ProductShape("Licensing", start = 10.0, end = 76.0, curve = 1.3, q4Lift = 0.0, noise = 3.0),
        )
    private val productNames = products.map { it.name }

    private class ProductShape(
        val name: String,
        val start: Double,
        val end: Double,
        val curve: Double,
        val q4Lift: Double,
        val noise: Double,
    )

    override fun deterministic(points: Int): StackedBarSampleData {
        val random = Random(61)
        val series =
            products.map { shape ->
                val values =
                    SampleSignals.trend(
                        count = points,
                        start = shape.start,
                        end = shape.end,
                        random = random,
                        curve = shape.curve,
                        cycleAmplitude = shape.q4Lift,
                        cyclePeriod = QUARTERS_PER_YEAR.toDouble(),
                        cyclePhase = Q4_PEAK_PHASE,
                        noise = shape.noise,
                    )
                shape.name to SampleSignals.rounded(values.map { it.coerceAtLeast(0.0) })
            }
        val startYear = YEAR_AFTER_LAST - (points + QUARTERS_PER_YEAR - 1) / QUARTERS_PER_YEAR
        return StackedBarSampleData(
            dataSet = series.toChartData(categories = SampleLabels.quarters(points, startYear)),
            segmentKeys = productNames,
            title = TITLE,
        )
    }

    override fun random(
        points: Int,
        range: IntRange,
    ): StackedBarSampleData {
        val safePoints = points.coerceAtLeast(1)
        val safeRangeStart = minOf(range.first, range.last)
        val safeRangeEnd = maxOf(range.first, range.last)
        val safeRange = safeRangeStart..safeRangeEnd
        val series =
            productNames.map { name ->
                name to List(safePoints) { safeRange.random().toDouble() }
            }
        return StackedBarSampleData(
            dataSet = series.toChartData(categories = SampleLabels.quarters(safePoints, RANDOM_START_YEAR)),
            segmentKeys = productNames,
            title = TITLE,
        )
    }
}
