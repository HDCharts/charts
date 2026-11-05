package io.github.hdcharts.heatmap.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.heatmap.HeatmapColorScale
import io.github.hdcharts.heatmap.HeatmapData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HeatmapMathTest {
    private val grayScale = HeatmapColorScale(listOf(Color.Black, Color.White))

    @Test
    fun valueAtIndex_readsRowMajorCellsAndRejectsOutsideIndices() {
        val data = matrix(listOf(1.0, 2.0, 3.0), listOf(4.0, null, 6.0))

        assertEquals(expected = 6.0, actual = data.valueAtIndex(5))
        assertEquals(expected = 4.0, actual = data.valueAtIndex(data.cellIndex(row = 1, column = 0)))
        assertNull(data.valueAtIndex(4))
        assertNull(data.valueAtIndex(6))
        assertNull(data.valueAtIndex(NO_SELECTION))
    }

    @Test
    fun interpolatedValue_reproducesALinearFieldExactly() {
        val data = matrix(listOf(0.0, 10.0), listOf(20.0, 30.0))

        assertEquals(expected = 0.0, actual = data.field(0.5, 0.5))
        assertEquals(expected = 5.0, actual = data.field(1.0, 0.5))
        assertEquals(expected = 10.0, actual = data.field(0.5, 1.0))
        assertEquals(expected = 15.0, actual = data.field(1.0, 1.0))
        assertEquals(expected = 7.5, actual = data.field(0.75, 0.75))
        assertEquals(expected = 30.0, actual = data.field(1.5, 1.5))
    }

    @Test
    fun interpolatedValue_extendsEdgeValuesBeyondTheOutermostCenters() {
        val data = matrix(listOf(0.0, 10.0), listOf(20.0, 30.0))

        assertEquals(expected = 0.0, actual = data.field(0.0, 0.0))
        assertEquals(expected = 0.0, actual = data.field(0.2, 0.4))
        assertEquals(expected = 30.0, actual = data.field(2.0, 2.0))
        assertEquals(expected = 25.0, actual = data.field(1.0, 1.9))
    }

    @Test
    fun interpolatedValue_singleRowAndSingleColumnInterpolateAlongTheirOnlyAxis() {
        val row = matrix(listOf(1.0, 3.0, 5.0))
        val column =
            HeatmapData(
                columns = listOf("A"),
                rows = listOf("1", "2", "3"),
                values = listOf(listOf(1.0), listOf(3.0), listOf(5.0)),
            )

        assertEquals(expected = 2.0, actual = row.field(1.0, 0.1))
        assertEquals(expected = 4.0, actual = row.field(2.0, 0.9))
        assertEquals(expected = 2.0, actual = column.field(0.9, 1.0))
        assertEquals(expected = 5.0, actual = column.field(0.5, 3.0))
        assertEquals(expected = 7.0, actual = matrix(listOf(7.0)).field(0.9, 0.1))
    }

    @Test
    fun interpolatedValue_neverBlendsAMissingValue() {
        val data = matrix(listOf(0.0, null), listOf(20.0, 30.0))

        // The missing cell itself has no value.
        assertNull(data.field(1.5, 0.5))
        // Next to the missing cell, only the present neighbors blend, with their weights rescaled.
        // Expected values come from an independent evaluation of the same Hermite blend.
        assertEquals(expected = 5.871766029246344, actual = data.field(0.75, 0.75)!!, absoluteTolerance = 1e-9)
        assertEquals(expected = 26.38920134983127, actual = data.field(1.25, 1.25)!!, absoluteTolerance = 1e-9)
        // Along the bottom row both neighbors exist and the row is a straight run from 20 to 30.
        assertEquals(expected = 27.5, actual = data.field(1.25, 1.5))
    }

    @Test
    fun interpolatedValue_staysContinuousAcrossCellEdgesBesideAMissingValue() {
        // Regression: cells beside a missing value used to switch to their own flat value, which put a
        // step of the full difference between neighbors on every edge around the gap.
        val data = matrix(listOf(0.0, null), listOf(20.0, 30.0))
        val edge = 1.0
        val nearHole = 1.25

        val left = data.field(edge - 1e-9, nearHole)!!
        val right = data.field(edge + 1e-9, nearHole)!!
        val above = data.field(0.75, edge - 1e-9)!!
        val below = data.field(0.75, edge + 1e-9)!!

        assertEquals(expected = left, actual = right, absoluteTolerance = 1e-6)
        assertEquals(expected = above, actual = below, absoluteTolerance = 1e-6)
        // The blend never leaves the range of the values it blends.
        listOf(0.6, 0.9, 1.1, 1.4).forEach { x ->
            listOf(0.6, 0.9, 1.1, 1.4).forEach { y ->
                data.field(x, y)?.let { assertTrue(it in 0.0..30.0, "x=$x, y=$y, value=$it") }
            }
        }
    }

    @Test
    fun interpolatedValue_isSmoothAcrossCellCentersForSmoothData() {
        // Regression: bilinear interpolation bends the field at every cell center, which shows as
        // contour creases around peaks and valleys. On this smooth field the slope jumped by up to
        // 18 value units per cell across a center; the shape-preserving bicubic keeps it continuous.
        val data = smoothField()
        val step = 1e-4

        var largestJump = 0.0
        for (column in 1 until 11) {
            for (rowStep in 0..28) {
                val x = column + 0.5
                val y = 0.5 + rowStep / 4.0
                val left = (data.field(x, y)!! - data.field(x - step, y)!!) / step
                val right = (data.field(x + step, y)!! - data.field(x, y)!!) / step
                largestJump = maxOf(largestJump, kotlin.math.abs(right - left))
            }
        }

        assertTrue(largestJump < 0.05, "largest slope jump $largestJump")
    }

    @Test
    fun interpolatedValue_keepsEveryCellValueAndStaysWithinItsNeighbors() {
        val data = smoothField()

        for (row in 0 until 8) {
            for (column in 0 until 12) {
                assertEquals(
                    data.values[row][column]!!,
                    data.field(column + 0.5, row + 0.5)!!,
                    absoluteTolerance = 1e-9,
                )
            }
        }
        // Between centers the field stays within the four values around it, so it adds no overshoot.
        for (i in 0..220) {
            for (j in 0..140) {
                val x = 0.5 + i / 20.0
                val y = 0.5 + j / 20.0
                val column =
                    kotlin.math
                        .floor(x - 0.5)
                        .toInt()
                        .coerceIn(0, 10)
                val row =
                    kotlin.math
                        .floor(y - 0.5)
                        .toInt()
                        .coerceIn(0, 6)
                val corners =
                    listOf(row to column, row to column + 1, row + 1 to column, row + 1 to column + 1)
                        .map { (r, c) -> data.values[r][c]!! }
                assertTrue(data.field(x, y)!! in corners.min()..corners.max(), "x=$x, y=$y")
            }
        }
    }

    @Test
    fun interpolatedValue_addsNoPeakAndKeepsMonotoneRunsMonotone() {
        // A single peak stays the highest point, at its own center.
        val peak = matrix(listOf(0.0, 2.0, 10.0, 2.0, 0.0))
        val samples = (0..100).map { peak.field(it / 20.0, 0.5)!! }
        assertEquals(expected = 10.0, actual = samples.max())
        assertEquals(expected = 10.0, actual = peak.field(2.5, 0.5))
        // A run that only rises keeps rising, even across a sharp jump, with no dip or bump.
        val step = matrix(listOf(0.0, 1.0, 2.0, 50.0, 51.0, 52.0))
        val run = (0..120).map { step.field(it / 20.0, 0.5)!! }
        assertTrue(run.zipWithNext().all { (a, b) -> b >= a - 1e-9 }, "run $run")
    }

    @Test
    fun interpolatedValue_staysFiniteForTheLargestFiniteValues() {
        val data = matrix(listOf(-Double.MAX_VALUE, Double.MAX_VALUE))
        val mapping = heatmapColorMapping(data, grayScale)

        assertEquals(expected = 0.0, actual = data.field(1.0, 0.5))
        assertEquals(
            expected = Double.MAX_VALUE,
            actual = matrix(listOf(Double.MAX_VALUE, Double.MAX_VALUE)).field(1.0, 0.5),
        )
        assertEquals(expected = 0.5f, actual = mapping.fraction(0.0))
    }

    @Test
    fun colorMapping_normalizesMixedSignValuesOverTheDataDomain() {
        val mapping = heatmapColorMapping(matrix(listOf(-4.0, 12.0), listOf(null, 0.0)), grayScale)

        assertEquals(expected = -4.0, actual = mapping.min)
        assertEquals(expected = 12.0, actual = mapping.max)
        assertEquals(expected = 0f, actual = mapping.fraction(-4.0))
        assertEquals(expected = 0.25f, actual = mapping.fraction(0.0))
        assertEquals(expected = 1f, actual = mapping.fraction(12.0))
    }

    @Test
    fun colorMapping_clampsValuesOutsideFixedBoundsToTheEndColors() {
        val mapping = heatmapColorMapping(matrix(listOf(-50.0, 50.0)), grayScale.copy(min = -10.0, max = 10.0))

        assertColor(expected = Color.Black, actual = mapping.color(-50.0))
        assertColor(expected = Color.Black, actual = mapping.color(-10.0))
        assertColor(expected = Color(0.5f, 0.5f, 0.5f), actual = mapping.color(0.0))
        assertColor(expected = Color.White, actual = mapping.color(10.0))
        assertColor(expected = Color.White, actual = mapping.color(50.0))
    }

    @Test
    fun colorMapping_resolvesEachOptionalBoundIndependently() {
        val data = matrix(listOf(5.0, 15.0))

        val fixedMin = heatmapColorMapping(data, grayScale.copy(min = 0.0))
        val fixedMax = heatmapColorMapping(data, grayScale.copy(max = 20.0))

        assertEquals(expected = 0.0 to 15.0, actual = fixedMin.min to fixedMin.max)
        assertEquals(expected = 5.0 to 20.0, actual = fixedMax.min to fixedMax.max)
    }

    @Test
    fun colorMapping_usesTheBoundEndColorWhenASingleBoundIsBeyondEveryValue() {
        val data = matrix(listOf(10.0, 50.0))

        val aboveMax = heatmapColorMapping(data, grayScale.copy(min = 100.0))
        val belowMin = heatmapColorMapping(data, grayScale.copy(max = 5.0))

        assertColor(expected = Color.Black, actual = aboveMax.color(10.0))
        assertColor(expected = Color.Black, actual = aboveMax.color(50.0))
        assertColor(expected = Color.White, actual = belowMin.color(10.0))
        assertColor(expected = Color.White, actual = belowMin.color(50.0))
        assertEquals(
            expected = listOf(HeatmapLegendLabel(0f, "100")),
            actual = heatmapLegendLabels(aboveMax, StyleDefaults.axisValueFormatter),
        )
    }

    @Test
    fun colorMapping_usesTheMiddleOfTheScaleForConstantValues() {
        val mapping = heatmapColorMapping(matrix(listOf(7.0, 7.0), listOf(7.0, null)), grayScale)

        assertTrue(mapping.isConstant)
        assertEquals(expected = 0.5f, actual = mapping.fraction(7.0))
        assertColor(expected = Color(0.5f, 0.5f, 0.5f), actual = mapping.color(7.0))
    }

    @Test
    fun colorMapping_blendsLinearlyBetweenEvenlySpacedStops() {
        val mapping = HeatmapColorMapping(min = -1.0, max = 1.0, colors = listOf(Color.Red, Color.White, Color.Blue))

        assertEquals(expected = listOf(0f, 0.5f, 1f), actual = mapping.knots)
        assertColor(expected = Color.White, actual = mapping.color(0.0))
        assertColor(expected = Color(1f, 0.5f, 0.5f), actual = mapping.color(-0.5))
        assertColor(expected = Color(0.5f, 0.5f, 1f), actual = mapping.color(0.5))
    }

    @Test
    fun stripStops_matchTheFieldAcrossOneRowWithKnownColors() {
        val data = matrix(listOf(0.0, 10.0))
        val mapping = heatmapColorMapping(data, grayScale)
        val gray = Color(0.5f, 0.5f, 0.5f)

        val stops = heatmapStripStops(data, mapping, row = 0, y = 0.5)

        // Flat beyond the outer centers, a straight ramp between them.
        assertEquals(expected = 0f, actual = stops.first().first)
        assertEquals(expected = 1f, actual = stops.last().first)
        assertTrue(stops.zipWithNext().all { (a, b) -> a.first <= b.first })
        assertColor(expected = Color.Black, actual = colorAt(stops, 0.1f))
        assertColor(expected = Color.Black, actual = colorAt(stops, 0.25f))
        assertColor(expected = Color(0.25f, 0.25f, 0.25f), actual = colorAt(stops, 0.375f))
        assertColor(expected = gray, actual = colorAt(stops, 0.5f))
        assertColor(expected = Color.White, actual = colorAt(stops, 0.75f))
        assertColor(expected = Color.White, actual = colorAt(stops, 0.9f))
    }

    @Test
    fun stripStops_addAStopWhereTheValueCrossesAnInnerColorStop() {
        val colors = listOf(Color.Black, Color.Red, Color.Green, Color.Blue, Color.White)
        val data = matrix(listOf(0.0, 10.0))
        val mapping = heatmapColorMapping(data, HeatmapColorScale(colors))

        val stops = heatmapStripStops(data, mapping, row = 0, y = 0.5)

        // Between the first center and the cell edge the value rises from 0 to 5, crossing the 0.25 stop.
        val crossing = stops.single { it.first == 0.375f }
        assertColor(expected = Color.Red, actual = crossing.second)
        assertTrue(stops.zipWithNext().all { (a, b) -> a.first <= b.first })
    }

    @Test
    fun stripStops_leaveMissingCellsTransparent() {
        val data = matrix(listOf(0.0, null, 10.0))
        val mapping = heatmapColorMapping(data, grayScale)

        val stops = heatmapStripStops(data, mapping, row = 0, y = 0.5)

        val missing = stops.filter { it.first > 1f / 3f && it.first < 2f / 3f }
        assertTrue(missing.isNotEmpty())
        assertTrue(missing.all { it.second == Color.Transparent })
        // Next to the gap, both cells keep their own color.
        assertTrue(
            stops.filter { it.first <= 1f / 3f }.all { it.second == Color.Black || it.second == Color.Transparent },
        )
    }

    @Test
    fun strips_reproduceTheInterpolatedFieldColors() {
        val data =
            matrix(
                listOf(-8.0, 2.0, 14.0, 5.0),
                listOf(3.0, null, 9.0, -1.0),
                listOf(12.0, 7.0, -3.0, 0.0),
            )
        val mapping = heatmapColorMapping(data, HeatmapColorScale(listOf(Color.Blue, Color.White, Color.Red)))
        val layout = HeatmapPlotLayout(left = 0f, top = 0f, width = 400f, height = 300f, columnCount = 4, rowCount = 3)

        val strips = heatmapInterpolatedStrips(data, mapping, layout)

        assertEquals(expected = 300f, actual = strips.sumOf { it.height.toDouble() }.toFloat())
        strips.forEach { strip ->
            val y = (strip.top + strip.height / 2f) / layout.cellHeight
            listOf(0.13, 0.62, 1.13, 1.37, 1.88, 2.21, 2.71, 3.13, 3.94).forEach { x ->
                val expected = data.field(x, y.toDouble())?.let(mapping::color) ?: Color.Transparent
                // Straight gradient segments follow the cubic field to about one 8-bit color step.
                assertColor(expected, colorAt(strip.stops, (x / 4.0).toFloat()), "x=$x, y=$y", tolerance = 2.5f / 255f)
            }
        }
    }

    @Test
    fun strips_sitOnWholePixelsAndCoverEveryRow() {
        val data = matrix(listOf(1.0, 2.0), listOf(3.0, 4.0), listOf(5.0, 6.0))
        val layout = HeatmapPlotLayout(left = 3f, top = 7f, width = 50f, height = 101f, columnCount = 2, rowCount = 3)

        val strips = heatmapInterpolatedStrips(data, heatmapColorMapping(data, grayScale), layout)

        assertEquals(expected = 7f, actual = strips.first().top)
        assertEquals(expected = 108f, actual = strips.last().top + strips.last().height)
        strips.zipWithNext().forEach { (upper, lower) ->
            assertEquals(expected = upper.top + upper.height, actual = lower.top)
        }
        assertTrue(strips.all { it.top == it.top.toInt().toFloat() && it.height >= 1f })
    }

    @Test
    fun cellIndexAt_mapsPointsToCellsIncludingTheGridEdges() {
        val layout = HeatmapPlotLayout(left = 10f, top = 20f, width = 100f, height = 50f, columnCount = 4, rowCount = 2)

        assertEquals(expected = 0, actual = layout.cellIndexAt(Offset(10f, 20f)))
        assertEquals(expected = 3, actual = layout.cellIndexAt(Offset(109.9f, 44.9f)))
        assertEquals(expected = 5, actual = layout.cellIndexAt(Offset(35f, 45f)))
        assertEquals(expected = 7, actual = layout.cellIndexAt(Offset(110f, 70f)))
        assertEquals(expected = NO_SELECTION, actual = layout.cellIndexAt(Offset(9.9f, 30f)))
        assertEquals(expected = NO_SELECTION, actual = layout.cellIndexAt(Offset(50f, 70.1f)))
    }

    @Test
    fun plotLayout_reservesLabelSpaceOnWholePixelsAndKeepsAPositiveGrid() {
        val layout =
            heatmapPlotLayout(
                width = 300f,
                height = 200f,
                leftSpace = 40.4f,
                topSpace = 15f,
                rightSpace = 15f,
                bottomSpace = 30f,
                columnCount = 24,
                rowCount = 7,
            )
        val tiny = heatmapPlotLayout(10f, 10f, 40f, 15f, 15f, 30f, columnCount = 3, rowCount = 3)

        assertEquals(expected = HeatmapPlotLayout(41f, 15f, 244f, 155f, 24, 7), actual = layout)
        val lastCell = layout.cellBounds(row = 6, column = 23)
        assertEquals(expected = layout.right, actual = lastCell.right, absoluteTolerance = 0.001f)
        assertEquals(expected = layout.bottom, actual = lastCell.bottom, absoluteTolerance = 0.001f)
        assertEquals(
            expected = 41f + 244f / 24f,
            actual = layout.cellBounds(row = 2, column = 1).left,
            absoluteTolerance = 0.001f,
        )
        assertEquals(expected = 1f, actual = tiny.width)
        assertEquals(expected = 1f, actual = tiny.height)
    }

    @Test
    fun cellRect_insetsBySpacingButKeepsAtLeastOnePixel() {
        assertEquals(expected = Rect(2f, 2f, 18f, 8f), actual = heatmapCellRect(Rect(0f, 0f, 20f, 10f), spacingPx = 4f))
        assertEquals(
            expected = Rect(1f, 0f, 2f, 0.5f),
            actual = heatmapCellRect(Rect(0f, 0f, 3f, 0.5f), spacingPx = 100f),
        )
        assertEquals(
            expected = Rect(0f, 0f, 20f, 10f),
            actual = heatmapCellRect(Rect(0f, 0f, 20f, 10f), spacingPx = 0f),
        )
    }

    @Test
    fun rowLabelStride_skipsRowsThatWouldOverlapOrExceedTheLabelCount() {
        assertEquals(
            expected = 1,
            actual = heatmapRowLabelStride(7, cellHeightPx = 30f, minSpacingPx = 18f, maxCount = null),
        )
        assertEquals(
            expected = 2,
            actual = heatmapRowLabelStride(24, cellHeightPx = 10f, minSpacingPx = 18f, maxCount = null),
        )
        assertEquals(
            expected = 6,
            actual = heatmapRowLabelStride(24, cellHeightPx = 10f, minSpacingPx = 18f, maxCount = 4),
        )
        assertEquals(
            expected = 24,
            actual = heatmapRowLabelStride(24, cellHeightPx = 0f, minSpacingPx = 18f, maxCount = null),
        )
    }

    @Test
    fun plotAspectRatio_followsTheMatrixShapeWithinLimits() {
        val min = StyleDefaults.heatmapMinPlotAspectRatio
        val max = StyleDefaults.heatmapMaxPlotAspectRatio

        assertEquals(expected = 2.5f, actual = heatmapPlotAspectRatio(24, 7, min, max))
        assertEquals(expected = 2f, actual = heatmapPlotAspectRatio(8, 4, min, max))
        assertEquals(expected = 1f, actual = heatmapPlotAspectRatio(1, 10, min, max))
    }

    @Test
    fun legendLabels_showTheValueAtEveryColorStop() {
        val diverging =
            HeatmapColorMapping(min = -10.0, max = 10.0, colors = listOf(Color.Blue, Color.White, Color.Red))
        val constant = HeatmapColorMapping(min = 7.0, max = 7.0, colors = listOf(Color.Black, Color.White))
        val formatter = StyleDefaults.axisValueFormatter

        assertEquals(
            expected =
                listOf(
                    HeatmapLegendLabel(0f, "-10"),
                    HeatmapLegendLabel(0.5f, "0"),
                    HeatmapLegendLabel(1f, "10"),
                ),
            actual = heatmapLegendLabels(diverging, formatter),
        )
        assertEquals(
            expected = listOf(HeatmapLegendLabel(0.5f, "7")),
            actual = heatmapLegendLabels(constant, formatter),
        )
    }

    @Test
    fun rowLabelColumnWidth_fitsTheWidestLabelUpToFortyPercentOfTheChart() {
        assertEquals(
            expected = 36f,
            actual = heatmapRowLabelColumnWidth(31.2f, edgePaddingPx = 4f, availableWidthPx = 400),
        )
        assertEquals(
            expected = 160f,
            actual = heatmapRowLabelColumnWidth(500f, edgePaddingPx = 4f, availableWidthPx = 400),
        )
        assertEquals(expected = 0f, actual = heatmapRowLabelColumnWidth(20f, edgePaddingPx = 4f, availableWidthPx = 0))
    }

    @Test
    fun label_fallsBackToTheOneBasedNumberForBlankLabels() {
        assertEquals(expected = "Mon", actual = heatmapLabel(listOf("Mon", " "), 0))
        assertEquals(expected = "2", actual = heatmapLabel(listOf("Mon", " "), 1))
    }

    private fun HeatmapData.field(
        x: Double,
        y: Double,
    ): Double? = heatmapInterpolatedValue(this, x, y)

    /** A smooth 12 x 8 field with interior peaks and valleys, sampled at the cell centers. */
    private fun smoothField(): HeatmapData =
        HeatmapData(
            columns = (0 until 12).map { "C$it" },
            rows = (0 until 8).map { "R$it" },
            values =
                (0 until 8).map { r ->
                    (0 until 12).map { c ->
                        50.0 +
                            40.0 * kotlin.math.sin(2 * kotlin.math.PI * c / 9.0) *
                            kotlin.math.cos(2 * kotlin.math.PI * r / 7.0)
                    }
                },
        )

    private fun matrix(vararg rows: List<Double?>): HeatmapData =
        HeatmapData(
            columns = rows.first().indices.map { "C$it" },
            rows = rows.indices.map { "R$it" },
            values = rows.toList(),
        )

    /** The color a gradient through [stops] draws at [offset], blending linearly between stops. */
    private fun colorAt(
        stops: List<Pair<Float, Color>>,
        offset: Float,
    ): Color {
        val after = stops.indexOfFirst { it.first >= offset }
        if (after <= 0) return stops[after.coerceAtLeast(0)].second
        val (startOffset, startColor) = stops[after - 1]
        val (endOffset, endColor) = stops[after]
        val fraction = if (endOffset == startOffset) 1f else (offset - startOffset) / (endOffset - startOffset)
        return lerpSrgb(startColor, endColor, fraction)
    }

    // sRGB colors store 8 bits per channel, so blending two stored stops can round by one step.
    private fun assertColor(
        expected: Color,
        actual: Color,
        message: String? = null,
        tolerance: Float = 1.5f / 255f,
    ) {
        val matches =
            listOf(
                expected.red - actual.red,
                expected.green - actual.green,
                expected.blue - actual.blue,
                expected.alpha - actual.alpha,
            ).all { kotlin.math.abs(it) <= tolerance }
        assertTrue(matches, "${message.orEmpty()} expected $expected, actual $actual")
    }
}
