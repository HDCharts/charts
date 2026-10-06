package io.github.hdcharts.gauge

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Each ring gauge block clamps its own fields, so a field added to a block fails to compile until it
 * is dealt with. These pin what each one clamps.
 */
class RingGaugeStyleBlocksClampTest {
    private val density = Density(2f)

    @Test
    fun range_keepsAnAscendingRange() {
        assertEquals(
            expected = GaugeRangeStyle(min = -20.0, max = 40.0),
            actual = GaugeRangeStyle(min = -20.0, max = 40.0).clamp(),
        )
    }

    @Test
    fun range_fallsBackToTheDefaultRangeWhenEqualOrReversed() {
        val default = GaugeRangeStyle(min = StyleDefaults.gaugeRangeMin, max = StyleDefaults.gaugeRangeMax)

        assertEquals(expected = default, actual = GaugeRangeStyle(min = 50.0, max = 50.0).clamp())
        assertEquals(expected = default, actual = GaugeRangeStyle(min = 80.0, max = 20.0).clamp())
    }

    @Test
    fun rings_clampsAlphaAndSizes() {
        val clamped =
            RingGaugeRingsStyle(
                alpha = 2f,
                baseColor = Color.Red,
                colors = persistentListOf(),
                width = Dp.Infinity,
                spacing = (-4).dp,
            ).clamp(density)

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.width)
        assertEquals(expected = 0.dp, actual = clamped.spacing)
    }

    @Test
    fun labels_fallsBackForAnUndrawableTextSize() {
        val clamped = GaugeLabelsStyle(visible = true, color = Color.Black, size = TextUnit.Unspecified).clamp(density)

        assertEquals(expected = StyleDefaults.axisLabelSize, actual = clamped.size)
    }
}
