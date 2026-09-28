package io.github.hdcharts.charts.unit.helpers

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.common.layout.placedHorizontalScrollPx
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartScrollTest {
    private val density = Density(density = 2f)

    @Test
    fun placedHorizontalScrollPx_keepsOffsetsInsideTheScrollRange() {
        with(density) {
            // 500 dp of content is 1000 px; a 400 px viewport scrolls up to 600 px.
            assertEquals(expected = 250, actual = placedHorizontalScrollPx(250, 500.dp, 400f))
            assertEquals(expected = 600, actual = placedHorizontalScrollPx(600, 500.dp, 400f))
            assertEquals(expected = 600, actual = placedHorizontalScrollPx(8_400, 500.dp, 400f))
            assertEquals(expected = 0, actual = placedHorizontalScrollPx(-5, 500.dp, 400f))
        }
    }

    @Test
    fun placedHorizontalScrollPx_contentNarrowerThanViewport_doesNotScroll() {
        with(density) {
            assertEquals(expected = 0, actual = placedHorizontalScrollPx(120, 100.dp, 400f))
        }
    }
}
