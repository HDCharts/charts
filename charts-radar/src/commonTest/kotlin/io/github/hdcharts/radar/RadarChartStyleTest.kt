package io.github.hdcharts.radar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.StyleDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RadarChartStyleTest {
    private val base = Color(0xFF4958A9)

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
