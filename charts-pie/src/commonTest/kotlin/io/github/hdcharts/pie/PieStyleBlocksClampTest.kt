package io.github.hdcharts.pie

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.DONUT_MAX_PERCENTAGE
import io.github.hdcharts.core.internal.DONUT_MIN_PERCENTAGE
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Each pie block clamps its own fields, so a field added to a block fails to compile until it is
 * dealt with. These pin what each one clamps.
 */
class PieStyleBlocksClampTest {
    private val density = Density(2f)

    @Test
    fun donut_clampsHolePercentageAndFallsBackForNaN() {
        assertEquals(
            expected = StyleDefaults.pieDonutHole,
            actual = PieChartDonutStyle(holePercentage = Float.NaN).clamp().holePercentage,
        )
        assertEquals(
            expected = DONUT_MIN_PERCENTAGE,
            actual = PieChartDonutStyle(holePercentage = -10f).clamp().holePercentage,
        )
        assertEquals(
            expected = DONUT_MAX_PERCENTAGE,
            actual = PieChartDonutStyle(holePercentage = 400f).clamp().holePercentage,
        )
    }

    @Test
    fun slices_clampsAlpha() {
        val slices = PieChartSlicesStyle(alpha = 2f, baseColor = Color.Red, colors = persistentListOf())

        assertEquals(expected = 1f, actual = slices.clamp().alpha)
    }

    @Test
    fun border_clampsWidth() {
        val clamped = PieChartBorderStyle(width = Dp.Infinity, color = Color.Black).clamp(density)

        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.width)
    }
}
