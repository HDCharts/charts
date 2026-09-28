package io.github.hdcharts.charts.unit.helpers

import io.github.hdcharts.charts.internal.barchart.aggregateForCompactDensity
import io.github.hdcharts.charts.internal.barchart.barValueYFraction
import io.github.hdcharts.charts.internal.barchart.baselineYForRange
import io.github.hdcharts.charts.internal.barchart.compactDensityRanges
import io.github.hdcharts.charts.internal.barchart.getSelectedIndexForContentX
import io.github.hdcharts.charts.internal.barchart.resolveBarRange
import io.github.hdcharts.charts.internal.barchart.unitWidth
import io.github.hdcharts.charts.internal.common.axis.AxisXPlanRequest
import io.github.hdcharts.charts.internal.common.axis.buildNumericYAxisTicks
import io.github.hdcharts.charts.internal.common.axis.planAxisXLabels
import io.github.hdcharts.charts.internal.common.axis.yAxisLabelColumnWidthPx
import io.github.hdcharts.charts.internal.common.model.toChartData
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.style.BarChartDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarChartGeometryTest {
    @Test
    fun automaticRanges_includeZeroAndMatchDrawingAndTicks() {
        val fixtures =
            listOf(
                listOf(5.0, 10.0) to (0.0 to 10.0),
                listOf(-10.0, -5.0) to (-10.0 to 0.0),
                listOf(-5.0, 10.0) to (-5.0 to 10.0),
                listOf(5.0, 5.0) to (0.0 to 5.0),
                listOf(0.0, 0.0) to (0.0 to 1.0),
            )
        fixtures.forEach { (values, expected) ->
            val (min, max) = values.toChartData().resolveBarRange(null, null)
            assertEquals(expected, min to max)
            val tickValues = mutableListOf<Double>()
            val ticks =
                buildNumericYAxisTicks(
                    min,
                    max,
                    5,
                    200f,
                    0f,
                    ChartValueFormatter {
                        tickValues.add(it)
                        it.toString()
                    },
                )
            ticks.zip(tickValues).forEach { (tick, value) ->
                assertEquals(tick.centerY, barValueYFraction(value, min, max).toFloat() * 200f, 0.001f)
            }
            assertEquals(barValueYFraction(0.0, min, max).toFloat() * 200f, baselineYForRange(min, max, 200f))
        }
    }

    @Test
    fun explicitRanges_clipAndRetainDoublePrecision() {
        val data = listOf(16_777_216.0, 16_777_217.0).toChartData()
        val (min, max) = data.resolveBarRange(16_777_216.0, 16_777_217.0)
        assertEquals(1.0, max - min)
        assertEquals(1.0, barValueYFraction(min - 100.0, min, max))
        assertEquals(0.0, barValueYFraction(max + 100.0, min, max))
        assertEquals(0.5, barValueYFraction(min + 0.5, min, max))
        assertEquals(0.0 to 16_777_217.0, data.resolveBarRange(3.0, 1.0))
        assertEquals(0.0 to 16_777_217.0, data.resolveBarRange(1.0, 1.0))
    }

    @Test
    fun extremeSignedRangesAndAverages_remainFinite() {
        val min = -Double.MAX_VALUE
        val max = Double.MAX_VALUE
        assertEquals(1.0, barValueYFraction(min, min, max))
        assertEquals(0.5, barValueYFraction(0.0, min, max))
        assertEquals(0.0, barValueYFraction(max, min, max))
        val ticks =
            buildNumericYAxisTicks(
                min,
                max,
                5,
                100f,
                0f,
                ChartValueFormatter {
                    assertTrue(it.isFinite())
                    it.toString()
                },
            )
        assertTrue(ticks.all { it.centerY.isFinite() })
        val values = listOf(max, max, min, min).toChartData(labels = listOf("A", "B", "C", "D"))
        assertEquals(listOf(max, min), aggregateForCompactDensity(values, 2).points)
        assertEquals(0.0, aggregateForCompactDensity(values, 1).points.single())
        assertEquals(0.0, barValueYFraction(Double.MIN_VALUE, 0.0, Double.MIN_VALUE))
    }

    @Test
    fun sourceBuckets_coverEveryIndexIncludingLastAndCapacityOne() {
        val ranges = compactDensityRanges(12, 3)
        assertEquals(listOf(0..3, 4..7, 8..11), ranges)
        assertEquals(2, ranges.indexOfFirst { 11 in it })
        assertEquals(listOf(0..11), compactDensityRanges(12, 1))
        assertEquals((0..11).toList(), ranges.flatMap { it.toList() })
    }

    @Test
    fun subpixelBins_keepCoordinatesAndLabelsWithinTheirActualDomain() {
        val unit = unitWidth(0.1f, 0f)
        assertEquals(0.1f, unit)
        assertEquals(17, getSelectedIndexForContentX(1.75f, 1000, unit))
        assertEquals(999, getSelectedIndexForContentX(99.95f, 1000, unit))
        val request =
            AxisXPlanRequest(
                dataSize = 1000,
                maxLabelCount = 6,
                isScrollable = false,
                unitWidthPx = unit,
                viewportWidthPx = 100f,
                minLabelSpacingPx = 10f,
            )
        val plan = planAxisXLabels(request = request, scrollOffsetPx = 0f)
        assertEquals(0..999, plan.visibleRange)
        assertTrue(plan.labelIndices.last() > 800)
        assertTrue(plan.labelIndices.zipWithNext().all { (a, b) -> (b - a) * unit >= 10f })
    }

    @Test
    fun longAxisLabels_reserveBoundedWholePixels() {
        val ticks = buildNumericYAxisTicks(0.0, 1e50, 5, 100f, 0f, BarChartDefaults.axisValueFormatter)
        val width = yAxisLabelColumnWidthPx(ticks.map { it.label }, 11f, 240)
        assertTrue(width <= 96f)
        assertEquals(width.toInt().toFloat(), width)
    }
}
