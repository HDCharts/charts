package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.LineSampleUseCase
import kotlin.random.Random

internal class DefaultLineSampleUseCase : LineSampleUseCase {
    companion object {
        private const val TITLE = "Net Revenue (\$K)"
        private const val DEFAULT_DAYS = 30
        private const val DENSE_DAYS = 365
        private const val SIGNED_MONTHS = 24
        private const val JUNE = 5

        // June 1 is a Monday; weekends dip.
        private val weekdayFactors = listOf(1.0, 1.03, 1.05, 1.04, 0.97, 0.8, 0.76)
    }

    override fun initialLineDataSet(): ChartData {
        val baseline =
            SampleSignals.trend(
                count = DEFAULT_DAYS,
                start = 12_400.0,
                end = 18_600.0,
                random = Random(11),
                noise = 380.0,
            )
        val values = baseline.mapIndexed { day, value -> value * weekdayFactors[day % weekdayFactors.size] }
        return SampleSignals
            .rounded(values)
            .toChartData(categories = SampleLabels.days(DEFAULT_DAYS, startMonth = JUNE), seriesName = TITLE)
    }

    override fun initialDenseLineDataSet(): ChartData {
        val random = Random(23)
        val weekly =
            SampleSignals.trend(
                count = DENSE_DAYS,
                start = 0.0,
                end = 0.0,
                random = random,
                cycleAmplitude = 70.0,
            )
        val values =
            SampleSignals
                .trend(
                    count = DENSE_DAYS,
                    start = 820.0,
                    end = 1_340.0,
                    random = random,
                    cycleAmplitude = 150.0,
                    cyclePeriod = DENSE_DAYS.toDouble(),
                    // Peaks in mid-December, lowest in mid-June.
                    cyclePhase = DENSE_DAYS * 0.3,
                    noise = 55.0,
                ).zip(weekly) { trend, cycle -> trend + cycle }
        return SampleSignals
            .rounded(values)
            .toChartData(categories = SampleLabels.days(DENSE_DAYS), seriesName = TITLE)
    }

    override fun initialSignedLineDataSet(): ChartData {
        val values =
            SampleSignals.trend(
                count = SIGNED_MONTHS,
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
                categories = SampleLabels.monthsWithYear(SIGNED_MONTHS, startYear = 2024),
                seriesName = TITLE,
            )
    }
}
