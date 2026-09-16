package io.github.hdcharts.app.gif

import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Shared data definitions for the docs GIF scenarios and the matching
 * landscape screenshot tests. Values are designed so the chart's y-axis
 * max equals `yAxisLabelCount * step` with `step` an integer.
 *
 * Each scenario uses 21 data points so the default `xLabels.count = 6`
 * produces a stride of exactly 4 between visible labels
 * (max count - 1 = 5; 20 / 5 = 4). Using 18 leaves a non-integer stride
 * (17 / 5 ≈ 3.4) that causes uneven spacing.
 */
internal object DocsGifScenariosData {
    internal const val BAR_TITLE = "Daily Net Cash Flow"
    internal const val LINE_TITLE = "Daily Support Tickets"
    internal const val MULTI_LINE_TITLE = "Weekly Revenue by Channel"
    internal const val HISTOGRAM_TITLE = "Request Duration Distribution"
    internal const val STACKED_BAR_TITLE = "Quarterly Revenue by Channel"
    internal const val STACKED_AREA_TITLE = "Monthly Active Subscribers by Plan"

    private const val POINTS = 21

    internal data class SingleSeriesScenario(
        val categories: List<String>,
        val values: List<Double>,
    )

    internal data class MultiSeriesScenario(
        val categories: List<String>,
        val items: List<Pair<String, List<Double>>>,
    )

    private val monthNames =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    /**
     * Repeats month abbreviations cyclically until [count] entries are
     * produced.
     */
    private fun monthLabels(count: Int): List<String> = List(count) { monthNames[it % monthNames.size] }

    /**
     * Returns [count] week-shaped labels of the form `W<index + 1>`
     * (W1, W2, ...).
     */
    private fun weekLabels(count: Int): List<String> = List(count) { "W${it + 1}" }

    /**
     * Returns [count] quarter-shaped labels of the form `Q<index + 1>`.
     */
    private fun quarterLabels(count: Int): List<String> = List(count) { "Q${it + 1}" }

    /**
     * Builds histogram bucket labels of the form `0-25ms`, `25-50ms`,
     * ... `<(count-1)*stepMs>ms+` for the catch-all tail.
     */
    private fun msBuckets(
        count: Int,
        stepMs: Int,
    ): List<String> {
        val bounds = List(count) { i -> "${i * stepMs}-${(i + 1) * stepMs}ms" }
        return bounds.dropLast(1) + listOf("${(count - 1) * stepMs}ms+")
    }

    /**
     * Integer ramp from [start] to [end] over [count] points. If [dip]
     * is provided, points whose index lies in `[[dipStart], [dipEnd])`
     * are scaled by `dip`. Pure: same inputs always produce the same
     * integer list.
     */
    private fun ramped(
        count: Int,
        start: Double,
        end: Double,
        dip: Double? = null,
        dipStart: Int = 0,
        dipEnd: Int = count,
    ): List<Double> {
        val base = List(count) { i -> start + (end - start) * i.toDouble() / (count - 1).toDouble() }
        if (dip == null) return base.map { it.roundToInt().toDouble() }
        return base.mapIndexed { i, v ->
            (if (i in dipStart until dipEnd) v * dip else v).roundToInt().toDouble()
        }
    }

    /**
     * Three-segment ramp: rise to [firstEnd], dip to `firstEnd * [dip]`,
     * then recover to [lastEnd]. Indices `[0, [dipStart])` rise; `[dipStart..dipEnd)`
     * dip; `[dipEnd..count)` recover. Returns an integer list of length [count].
     */
    private fun bent(
        count: Int,
        firstStart: Double,
        firstEnd: Double,
        dip: Double = 0.5,
        dipStart: Int = (count / 3),
        dipEnd: Int = (2 * count / 3),
        lastEnd: Double = firstEnd,
    ): List<Double> {
        val rising = ramped(count = dipStart, start = firstStart, end = firstEnd)
        val dipRamp = ramped(count = dipEnd - dipStart + 1, start = firstEnd, end = firstEnd * dip)
        val recovering = ramped(count = count - dipEnd, start = firstEnd * dip, end = lastEnd)
        return rising + dipRamp.drop(1) + recovering
    }

    /**
     * Adds deterministic integer jitter to a ramped series while keeping
     * the overall trend. The noise is the sum of three sines at different
     * frequencies with phases driven by [seed], so the same inputs always
     * produce the same output (no [Math.random], no clock). Output is
     * rounded to integer; values stay non-negative.
     */
    private fun jitter(
        values: List<Double>,
        amplitude: Double,
        seed: Int,
    ): List<Double> {
        val ph1 = (seed * 2.71) % (2 * PI)
        val ph2 = (seed * 5.13 + 1.2) % (2 * PI)
        return values.mapIndexed { i, v ->
            val progress = i.toDouble() / (values.size - 1).toDouble()
            val weight = 0.4 + 0.6 * sin(PI * progress)
            val wobble =
                weight *
                    (
                        amplitude * sin((2 * PI / 5.0) * (i + seed)) +
                            amplitude * 0.6 * sin((2 * PI / 3.0) * i + ph1) +
                            amplitude * 0.4 * sin((2 * PI * i.toDouble() / values.size) * 2 + ph2)
                    )
            (v + wobble).coerceAtLeast(0.0).roundToInt().toDouble()
        }
    }

