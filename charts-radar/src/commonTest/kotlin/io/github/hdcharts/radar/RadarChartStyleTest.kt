package io.github.hdcharts.radar

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
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RadarChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val density = Density(2f)

    @Test
    fun radar_followsSeriesRules() {
        val polygon =
            RadarPolygonStyle(
                fillVisible = true,
                fillAlpha = 0.25f,
                lineColor = base,
                lineColors = emptyList(),
                lineWidth = 3.dp,
            )

        assertEquals(generateColorShades(base, 3), polygon.resolveLineColors(3))
        assertEquals(listOf(base), polygon.resolveLineColors(1))
    }

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

    @Test
    fun defaults_useStyleDefaults() =
        runComposeUiTest {
            lateinit var selection: RadarSelectionStyle
            lateinit var grid: RadarGridStyle
            lateinit var axes: RadarAxesStyle
            lateinit var points: RadarPointStyle
            var gridColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val selectionStyle = RadarChartDefaults.selection()
                    val gridStyle = RadarChartDefaults.grid()
                    val axesStyle = RadarChartDefaults.axes()
                    val pointStyle = RadarChartDefaults.points()
                    val themeGridColor = StyleDefaults.gridColor
                    SideEffect {
                        selection = selectionStyle
                        grid = gridStyle
                        axes = axesStyle
                        points = pointStyle
                        gridColor = themeGridColor
                    }
                }
            }

            runOnIdle {
                assertTrue(selection.visible)
                assertEquals(expected = StyleDefaults.unselectedAlpha, actual = selection.unselectedAlpha)
                assertEquals(
                    expected = StyleDefaults.radarUnfocusedSeriesAlpha,
                    actual = selection.unfocusedSeriesAlpha,
                )
                assertEquals(expected = StyleDefaults.selectedPointSize, actual = selection.pointSize)
                assertEquals(expected = StyleDefaults.gridSteps, actual = grid.steps)
                assertEquals(expected = StyleDefaults.axisLabelPadding, actual = axes.labelPadding)
                assertEquals(expected = gridColor, actual = grid.color)
                assertEquals(expected = gridColor, actual = axes.lineColor)
                assertEquals(expected = StyleDefaults.pointsVisible, actual = points.visible)
                assertEquals(
                    expected = StyleDefaults.radarAxisLabelsVisible,
                    actual = axes.labelVisible,
                )
            }
        }
}
