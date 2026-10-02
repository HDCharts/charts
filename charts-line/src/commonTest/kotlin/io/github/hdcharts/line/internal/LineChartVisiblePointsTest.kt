package io.github.hdcharts.line.internal

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.ANIMATION_TARGET
import io.github.hdcharts.core.internal.bezier.DEFAULT_BEZIER_TENSION
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.line.LineAxisStyle
import io.github.hdcharts.line.LineChartStyle
import io.github.hdcharts.line.LinePointStyle
import io.github.hdcharts.line.LineRangeStyle
import io.github.hdcharts.line.LineSelectionStyle
import io.github.hdcharts.line.LineVisualStyle
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The expanded view draws only the points on screen, so the line it draws has to be the line the
 * whole series draws.
 *
 * The path is the guard, not the pixels. Skia renders the same geometry slightly differently
 * depending on how much path follows it, so a culled and a full path differ by a few alpha steps
 * in the middle of the viewport even when every point of it is identical. Comparing the paths
 * compares the geometry, which is what culling can actually change.
 */
class LineChartVisiblePointsTest {
    @Test
    fun bezierLineInTheMiddleOfTheContent_drawsTheSameSegmentsAsTheWholeSeries() {
        assertCulledPathMatchesFullSeries(bezier = true, pointsVisible = false)
    }

    @Test
    fun straightLineInTheMiddleOfTheContent_drawsTheSameSegmentsAsTheWholeSeries() {
        assertCulledPathMatchesFullSeries(bezier = false, pointsVisible = false)
    }

    @Test
    fun bezierLineWithPointsInTheMiddleOfTheContent_drawsTheSameSegmentsAsTheWholeSeries() {
        assertCulledPathMatchesFullSeries(bezier = true, pointsVisible = true)
    }

    @Test
    fun straightLineWithPointsInTheMiddleOfTheContent_drawsTheSameSegmentsAsTheWholeSeries() {
        assertCulledPathMatchesFullSeries(bezier = false, pointsVisible = true)
    }

    @Test
    fun bezierLineAtTheStartOfTheContent_drawsTheSameSegmentsAsTheWholeSeries() {
        assertCulledPathMatchesFullSeries(bezier = true, pointsVisible = true, viewportStartPx = 0f)
    }

    @Test
    fun bezierLineAtTheEndOfTheContent_drawsTheSameSegmentsAsTheWholeSeries() {
        assertCulledPathMatchesFullSeries(bezier = true, pointsVisible = true, viewportStartPx = LAST_VIEWPORT_START_PX)
    }

    @Test
    fun straightLineAtTheEndOfTheContent_drawsTheSameSegmentsAsTheWholeSeries() {
        assertCulledPathMatchesFullSeries(
            bezier = false,
            pointsVisible = true,
            viewportStartPx = LAST_VIEWPORT_START_PX,
        )
    }

    @Test
    fun culledChart_drawsLessThanTheWholeSeries() {
        val style = style(bezier = true, pointsVisible = true)
        val values = scaledValues()
        val drawRange =
            lineChartDrawRange(
                valuesCount = VALUES_COUNT,
                stepX = STEP_X,
                viewportStartPx = VIEWPORT_START_PX,
                viewportWidthPx = VIEWPORT_WIDTH,
                overscanPx = POINT_RADIUS_PX,
            )

        // Without this the other tests would pass even if the culling stopped working.
        assertTrue(
            drawRange.count() < VALUES_COUNT / 2,
            "The expanded view has to draw only the points near the viewport: $drawRange",
        )
        assertTrue(render(values = values, drawRange = drawRange, style = style).segments.size < VALUES_COUNT)
    }

    @Test
    fun culledRange_coversTheViewportWithTheOverscanOfTheWidestMarker() {
        val drawRange =
            lineChartDrawRange(
                valuesCount = VALUES_COUNT,
                stepX = STEP_X,
                viewportStartPx = VIEWPORT_START_PX,
                viewportWidthPx = VIEWPORT_WIDTH,
                overscanPx = POINT_RADIUS_PX,
            )

        // The viewport holds points 500..700. The overscan is one point for the segment crossing
        // each edge, plus the two points the markers reach past it.
        assertEquals(expected = 497, actual = drawRange.first)
        assertEquals(expected = 703, actual = drawRange.last)
    }

