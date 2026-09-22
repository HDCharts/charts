package io.github.hdcharts.app.gif

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Shared, generic value/label generators for the docs GIF scenario fixtures. Scenario-specific
 * data (which parameters, which title) lives with each scenario's own composable in
 * `io.github.hdcharts.app.gif.docs`; only the reusable math lives here.
 *
 * Each scenario uses 21 data points so the default `xLabels.count = 6`
 * produces a stride of exactly 4 between visible labels
 * (max count - 1 = 5; 20 / 5 = 4). Using 18 leaves a non-integer stride
 * (17 / 5 ≈ 3.4) that causes uneven spacing.
 */
internal object DocsGifScenariosData {
    internal const val POINTS = 21

    private val monthNames =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    /**
     * Repeats month abbreviations cyclically until [count] entries are
     * produced.
     */
    fun monthLabels(count: Int): List<String> = List(count) { monthNames[it % monthNames.size] }

    /**
     * Returns [count] week-shaped labels of the form `W<index + 1>`
     * (W1, W2, ...).
     */
    fun weekLabels(count: Int): List<String> = List(count) { "W${it + 1}" }

    /**
     * Returns [count] quarter-shaped labels of the form `Q<index + 1>`.
     */
    fun quarterLabels(count: Int): List<String> = List(count) { "Q${it + 1}" }

    /**
     * Builds histogram bucket labels of the form `0-25ms`, `25-50ms`,
     * ... `<(count-1)*stepMs>ms+` for the catch-all tail.
     */
    fun msBuckets(
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
    fun ramped(
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
    fun bent(
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
    fun jitter(
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
    fun normalizeMax(
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
    fun normalizeStacked(
        series: List<List<Double>>,
        targetMax: Double,
    ): List<List<Double>> {
        val maxSum = series[0].indices.maxOf { i -> series.sumOf { it[i] } }
        if (maxSum <= 0.0) return series
        val scale = targetMax / maxSum
        return series.map { col -> col.map { (it * scale).roundToInt().toDouble() } }
    }

    /** [ramped] jittered and normalized in one call, for a single independently-scaled series. */
    fun rampedSeries(
        count: Int,
        start: Double,
        end: Double,
        amplitude: Double,
        seed: Int,
        targetMax: Double,
        dip: Double? = null,
        dipStart: Int = 0,
        dipEnd: Int = count,
    ): List<Double> = normalizeMax(jitter(ramped(count, start, end, dip, dipStart, dipEnd), amplitude, seed), targetMax)

    /** [bent] jittered and normalized in one call, for a single independently-scaled series. */
    fun bentSeries(
        count: Int,
        firstStart: Double,
        firstEnd: Double,
        amplitude: Double,
        seed: Int,
        targetMax: Double,
        dip: Double = 0.5,
        dipStart: Int = count / 3,
        dipEnd: Int = 2 * count / 3,
        lastEnd: Double = firstEnd,
    ): List<Double> {
        val jittered = jitter(bent(count, firstStart, firstEnd, dip, dipStart, dipEnd, lastEnd), amplitude, seed)
        return normalizeMax(jittered, targetMax)
    }

    /** [ramped] jittered but left unnormalized, for a series later combined via [normalizeStacked]. */
    fun jitteredRamped(
        count: Int,
        start: Double,
        end: Double,
        amplitude: Double,
        seed: Int,
        dip: Double? = null,
        dipStart: Int = 0,
        dipEnd: Int = count,
    ): List<Double> = jitter(ramped(count, start, end, dip, dipStart, dipEnd), amplitude, seed)

    /** [bent] jittered but left unnormalized, for a series later combined via [normalizeStacked]. */
    fun jitteredBent(
        count: Int,
        firstStart: Double,
        firstEnd: Double,
        amplitude: Double,
        seed: Int,
        dip: Double = 0.5,
        dipStart: Int = count / 3,
        dipEnd: Int = 2 * count / 3,
        lastEnd: Double = firstEnd,
    ): List<Double> = jitter(bent(count, firstStart, firstEnd, dip, dipStart, dipEnd, lastEnd), amplitude, seed)
}
