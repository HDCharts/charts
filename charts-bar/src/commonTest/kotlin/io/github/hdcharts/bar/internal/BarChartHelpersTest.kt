package io.github.hdcharts.bar.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.core.internal.axis.AxisXPlanRequest
import io.github.hdcharts.core.internal.axis.buildNumericYAxisTicks
import io.github.hdcharts.core.internal.axis.planAxisXLabels
import io.github.hdcharts.core.internal.axis.visibleIndexRange
import io.github.hdcharts.core.internal.axis.yAxisLabelColumnWidthPx
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.BarChartDefaults
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarChartHelpersTest {
    /** Bar and histogram draw one series, so their data is one series and a category list. */
    private fun barData(
        values: List<Double>,
        categories: List<String> = emptyList(),
    ) = ChartData(
        categories = categories,
        series = listOf(ChartSeries(name = "Series", values = values)),
    )

    private data class GetSelectedIndexTestCase(
        val position: Offset,
        val values: List<Double>,
        val size: IntSize,
        val spacingPx: Float,
        val expectedIndex: Int,
    )

    @Test
    fun getSelectedIndex_shouldReturnCorrectIndex() {
        // Arrange
        val testCases =
            listOf(
                GetSelectedIndexTestCase(
                    position = Offset(500.0F, 500.0F),
                    values = listOf(1.0, 2.0, 3.0, 4.0),
                    size = IntSize(1000, 1000),
                    spacingPx = 0f,
                    expectedIndex = 2,
                ),
                GetSelectedIndexTestCase(
                    position = Offset(900.0F, 900.0F),
                    values = listOf(100.0, 200.0, 300.0, 400.0, 500.0),
                    size = IntSize(1500, 1500),
                    spacingPx = 0f,
                    expectedIndex = 3,
                ),
                GetSelectedIndexTestCase(
                    position = Offset(400.0F, 600.0F),
                    values = listOf(50.0, 100.0, 150.0, 200.0, 250.0),
                    size = IntSize(800, 1200),
                    spacingPx = 0f,
                    expectedIndex = 2,
                ),
                GetSelectedIndexTestCase(
                    position = Offset(520.0F, 500.0F),
                    values = listOf(1.0, 2.0, 3.0, 4.0),
                    size = IntSize(1000, 1000),
                    spacingPx = 20f,
                    expectedIndex = 2,
                ),
            )

        testCases.forEach { testCase ->
            // Act
            val result =
                getSelectedIndex(
                    testCase.position,
                    testCase.values.size,
                    testCase.size,
                    testCase.spacingPx,
                )

            // Assert
            assertTrue { result == testCase.expectedIndex }
        }
    }

    @Test
    fun shouldUseScrollableDensity_resolvesFromThreshold() {
        assertEquals(expected = false, actual = shouldUseScrollableDensity(pointsCount = 49))
        assertEquals(expected = true, actual = shouldUseScrollableDensity(pointsCount = 50))
    }

    @Test
    fun shouldUseScrollableDensity_returnsFalseForEmptyData() {
        assertEquals(expected = false, actual = shouldUseScrollableDensity(pointsCount = 0))
    }

    @Test
    fun contentWidth_usesBarUnitAndSpacing() {
        val barWidth = 8f
        val spacing = 2f
        val unit = unitWidth(barWidthPx = barWidth, spacingPx = spacing)
        val width = contentWidth(dataSize = 5, unitWidthPx = unit, spacingPx = spacing)
        assertEquals(expected = 48f, actual = width)
    }

    @Test
    fun barEdges_adjacentFractionalBarsShareWholePixelEdges() {
        // 7 bins across 300px gives fractional bars; fractional edges leave anti-aliased seams.
        val barWidth = 300f / 7
        val lefts = (0 until 7).map { barLeftPx(index = it, barWidthPx = barWidth, spacingPx = 0f) }
        val rights = (0 until 7).map { barRightPx(index = it, barWidthPx = barWidth, spacingPx = 0f) }

        (lefts + rights).forEach { edge -> assertEquals(expected = round(edge), actual = edge) }
        assertEquals(expected = lefts.drop(1), actual = rights.dropLast(1))
        assertEquals(expected = 0f, actual = lefts.first())
        assertEquals(expected = 300f, actual = rights.last())
    }

    @Test
    fun barEdges_narrowTouchingBarsKeepExactEdges() {
        // Snapping 1.5px bins would make them alternate between 1px and 2px wide.
        assertEquals(expected = 4.5f, actual = barLeftPx(index = 3, barWidthPx = 1.5f, spacingPx = 0f))
        assertEquals(expected = 6f, actual = barRightPx(index = 3, barWidthPx = 1.5f, spacingPx = 0f))
    }

    @Test
    fun barEdges_subpixelSpacingKeepsExactEdges() {
        // Snapping half-pixel gaps would make them alternate between 0px and 1px.
        assertEquals(expected = 13.5f, actual = barLeftPx(index = 3, barWidthPx = 4f, spacingPx = 0.5f))
        assertEquals(expected = 17.5f, actual = barRightPx(index = 3, barWidthPx = 4f, spacingPx = 0.5f))
    }

    @Test
    fun barEdges_spacedBarsKeepExactEdges() {
        assertEquals(expected = 9f, actual = barLeftPx(index = 3, barWidthPx = 1.5f, spacingPx = 1.5f))
        assertEquals(expected = 10.5f, actual = barRightPx(index = 3, barWidthPx = 1.5f, spacingPx = 1.5f))
    }

    @Test
    fun maxBarsThatFit_calculatesCapacityFromMinBarWidthAndSpacing() {
        val capacity =
            maxBarsThatFit(
                viewportWidthPx = 200f,
                spacingPx = 10f,
                minBarWidthPx = 8f,
            )

        assertEquals(expected = 11, actual = capacity)
    }

    @Test
    fun maxBarsThatFit_returnsAtLeastOneForTinyViewport() {
        val capacity =
            maxBarsThatFit(
                viewportWidthPx = 0f,
                spacingPx = 10f,
                minBarWidthPx = 8f,
            )

        assertEquals(expected = 1, actual = capacity)
    }

    @Test
    fun maxBarsThatFit_clampsNegativeSpacingToZero() {
        val capacity =
            maxBarsThatFit(
                viewportWidthPx = 200f,
                spacingPx = -10f,
                minBarWidthPx = 8f,
            )

        assertEquals(expected = 25, actual = capacity)
    }

    @Test
    fun compactDensityCenterIndices_nonDense_returnsIdentityIndices() {
        val centers =
            compactDensityCenterIndices(
                sourcePointsCount = 8,
                targetPoints = 12,
            )

        assertEquals(expected = listOf(0, 1, 2, 3, 4, 5, 6, 7), actual = centers)
    }

    @Test
    fun compactDensityCenterIndices_dense_returnsBucketCenters() {
        val centers =
            compactDensityCenterIndices(
                sourcePointsCount = 10,
                targetPoints = 4,
            )

        assertEquals(expected = listOf(1, 4, 7, 9), actual = centers)
    }

    @Test
    fun compactDensity_capacityOne_aggregatesToOneBucketAndItsCenterSourceIndex() {
        val data = barData(values = List(10) { (it + 1).toDouble() }, categories = List(10) { "B$it" })

        val aggregated = aggregateForCompactDensity(data = data, targetPoints = 1)
        val centers = compactDensityCenterIndices(sourcePointsCount = 10, targetPoints = 1)

        assertEquals(expected = listOf(5.5), actual = aggregated.barValues)
        assertEquals(expected = listOf("B4"), actual = aggregated.categories.toList())
        assertEquals(expected = listOf(4), actual = centers)
    }

    @Test
    fun getSelectedIndexForContentX_clampsNegativeAndOverflowCoordinates() {
        val unit = unitWidth(barWidthPx = 8f, spacingPx = 2f)
        val leftClamped = getSelectedIndexForContentX(contentX = -100f, dataSize = 5, unitWidthPx = unit)
        val rightClamped = getSelectedIndexForContentX(contentX = 1_000f, dataSize = 5, unitWidthPx = unit)
        assertEquals(expected = 0, actual = leftClamped)
        assertEquals(expected = 4, actual = rightClamped)
    }

    @Test
    fun visibleIndexRange_invalidInputsReturnEmpty() {
        val noData =
            visibleIndexRange(
                dataSize = 0,
                viewportWidthPx = 100f,
                scrollOffsetPx = 0f,
                unitWidthPx = 10f,
            )
        val noViewport =
            visibleIndexRange(
                dataSize = 10,
                viewportWidthPx = 0f,
                scrollOffsetPx = 0f,
                unitWidthPx = 10f,
            )
        val noUnitWidth =
            visibleIndexRange(
                dataSize = 10,
                viewportWidthPx = 100f,
                scrollOffsetPx = 0f,
                unitWidthPx = 0f,
            )

        assertEquals(expected = IntRange.EMPTY, actual = noData)
        assertEquals(expected = IntRange.EMPTY, actual = noViewport)
        assertEquals(expected = IntRange.EMPTY, actual = noUnitWidth)
    }

    @Test
    fun visibleIndexRange_clampsOverscrollToLastIndex() {
        val range =
            visibleIndexRange(
                dataSize = 10,
                viewportWidthPx = 100f,
                scrollOffsetPx = 1_000f,
                unitWidthPx = 10f,
            )
        assertEquals(expected = 9..9, actual = range)
    }

    @Test
    fun axisValueFormatter_trimsRedundantZeros() {
        assertEquals(expected = "12", actual = BarChartDefaults.axisValueFormatter.format(12.0))
        assertEquals(expected = "12.5", actual = BarChartDefaults.axisValueFormatter.format(12.5))
        assertEquals(expected = "12.35", actual = BarChartDefaults.axisValueFormatter.format(12.345))
        assertEquals(expected = "0", actual = BarChartDefaults.axisValueFormatter.format(-0.0001))
    }

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
            val (min, max) = barData(values).resolveBarRange(null, null)
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
        val data = barData(listOf(16_777_216.0, 16_777_217.0))
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
        val values = barData(listOf(max, max, min, min), categories = listOf("A", "B", "C", "D"))
        assertEquals(listOf(max, min), aggregateForCompactDensity(values, 2).barValues)
        assertEquals(0.0, aggregateForCompactDensity(values, 1).barValues.single())
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
