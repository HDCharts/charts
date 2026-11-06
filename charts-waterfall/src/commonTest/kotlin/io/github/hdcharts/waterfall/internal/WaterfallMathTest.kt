package io.github.hdcharts.waterfall.internal

import androidx.compose.ui.geometry.Offset
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.style.BarRangeStyle
import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry.Change
import io.github.hdcharts.waterfall.WaterfallEntry.Total
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WaterfallMathTest {
    private val automatic = BarRangeStyle(null, null)

    @Test
    fun mixedChangesFloatFromPreviousBalanceAndTotalsLeaveItUnchanged() {
        val data =
            WaterfallData(
                listOf(
                    Change("Sales", 100.0),
                    Change("Cost", -140.0),
                    Total("Loss"),
                    Change("Refund", 60.0),
                    Total("Net"),
                ),
            )
        assertEquals(
            listOf(
                WaterfallBar(0.0, 100.0),
                WaterfallBar(100.0, -40.0),
                WaterfallBar(0.0, -40.0),
                WaterfallBar(-40.0, 20.0),
                WaterfallBar(0.0, 20.0),
            ),
            waterfallBars(data),
        )
        assertEquals(20.0, data.runningTotalAt(4))
    }

    @Test
    fun firstAndConsecutiveTotalsShowTheComputedBalance() {
        val data = WaterfallData(listOf(Total("Start"), Change("Income", 30.0), Total("Subtotal"), Total("Final")))
        assertEquals(listOf(0.0, 30.0, 30.0, 30.0), waterfallBars(data).map { it.end })
        assertEquals(listOf(0.0, 0.0, 0.0, 0.0), waterfallBars(data).map { it.start })
    }

    @Test
    fun rangeIncludesIntermediatePeakAndTrough() {
        val bars =
            waterfallBars(
                WaterfallData(listOf(Change("A", 100.0), Change("B", -160.0), Change("C", 70.0), Total("End"))),
            )
        assertEquals(-60.0 to 100.0, waterfallRange(bars, automatic))
    }

    @Test
    fun allPositiveAndAllNegativeRangesIncludeZero() {
        assertEquals(0.0 to 75.0, waterfallRange(waterfallBars(changes(50.0, 25.0)), automatic))
        assertEquals(-75.0 to 0.0, waterfallRange(waterfallBars(changes(-50.0, -25.0)), automatic))
    }

    @Test
    fun zeroAndSingleTotalHaveDrawableRange() {
        assertEquals(0.0 to 1.0, waterfallRange(waterfallBars(changes(0.0)), automatic))
        assertEquals(listOf(WaterfallBar(0.0, 0.0)), waterfallBars(WaterfallData(listOf(Total("Total")))))
    }

    @Test
    fun tinyChangesKeepDoublePrecisionBeforePixelNormalization() {
        val data = changes(1e-12, -3e-12, 1e-12)
        assertEquals(-1e-12, data.runningTotalAt(2), 1e-27)
        val range = waterfallRange(waterfallBars(data), automatic)
        assertEquals(-2e-12, range.first, 1e-27)
        assertEquals(1e-12, range.second, 1e-27)
    }

    @Test
    fun extremeSignedBoundsMapToFinitePixelCoordinates() {
        val range = -Double.MAX_VALUE to Double.MAX_VALUE
        val layout = layout(count = 1)
        assertEquals(0f, layout.y(Double.MAX_VALUE, range))
        assertEquals(100f, layout.y(0.0, range))
        assertEquals(200f, layout.y(-Double.MAX_VALUE, range))
    }

    @Test
    fun explicitRangeUsesTheSharedBarRangeContract() {
        val bars = waterfallBars(changes(10.0))
        assertEquals(-5.0 to 20.0, waterfallRange(bars, BarRangeStyle(-5.0, 20.0)))
        assertEquals(0.0 to 10.0, waterfallRange(bars, BarRangeStyle(20.0, -5.0)))
    }

    @Test
    fun negativeAndCrossingBarsUseTheirActualFloatingBounds() {
        val layout = layout(count = 3)
        val range = -50.0 to 150.0
        val positive = layout.bounds(0, WaterfallBar(0.0, 100.0), range)
        val negative = layout.bounds(1, WaterfallBar(100.0, -50.0), range)
        val total = layout.bounds(2, WaterfallBar(0.0, -50.0), range)
        assertEquals(50f, positive.top)
        assertEquals(150f, positive.bottom)
        assertEquals(50f, negative.top)
        assertEquals(200f, negative.bottom)
        assertEquals(150f, total.top)
        assertEquals(200f, total.bottom)
        assertEquals(25f, positive.left)
        assertEquals(75f, positive.right)
        assertEquals(125f, negative.left)
    }

    @Test
    fun connectorLevelFollowsTheEndingBalanceForBothSignsAndTotals() {
        val bars =
            waterfallBars(WaterfallData(listOf(Change("A", 100.0), Change("B", -150.0), Total("C"), Change("D", 75.0))))
        val layout = layout(count = 4)
        val range = -50.0 to 150.0
        assertEquals(listOf(50f, 200f, 200f), bars.dropLast(1).map { layout.y(it.end, range) })
        assertEquals(200f, layout.bounds(3, bars[3], range).bottom)
    }

    @Test
    fun narrowSlotsShrinkBarsAndRetainSpace() {
        val layout = layout(count = 100)
        val first = layout.bounds(0, WaterfallBar(0.0, 1.0), 0.0 to 1.0)
        val next = layout.bounds(1, WaterfallBar(1.0, 2.0), 0.0 to 2.0)
        assertTrue(first.width > 0f)
        assertTrue(first.right < next.left)
        assertTrue(first.left >= layout.left)
    }

    @Test
    fun zeroChangeHasVisibleHeightAndCanBeSelectedAtBaseline() {
        val layout = layout(count = 1)
        val bounds = layout.bounds(0, WaterfallBar(0.0, 0.0), 0.0 to 1.0)
        assertEquals(1f, bounds.height)
        assertEquals(0, waterfallHitIndex(Offset(150f, 200f), listOf(bounds), 8f))
    }

    @Test
    fun hitTestingRejectsGapsAndBlankSpaceAboveFloatingBar() {
        val layout = layout(count = 3)
        val bounds =
            listOf(
                layout.bounds(0, WaterfallBar(50.0, 100.0), 0.0 to 200.0),
                layout.bounds(
                    1,
                    WaterfallBar(100.0, 50.0),
                    0.0 to 200.0,
                ),
            )
        assertEquals(0, waterfallHitIndex(Offset(50f, 125f), bounds, 0f))
        assertEquals(NO_SELECTION, waterfallHitIndex(Offset(100f, 125f), bounds, 0f))
        assertEquals(NO_SELECTION, waterfallHitIndex(Offset(50f, 20f), bounds, 0f))
    }

    @Test
    fun immutableDataSnapshotsCallerList() {
        val entries = mutableListOf<io.github.hdcharts.waterfall.WaterfallEntry>(Change("Opening", 10.0))
        val data = WaterfallData(entries)
        entries.clear()
        assertEquals(10.0, data.runningTotalAt(0))
        assertFailsWith<IllegalArgumentException> { data.runningTotalAt(1) }
    }

    private fun changes(vararg amounts: Double) =
        WaterfallData(amounts.mapIndexed { i, amount -> Change("$i", amount) })

    private fun layout(count: Int) = WaterfallPlotLayout(0f, 0f, 300f, 200f, count, 10f, 50f)
}
