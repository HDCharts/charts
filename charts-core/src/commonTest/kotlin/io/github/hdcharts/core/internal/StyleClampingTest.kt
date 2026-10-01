package io.github.hdcharts.core.internal

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class StyleClampingTest {
    private val density = Density(density = 2f)

    @Test
    fun clampAlpha_keepsValuesInRangeAndClampsTheRest() {
        assertEquals(expected = 0.4f, actual = 0.4f.clampAlpha())
        assertEquals(expected = 1f, actual = 1.5f.clampAlpha())
        assertEquals(expected = 0f, actual = (-0.2f).clampAlpha())
        assertEquals(expected = 1f, actual = Float.POSITIVE_INFINITY.clampAlpha())
        assertEquals(expected = 0f, actual = Float.NEGATIVE_INFINITY.clampAlpha())
    }

    @Test
    fun clampAlpha_drawsNaNAsOpaque() {
        assertEquals(expected = 1f, actual = Float.NaN.clampAlpha())
    }

    @Test
    fun clampSize_keepsValuesInRangeAndClampsTheRest() {
        val maxSize = with(density) { MAX_SIZE_PX.toDp() }

        assertEquals(expected = 2.dp, actual = 2.dp.clampSize(fallback = 1.dp, density = density))
        assertEquals(expected = 0.dp, actual = (-1).dp.clampSize(fallback = 1.dp, density = density))
        assertEquals(expected = maxSize, actual = Dp.Infinity.clampSize(fallback = 1.dp, density = density))
        assertEquals(expected = maxSize, actual = 10_000.dp.clampSize(fallback = 1.dp, density = density))
    }

    @Test
    fun clampSize_usesFallbackForUnspecified() {
        assertEquals(expected = 4.dp, actual = Dp.Unspecified.clampSize(fallback = 4.dp, density = density))
    }

    @Test
    fun clampTextSize_keepsDrawableValuesInRange() {
        assertEquals(expected = 12.sp, actual = 12.sp.clampTextSize(fallback = 11.sp, density = density))
    }

    @Test
    fun clampTextSize_usesFallbackForValuesThatCannotBeDrawn() {
        assertEquals(expected = 11.sp, actual = Float.NaN.sp.clampTextSize(fallback = 11.sp, density = density))
        assertEquals(expected = 11.sp, actual = 0.sp.clampTextSize(fallback = 11.sp, density = density))
        assertEquals(expected = 11.sp, actual = (-4).sp.clampTextSize(fallback = 11.sp, density = density))
        assertEquals(
            expected = 11.sp,
            actual = Float.POSITIVE_INFINITY.sp.clampTextSize(fallback = 11.sp, density = density),
        )
        assertEquals(expected = 11.sp, actual = TextUnit.Unspecified.clampTextSize(fallback = 11.sp, density = density))
        assertEquals(expected = 11.sp, actual = 12.em.clampTextSize(fallback = 11.sp, density = density))
    }

    @Test
    fun clampTextSize_capsTheResolvedPixelSize() {
        val maxSp = with(density) { MAX_SIZE_PX.toSp() }

        assertEquals(expected = maxSp, actual = 100_000.sp.clampTextSize(fallback = 11.sp, density = density))
    }

    @Test
    fun clampGridSteps_keepsStepsInRange() {
        assertEquals(expected = 4, actual = 4.clampGridSteps())
        assertEquals(expected = 0, actual = (-3).clampGridSteps())
        assertEquals(expected = MAX_GRID_STEPS, actual = Int.MAX_VALUE.clampGridSteps())
    }
}
