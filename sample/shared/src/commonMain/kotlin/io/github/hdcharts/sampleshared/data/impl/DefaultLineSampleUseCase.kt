package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.LineSampleUseCase
import kotlin.random.Random

internal class DefaultLineSampleUseCase : LineSampleUseCase {
    companion object {
        private const val TITLE = "Net Revenue (\$K)"
        private const val DAYS_PER_YEAR = 365
    }

    override fun deterministic(
        points: Int,
        signed: Boolean,
    ): ChartData = if (signed) signedMonths(points) else seasonalDays(points)

    private fun seasonalDays(points: Int): ChartData {
        val random = Random(23)
        val weekly =
            SampleSignals.trend(
                count = points,
                start = 0.0,
                end = 0.0,
                random = random,
                cycleAmplitude = 70.0,
            )
        val values =
            SampleSignals
                .trend(
                    count = points,
                    start = 820.0,
                    end = 1_340.0,
                    random = random,
                    cycleAmplitude = 150.0,
                    cyclePeriod = DAYS_PER_YEAR.toDouble(),
                    // Peaks in mid-December, lowest in mid-June.
                    cyclePhase = DAYS_PER_YEAR * 0.3,
                    noise = 55.0,
                ).zip(weekly) { trend, cycle -> trend + cycle }
        return SampleSignals
            .rounded(values)
            .toChartData(categories = SampleLabels.days(points), seriesName = TITLE)
    }

    private fun signedMonths(points: Int): ChartData {
        val values =
            SampleSignals.trend(
                count = points,
                start = -9.5,
                end = 13.8,
                random = Random(5),
                cycleAmplitude = 3.6,
                cyclePeriod = 6.0,
                noise = 1.8,
            )
        return SampleSignals
            .rounded(values, decimals = 1)
            .toChartData(
                categories = SampleLabels.monthsWithYear(points, startYear = 2024),
                seriesName = TITLE,
            )
    }
}