    @Test
    fun culledRange_atTheEndOfTheContent_staysInsideTheSeries() {
        val drawRange =
            lineChartDrawRange(
                valuesCount = VALUES_COUNT,
                stepX = STEP_X,
                viewportStartPx = LAST_VIEWPORT_START_PX,
                viewportWidthPx = VIEWPORT_WIDTH,
                overscanPx = POINT_RADIUS_PX,
            )

        assertEquals(expected = VALUES_COUNT - 1, actual = drawRange.last)
    }

    @Test
    fun culledChart_drawsTheViewportToTheSamePixelsAsTheWholeSeries() {
        val style = style(bezier = true, pointsVisible = true)
        val values = scaledValues()
        val full = render(values = values, drawRange = 0 until VALUES_COUNT, style = style)
        val culled =
            render(
                values = values,
                drawRange =
                    lineChartDrawRange(
                        valuesCount = VALUES_COUNT,
                        stepX = STEP_X,
                        viewportStartPx = VIEWPORT_START_PX,
                        viewportWidthPx = VIEWPORT_WIDTH,
                        overscanPx = POINT_RADIUS_PX,
                    ),
                style = style,
            )

        val left = VIEWPORT_START_PX.toInt()
        val fullPixels = full.bitmap.toPixelMap(startX = left, width = VIEWPORT_WIDTH.toInt(), height = VIEWPORT_HEIGHT)
        val culledPixels =
            culled.bitmap.toPixelMap(
                startX = left,
                width = VIEWPORT_WIDTH.toInt(),
                height = VIEWPORT_HEIGHT,
            )

        // The same geometry renders a few alpha steps apart because the rasterizer sees a different
        // amount of path, so the pixels are compared with that slack. A changed curve or a dropped
        // marker moves pixels far more than this.
        var worstDelta = 0f
        for (y in 0 until VIEWPORT_HEIGHT) {
            for (x in 0 until VIEWPORT_WIDTH.toInt()) {
                val expected = fullPixels[x, y]
                val actual = culledPixels[x, y]
                val delta =
                    maxOf(
                        abs(expected.red - actual.red),
                        abs(expected.green - actual.green),
                        abs(expected.blue - actual.blue),
                        abs(expected.alpha - actual.alpha),
                    )
                if (delta > worstDelta) worstDelta = delta
            }
        }
        assertTrue(
            worstDelta <= MAX_PIXEL_DELTA,
            "The viewport renders $worstDelta apart, more than the $MAX_PIXEL_DELTA the rasterizer needs",
        )
    }

    private fun assertCulledPathMatchesFullSeries(
        bezier: Boolean,
        pointsVisible: Boolean,
        viewportStartPx: Float = VIEWPORT_START_PX,
    ) {
        val style = style(bezier = bezier, pointsVisible = pointsVisible)
        val values = scaledValues()
        val overscanPx = maxOf(STROKE_WIDTH_PX / 2f, if (pointsVisible) POINT_RADIUS_PX else 0f)
        val drawRange =
            lineChartDrawRange(
                valuesCount = VALUES_COUNT,
                stepX = STEP_X,
                viewportStartPx = viewportStartPx,
                viewportWidthPx = VIEWPORT_WIDTH,
                overscanPx = overscanPx,
            )

        val fullSegments = render(values = values, drawRange = 0 until VALUES_COUNT, style = style).segments
        val culledSegments = render(values = values, drawRange = drawRange, style = style).segments

        assertTrue(
            culledSegments.isNotEmpty(),
            "The viewport has to hold part of the line, or the test compares two empty paths",
        )
        // The culled path starts at its first drawn point, so the matching part of the full path is
        // the one that starts at the same X. Every segment the culled path draws, including the two
        // in the overscan on each side, has to be the segment the full path draws there.
        val firstDrawnX = drawRange.first * STEP_X
        val fullFromFirstDrawn =
            fullSegments.dropWhile { it.first() < firstDrawnX }.take(culledSegments.size)

        assertEquals(
            expected = fullFromFirstDrawn,
            actual = culledSegments,
            message =
                "Bezier: $bezier, points: $pointsVisible, viewport at $viewportStartPx px. " +
                    "A culled path has to draw the segments the full path draws, with the control " +
                    "points of each segment's true neighbours.",
        )
    }

    /** The pixels of one draw, and the path it built. */
    private class Render(
        val bitmap: ImageBitmap,
        val segments: List<List<Float>>,
    )

