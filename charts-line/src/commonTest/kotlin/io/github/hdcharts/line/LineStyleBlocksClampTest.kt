package io.github.hdcharts.line

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Each line block clamps its own fields, so a field added to a block fails to compile until it is
 * dealt with. These pin what each one clamps.
 */
class LineStyleBlocksClampTest {
    private val density = Density(2f)

    @Test
    fun line_clampsAlphaAndStrokeWidth() {
        val clamped =
            LineVisualStyle(
                color = Color.Blue,
                alpha = 1.4f,
                colors = persistentListOf(),
                strokeWidth = Dp.Infinity,
                bezier = true,
            ).clamp(density)

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.strokeWidth)
    }

    @Test
    fun points_clampsSize() {
        val clamped = LinePointStyle(color = Color.Blue, size = (-3f).dp, visible = true).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.size)
    }

    @Test
    fun selection_clampsWidthAndBothMarkerSizes() {
        val clamped =
            LineSelectionStyle(
                visible = true,
                color = Color.Blue,
                width = Float.NaN.dp,
                markerColor = Color.White,
                markerSize = Dp.Unspecified,
                pointSize = Dp.Infinity,
            ).clamp(density)

        assertEquals(expected = StyleDefaults.lineWidth, actual = clamped.width)
        assertEquals(expected = StyleDefaults.lineSelectionMarkerSize, actual = clamped.markerSize)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.pointSize)
    }

    @Test
    fun axis_clampsLineWidthAndPassesLabelsThrough() {
        val labels =
            AxisLabelStyle(visible = true, color = Color.Black, size = StyleDefaults.axisLabelSize, maxCount = null)
        val clamped =
            LineAxisStyle(
                visible = true,
                color = Color.Black,
                lineWidth = (-1f).dp,
                xLabels = labels,
                yLabels = labels,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.lineWidth)
        assertEquals(expected = labels, actual = clamped.yLabels)
    }
}
