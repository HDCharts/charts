package io.github.hdcharts.stackedbar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Each stacked bar block clamps its own fields, so a field added to a block fails to compile until
 * it is dealt with. These pin what each one clamps.
 */
class StackedBarStyleBlocksClampTest {
    private val density = Density(2f)

    @Test
    fun segments_clampsAlpha() {
        val clamped =
            StackedBarSegmentStyle(
                color = Color.Blue,
                colors = persistentListOf(),
                alpha = -1f,
            ).clamp()

        assertEquals(expected = 0f, actual = clamped.alpha)
    }

    @Test
    fun layout_clampsSpaceAndMinBarWidth() {
        val clamped =
            StackedBarLayoutStyle(
                space = Dp.Infinity,
                minBarWidth = Float.NaN.dp,
            ).clamp(density)

        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.space)
        assertEquals(expected = StyleDefaults.minBarWidth, actual = clamped.minBarWidth)
    }

    @Test
    fun selection_clampsWidthAndUnselectedAlpha() {
        val clamped =
            StackedBarSelectionStyle(
                visible = true,
                color = Color.Blue,
                width = (-6f).dp,
                unselectedAlpha = 3f,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.width)
        assertEquals(expected = 1f, actual = clamped.unselectedAlpha)
    }
}
