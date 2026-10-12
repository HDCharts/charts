package io.github.hdcharts.line

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxisLinesDefaults
import io.github.hdcharts.core.style.GridDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LineChartAxesTest {
    @Test
    fun masterVisibilityOverridesIndependentGridAndAxisLineSwitches() =
        runComposeUiTest {
            val axesVisible = mutableStateOf(true)
            val linesVisible = mutableStateOf(true)
            val gridVisible = mutableStateOf(true)
            lateinit var captureLayer: GraphicsLayer
            lateinit var captureScope: CoroutineScope
            var pixels: PixelMap? = null
            setContent {
                MaterialTheme {
                    captureLayer = rememberGraphicsLayer()
                    captureScope = rememberCoroutineScope()
                    LineChart(
                        data = listOf(20.0, 40.0, 30.0).toChartData(categories = listOf("A", "B", "C")),
                        modifier =
                            Modifier
                                .size(width = 400.dp, height = 300.dp)
                                .drawWithContent {
                                    captureLayer.record { this@drawWithContent.drawContent() }
                                    drawLayer(captureLayer)
                                }.background(Color.White),
                        style =
                            LineChartDefaults.style(
                                line = LineChartDefaults.line(color = Color.Blue),
                                range = LineChartDefaults.range(min = 0.0, max = 100.0),
                                axes =
                                    AxesDefaults.style(
                                        visible = axesVisible.value,
                                        lines =
                                            AxisLinesDefaults.style(
                                                visible = linesVisible.value,
                                                color = Color.Red,
                                                lineWidth = 4.dp,
                                            ),
                                        grid =
                                            GridDefaults.style(
                                                visible = gridVisible.value,
                                                color = Color.Green,
                                                lineWidth = 4.dp,
                                            ),
                                    ),
                            ),
                        animateOnStart = false,
                    )
                }
            }

            fun assertStrokes(
                lines: Boolean,
                grid: Boolean,
            ) {
                waitForIdle()
                runOnIdle {
                    pixels = null
                    captureScope.launch { pixels = captureLayer.toImageBitmap().toPixelMap() }
                }
                waitUntil(timeoutMillis = 3_000L) { pixels != null }
                val image = checkNotNull(pixels)
                var hasAxisLines = false
                var hasGrid = false
                for (y in 0 until image.height) {
                    for (x in 0 until image.width) {
                        hasAxisLines = hasAxisLines || image[x, y] == Color.Red
                        hasGrid = hasGrid || image[x, y] == Color.Green
                    }
                }
                assertEquals(lines, hasAxisLines, "Axis strokes")
                assertEquals(grid, hasGrid, "Grid strokes")
            }

            assertStrokes(lines = true, grid = true)
            runOnIdle { linesVisible.value = false }
            assertStrokes(lines = false, grid = true)
            onNodeWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertIsDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertIsDisplayed()

            runOnIdle {
                linesVisible.value = true
                gridVisible.value = false
            }
            assertStrokes(lines = true, grid = false)

            runOnIdle {
                gridVisible.value = true
                axesVisible.value = false
            }
            assertStrokes(lines = false, grid = false)
            onAllNodesWithTag(TestTags.LINE_CHART_X_AXIS_LABELS).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).assertCountEquals(0)

            runOnIdle { axesVisible.value = true }
            assertStrokes(lines = true, grid = true)
        }
}
