package io.github.hdcharts.stackedarea

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Each stacked area block clamps its own fields, so a field added to a block fails to compile until
 * it is dealt with. These pin what each one clamps.
 */
class StackedAreaStyleBlocksClampTest {
    private val density = Density(2f)

    @Test
    fun fill_clampsAlpha() {
        val clamped =
            StackedAreaFillStyle(
                color = Color.Blue,
                colors = persistentListOf(),
                alpha = Float.NaN,
                bezier = true,
            ).clamp()

        assertEquals(expected = 1f, actual = clamped.alpha)
    }

    @Test
    fun selection_clampsWidthAndUnselectedAlpha() {
        val clamped =
            StackedAreaSelectionStyle(
                visible = true,
                color = Color.Blue,
                width = (-8f).dp,
                unselectedAlpha = 0.5f,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.width)
        assertEquals(expected = 0.5f, actual = clamped.unselectedAlpha)
    }
}
