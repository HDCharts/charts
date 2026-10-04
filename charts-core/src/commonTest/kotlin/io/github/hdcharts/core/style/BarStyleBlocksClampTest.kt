package io.github.hdcharts.core.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.MAX_GRID_STEPS
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Each bar block clamps its own fields, so a field added to a block fails to compile until it is
 * dealt with. These pin what each one clamps; the chart-level `BarChartStyle.clamped` composes them
 * and adds nothing.
 */
class BarStyleBlocksClampTest {
    private val density = Density(2f)

    @Test
    fun bars_clampsAlphaAndSizes() {
        val clamped =
            BarBarsStyle(
                color = Color.Red,
                colors = persistentListOf(),
                alpha = Float.NaN,
                space = Dp.Infinity,
                minBarWidth = (-1f).dp,
            ).clamp(density)

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.space)
        assertEquals(expected = 0.dp, actual = clamped.minBarWidth)
    }

    @Test
    fun grid_clampsStepsAndLineWidth() {
        val clamped =
            BarGridStyle(
                visible = true,
                steps = Int.MAX_VALUE,
                color = Color.Gray,
                lineWidth = Dp.Unspecified,
            ).clamp(density)

        assertEquals(expected = MAX_GRID_STEPS, actual = clamped.steps)
        assertEquals(expected = StyleDefaults.lineWidth, actual = clamped.lineWidth)
    }

    @Test
    fun axis_clampsLineWidthAndPassesLabelsThrough() {
        val labels =
            AxisLabelStyle(visible = true, color = Color.Black, size = StyleDefaults.axisLabelSize, maxCount = null)
        val clamped =
            BarAxisStyle(
                visible = true,
                color = Color.Black,
                lineWidth = Float.NaN.dp,
                xLabels = labels,
                yLabels = labels,
            ).clamp(density)

        assertEquals(expected = StyleDefaults.lineWidth, actual = clamped.lineWidth)
        assertEquals(expected = labels, actual = clamped.xLabels)
        assertEquals(expected = labels, actual = clamped.yLabels)
    }

    @Test
    fun selection_clampsWidthAndUnselectedAlpha() {
        val clamped =
            BarSelectionStyle(
                visible = true,
                color = Color.Blue,
                width = (-2f).dp,
                unselectedAlpha = 1.5f,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.width)
        assertEquals(expected = 1f, actual = clamped.unselectedAlpha)
    }

    @Test
    fun range_hasNothingToClamp() {
        val clamped = BarRangeStyle(min = Double.NaN, max = Double.POSITIVE_INFINITY)

        assertEquals(expected = clamped, actual = clamped)
    }
}
