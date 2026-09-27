package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.sampleshared.data.StackedAreaSampleData
import io.github.hdcharts.sampleshared.data.StackedAreaSampleUseCase
import kotlin.random.Random

internal class DefaultStackedAreaSampleUseCase : StackedAreaSampleUseCase {
    companion object {
        private const val TITLE = "Active Subscribers by Plan"
        private const val DEFAULT_MONTHS = 24
        private const val NO_CATEGORIES_POINTS = 12
        private const val NO_CATEGORIES_PLANS = 3
        private const val DENSE_MONTHS = 72
        private const val DENSE_START_YEAR = 2020
        private const val START_YEAR = 2024
        private const val MONTHS_PER_YEAR = 12.0
        private val REFRESH_RANGE = 100..1000
    }

    private class PlanShape(
        val name: String,
        val start: Double,
        val end: Double,
        val curve: Double,
        val cycleAmplitude: Double,
        val noise: Double,
    )

    // Subscribers per plan. Free dips each summer; Pro accelerates and passes Starter late in year two.
    private val plans =
        listOf(
            PlanShape("Free", start = 4_200.0, end = 5_100.0, curve = 1.0, cycleAmplitude = 260.0, noise = 90.0),
            PlanShape("Starter", start = 1_800.0, end = 2_450.0, curve = 1.0, cycleAmplitude = 60.0, noise = 50.0),
            PlanShape("Pro", start = 520.0, end = 2_720.0, curve = 1.8, cycleAmplitude = 0.0, noise = 45.0),
            PlanShape("Enterprise", start = 90.0, end = 640.0, curve = 1.4, cycleAmplitude = 0.0, noise = 18.0),
        )

    override fun initialStackedAreaSample(): StackedAreaSampleData =
        planSample(seed = 47, months = DEFAULT_MONTHS, startYear = START_YEAR)

    override fun initialDenseStackedAreaSample(): StackedAreaSampleData =
        planSample(seed = 59, months = DENSE_MONTHS, startYear = DENSE_START_YEAR)

    override fun initialStackedAreaNoCategoriesData(): StackedAreaSampleData =
        planSample(
            seed = 53,
            months = NO_CATEGORIES_POINTS,
            startYear = null,
            planCount = NO_CATEGORIES_PLANS,
        )

    override fun stackedAreaRefreshRange(): IntRange = REFRESH_RANGE

    override fun stackedAreaSample(
        points: Int,
        range: IntRange,
    ): StackedAreaSampleData {
        val safePoints = points.coerceAtLeast(2)
        val safeRangeStart = minOf(range.first, range.last).coerceAtLeast(0)
        val safeRangeEnd = maxOf(range.first, range.last).coerceAtLeast(safeRangeStart)
        val safeRange = safeRangeStart..safeRangeEnd
        val series =
            plans.map { plan ->
                plan.name to List(safePoints) { safeRange.random().toDouble() }
            }
        return StackedAreaSampleData(
            data = series.toChartData(categories = SampleLabels.monthsWithYear(safePoints, START_YEAR)),
            seriesKeys = series.map { it.first },
            title = TITLE,
        )
    }

    // A null startYear leaves the points without time labels.
    private fun planSample(
        seed: Int,
        months: Int,
        startYear: Int?,
        planCount: Int = plans.size,
    ): StackedAreaSampleData {
        val random = Random(seed)
        val series = plans.take(planCount).map { plan -> plan.name to planValues(plan, months, random) }
        val categories = startYear?.let { SampleLabels.monthsWithYear(months, it) }.orEmpty()
        return StackedAreaSampleData(
            data = series.toChartData(categories = categories),
            seriesKeys = series.map { it.first },
            title = TITLE,
        )
    }

    private fun planValues(
        plan: PlanShape,
        count: Int,
        random: Random,
    ): List<Double> =
        SampleSignals.rounded(
            SampleSignals.trend(
                count = count,
                start = plan.start,
                end = plan.end,
                random = random,
                curve = plan.curve,
                cycleAmplitude = plan.cycleAmplitude,
                cyclePeriod = MONTHS_PER_YEAR,
                cyclePhase = MONTHS_PER_YEAR / 4,
                noise = plan.noise,
            ),
        )
}
