package io.github.dautovicharis.charts.unit.helpers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.dautovicharis.charts.internal.barchart.YAxisTick
import io.github.dautovicharis.charts.internal.barchart.buildYAxisTicks
import io.github.dautovicharis.charts.internal.barchart.compactDensityCenterIndices
import io.github.dautovicharis.charts.internal.barchart.contentWidth
import io.github.dautovicharis.charts.internal.barchart.estimateXAxisLabelFootprintPx
import io.github.dautovicharis.charts.internal.barchart.estimateYAxisLabelWidthPx
import io.github.dautovicharis.charts.internal.barchart.formatAxisValue
import io.github.dautovicharis.charts.internal.barchart.getSelectedIndex
import io.github.dautovicharis.charts.internal.barchart.getSelectedIndexForContentX
import io.github.dautovicharis.charts.internal.barchart.maxBarsThatFit
import io.github.dautovicharis.charts.internal.barchart.sampledLabelIndices
import io.github.dautovicharis.charts.internal.barchart.scrollableLabelIndices
import io.github.dautovicharis.charts.internal.barchart.shouldUseScrollableDensity
import io.github.dautovicharis.charts.internal.barchart.unitWidth
import io.github.dautovicharis.charts.internal.barchart.visibleIndexRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarChartHelpersTest {
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
    fun getSelectedIndexForContentX_clampsNegativeAndOverflowCoordinates() {
        val unit = unitWidth(barWidthPx = 8f, spacingPx = 2f)
        val leftClamped = getSelectedIndexForContentX(contentX = -100f, dataSize = 5, unitWidthPx = unit)
        val rightClamped = getSelectedIndexForContentX(contentX = 1_000f, dataSize = 5, unitWidthPx = unit)
        assertEquals(expected = 0, actual = leftClamped)
        assertEquals(expected = 4, actual = rightClamped)
    }

    @Test
    fun sampledLabelIndices_withVisibleRangeIncludesEndpoints() {
        val sampled =
            sampledLabelIndices(
                dataSize = 120,
                maxCount = 6,
                visibleRange = 20..30,
            )

        assertEquals(expected = 20, actual = sampled.first())
        assertEquals(expected = 30, actual = sampled.last())
        assertTrue(sampled.size <= 6)
    }

    @Test
    fun sampledLabelIndices_maxCountBelowTwoStillReturnsTwoEndpoints() {
        val sampled = sampledLabelIndices(dataSize = 10, maxCount = 1)
        assertEquals(expected = listOf(0, 9), actual = sampled)
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
    fun scrollableLabelIndices_includesFirstAtLeftBoundary() {
        val indices =
            scrollableLabelIndices(
                dataSize = 120,
                maxCount = 6,
                visibleRange = 0..12,
            )
        assertEquals(expected = 0, actual = indices.first())
    }

    @Test
    fun scrollableLabelIndices_includesLastAtRightBoundary() {
        val indices =
            scrollableLabelIndices(
                dataSize = 120,
                maxCount = 6,
                visibleRange = 108..119,
            )
        assertEquals(expected = 119, actual = indices.last())
    }

    @Test
    fun scrollableLabelIndices_returnsStrictlyIncreasingDistinctIndices() {
        val indices =
            scrollableLabelIndices(
                dataSize = 120,
                maxCount = 6,
                visibleRange = 24..67,
            )

        assertEquals(expected = indices.distinct().size, actual = indices.size)
        assertTrue(indices.zipWithNext().all { (left, right) -> left < right })
    }

    @Test
    fun scrollableLabelIndices_densityBoundedRelativeToMaxCount() {
        val maxCount = 6
        val indices =
            scrollableLabelIndices(
                dataSize = 120,
                maxCount = maxCount,
                visibleRange = 20..30,
            )
        assertTrue(indices.size <= maxCount + 2)
    }

    @Test
    fun buildYAxisTicks_includesBothRangeEndpoints() {
        val ticks =
            buildYAxisTicks(
                minValue = -10.0,
                maxValue = 30.0,
                labelCount = 5,
                chartHeightPx = 200f,
            )

        assertEquals(expected = 5, actual = ticks.size)
        assertEquals(expected = "30", actual = ticks.first().label)
        assertEquals(expected = "-10", actual = ticks.last().label)
        assertEquals(expected = 0f, actual = ticks.first().centerY)
        assertEquals(expected = 200f, actual = ticks.last().centerY)
    }

    @Test
    fun formatAxisValue_trimsRedundantZeros() {
        assertEquals(expected = "12", actual = formatAxisValue(12.0))
        assertEquals(expected = "12.5", actual = formatAxisValue(12.5))
        assertEquals(expected = "12.35", actual = formatAxisValue(12.345))
        assertEquals(expected = "0", actual = formatAxisValue(-0.0001))
    }

    @Test
    fun estimateXAxisLabelFootprintPx_usesLongestResolvedLabel() {
        val footprint =
            estimateXAxisLabelFootprintPx(
                labels = listOf("Jan", "", "September"),
                dataSize = 3,
                fontSizePx = 10f,
                tiltDegrees = 0f,
            )

        assertTrue(kotlin.math.abs(footprint.width - 52.2f) < 0.001f)
        assertTrue(kotlin.math.abs(footprint.height - 12f) < 0.001f)
    }

    @Test
    fun estimateXAxisLabelFootprintPx_withTiltIncreasesHeightAndReducesWidth() {
        val horizontal =
            estimateXAxisLabelFootprintPx(
                labels = listOf("September"),
                dataSize = 1,
                fontSizePx = 10f,
                tiltDegrees = 0f,
            )
        val tilted =
            estimateXAxisLabelFootprintPx(
                labels = listOf("September"),
                dataSize = 1,
                fontSizePx = 10f,
                tiltDegrees = 45f,
            )

        assertTrue(tilted.height > horizontal.height)
        assertTrue(tilted.width < horizontal.width)
    }

    @Test
    fun estimateYAxisLabelWidthPx_usesLongestTickLabelWithoutPadding() {
        val width =
            estimateYAxisLabelWidthPx(
                ticks =
                    listOf(
                        YAxisTick(label = "5", centerY = 0f),
                        YAxisTick(label = "-123.45", centerY = 50f),
                    ),
                fontSizePx = 10f,
            )

        assertTrue(kotlin.math.abs(width - 40.6f) < 0.001f)
    }

    @Test
    fun estimateYAxisLabelWidthPx_emptyTicksReturnsZero() {
        val width =
            estimateYAxisLabelWidthPx(
                ticks = emptyList(),
                fontSizePx = 10f,
            )

        assertEquals(expected = 0f, actual = width)
    }
}
