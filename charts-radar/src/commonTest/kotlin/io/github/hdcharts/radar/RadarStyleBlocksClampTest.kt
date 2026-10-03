package io.github.hdcharts.radar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.MAX_GRID_STEPS
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Each radar block clamps its own fields, so a field added to a block fails to compile until it is
 * dealt with. These pin what each one clamps.
 */
class RadarStyleBlocksClampTest {
    private val density = Density(2f)

    @Test
    fun grid_clampsLineWidthAndSteps() {
        val clamped =
            RadarGridStyle(
                visible = true,
                color = Color.Gray,
                lineWidth = Dp.Unspecified,
                steps = Int.MAX_VALUE,
            ).clamp(density)

        assertEquals(expected = StyleDefaults.lineWidth, actual = clamped.lineWidth)
        assertEquals(expected = MAX_GRID_STEPS, actual = clamped.steps)
    }

    @Test
    fun axes_clampsLineWidthLabelSizeAndLabelPadding() {
        val clamped =
            RadarAxesStyle(
                visible = true,
                lineColor = Color.Gray,
                lineWidth = (-1f).dp,
                labelColor = Color.Black,
                labelSize = 0.sp,
                labelPadding = Dp.Infinity,
                labelVisible = true,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.lineWidth)
        assertEquals(expected = StyleDefaults.axisLabelSize, actual = clamped.labelSize)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.labelPadding)
    }

    @Test
    fun polygon_clampsFillAlphaAndLineWidth() {
        val clamped =
            RadarPolygonStyle(
                fillVisible = true,
                fillAlpha = Float.NaN,
                lineColor = Color.Blue,
                lineColors = persistentListOf(),
                lineWidth = Dp.Unspecified,
            ).clamp(density)

        assertEquals(expected = 1f, actual = clamped.fillAlpha)
        assertEquals(expected = StyleDefaults.seriesLineWidth, actual = clamped.lineWidth)
    }

    @Test
    fun points_clampsSize() {
        val clamped =
            RadarPointStyle(
                visible = true,
                color = Color.Blue,
                colorSameAsLine = false,
                size = Dp.Infinity,
            ).clamp(density)

        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.size)
    }

    @Test
    fun selection_clampsPointSizeAndBothAlphas() {
        val clamped =
            RadarSelectionStyle(
                visible = true,
                pointSize = (-4f).dp,
                unselectedAlpha = 5f,
                unfocusedSeriesAlpha = Float.NaN,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.pointSize)
        assertEquals(expected = 1f, actual = clamped.unselectedAlpha)
        assertEquals(expected = 1f, actual = clamped.unfocusedSeriesAlpha)
    }
}
