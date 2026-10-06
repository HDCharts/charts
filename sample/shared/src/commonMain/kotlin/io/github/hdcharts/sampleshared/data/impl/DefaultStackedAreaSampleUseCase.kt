package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.StackedAreaSampleData
import io.github.hdcharts.sampleshared.data.StackedAreaSampleUseCase
import kotlin.random.Random

internal class DefaultStackedAreaSampleUseCase : StackedAreaSampleUseCase {
    companion object {
        private const val TITLE = "Active Subscribers by Plan"
        private const val RANDOM_START_YEAR = 2024
        private const val YEAR_AFTER_LAST = 2026
        private const val MONTHS_PER_YEAR = 12
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

    override fun deterministic(points: Int): StackedAreaSampleData {
        val random = Random(47)
        val series = plans.map { plan -> plan.name to planValues(plan = plan, count = points, random = random) }
        val startYear = YEAR_AFTER_LAST - (points + MONTHS_PER_YEAR - 1) / MONTHS_PER_YEAR
        return StackedAreaSampleData(
            data = series.toChartData(categories = SampleLabels.monthsWithYear(points, startYear)),
            seriesKeys = series.map { it.first },
            title = TITLE,
        )
    }

    override fun random(
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
            data = series.toChartData(categories = SampleLabels.monthsWithYear(safePoints, RANDOM_START_YEAR)),
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
                cyclePeriod = MONTHS_PER_YEAR.toDouble(),
                cyclePhase = MONTHS_PER_YEAR / 4.0,
                noise = plan.noise,
            ),
        )
}
