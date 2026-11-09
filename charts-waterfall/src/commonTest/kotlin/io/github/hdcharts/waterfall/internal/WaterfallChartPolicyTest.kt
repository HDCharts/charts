package io.github.hdcharts.waterfall.internal

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.AxisInputs
import io.github.hdcharts.core.internal.AxisLabelValidationValues
import io.github.hdcharts.core.style.BarRangeStyle
import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry.Change
import io.github.hdcharts.waterfall.WaterfallEntry.Total
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WaterfallChartPolicyTest {
    private val labels = AxisLabelValidationValues(12.sp, null)
    private val inputs = WaterfallValidationInputs(AxisInputs(labels, labels), BarRangeStyle(null, null))
    private val density = Density(1f)

    @Test
    fun singleChangesZeroAndComputedTotalsAreValid() {
        val cases =
            listOf(
                listOf(Change("Positive", 5.0)),
                listOf(Change("Negative", -5.0)),
                listOf(Change("Zero", 0.0)),
                listOf(Total("Opening")),
                listOf(Total("First"), Total("Last")),
                listOf(Change("A", Double.MAX_VALUE), Change("B", -Double.MAX_VALUE), Total("Zero")),
            )
        cases.forEach { entries ->
            assertTrue(WaterfallChartPolicy.errorsFor(WaterfallData(entries), inputs, density).isEmpty())
        }
    }

    @Test
    fun allNonFiniteChangeAmountsAreRejected() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertEquals(listOf("Change amounts must be finite."), errorsFor(listOf(Change("Invalid", value))))
        }
    }

    @Test
    fun intermediateOverflowIsRejectedEvenWhenLaterChangesWouldCancelIt() {
        val data = listOf(Change("A", Double.MAX_VALUE), Change("B", Double.MAX_VALUE), Change("C", -Double.MAX_VALUE))
        assertEquals(listOf("Every running total must be finite."), errorsFor(data))
    }

    @Test
    fun negativeOverflowIsRejected() {
        assertEquals(
            listOf("Every running total must be finite."),
            errorsFor(listOf(Change("A", -Double.MAX_VALUE), Change("B", -Double.MAX_VALUE))),
        )
    }

    @Test
    fun nonFiniteFixedRangeIsRejected() {
        assertTrue(
            WaterfallChartPolicy
                .errorsFor(
                    WaterfallData(listOf(Total("Zero"))),
                    inputs.copy(range = BarRangeStyle(Double.NaN, null)),
                    density,
                ).isNotEmpty(),
        )
    }

    @Test
    fun invalidLabelCountsAreRejectedOnBothAxes() {
        val invalid = labels.copy(maxCount = 0)
        assertTrue(
            WaterfallChartPolicy
                .errorsFor(
                    WaterfallData(listOf(Total("Zero"))),
                    inputs.copy(axes = AxisInputs(invalid, invalid)),
                    density,
                ).isNotEmpty(),
        )
    }

    private fun errorsFor(entries: List<io.github.hdcharts.waterfall.WaterfallEntry>) =
        WaterfallChartPolicy.errorsFor(WaterfallData(entries), inputs, density)
}
