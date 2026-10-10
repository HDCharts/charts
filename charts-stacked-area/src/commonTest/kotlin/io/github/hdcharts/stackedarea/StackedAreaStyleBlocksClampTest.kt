package io.github.hdcharts.stackedarea

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.ChartGradients
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Each stacked area block clamps its own fields, so a field added to a block fails to compile until
 * it is dealt with. These pin what each one clamps.
 */
class StackedAreaStyleBlocksClampTest {
    private val density = Density(2f)
    private val fill =
        StackedAreaFillStyle(
            color = Color.Blue,
            colors = persistentListOf(),
            alpha = 1f,
            bezier = false,
            gradient = null,
            seriesGradients = persistentListOf(),
        )

    @Test
    fun fill_clampsAlphaAndSharedGradient() {
        val clamped =
            fill
                .copy(
                    alpha = Float.NaN,
                    bezier = true,
                    gradient = ChartGradients.fade(endAlpha = 2f),
                ).clamp()

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = ChartGradients.fade(endAlpha = 1f), actual = clamped.gradient)
    }

    @Test
    fun fill_clampsSeriesGradientsAndPreservesSolidFallbackIndices() {
        val shared = ChartGradients.fade(endAlpha = 0.2f)
        val clamped =
            fill
                .copy(
                    gradient = shared,
                    seriesGradients =
                        persistentListOf(
                            ChartGradients.fade(endAlpha = 2f),
                            null,
                            ChartGradients.vertical(colors = emptyList()),
                            ChartGradients.fade(endAlpha = -1f),
                        ),
                ).clamp()

        assertEquals<List<ChartGradient?>>(
            expected = listOf(ChartGradients.fade(endAlpha = 1f), null, null, ChartGradients.fade(endAlpha = 0f)),
            actual = clamped.seriesGradients,
        )
        assertEquals(shared, clamped.gradient)
    }

    @Test
    fun fill_invalidSharedGradientUsesSolidFallback() {
        val clamped = fill.copy(gradient = ChartGradients.vertical(colors = emptyList())).clamp()

        assertNull(clamped.gradient)
        assertNull(clamped.gradientForSeries(0))
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
