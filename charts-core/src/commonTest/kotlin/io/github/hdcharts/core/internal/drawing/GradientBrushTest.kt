package io.github.hdcharts.core.internal.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.GradientPoint
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.GradientStop
import kotlin.test.Test
import kotlin.test.assertEquals

class GradientBrushTest {
    private val barColor = Color(0xFF4958A9)
    private val shape = Rect(left = 10f, top = 40f, right = 30f, bottom = 100f)
    private val plot = Rect(left = 0f, top = 0f, right = 200f, bottom = 100f)

    @Test
    fun shapeSpan_mapsPositionsToShapeBounds() {
        assertEquals(
            expected = verticalBrush(start = Offset(x = 20f, y = 40f), end = Offset(x = 20f, y = 100f)),
            actual = brush(gradient = vertical(span = GradientSpan.Shape)),
        )
    }

    @Test
    fun plotSpan_mapsPositionsToPlotBounds() {
        assertEquals(
            expected = verticalBrush(start = Offset(x = 100f, y = 0f), end = Offset(x = 100f, y = 100f)),
            actual = brush(gradient = vertical(span = GradientSpan.Plot)),
        )
    }

    @Test
    fun mirroredShapeSpan_runsFromBottomEdge() {
        assertEquals(
            expected = verticalBrush(start = Offset(x = 20f, y = 100f), end = Offset(x = 20f, y = 40f)),
            actual = brush(gradient = vertical(span = GradientSpan.Shape), mirrored = true),
        )
    }

    @Test
    fun mirroredPlotSpan_isNotFlipped() {
        assertEquals(
            expected = verticalBrush(start = Offset(x = 100f, y = 0f), end = Offset(x = 100f, y = 100f)),
            actual = brush(gradient = vertical(span = GradientSpan.Plot), mirrored = true),
        )
    }

    @Test
    fun seriesStop_paintsShapeColorAtStopAlpha() {
        val gradient =
            ChartGradient.Linear(
                stops =
                    listOf(
                        GradientStop.Series(offset = 0f, alpha = 1f),
                        GradientStop.Series(offset = 1f, alpha = 0.2f),
                    ),
                start = GradientPoint(x = 0.5f, y = 0f),
                end = GradientPoint(x = 0.5f, y = 1f),
                span = GradientSpan.Shape,
                tileMode = TileMode.Clamp,
            )

        assertEquals(
            expected =
                Brush.linearGradient(
                    0f to barColor.copy(alpha = 1f),
                    1f to barColor.copy(alpha = 0.2f),
                    start = Offset(x = 20f, y = 40f),
                    end = Offset(x = 20f, y = 100f),
                ),
            actual = brush(gradient = gradient),
        )
    }

    @Test
    fun shapeWithoutArea_paintsFirstStop() {
        val flatBar = Rect(left = 10f, top = 100f, right = 30f, bottom = 100f)

        assertEquals(
            expected = SolidColor(Color.Red),
            actual =
                gradientBrush(
                    color = barColor,
                    gradient = vertical(span = GradientSpan.Shape),
                    shapeBounds = flatBar,
                    plotBounds = plot,
                    mirrored = false,
                ),
        )
    }

    private fun brush(
        gradient: ChartGradient,
        mirrored: Boolean = false,
    ): Brush =
        gradientBrush(
            color = barColor,
            gradient = gradient,
            shapeBounds = shape,
            plotBounds = plot,
            mirrored = mirrored,
        )

    private fun vertical(span: GradientSpan) =
        ChartGradient.Linear(
            stops = redToBlue,
            start = GradientPoint(x = 0.5f, y = 0f),
            end = GradientPoint(x = 0.5f, y = 1f),
            span = span,
            tileMode = TileMode.Clamp,
        )

    private fun verticalBrush(
        start: Offset,
        end: Offset,
    ): Brush = Brush.linearGradient(0f to Color.Red, 1f to Color.Blue, start = start, end = end)

    private val redToBlue =
        listOf(
            GradientStop.Fixed(offset = 0f, color = Color.Red),
            GradientStop.Fixed(offset = 1f, color = Color.Blue),
        )
}
