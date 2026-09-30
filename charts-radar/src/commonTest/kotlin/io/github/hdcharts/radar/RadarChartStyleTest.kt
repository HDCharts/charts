package io.github.hdcharts.radar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.palette.generateColorShades
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