    /**
     * Scales [values] uniformly so the maximum index lands exactly on
     * [targetMax]. Keeps relative shape intact while guaranteeing the
     * y-axis ticks can be placed at integer steps of `(targetMax /
     * (labelCount - 1))`.
     */
    private fun normalizeMax(
        values: List<Double>,
        targetMax: Double,
    ): List<Double> {
        val actualMax = values.max()
        if (actualMax <= 0.0) return values
        val scale = targetMax / actualMax
        return values.map { (it * scale).roundToInt().toDouble() }
    }

    /**
     * Scales a stack of independent series uniformly so that the maximum
     * column sum equals [targetMax]. Used by stacked-bar and stacked-area
     * so the per-point y-axis max lands on the chosen tick-friendly
     * integer.
     */
    private fun normalizeStacked(
        series: List<List<Double>>,
        targetMax: Double,
    ): List<List<Double>> {
        val maxSum = series[0].indices.maxOf { i -> series.sumOf { it[i] } }
        if (maxSum <= 0.0) return series
        val scale = targetMax / maxSum
        return series.map { col -> col.map { (it * scale).roundToInt().toDouble() } }
    }

    fun bar(): SingleSeriesScenario =
        SingleSeriesScenario(
            categories = monthLabels(POINTS),
            values =
                normalizeMax(
                    jitter(ramped(POINTS, start = 80.0, end = 280.0), amplitude = 18.0, seed = 7),
                    targetMax = 280.0,
                ),
        )

    fun line(): SingleSeriesScenario =
        SingleSeriesScenario(
            categories = weekLabels(POINTS),
            values =
                normalizeMax(
                    jitter(ramped(POINTS, start = 30.0, end = 360.0), amplitude = 45.0, seed = 19),
                    targetMax = 360.0,
                ),
        )

    fun multiLine(): MultiSeriesScenario =
        MultiSeriesScenario(
            categories = weekLabels(POINTS),
            items =
                listOf(
                    "Web Store" to
                        normalizeMax(
                            jitter(ramped(POINTS, 180.0, 720.0, dip = 0.85, dipStart = 7, dipEnd = 8), 25.0, 13),
                            targetMax = 720.0,
                        ),
                    "Mobile App" to
                        normalizeMax(
                            jitter(ramped(POINTS, 120.0, 580.0, dip = 0.92, dipStart = 6, dipEnd = 7), 20.0, 17),
                            targetMax = 580.0,
                        ),
                    "Partner Sales" to
                        normalizeMax(
                            jitter(ramped(POINTS, 60.0, 340.0, dip = 1.0, dipStart = 9, dipEnd = 12), 12.0, 23),
                            targetMax = 340.0,
                        ),
                ),
        )

    fun histogram(): SingleSeriesScenario =
        SingleSeriesScenario(
            categories = msBuckets(POINTS, 25),
            values =
                normalizeMax(
                    jitter(
                        bent(
                            count = POINTS,
                            firstStart = 4.0,
                            firstEnd = 80.0,
                            dip = 0.75,
                            dipStart = 10,
                            dipEnd = 18,
                            lastEnd = 36.0,
                        ),
                        amplitude = 4.0,
                        seed = 31,
                    ),
                    targetMax = 80.0,
                ),
        )

    fun stackedBar(): MultiSeriesScenario {
        val (online, retail, wholesale) =
            normalizeStacked(
                listOf(
                    jitter(ramped(POINTS, 280.0, 1080.0), 18.0, 41),
                    jitter(ramped(POINTS, 480.0, 1400.0), 25.0, 53),
                    jitter(
                        bent(POINTS, 360.0, 760.0, dip = 0.55, dipStart = 9, dipEnd = 12, lastEnd = 720.0),
                        30.0,
                        67,
                    ),
                ),
                targetMax = 3200.0,
            )
        return MultiSeriesScenario(
            categories = quarterLabels(POINTS),
            items =
                listOf(
                    "Online" to online,
                    "Retail" to retail,
                    "Wholesale" to wholesale,
                ),
        )
    }

    fun stackedArea(): MultiSeriesScenario {
        val (freePlan, standardPlan, premiumPlan) =
            normalizeStacked(
                listOf(
                    jitter(ramped(POINTS, 240.0, 880.0), 25.0, 71),
                    jitter(ramped(POINTS, 100.0, 700.0), 30.0, 83),
                    jitter(bent(POINTS, 60.0, 660.0, dip = 0.7, dipStart = 9, dipEnd = 12, lastEnd = 360.0), 25.0, 89),
                ),
                targetMax = 1600.0,
            )
        return MultiSeriesScenario(
            categories = monthLabels(POINTS),
            items =
                listOf(
                    "Free Plan" to freePlan,
                    "Standard Plan" to standardPlan,
                    "Premium Plan" to premiumPlan,
                ),
        )
    }

    fun buildSingleSeries(
        scenario: SingleSeriesScenario,
        seriesName: String,
    ): ChartData =
        scenario.values.toChartData(
            categories = scenario.categories,
            seriesName = seriesName,
        )
}
