package io.github.hdcharts.sampleshared.data.impl

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

/**
 * Builds sample series that look like real metrics: a trend, a repeating cycle, and noise.
 * Pass a seeded [Random] so the fixtures stay the same on every run.
 */
internal object SampleSignals {
    /**
     * Moves from [start] to [end] over [count] points. [curve] above 1 starts slow and
     * accelerates. [cycleAmplitude] adds a sine wave with [cyclePeriod] points per cycle.
     */
    fun trend(
        count: Int,
        start: Double,
        end: Double,
        random: Random,
        curve: Double = 1.0,
        cycleAmplitude: Double = 0.0,
        cyclePeriod: Double = 7.0,
        cyclePhase: Double = 0.0,
        noise: Double = 0.0,
    ): List<Double> {
        val steps = (count - 1).coerceAtLeast(1).toDouble()
        return List(count) { index ->
            val progress = (index / steps).pow(curve)
            val cycle = cycleAmplitude * sin(2 * PI * (index + cyclePhase) / cyclePeriod)
            val jitter = if (noise > 0.0) random.nextDouble(-noise, noise) else 0.0
            start + (end - start) * progress + cycle + jitter
        }
    }

    /**
     * Counts for [count] equal-width bins with a peak near [peakBin] and a long right tail,
     * the shape of request latencies or order sizes.
     */
    fun longTailCounts(
        count: Int,
        peakBin: Double,
        peak: Double,
        spread: Double,
        random: Random,
        noise: Double = 0.0,
    ): List<Double> {
        val peakCenter = ln(peakBin + 0.5)
        return List(count) { index ->
            val distance = ln(index + 0.5) - peakCenter
            val jitter = if (noise > 0.0) 1.0 + random.nextDouble(-noise, noise) else 1.0
            peak * exp(-(distance * distance) / (2 * spread * spread)) * jitter
        }
    }

    fun rounded(
        values: List<Double>,
        decimals: Int = 0,
    ): List<Double> {
        val factor = 10.0.pow(decimals)
        return values.map { value -> round(value * factor) / factor }
    }
}

/** Period labels for sample categories. */
internal object SampleLabels {
    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val monthLengths = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    /** "Jan", "Feb", … repeating after December. */
    fun months(count: Int): List<String> = List(count) { index -> months[index % months.size] }

    /** "Jan '24", "Feb '24", … */
    fun monthsWithYear(
        count: Int,
        startYear: Int,
    ): List<String> = List(count) { index -> "${months[index % 12]} '${shortYear(startYear + index / 12)}" }

    /** "Q1 '24", "Q2 '24", … */
    fun quarters(
        count: Int,
        startYear: Int,
    ): List<String> = List(count) { index -> "Q${index % 4 + 1} '${shortYear(startYear + index / 4)}" }

    /** "Jan 1", "Jan 2", … in a non-leap year, starting from [startMonth] (0 = January). */
    fun days(
        count: Int,
        startMonth: Int = 0,
    ): List<String> {
        val labels = ArrayList<String>(count)
        var month = startMonth
        var day = 1
        repeat(count) {
            labels += "${months[month % 12]} $day"
            day++
            if (day > monthLengths[month % 12]) {
                day = 1
                month++
            }
        }
        return labels
    }

    private fun shortYear(year: Int): String = (year % 100).toString().padStart(2, '0')
}
