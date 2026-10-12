package io.github.hdcharts.core.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.MAX_GRID_STEPS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AxesStyleTest {
    @Test
    fun defaults_showEveryElementWithSharedColorsAndSizes() =
        runComposeUiTest {
            lateinit var axes: AxesStyle
            var axisColor = Color.Unspecified
            var gridColor = Color.Unspecified
            var labelColor = Color.Unspecified
            setContent {
                MaterialTheme {
                    val style = AxesDefaults.style()
                    val scheme = MaterialTheme.colorScheme
                    SideEffect {
                        axes = style
                        axisColor = scheme.outline
                        gridColor = scheme.outlineVariant
                        labelColor = scheme.onSurfaceVariant
                    }
                }
            }
            runOnIdle {
                assertTrue(axes.visible)
                assertTrue(axes.lines.visible)
                assertTrue(axes.grid.visible)
                assertTrue(axes.xLabels.visible)
                assertTrue(axes.yLabels.visible)
                assertEquals(axisColor, axes.lines.color)
                assertEquals(gridColor, axes.grid.color)
                assertEquals(labelColor, axes.xLabels.color)
                assertEquals(axes.xLabels, axes.yLabels)
                assertEquals(StyleDefaults.lineWidth, axes.lines.lineWidth)
                assertEquals(StyleDefaults.lineWidth, axes.grid.lineWidth)
                assertEquals(StyleDefaults.gridSteps, axes.grid.steps)
            }
        }

    @Test
    fun clamp_sanitizesStrokesAndGridWithoutChangingVisibilityOrLabels() {
        val labels = AxisLabelStyle(visible = true, color = Color.Black, size = 12.sp, maxCount = 9)
        val source =
            AxesStyle(
                visible = false,
                lines = AxisLinesStyle(visible = true, color = Color.Red, lineWidth = (-1).dp),
                grid =
                    GridStyle(
                        visible = false,
                        steps = Int.MAX_VALUE,
                        color = Color.Green,
                        lineWidth = Dp.Unspecified,
                    ),
                xLabels = labels,
                yLabels = labels.copy(visible = false),
            )
        val clamped = source.clamp(Density(2f))

        assertFalse(clamped.visible)
        assertTrue(clamped.lines.visible)
        assertFalse(clamped.grid.visible)
        assertEquals(Color.Red, clamped.lines.color)
        assertEquals(0.dp, clamped.lines.lineWidth)
        assertEquals(MAX_GRID_STEPS, clamped.grid.steps)
        assertEquals(StyleDefaults.lineWidth, clamped.grid.lineWidth)
        assertEquals(source.xLabels, clamped.xLabels)
        assertEquals(source.yLabels, clamped.yLabels)
        assertEquals((-1).dp, source.lines.lineWidth)
    }
}
