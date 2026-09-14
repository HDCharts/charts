package io.github.hdcharts.charts.unit.helpers

import io.github.hdcharts.charts.internal.common.axis.buildNumericYAxisTicks
import io.github.hdcharts.charts.internal.common.axis.centeredLabelIndexRange
import io.github.hdcharts.charts.internal.common.axis.sampledLabelIndices
import io.github.hdcharts.charts.internal.common.axis.scrollableLabelIndices
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AxisHelpersTest {
    @Test
    fun buildNumericYAxisTicks_appliesInsetToFirstAndLastTick() {
        val ticks =
            buildNumericYAxisTicks(
                minValue = 0.0,
                maxValue = 100.0,
                labelCount = 5,
                plotHeightPx = 200f,
                verticalInsetPx = 10f,
            )

        assertEquals(expected = 5, actual = ticks.size)
        assertEquals(expected = "100", actual = ticks.first().label)
        assertEquals(expected = "0", actual = ticks.last().label)
        assertEquals(expected = 10f, actual = ticks.first().centerY)
        assertEquals(expected = 190f, actual = ticks.last().centerY)
    }

    @Test
    fun buildNumericYAxisTicks_withoutInset_usesPlotBoundaries() {
        val ticks =
            buildNumericYAxisTicks(
                minValue = -10.0,
                maxValue = 30.0,
                labelCount = 5,
                plotHeightPx = 200f,
            )

        assertEquals(expected = 5, actual = ticks.size)
        assertEquals(expected = "30", actual = ticks.first().label)
        assertEquals(expected = "-10", actual = ticks.last().label)
        assertEquals(expected = 0f, actual = ticks.first().centerY)
        assertEquals(expected = 200f, actual = ticks.last().centerY)
    }

    @Test
    fun buildNumericYAxisTicks_nonPositiveHeight_returnsEmpty() {
        val zeroHeight =
            buildNumericYAxisTicks(
                minValue = 0.0,
                maxValue = 10.0,
                labelCount = 3,
                plotHeightPx = 0f,
            )
        val negativeHeight =
            buildNumericYAxisTicks(
                minValue = 0.0,
                maxValue = 10.0,
                labelCount = 3,
                plotHeightPx = -1f,
            )

        assertTrue(zeroHeight.isEmpty())
        assertTrue(negativeHeight.isEmpty())
    }

    @Test
    fun centeredLabelIndexRange_excludesEdgeIndicesWhenLabelWouldClip() {
        val range =
            centeredLabelIndexRange(
                dataSize = 120,
                unitWidthPx = 10f,
                viewportWidthPx = 1190f,
                scrollOffsetPx = 0f,
                firstCenterPx = 0f,
                labelWidthPx = 80f,
                edgePaddingPx = 4f,
            )

        assertEquals(expected = 5..114, actual = range)
    }

    @Test
    fun sampledLabelIndices_emptyVisibleRange_returnsEmpty() {
        val sampled =
            sampledLabelIndices(
                dataSize = 20,
                maxCount = 6,
                visibleRange = IntRange.EMPTY,
            )

        assertTrue(sampled.isEmpty())
    }

    @Test
    fun scrollableLabelIndices_adjacentVisibleWindows_keepStableStride() {
        val compactWindow =
            scrollableLabelIndices(
                dataSize = 120,
                maxCount = 6,
                visibleRange = 20..31,
            )
        val expandedWindow =
            scrollableLabelIndices(
                dataSize = 120,
                maxCount = 6,
                visibleRange = 20..32,
            )

        val compactStep = compactWindow.zipWithNext().first().second - compactWindow.first()
        val expandedStep = expandedWindow.zipWithNext().first().second - expandedWindow.first()

        assertEquals(expected = 2, actual = compactStep)
        assertEquals(expected = 2, actual = expandedStep)
    }

    @Test
    fun scrollableLabelIndices_withStableVisibleCount_preventsStrideFlip() {
        val firstWindow =
            scrollableLabelIndices(
                dataSize = 200,
                maxCount = 3,
                visibleRange = 109..115,
                stableVisibleCount = 7,
            )
        val secondWindow =
            scrollableLabelIndices(
                dataSize = 200,
                maxCount = 3,
                visibleRange = 109..116,
                stableVisibleCount = 7,
            )

        val firstStep = firstWindow.zipWithNext().first().second - firstWindow.first()
        val secondStep = secondWindow.zipWithNext().first().second - secondWindow.first()

        assertEquals(expected = 2, actual = firstStep)
        assertEquals(expected = 2, actual = secondStep)
    }

    @Test
    fun scrollableLabelIndices_nearRightWindow_doesNotInjectDatasetLastEdge() {
        val indices =
            scrollableLabelIndices(
                dataSize = 274,
                maxCount = 6,
                visibleRange = 259..271,
                stableVisibleCount = 16,
            )

        assertTrue(indices.isNotEmpty())
        assertTrue(273 !in indices)
    }
}
