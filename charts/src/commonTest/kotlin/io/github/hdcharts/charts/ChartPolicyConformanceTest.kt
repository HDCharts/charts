package io.github.hdcharts.charts

import io.github.hdcharts.bar.internal.BarChartSpec
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.gauge.internal.RingGaugeChartSpec
import io.github.hdcharts.histogram.internal.HistogramChartSpec
import io.github.hdcharts.line.internal.LineChartSpec
import io.github.hdcharts.pie.internal.PieChartSpec
import io.github.hdcharts.radar.internal.RadarChartSpec
import io.github.hdcharts.stackedarea.internal.StackedAreaChartSpec
import io.github.hdcharts.stackedbar.internal.StackedBarChartSpec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The policy table from `docs/wiki/dev/internals/entry-seam.md`, in test form. Each chart joins as it
 * moves onto the entry seam, one chart per change.
 *
 * The markdown table is maintained by hand alongside this file.
 */
@OptIn(InternalChartsApi::class)
class ChartPolicyConformanceTest {
    @Test
    fun declaredPoliciesMatchTheTable() {
        assertEquals(expected = EXPECTED_BY_CHART, actual = DECLARED_BY_CHART.mapValues { it.value.toRow() })
    }

    @Test
    fun everyChartOnTheSeamDeclaresARow() {
        assertEquals(expected = SEAM_CHARTS, actual = DECLARED_BY_CHART.keys)
    }

    /**
     * What the table records about each policy. [colors] is the rule rather than a value, because
     * `colorsMatch` is a function and two policies can declare the same behaviour two ways.
     */
    private data class PolicyRow(
        val minValues: Int,
        val allowNegative: Boolean,
        val singleSeries: Boolean,
        val hasAxis: Boolean,
        val hasFixedRange: Boolean,
        val colors: String,
    )

    private companion object {
        /** The charts on the seam. Add the next one here as it migrates. */
        val SEAM_CHARTS =
            setOf(
                "Line",
                "Bar",
                "Histogram",
                "Radar",
                "Stacked bar",
                "Stacked area",
                "Pie",
                "Ring gauge",
            )

        val DECLARED_BY_CHART =
            mapOf(
                "Line" to LineChartSpec.policy,
                "Bar" to BarChartSpec.policy,
                "Histogram" to HistogramChartSpec.policy,
                "Radar" to RadarChartSpec.policy,
                "Stacked bar" to StackedBarChartSpec.policy,
                "Stacked area" to StackedAreaChartSpec.policy,
                "Pie" to PieChartSpec.policy,
                "Ring gauge" to RingGaugeChartSpec.policy,
            )

        val EXPECTED_BY_CHART =
            mapOf(
                "Line" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_VALUES,
                        allowNegative = true,
                        singleSeries = false,
                        hasAxis = true,
                        hasFixedRange = true,
                        colors = "series count",
                    ),
                "Bar" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_VALUES,
                        allowNegative = true,
                        singleSeries = true,
                        hasAxis = true,
                        hasFixedRange = true,
                        colors = "value count",
                    ),
                "Histogram" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_VALUES,
                        allowNegative = false,
                        singleSeries = true,
                        hasAxis = true,
                        hasFixedRange = true,
                        colors = "value count",
                    ),
                "Radar" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_RADAR_VALUES,
                        allowNegative = true,
                        singleSeries = false,
                        hasAxis = false,
                        hasFixedRange = false,
                        colors = "series count, only with more than one series",
                    ),
                "Stacked bar" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_VALUES,
                        allowNegative = false,
                        singleSeries = false,
                        hasAxis = true,
                        hasFixedRange = false,
                        colors = "series count",
                    ),
                "Stacked area" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_VALUES,
                        allowNegative = false,
                        singleSeries = false,
                        hasAxis = true,
                        hasFixedRange = false,
                        colors = "series count",
                    ),
                "Pie" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_VALUES,
                        allowNegative = false,
                        singleSeries = true,
                        hasAxis = false,
                        hasFixedRange = false,
                        colors = "value count",
                    ),
                "Ring gauge" to
                    PolicyRow(
                        minValues = ValidationErrors.MIN_RING_GAUGE_VALUES,
                        allowNegative = true,
                        singleSeries = true,
                        hasAxis = false,
                        hasFixedRange = true,
                        colors = "value count",
                    ),
            )

        fun ChartPolicy.toRow() =
            PolicyRow(
                minValues = minValues,
                allowNegative = allowNegative,
                singleSeries = singleSeries,
                hasAxis = hasAxis,
                hasFixedRange = hasFixedRange,
                colors = describeColors(),
            )

        /**
         * A single-series chart matches its value count, which [ChartPolicy.singleSeries] already
         * says. For every other chart the rule is read off the declaration by asking it about one
         * series and about two.
         */
        fun ChartPolicy.describeColors(): String {
            val rule = colorsMatch
            return when {
                singleSeries -> "value count"
                rule == null -> "skipped"
                rule(ONE_SERIES) == null && rule(TWO_SERIES) == null -> "skipped"
                rule(ONE_SERIES) != null && rule(TWO_SERIES) != null -> "series count"
                else -> "series count, only with more than one series"
            }
        }

        val ONE_SERIES = chartDataOf(series = arrayOf(ChartSeries(name = "One", values = listOf(1.0, 2.0))))

        val TWO_SERIES =
            chartDataOf(
                series =
                    arrayOf(
                        ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                        ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
                    ),
            )
    }
}
