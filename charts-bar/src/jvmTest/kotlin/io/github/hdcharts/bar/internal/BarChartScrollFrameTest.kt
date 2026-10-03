package io.github.hdcharts.bar.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.model.ChartData
import io.github.hdcharts.core.style.BarChartDefaults
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Renders single frames, which the Compose test rule would merge, to check the frame right after a scroll clamp. */
class BarChartScrollFrameTest {
    @Test
    fun zoomOutAtScrollEnd_firstFrameDrawsEveryVisibleBar() {
        val scrollState = ScrollState(initial = 0)
        var zoomScale by mutableFloatStateOf(4f)
        val scene =
            ImageComposeScene(width = 1000, height = 400, density = Density(1f)) {
                val animatedValues = remember { List(BAR_COUNT) { Animatable(1f) } }
                BarChartContent(
                    chartData =
                        ChartData(
                            labels = List(BAR_COUNT) { "B$it" },
                            points = List(BAR_COUNT) { 50.0 },
                        ),
                    style = BarChartDefaults.style(),
                    interactionEnabled = true,
                    dragSelectionEnabled = false,
                    animatedValues = animatedValues,
                    barColors = emptyList(),
                    defaultBarColor = BAR_COLOR,
                    fixedMin = 0.0,
                    fixedMax = 100.0,
                    axisValueFormatter = BarChartDefaults.axisValueFormatter,
                    isScrollable = true,
                    spacingPx = 10f,
                    minBarWidthPx = 10f,
                    scrollState = scrollState,
                    zoomScale = zoomScale,
                    zoomMin = 1f,
                    zoomMax = 4f,
                    zoomStep = 1.25f,
                    selectedIndex = NO_SELECTION,
                    onToggleSelection = {},
                    onSelectIndex = {},
                    onClearSelection = {},
                    onZoomScaleChange = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        try {
            scene.render(nanoTime = 0L)
            runBlocking { scrollState.scrollTo(scrollState.maxValue) }
            scene.render(nanoTime = FRAME_NANOS)
            val atEnd = barPixels(scene.render(nanoTime = 2 * FRAME_NANOS))

            // Zooming out shrinks the content, so the scroll offset is clamped in the next measure pass.
            zoomScale = 3.2f
            val firstFrame = barPixels(scene.render(nanoTime = 3 * FRAME_NANOS))
            val nextFrame = barPixels(scene.render(nanoTime = 4 * FRAME_NANOS))

            assertTrue(atEnd > 0, "bars drawn before zooming out")
            assertEquals(
                expected = nextFrame,
                actual = firstFrame,
                message = "bar pixels in the first frame after zooming out",
            )
        } finally {
            scene.close()
        }
    }

    private fun barPixels(image: Image): Int {
        val bitmap = Bitmap.makeFromImage(image)
        val y = image.height / 3
        return (0 until image.width).count { x -> bitmap.getColor(x, y) == BAR_ARGB }
    }

    private companion object {
        const val BAR_COUNT = 400
        const val FRAME_NANOS = 16_000_000L
        val BAR_COLOR = Color(0xFFFF0000)
        const val BAR_ARGB = 0xFFFF0000.toInt()
    }
}
