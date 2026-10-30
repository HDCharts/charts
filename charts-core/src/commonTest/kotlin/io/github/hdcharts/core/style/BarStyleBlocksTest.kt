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
import kotlin.test.assertTrue

class BarStyleBlocksTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)
    private val density = Density(2f)

    @Test
    fun bars_emptyColors_repeatColor() {
        assertEquals(List(4) { base }, barsStyle(colors = emptyList()).resolveColors(4))
    }

    @Test
    fun bars_explicitColors_areReturned() {
        assertEquals(explicit, barsStyle(colors = explicit).resolveColors(3))
    }

    @Test
    fun bars_nonPositiveCount_returnsEmpty() {
        assertTrue(barsStyle(colors = emptyList()).resolveColors(0).isEmpty())
    }

    @Test
    fun bars_clampsAlphaAndSizes() {
        val clamped =
            BarBarsStyle(
                color = Color.Red,
                colors = persistentListOf(),
                gradient = ChartGradients.fade(endAlpha = 2f),
                alpha = Float.NaN,
                space = Dp.Infinity,
                minBarWidth = (-1f).dp,
            ).clamp(density)

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = ChartGradients.fade(endAlpha = 1f), actual = clamped.gradient)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.space)
        assertEquals(expected = 0.dp, actual = clamped.minBarWidth)
    }

    @Test
    fun grid_clampsStepsAndLineWidth() {
        val clamped =
            GridStyle(
                visible = true,
                steps = Int.MAX_VALUE,
                color = Color.Gray,
                lineWidth = Dp.Unspecified,
            ).clamp(density)

        assertEquals(expected = MAX_GRID_STEPS, actual = clamped.steps)
        assertEquals(expected = StyleDefaults.lineWidth, actual = clamped.lineWidth)
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

    private fun barsStyle(colors: List<Color>): BarBarsStyle =
        BarBarsStyle(color = base, colors = colors, gradient = null, alpha = 0.4f, space = 10.dp, minBarWidth = 10.dp)
}