    private fun render(
        values: FloatArray,
        drawRange: IntRange,
        style: LineChartStyle,
    ): Render {
        val bitmap = ImageBitmap(width = CONTENT_WIDTH, height = VIEWPORT_HEIGHT)
        val scratch = LineChartDrawScratch(valuesCapacity = VALUES_COUNT)

        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(bitmap),
            size = Size(width = CONTENT_WIDTH.toFloat(), height = VIEWPORT_HEIGHT.toFloat()),
        ) {
            drawChartPath(
                values = values,
                valuesCount = VALUES_COUNT,
                drawRange = drawRange,
                style = style,
                lineStroke = LINE_STROKE,
                lineColor = LINE_COLOR,
                bezierTension = DEFAULT_BEZIER_TENSION,
                scratch = scratch,
                lineAnimationProgress = ANIMATION_TARGET,
                markerRevealProgress = ANIMATION_TARGET,
                stepXOverride = STEP_X,
                verticalInset = VERTICAL_INSET,
                revealViewportStartPx = 0f,
                revealViewportWidthPx = CONTENT_WIDTH.toFloat(),
            )
        }

        return Render(bitmap = bitmap, segments = segmentsOf(scratch.path))
    }

    /**
     * Every drawn segment of [path], in order. The opening move is left out: a culled path starts
     * at its first drawn point instead of at the first point of the series.
     */
    private fun segmentsOf(path: Path): List<List<Float>> {
        val segments = mutableListOf<List<Float>>()
        val iterator = path.iterator()
        while (iterator.hasNext()) {
            val segment = iterator.next()
            if (segment.type == PathSegment.Type.Move) continue
            segments.add(segment.points.toList())
        }
        return segments
    }

    private companion object {
        const val VALUES_COUNT = 2_000
        const val STEP_X = 4f
        const val CONTENT_WIDTH = 8_000
        const val VIEWPORT_WIDTH = 800f
        const val VIEWPORT_START_PX = 2_000f
        const val VIEWPORT_HEIGHT = 200
        const val VERTICAL_INSET = 6f
        const val STROKE_WIDTH_PX = 4f
        const val POINT_RADIUS_PX = 8f
        const val MAX_PIXEL_DELTA = 0.05f
        val LINE_COLOR = Color(0xFF4958A9)
        const val LAST_VIEWPORT_START_PX = (CONTENT_WIDTH - VIEWPORT_WIDTH - 1f)

        val LINE_STROKE =
            Stroke(
                width = STROKE_WIDTH_PX,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )

        /** Values scaled to the canvas height, on a curve that never repeats a value. */
        fun scaledValues(): FloatArray =
            FloatArray(VALUES_COUNT) { index ->
                val curve = (sin(index / 37.0) + 1.0) / 2.0
                val ramp = index.toDouble() / VALUES_COUNT
                (VIEWPORT_HEIGHT * (0.1 + 0.8 * (curve * 0.6 + ramp * 0.4))).toFloat()
            }

        fun style(
            bezier: Boolean,
            pointsVisible: Boolean,
        ): LineChartStyle =
            LineChartStyle(
                chartContainerStyle =
                    ChartContainerStyle(
                        styleTitle = TextStyle(),
                        contentPadding = 0.dp,
                    ),
                line =
                    LineVisualStyle(
                        color = LINE_COLOR,
                        alpha = 1f,
                        colors = persistentListOf(LINE_COLOR),
                        strokeWidth = STROKE_WIDTH_PX.dp,
                        bezier = bezier,
                    ),
                points =
                    LinePointStyle(
                        color = LINE_COLOR,
                        size = POINT_RADIUS_PX.dp,
                        visible = pointsVisible,
                    ),
                selection =
                    LineSelectionStyle(
                        visible = true,
                        color = LINE_COLOR,
                        width = 1.dp,
                        markerColor = LINE_COLOR,
                        markerSize = 5.dp,
                        pointSize = 5.dp,
                    ),
                axis =
                    LineAxisStyle(
                        visible = true,
                        color = Color.Black,
                        lineWidth = 1.dp,
                        xLabels = axisLabels(),
                        yLabels = axisLabels(),
                    ),
                range = LineRangeStyle(min = null, max = null),
                legend = LegendStyle(visible = false),
                zoomControlsVisible = false,
            )

        fun axisLabels(): AxisLabelStyle =
            AxisLabelStyle(
                visible = true,
                color = Color.Black,
                size = TextUnit.Unspecified,
                maxCount = null,
            )
    }
}
