package io.github.hdcharts.core.internal.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.axis.AxisYLayoutTick
import io.github.hdcharts.core.internal.axis.buildNumericYAxisTicks
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.GridStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class HorizontalGridTest {
    @Test
    fun gridLineY_withoutInset_spansFullHeight() {
        assertEquals(
            expected = listOf(0f, 25f, 50f, 75f, 100f),
            actual = lineYs(steps = 4, heightPx = 100f, verticalInsetPx = 0f),
        )
    }

    @Test
    fun gridLineY_withInset_keepsEdgeLinesInside() {
        assertEquals(
            expected = listOf(10f, 30f, 50f, 70f, 90f),
            actual = lineYs(steps = 4, heightPx = 100f, verticalInsetPx = 10f),
        )
    }

    @Test
    fun gridLineY_insetPastHalfHeight_collapsesToCenter() {
        assertEquals(
            expected = listOf(50f, 50f, 50f),
            actual = lineYs(steps = 2, heightPx = 100f, verticalInsetPx = 80f),
        )
    }

    @Test
    fun forEachGridLineY_withLabelTicks_drawsOnTickCenters() {
        val ticks =
            buildNumericYAxisTicks(
                minValue = 0.0,
                maxValue = 100.0,
                labelCount = 3,
                plotHeightPx = 150f,
                verticalInsetPx = 6f,
                formatter = ChartValueFormatter { value -> value.toString() },
            )
        assertEquals(
            expected = ticks.map { tick -> tick.centerY },
            actual = gridLineYs(grid = grid(steps = 4), ticks = ticks, heightPx = 150f, verticalInsetPx = 6f),
        )
    }

    @Test
    fun forEachGridLineY_withoutLabelTicks_usesGridSteps() {
        assertEquals(
            expected = listOf(0f, 25f, 50f, 75f, 100f),
            actual = gridLineYs(grid = grid(steps = 4), ticks = emptyList(), heightPx = 100f, verticalInsetPx = 0f),
        )
    }

    @Test
    fun forEachGridLineY_zeroStepsWithLabelTicks_drawsNothing() {
        val ticks = listOf(AxisYLayoutTick(label = "100", centerY = 0f), AxisYLayoutTick(label = "0", centerY = 100f))
        assertEquals(
            expected = emptyList(),
            actual = gridLineYs(grid = grid(steps = 0), ticks = ticks, heightPx = 100f, verticalInsetPx = 0f),
        )
    }

    @Test
    fun forEachGridLineY_hidden_drawsNothing() {
        assertEquals(
            expected = emptyList(),
            actual =
                gridLineYs(
                    grid = grid(steps = 4, visible = false),
                    ticks = emptyList(),
                    heightPx = 100f,
                    verticalInsetPx = 0f,
                ),
        )
    }

    private fun grid(
        steps: Int,
        visible: Boolean = true,
    ): GridStyle = GridStyle(visible = visible, steps = steps, color = Color.Gray, lineWidth = 1.dp)

    private fun gridLineYs(
        grid: GridStyle,
        ticks: List<AxisYLayoutTick>,
        heightPx: Float,
        verticalInsetPx: Float,
    ): List<Float> =
        buildList {
            forEachGridLineY(grid = grid, ticks = ticks, heightPx = heightPx, verticalInsetPx = verticalInsetPx) { y ->
                add(y)
            }
        }

    private fun lineYs(
        steps: Int,
        heightPx: Float,
        verticalInsetPx: Float,
    ): List<Float> =
        (0..steps).map { step ->
            gridLineY(step = step, steps = steps, heightPx = heightPx, verticalInsetPx = verticalInsetPx)
        }
}
