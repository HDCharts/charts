package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.sampleshared.data.MultiLineSampleData
import io.github.hdcharts.sampleshared.data.MultiLineSampleUseCase
import kotlin.random.Random

internal class DefaultMultiLineSampleUseCase : MultiLineSampleUseCase {
    companion object {
        private const val TITLE = "Revenue by Channel"
        private const val DAYS = 365
        private const val MONTHS = 12
        private const val DAYS_PER_MONTH = DAYS / MONTHS.toDouble()

        // January 1, 2025 is a Wednesday; index 0 is Monday.
        private const val FIRST_WEEKDAY = 2
        private const val DAILY_NOISE = 0.04
    }

    // Revenue in $K. Mobile overtakes Retail in spring and the Web Store in October;
    // Web, Mobile, and Retail all lift for the holidays.
    private val multiLineItems =
        listOf(
            "Web Store" to listOf(420.0, 405.0, 440.0, 452.0, 468.0, 455.0, 470.0, 488.0, 510.0, 546.0, 640.0, 720.0),
            "Mobile App" to listOf(180.0, 205.0, 238.0, 262.0, 301.0, 338.0, 372.0, 410.0, 468.0, 552.0, 668.0, 781.0),
            "Retail" to listOf(390.0, 372.0, 380.0, 351.0, 342.0, 318.0, 305.0, 296.0, 288.0, 301.0, 355.0, 402.0),
            "Partners" to listOf(150.0, 162.0, 171.0, 168.0, 185.0, 197.0, 204.0, 216.0, 228.0, 236.0, 251.0, 268.0),
        )

    private val heroItems =
        listOf(
            "Web Store" to listOf(420.0, 510.0, 480.0, 530.0, 560.0, 590.0),
            "Mobile App" to listOf(360.0, 420.0, 410.0, 460.0, 500.0, 540.0),
            "Partner Sales" to listOf(280.0, 320.0, 340.0, 360.0, 390.0, 420.0),
        )
    private val heroCategories = listOf("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6")

    // Weekend share of a weekday's revenue per channel: stores gain on weekends, partners drop.
    private val weekendFactors = listOf(0.85, 1.12, 1.3, 0.6)

    override fun initialMultiLineSample(): MultiLineSampleData =
        multiLineSample(multiLineItems, SampleLabels.months(MONTHS))

    override fun initialMultiLineNoCategoriesSample(): MultiLineSampleData =
        multiLineSample(multiLineItems, emptyList())

    override fun initialHeroSample(): MultiLineSampleData = multiLineSample(heroItems, heroCategories)

    /** The monthly story spread over each day of 2025, with a weekly cycle per channel. */
    override fun initialDenseMultiLineSample(): MultiLineSampleData {
        val random = Random(67)
        val series =
            multiLineItems.zip(weekendFactors) { (name, monthly), weekendFactor ->
                val values =
                    List(DAYS) { day ->
                        val weekday = (day + FIRST_WEEKDAY) % 7
                        val weekFactor = if (weekday >= 5) weekendFactor else 1.0
                        val jitter = 1.0 + random.nextDouble(-DAILY_NOISE, DAILY_NOISE)
                        monthlyValueAt(monthly, day) / DAYS_PER_MONTH * weekFactor * jitter
                    }
                name to SampleSignals.rounded(values, decimals = 1)
            }
        return multiLineSample(series, SampleLabels.days(DAYS))
    }

    private fun multiLineSample(
        items: List<Pair<String, List<Double>>>,
        categories: List<String>,
    ): MultiLineSampleData =
        MultiLineSampleData(
            dataSet = items.toChartData(categories = categories),
            seriesKeys = items.map { it.first },
            title = TITLE,
        )

    // Interpolates between month midpoints so the daily line has no steps at month boundaries.
    private fun monthlyValueAt(
        monthly: List<Double>,
        day: Int,
    ): Double {
        val position = ((day + 0.5) / DAYS_PER_MONTH - 0.5).coerceIn(0.0, MONTHS - 1.0)
        val lower = position.toInt()
        val upper = (lower + 1).coerceAtMost(MONTHS - 1)
        val fraction = position - lower
        return monthly[lower] + (monthly[upper] - monthly[lower]) * fraction
    }
}
