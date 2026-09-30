package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.StackedBarSampleData
import io.github.hdcharts.sampleshared.data.StackedBarSampleUseCase
import kotlin.random.Random

internal class DefaultStackedBarSampleUseCase : StackedBarSampleUseCase {
    companion object {
        private const val TITLE = "Quarterly Revenue by Product"
        private const val START_YEAR = 2024
        private const val QUARTERS_PER_YEAR = 4.0
        private const val DENSE_QUARTERS = 64
        private const val DENSE_START_YEAR = 2010

        // Shifts the yearly cycle so it peaks in Q4.
        private const val Q4_PEAK_PHASE = -2.0
        private val REFRESH_RANGE = 100..1000
    }

    // Revenue in $K per quarter. Subscriptions grow steadily; hardware shrinks except for Q4.
    private val products =
        listOf(
            "Subscriptions" to listOf(210.0, 228.0, 247.0, 262.0, 281.0, 302.0, 326.0, 351.0),
            "Hardware" to listOf(184.0, 171.0, 163.0, 228.0, 158.0, 149.0, 141.0, 206.0),
            "Services" to listOf(96.0, 104.0, 99.0, 131.0, 108.0, 117.0, 112.0, 148.0),
            "Licensing" to listOf(42.0, 47.0, 51.0, 55.0, 60.0, 66.0, 71.0, 78.0),
        )
    private val productNames = products.map { it.first }

    // The same products over 16 years, each ending near its 2025 revenue.
    private val denseShapes =
        listOf(
            DenseShape(start = 20.0, end = 350.0, curve = 1.6, q4Lift = 8.0, noise = 6.0),
            DenseShape(start = 260.0, end = 190.0, curve = 1.0, q4Lift = 40.0, noise = 8.0),
            DenseShape(start = 55.0, end = 130.0, curve = 1.0, q4Lift = 14.0, noise = 5.0),
            DenseShape(start = 10.0, end = 76.0, curve = 1.3, q4Lift = 0.0, noise = 3.0),
        )

    private class DenseShape(
        val start: Double,
        val end: Double,
        val curve: Double,
        val q4Lift: Double,
        val noise: Double,
    )

    private val noCategoriesItems =
        listOf(
            "Subscriptions" to listOf(220.0, 275.0, 248.0, 331.0, 362.0),
            "Hardware" to listOf(310.0, 284.0, 296.0, 258.0, 241.0),
            "Services" to listOf(120.0, 146.0, 188.0, 175.0, 214.0),
        )

    override fun initialStackedBarSample(): StackedBarSampleData =
        StackedBarSampleData(
            dataSet =
                products.toChartData(
                    categories = SampleLabels.quarters(products.first().second.size, START_YEAR),
                ),
            segmentKeys = productNames,
            title = TITLE,
        )

    override fun initialDenseStackedBarSample(): StackedBarSampleData {
        val random = Random(61)
        val series =
            productNames.zip(denseShapes) { name, shape ->
                val values =
                    SampleSignals.trend(
                        count = DENSE_QUARTERS,
                        start = shape.start,
                        end = shape.end,
                        random = random,
                        curve = shape.curve,
                        cycleAmplitude = shape.q4Lift,
                        cyclePeriod = QUARTERS_PER_YEAR,
                        cyclePhase = Q4_PEAK_PHASE,
                        noise = shape.noise,
                    )
                name to SampleSignals.rounded(values.map { it.coerceAtLeast(0.0) })
            }
        return StackedBarSampleData(
            dataSet = series.toChartData(categories = SampleLabels.quarters(DENSE_QUARTERS, DENSE_START_YEAR)),
            segmentKeys = productNames,
            title = TITLE,
        )
    }

    override fun initialStackedBarNoCategoriesDataSet(): StackedBarSampleData =
        StackedBarSampleData(
            dataSet = noCategoriesItems.toChartData(),
            segmentKeys = emptyList(),
            title = TITLE,
        )

    override fun stackedBarRefreshRange(): IntRange = REFRESH_RANGE

    override fun stackedBarSample(
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
            dataSet = series.toChartData(categories = SampleLabels.quarters(safePoints, START_YEAR)),
            segmentKeys = productNames,
            title = TITLE,
        )
    }
}
