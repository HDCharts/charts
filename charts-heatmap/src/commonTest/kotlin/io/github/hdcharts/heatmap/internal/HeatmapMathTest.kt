package io.github.hdcharts.heatmap.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.heatmap.HeatmapColorScale
import io.github.hdcharts.heatmap.HeatmapData
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Field coordinates are in cells from the plot's top-left corner, so cell centers sit at n + 0.5.
private const val FIRST_CENTER = 0.5
private const val SECOND_CENTER = 1.5
private const val FIRST_EDGE = 1.0
private const val QUARTER_PAST_FIRST_CENTER = 0.75
private const val QUARTER_BEFORE_SECOND_CENTER = 1.25

// Color-scale positions.
private const val SCALE_START = 0f
private const val SCALE_MIDDLE = 0.5f
private const val SCALE_END = 1f

// The 2 x 2 plane rises by these amounts per column and per row.
private const val PLANE_COLUMN_STEP = 10.0
private const val PLANE_ROW_STEP = 20.0

// A smooth field with interior peaks and valleys: mean + amplitude * sin(2 pi c / 9) * cos(2 pi r / 7).
private const val SMOOTH_COLUMNS = 12
private const val SMOOTH_ROWS = 8
private const val SMOOTH_MEAN = 50.0
private const val SMOOTH_AMPLITUDE = 40.0
private const val SMOOTH_COLUMN_PERIOD = 9.0
private const val SMOOTH_ROW_PERIOD = 7.0

// Agreement for values that should match up to floating-point rounding.
private const val EXACT = 1e-9
private const val PIXEL_TOLERANCE = 0.001f

// One 8-bit color channel step. Blending two stored sRGB stops can round by about one step.
private const val COLOR_STEP = 1f / 255f
private const val ROUNDING_TOLERANCE = 1.5f * COLOR_STEP

// Straight gradient segments follow the cubic field to about one color step.
private const val CUBIC_SEGMENT_TOLERANCE = 2.5f * COLOR_STEP

class HeatmapMathTest {
    private val grayScale = HeatmapColorScale(listOf(Color.Black, Color.White))
    private val divergingScale = HeatmapColorScale(listOf(Color.Blue, Color.White, Color.Red))
    private val midGray = Color(0.5f, 0.5f, 0.5f)

    // 0 and 10 in the top row, 20 and 30 below.
    private val plane =
        matrix(
            listOf(0.0, PLANE_COLUMN_STEP),
            listOf(PLANE_ROW_STEP, PLANE_ROW_STEP + PLANE_COLUMN_STEP),
        )

    // The same plane with its top-right value missing.
    private val planeWithHole =
        matrix(
            listOf(0.0, null),
            listOf(PLANE_ROW_STEP, PLANE_ROW_STEP + PLANE_COLUMN_STEP),
        )

    @Test
    fun valueAtIndex_readsRowMajorCellsAndRejectsOutsideIndices() {
        val data = matrix(listOf(1.0, 2.0, 3.0), listOf(4.0, null, 6.0))
        val lastCell = data.cellIndex(row = 1, column = 2)

        assertEquals(expected = 6.0, actual = data.valueAtIndex(lastCell))
        assertEquals(expected = 4.0, actual = data.valueAtIndex(data.cellIndex(row = 1, column = 0)))
        assertNull(data.valueAtIndex(data.cellIndex(row = 1, column = 1)))
        assertNull(data.valueAtIndex(lastCell + 1))
        assertNull(data.valueAtIndex(NO_SELECTION))
    }

    @Test
    fun interpolatedValue_reproducesALinearFieldExactly() {
        listOf(
            FIRST_CENTER to FIRST_CENTER,
            FIRST_EDGE to FIRST_CENTER,
            FIRST_CENTER to FIRST_EDGE,
            FIRST_EDGE to FIRST_EDGE,
            QUARTER_PAST_FIRST_CENTER to QUARTER_PAST_FIRST_CENTER,
            SECOND_CENTER to SECOND_CENTER,
        ).forEach { (x, y) ->
            assertEquals(
                expected = planeValue(x, y),
                actual = plane.field(x, y)!!,
                absoluteTolerance = EXACT,
                message = "x=$x, y=$y",
            )
        }
    }

    @Test
    fun interpolatedValue_extendsEdgeValuesBeyondTheOutermostCenters() {
        val plotEnd = plane.columns.size.toDouble()
        val outerHalfCell = 0.25

        // Beyond the outermost centers the field holds the value at the nearest center.
        listOf(
            0.0 to 0.0,
            outerHalfCell to outerHalfCell,
            plotEnd to plotEnd,
            FIRST_EDGE to plotEnd - outerHalfCell,
        ).forEach { (x, y) ->
            val expected =
                planeValue(x.coerceIn(FIRST_CENTER, SECOND_CENTER), y.coerceIn(FIRST_CENTER, SECOND_CENTER))
            assertEquals(
                expected = expected,
                actual = plane.field(x, y)!!,
                absoluteTolerance = EXACT,
                message = "x=$x, y=$y",
            )
        }
    }

    @Test
    fun interpolatedValue_singleRowAndSingleColumnInterpolateAlongTheirOnlyAxis() {
        val ramp = listOf(1.0, 3.0, 5.0)
        val row = matrix(ramp)
        val column =
            HeatmapData(
                columns = listOf("A"),
                rows = ramp.indices.map { "R$it" },
                values = ramp.map { listOf(it) },
            )
        val secondEdge = 2.0
        val plotEnd = ramp.size.toDouble()
        // Positions across the single row or column, which has no neighbors to blend with.
        val nearStart = 0.1
        val nearEnd = 0.9
        val only = 7.0

        assertEquals(expected = midpoint(ramp[0], ramp[1]), actual = row.field(FIRST_EDGE, nearStart))
        assertEquals(expected = midpoint(ramp[1], ramp[2]), actual = row.field(secondEdge, nearEnd))
        assertEquals(expected = midpoint(ramp[0], ramp[1]), actual = column.field(nearEnd, FIRST_EDGE))
        assertEquals(expected = ramp.last(), actual = column.field(FIRST_CENTER, plotEnd))
        assertEquals(expected = only, actual = matrix(listOf(only)).field(nearEnd, nearStart))
    }

    @Test
    fun interpolatedValue_neverBlendsAMissingValue() {
        // Next to the missing cell, only the present neighbors blend, with their weights rescaled.
        // Expected values come from an independent evaluation of the same Hermite blend.
        val besideHoleInFirstCell = 5.871766029246344
        val besideHoleInLastCell = 26.38920134983127
        val bottomStart = planeWithHole.values[1][0]!!
        val bottomEnd = planeWithHole.values[1][1]!!

        // The missing cell itself has no value.
        assertNull(planeWithHole.field(SECOND_CENTER, FIRST_CENTER))
        assertEquals(
            expected = besideHoleInFirstCell,
            actual = planeWithHole.field(QUARTER_PAST_FIRST_CENTER, QUARTER_PAST_FIRST_CENTER)!!,
            absoluteTolerance = EXACT,
        )
        assertEquals(
            expected = besideHoleInLastCell,
            actual = planeWithHole.field(QUARTER_BEFORE_SECOND_CENTER, QUARTER_BEFORE_SECOND_CENTER)!!,
            absoluteTolerance = EXACT,
        )
        // Along the bottom row both neighbors exist and the row is a straight run.
        val fractionAlongBottomRow = QUARTER_BEFORE_SECOND_CENTER - FIRST_CENTER
        assertEquals(
            expected = bottomStart + (bottomEnd - bottomStart) * fractionAlongBottomRow,
            actual = planeWithHole.field(QUARTER_BEFORE_SECOND_CENTER, SECOND_CENTER),
        )
    }

    @Test
    fun interpolatedValue_staysContinuousAcrossCellEdgesBesideAMissingValue() {
        // Regression: cells beside a missing value used to switch to their own flat value, which put a
        // step of the full difference between neighbors on every edge around the gap.
        val nearHole = QUARTER_BEFORE_SECOND_CENTER
        val justAcross = 1e-9
        val continuityTolerance = 1e-6
        val aroundHole = listOf(0.6, 0.9, 1.1, 1.4)
        val presentValues = planeWithHole.values.flatten().filterNotNull()

        val left = planeWithHole.field(FIRST_EDGE - justAcross, nearHole)!!
        val right = planeWithHole.field(FIRST_EDGE + justAcross, nearHole)!!
        val above = planeWithHole.field(QUARTER_PAST_FIRST_CENTER, FIRST_EDGE - justAcross)!!
        val below = planeWithHole.field(QUARTER_PAST_FIRST_CENTER, FIRST_EDGE + justAcross)!!

        assertEquals(expected = left, actual = right, absoluteTolerance = continuityTolerance)
        assertEquals(expected = above, actual = below, absoluteTolerance = continuityTolerance)
        // The blend never leaves the range of the values it blends.
        aroundHole.forEach { x ->
            aroundHole.forEach { y ->
                planeWithHole.field(x, y)?.let { value ->
                    assertTrue(value in presentValues.min()..presentValues.max(), "x=$x, y=$y, value=$value")
                }
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
        val samplesPerCell = 4
        // A smooth field differs only by finite-difference error here.
        val maxSlopeJump = 0.05

        var largestJump = 0.0
        for (column in 1 until SMOOTH_COLUMNS - 1) {
            for (sample in 0..(SMOOTH_ROWS - 1) * samplesPerCell) {
                val x = column + FIRST_CENTER
                val y = FIRST_CENTER + sample / samplesPerCell.toDouble()
                val left = (data.field(x, y)!! - data.field(x - step, y)!!) / step
                val right = (data.field(x + step, y)!! - data.field(x, y)!!) / step
                largestJump = maxOf(largestJump, abs(right - left))
            }
        }

        assertTrue(largestJump < maxSlopeJump, "largest slope jump $largestJump")
    }

    @Test
    fun interpolatedValue_keepsEveryCellValueAndStaysWithinItsNeighbors() {
        val data = smoothField()
        val samplesPerCell = 20

        for (row in 0 until SMOOTH_ROWS) {
            for (column in 0 until SMOOTH_COLUMNS) {
                assertEquals(
                    expected = data.values[row][column]!!,
                    actual = data.field(column + FIRST_CENTER, row + FIRST_CENTER)!!,
                    absoluteTolerance = EXACT,
                )
            }
        }
        // Between centers the field stays within the four values around it, so it adds no overshoot.
        for (i in 0..(SMOOTH_COLUMNS - 1) * samplesPerCell) {
            for (j in 0..(SMOOTH_ROWS - 1) * samplesPerCell) {
                val x = FIRST_CENTER + i / samplesPerCell.toDouble()
                val y = FIRST_CENTER + j / samplesPerCell.toDouble()
                val column = floor(x - FIRST_CENTER).toInt().coerceIn(0, SMOOTH_COLUMNS - 2)
                val row = floor(y - FIRST_CENTER).toInt().coerceIn(0, SMOOTH_ROWS - 2)
                val corners =
                    listOf(row to column, row to column + 1, row + 1 to column, row + 1 to column + 1)
                        .map { (r, c) -> data.values[r][c]!! }
                assertTrue(data.field(x, y)!! in corners.min()..corners.max(), "x=$x, y=$y")
            }
        }
    }

    @Test
    fun interpolatedValue_addsNoPeakAndKeepsMonotoneRunsMonotone() {
        val samplesPerCell = 20

        // A single peak stays the highest point, at its own center.
        val peakValue = 10.0
        val peakColumn = 2
        val peak = matrix(listOf(0.0, 2.0, peakValue, 2.0, 0.0))
        val samples = peak.samplesAcrossFirstRow(samplesPerCell)
        assertEquals(expected = peakValue, actual = samples.max())
        assertEquals(expected = peakValue, actual = peak.field(peakColumn + FIRST_CENTER, FIRST_CENTER))

        // A run that only rises keeps rising, even across a sharp jump, with no dip or bump.
        val step = matrix(listOf(0.0, 1.0, 2.0, 50.0, 51.0, 52.0))
        val run = step.samplesAcrossFirstRow(samplesPerCell)
        assertTrue(run.zipWithNext().all { (a, b) -> b >= a - EXACT }, "run $run")
    }

    @Test
    fun interpolatedValue_staysFiniteForTheLargestFiniteValues() {
        val data = matrix(listOf(-Double.MAX_VALUE, Double.MAX_VALUE))
        val mapping = heatmapColorMapping(data, grayScale)

        // Halfway between the largest opposite values is zero, without overflowing on the way.
        assertEquals(expected = 0.0, actual = data.field(FIRST_EDGE, FIRST_CENTER))
        assertEquals(
            expected = Double.MAX_VALUE,
            actual = matrix(listOf(Double.MAX_VALUE, Double.MAX_VALUE)).field(FIRST_EDGE, FIRST_CENTER),
        )
        assertEquals(expected = SCALE_MIDDLE, actual = mapping.fraction(0.0))
    }

    @Test
    fun colorMapping_normalizesMixedSignValuesOverTheDataDomain() {
        val low = -4.0
        val high = 12.0
        val zero = 0.0
        val mapping = heatmapColorMapping(matrix(listOf(low, high), listOf(null, zero)), grayScale)

        assertEquals(expected = low, actual = mapping.min)
        assertEquals(expected = high, actual = mapping.max)
        assertEquals(expected = SCALE_START, actual = mapping.fraction(low))
        assertEquals(expected = ((zero - low) / (high - low)).toFloat(), actual = mapping.fraction(zero))
        assertEquals(expected = SCALE_END, actual = mapping.fraction(high))
    }

    @Test
    fun colorMapping_clampsValuesOutsideFixedBoundsToTheEndColors() {
        val bound = 10.0
        val beyondBound = 50.0
        val mapping =
            heatmapColorMapping(matrix(listOf(-beyondBound, beyondBound)), grayScale.copy(min = -bound, max = bound))

        assertColor(expected = Color.Black, actual = mapping.color(-beyondBound))
        assertColor(expected = Color.Black, actual = mapping.color(-bound))
        assertColor(expected = midGray, actual = mapping.color(0.0))
        assertColor(expected = Color.White, actual = mapping.color(bound))
        assertColor(expected = Color.White, actual = mapping.color(beyondBound))
    }

    @Test
    fun colorMapping_resolvesEachOptionalBoundIndependently() {
        val dataMin = 5.0
        val dataMax = 15.0
        val fixedMin = 0.0
        val fixedMax = 20.0
        val data = matrix(listOf(dataMin, dataMax))

        val withFixedMin = heatmapColorMapping(data, grayScale.copy(min = fixedMin))
        val withFixedMax = heatmapColorMapping(data, grayScale.copy(max = fixedMax))

        assertEquals(expected = fixedMin to dataMax, actual = withFixedMin.min to withFixedMin.max)
        assertEquals(expected = dataMin to fixedMax, actual = withFixedMax.min to withFixedMax.max)
    }

    @Test
    fun colorMapping_usesTheBoundEndColorWhenASingleBoundIsBeyondEveryValue() {
        val dataMin = 10.0
        val dataMax = 50.0
        val aboveData = 100.0
        val belowData = 5.0
        val data = matrix(listOf(dataMin, dataMax))

        val minAboveData = heatmapColorMapping(data, grayScale.copy(min = aboveData))
        val maxBelowData = heatmapColorMapping(data, grayScale.copy(max = belowData))

        assertColor(expected = Color.Black, actual = minAboveData.color(dataMin))
        assertColor(expected = Color.Black, actual = minAboveData.color(dataMax))
        assertColor(expected = Color.White, actual = maxBelowData.color(dataMin))
        assertColor(expected = Color.White, actual = maxBelowData.color(dataMax))
        // The legend shows the bound once, at the end of the scale its color comes from.
        assertEquals(
            expected = listOf(HeatmapLegendLabel(SCALE_START, "100")),
            actual = heatmapLegendLabels(minAboveData, StyleDefaults.axisValueFormatter),
        )
    }

    @Test
    fun colorMapping_usesTheMiddleOfTheScaleForConstantValues() {
        val value = 7.0
        val mapping = heatmapColorMapping(matrix(listOf(value, value), listOf(value, null)), grayScale)

        assertTrue(mapping.isConstant)
        assertEquals(expected = SCALE_MIDDLE, actual = mapping.fraction(value))
        assertColor(expected = midGray, actual = mapping.color(value))
    }

    @Test
    fun colorMapping_blendsLinearlyBetweenEvenlySpacedStops() {
        val bound = 1.0
        val halfway = 0.5
        val mapping =
            HeatmapColorMapping(min = -bound, max = bound, colors = listOf(Color.Red, Color.White, Color.Blue))

        assertEquals(expected = listOf(SCALE_START, SCALE_MIDDLE, SCALE_END), actual = mapping.knots)
        assertColor(expected = Color.White, actual = mapping.color(0.0))
        // Halfway between red and white, and between white and blue.
        assertColor(expected = Color(1f, 0.5f, 0.5f), actual = mapping.color(-halfway))
        assertColor(expected = Color(0.5f, 0.5f, 1f), actual = mapping.color(halfway))
    }

    @Test
    fun stripStops_matchTheFieldAcrossOneRowWithKnownColors() {
        val data = matrix(listOf(0.0, 10.0))
        val mapping = heatmapColorMapping(data, grayScale)
        // Offsets are fractions of the plot width. Two columns put the centers at a quarter and three quarters.
        val plotStart = 0f
        val plotEnd = 1f
        val firstCenter = 0.25f
        val sharedEdge = 0.5f
        val secondCenter = 0.75f
        val quarterBetweenCenters = 0.375f
        val insideOuterHalfCell = 0.1f
        val quarterGray = Color(0.25f, 0.25f, 0.25f)

        val stops = heatmapStripStops(data, mapping, row = 0, y = FIRST_CENTER)

        // Flat beyond the outer centers, a straight ramp between them.
        assertEquals(expected = plotStart, actual = stops.first().first)
        assertEquals(expected = plotEnd, actual = stops.last().first)
        assertTrue(stops.zipWithNext().all { (a, b) -> a.first <= b.first })
        assertColor(expected = Color.Black, actual = colorAt(stops, insideOuterHalfCell))
        assertColor(expected = Color.Black, actual = colorAt(stops, firstCenter))
        assertColor(expected = quarterGray, actual = colorAt(stops, quarterBetweenCenters))
        assertColor(expected = midGray, actual = colorAt(stops, sharedEdge))
        assertColor(expected = Color.White, actual = colorAt(stops, secondCenter))
        assertColor(expected = Color.White, actual = colorAt(stops, plotEnd - insideOuterHalfCell))
    }

    @Test
    fun stripStops_addAStopWhereTheValueCrossesAnInnerColorStop() {
        val colors = listOf(Color.Black, Color.Red, Color.Green, Color.Blue, Color.White)
        val data = matrix(listOf(0.0, 10.0))
        val mapping = heatmapColorMapping(data, HeatmapColorScale(colors))
        // Red sits at a quarter of the scale, value 2.5. The value rises straight from 0 at the first center
        // (offset 0.25) to 10 at the second (offset 0.75), so it reaches 2.5 at offset 0.375.
        val redStopOffset = 0.375f

        val stops = heatmapStripStops(data, mapping, row = 0, y = FIRST_CENTER)

        val crossing = stops.single { it.first == redStopOffset }
        assertColor(expected = Color.Red, actual = crossing.second)
        assertTrue(stops.zipWithNext().all { (a, b) -> a.first <= b.first })
    }

    @Test
    fun stripStops_leaveMissingCellsTransparent() {
        val data = matrix(listOf(0.0, null, 10.0))
        val mapping = heatmapColorMapping(data, grayScale)
        val gapStart = 1f / data.columns.size
        val gapEnd = 2f / data.columns.size

        val stops = heatmapStripStops(data, mapping, row = 0, y = FIRST_CENTER)

        val missing = stops.filter { it.first > gapStart && it.first < gapEnd }
        assertTrue(missing.isNotEmpty())
        assertTrue(missing.all { it.second == Color.Transparent })
        // Next to the gap, both cells keep their own color.
        assertTrue(
            stops.filter { it.first <= gapStart }.all { it.second == Color.Black || it.second == Color.Transparent },
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
        val mapping = heatmapColorMapping(data, divergingScale)
        val layout =
            HeatmapPlotLayout(
                left = 0f,
                top = 0f,
                width = 400f,
                height = 300f,
                columnCount = data.columns.size,
                rowCount = data.rows.size,
            )
        // Positions in cells, on both sides of every center and in every cell, away from cell edges.
        val sampleColumns = listOf(0.13, 0.62, 1.13, 1.37, 1.88, 2.21, 2.71, 3.13, 3.94)

        val strips = heatmapInterpolatedStrips(data, mapping, layout)

        assertEquals(expected = layout.height, actual = strips.sumOf { it.height.toDouble() }.toFloat())
        strips.forEach { strip ->
            val y = (strip.top + strip.height / 2f) / layout.cellHeight
            sampleColumns.forEach { x ->
                val expected = data.field(x, y.toDouble())?.let(mapping::color) ?: Color.Transparent
                assertColor(
                    expected = expected,
                    actual = colorAt(strip.stops, (x / layout.columnCount).toFloat()),
                    message = "x=$x, y=$y",
                    tolerance = CUBIC_SEGMENT_TOLERANCE,
                )
            }
        }
    }

    @Test
    fun strips_boundTheirWorkByThePlotSizeForDenseMatrices() {
        // Regression: every half cell used to get four segments however narrow it was, so a 300 x 300
        // matrix on a 1000 x 400 pixel plot built 3,071 stops per strip and about 920,000 in total.
        val size = 300
        val plotWidth = 1000f
        val plotHeight = 400f
        val columnFrequency = 0.37
        val rowFrequency = 0.21
        // One segment per half cell, plus the color-stop crossings, stays within three stops per column.
        val maxStopsPerColumn = 3
        val data =
            HeatmapData(
                columns = (0 until size).map { "C$it" },
                rows = (0 until size).map { "R$it" },
                values =
                    (0 until size).map { r ->
                        (0 until size).map { c ->
                            SMOOTH_MEAN +
                                SMOOTH_AMPLITUDE * sin(c * columnFrequency + r * rowFrequency)
                        }
                    },
            )
        val mapping = heatmapColorMapping(data, divergingScale)
        val layout =
            HeatmapPlotLayout(
                left = 0f,
                top = 0f,
                width = plotWidth,
                height = plotHeight,
                columnCount = size,
                rowCount = size,
            )

        val strips = heatmapInterpolatedStrips(data, mapping, layout)

        // At most one strip per pixel row.
        assertTrue(strips.size <= plotHeight.toInt(), "strips ${strips.size}")
        val stopsPerStrip = strips.maxOf { it.stops.size }
        assertTrue(stopsPerStrip <= maxStopsPerColumn * size, "stops per strip $stopsPerStrip")
        // The stops still follow the field at their own positions: cell centers and a cell edge.
        // Rows here are 1.33 pixels tall, so each strip samples its own row at the row's center.
        val strip = strips[strips.size / 2]
        val y = ((strip.top + strip.height / 2f) / layout.cellHeight).toInt() + FIRST_CENTER
        val stopColumns = listOf(10.5, 151.0, 299.5)
        stopColumns.forEach { x ->
            assertColor(
                expected = mapping.color(data.field(x, y)!!),
                actual = colorAt(strip.stops, (x / size).toFloat()),
                message = "x=$x",
            )
        }
    }

    @Test
    fun strips_sitOnWholePixelsAndCoverEveryRow() {
        val data = matrix(listOf(1.0, 2.0), listOf(3.0, 4.0), listOf(5.0, 6.0))
        // An offset grid whose height is not a multiple of its row count.
        val layout =
            HeatmapPlotLayout(
                left = 3f,
                top = 7f,
                width = 50f,
                height = 101f,
                columnCount = data.columns.size,
                rowCount = data.rows.size,
            )
        val onePixel = 1f

        val strips = heatmapInterpolatedStrips(data, heatmapColorMapping(data, grayScale), layout)

        assertEquals(expected = layout.top, actual = strips.first().top)
        assertEquals(expected = layout.bottom, actual = strips.last().top + strips.last().height)
        strips.zipWithNext().forEach { (upper, lower) ->
            assertEquals(expected = upper.top + upper.height, actual = lower.top)
        }
        assertTrue(strips.all { it.top == it.top.toInt().toFloat() && it.height >= onePixel })
    }

    @Test
    fun cellIndexAt_mapsPointsToCellsIncludingTheGridEdges() {
        val layout = HeatmapPlotLayout(left = 10f, top = 20f, width = 100f, height = 50f, columnCount = 4, rowCount = 2)
        val nudge = 0.1f
        val lastRow = layout.rowCount - 1
        val lastColumn = layout.columnCount - 1

        fun index(
            row: Int,
            column: Int,
        ) = row * layout.columnCount + column

        assertEquals(expected = index(0, 0), actual = layout.cellIndexAt(Offset(layout.left, layout.top)))
        assertEquals(
            expected = index(0, lastColumn),
            actual = layout.cellIndexAt(Offset(layout.right - nudge, layout.top + layout.cellHeight - nudge)),
        )
        // A point on an inner edge belongs to the cell after it.
        assertEquals(
            expected = index(1, 1),
            actual = layout.cellIndexAt(Offset(layout.left + layout.cellWidth, layout.top + layout.cellHeight)),
        )
        // The right and bottom grid edges belong to the last column and row.
        assertEquals(
            expected = index(lastRow, lastColumn),
            actual = layout.cellIndexAt(Offset(layout.right, layout.bottom)),
        )
        assertEquals(
            expected = NO_SELECTION,
            actual = layout.cellIndexAt(Offset(layout.left - nudge, layout.top + layout.cellHeight)),
        )
        assertEquals(
            expected = NO_SELECTION,
            actual = layout.cellIndexAt(Offset(layout.left + layout.width / 2, layout.bottom + nudge)),
        )
    }

    @Test
    fun plotLayout_reservesLabelSpaceOnWholePixelsAndKeepsAPositiveGrid() {
        val width = 300f
        val height = 200f
        val leftSpace = 40.4f
        val topSpace = 15f
        val rightSpace = 15f
        val bottomSpace = 30f
        val columns = 24
        val rows = 7
        val expectedLeft = 41f // 40.4 rounded up to a whole pixel
        val expectedWidth = 244f // 300 - 41 - 15
        val expectedHeight = 155f // 200 - 15 - 30
        val minimumGridSize = 1f
        val tinyChartSize = 10f
        val tinyGridCells = 3
        val secondColumn = 1

        val layout =
            heatmapPlotLayout(
                width = width,
                height = height,
                leftSpace = leftSpace,
                topSpace = topSpace,
                rightSpace = rightSpace,
                bottomSpace = bottomSpace,
                columnCount = columns,
                rowCount = rows,
            )
        // Labels leave no room in a chart smaller than its label space.
        val tiny =
            heatmapPlotLayout(
                width = tinyChartSize,
                height = tinyChartSize,
                leftSpace = leftSpace,
                topSpace = topSpace,
                rightSpace = rightSpace,
                bottomSpace = bottomSpace,
                columnCount = tinyGridCells,
                rowCount = tinyGridCells,
            )

        assertEquals(
            expected = HeatmapPlotLayout(expectedLeft, topSpace, expectedWidth, expectedHeight, columns, rows),
            actual = layout,
        )
        val lastCell = layout.cellBounds(row = rows - 1, column = columns - 1)
        assertEquals(expected = layout.right, actual = lastCell.right, absoluteTolerance = PIXEL_TOLERANCE)
        assertEquals(expected = layout.bottom, actual = lastCell.bottom, absoluteTolerance = PIXEL_TOLERANCE)
        assertEquals(
            expected = expectedLeft + expectedWidth / columns * secondColumn,
            actual = layout.cellBounds(row = 2, column = secondColumn).left,
            absoluteTolerance = PIXEL_TOLERANCE,
        )
        assertEquals(expected = minimumGridSize, actual = tiny.width)
        assertEquals(expected = minimumGridSize, actual = tiny.height)
    }

    @Test
    fun cellRect_insetsBySpacingButKeepsAtLeastOnePixel() {
        val slot = Rect(0f, 0f, 20f, 10f)
        val spacing = 4f
        val inset = spacing / 2
        // Spacing far wider than a slot still leaves one pixel across, centered; a slot under a pixel tall
        // keeps its height.
        val tinySlot = Rect(0f, 0f, 3f, 0.5f)
        val hugeSpacing = 100f
        val onePixelCentered = Rect(1f, tinySlot.top, 2f, tinySlot.bottom)

        assertEquals(
            expected = Rect(slot.left + inset, slot.top + inset, slot.right - inset, slot.bottom - inset),
            actual = heatmapCellRect(slot, spacingPx = spacing),
        )
        assertEquals(expected = onePixelCentered, actual = heatmapCellRect(tinySlot, spacingPx = hugeSpacing))
        assertEquals(expected = slot, actual = heatmapCellRect(slot, spacingPx = 0f))
    }

    @Test
    fun rowLabelStride_skipsRowsThatWouldOverlapOrExceedTheLabelCount() {
        val labelSpacing = 18f
        val fewRows = 7
        val manyRows = 24
        val tallCell = 30f
        val shortCell = 10f
        val maxLabels = 4
        // Ten-pixel rows need two rows for each 18-pixel label.
        val shortCellStride = 2

        assertEquals(
            expected = 1,
            actual =
                heatmapRowLabelStride(
                    rowCount = fewRows,
                    cellHeightPx = tallCell,
                    minSpacingPx = labelSpacing,
                    maxCount = null,
                ),
        )
        assertEquals(
            expected = shortCellStride,
            actual =
                heatmapRowLabelStride(
                    rowCount = manyRows,
                    cellHeightPx = shortCell,
                    minSpacingPx = labelSpacing,
                    maxCount = null,
                ),
        )
        assertEquals(
            expected = manyRows / maxLabels,
            actual =
                heatmapRowLabelStride(
                    rowCount = manyRows,
                    cellHeightPx = shortCell,
                    minSpacingPx = labelSpacing,
                    maxCount = maxLabels,
                ),
        )
        // Without row height, only the first row gets a label.
        assertEquals(
            expected = manyRows,
            actual =
                heatmapRowLabelStride(
                    rowCount = manyRows,
                    cellHeightPx = 0f,
                    minSpacingPx = labelSpacing,
                    maxCount = null,
                ),
        )
    }

    @Test
    fun plotAspectRatio_followsTheMatrixShapeWithinLimits() {
        val min = StyleDefaults.heatmapMinPlotAspectRatio
        val max = StyleDefaults.heatmapMaxPlotAspectRatio
        val wide = 24 to 7
        val withinLimits = 8 to 4
        val tall = 1 to 10

        assertEquals(expected = max, actual = heatmapPlotAspectRatio(wide.first, wide.second, min, max))
        assertEquals(
            expected = withinLimits.first.toFloat() / withinLimits.second,
            actual = heatmapPlotAspectRatio(withinLimits.first, withinLimits.second, min, max),
        )
        assertEquals(expected = min, actual = heatmapPlotAspectRatio(tall.first, tall.second, min, max))
    }

    @Test
    fun legendLabels_showTheValueAtEveryColorStop() {
        val bound = 10.0
        val constantValue = 7.0
        val diverging = HeatmapColorMapping(min = -bound, max = bound, colors = divergingScale.colors)
        val constant = HeatmapColorMapping(min = constantValue, max = constantValue, colors = grayScale.colors)
        val formatter = StyleDefaults.axisValueFormatter

        assertEquals(
            expected =
                listOf(
                    HeatmapLegendLabel(SCALE_START, "-10"),
                    HeatmapLegendLabel(SCALE_MIDDLE, "0"),
                    HeatmapLegendLabel(SCALE_END, "10"),
                ),
            actual = heatmapLegendLabels(diverging, formatter),
        )
        assertEquals(
            expected = listOf(HeatmapLegendLabel(SCALE_MIDDLE, "7")),
            actual = heatmapLegendLabels(constant, formatter),
        )
    }

    @Test
    fun rowLabelColumnWidth_fitsTheWidestLabelUpToFortyPercentOfTheChart() {
        val edgePadding = 4f
        val availableWidth = 400
        val maxShare = 0.4f
        val shortLabel = 31.2f
        val longLabel = 500f

        // The widest label plus padding, rounded up to a whole pixel.
        assertEquals(
            expected = ceil(shortLabel + edgePadding),
            actual =
                heatmapRowLabelColumnWidth(
                    shortLabel,
                    edgePaddingPx = edgePadding,
                    availableWidthPx = availableWidth,
                ),
        )
        assertEquals(
            expected = availableWidth * maxShare,
            actual =
                heatmapRowLabelColumnWidth(
                    longLabel,
                    edgePaddingPx = edgePadding,
                    availableWidthPx = availableWidth,
                ),
            absoluteTolerance = PIXEL_TOLERANCE,
        )
        assertEquals(
            expected = 0f,
            actual = heatmapRowLabelColumnWidth(shortLabel, edgePaddingPx = edgePadding, availableWidthPx = 0),
        )
    }

    @Test
    fun label_fallsBackToTheOneBasedNumberForBlankLabels() {
        val labels = listOf("Mon", " ")
        val blankIndex = 1

        assertEquals(expected = labels[0], actual = heatmapLabel(labels, 0))
        assertEquals(expected = "${blankIndex + 1}", actual = heatmapLabel(labels, blankIndex))
    }

    private fun HeatmapData.field(
        x: Double,
        y: Double,
    ): Double? = heatmapInterpolatedValue(this, x, y)

    /** The field across the whole first row, [samplesPerCell] samples per cell. */
    private fun HeatmapData.samplesAcrossFirstRow(samplesPerCell: Int): List<Double> =
        (0..columns.size * samplesPerCell).map { field(it / samplesPerCell.toDouble(), FIRST_CENTER)!! }

    /** The exact plane value at ([x], [y]), which rises linearly from the first center. */
    private fun planeValue(
        x: Double,
        y: Double,
    ): Double = PLANE_COLUMN_STEP * (x - FIRST_CENTER) + PLANE_ROW_STEP * (y - FIRST_CENTER)

    private fun midpoint(
        a: Double,
        b: Double,
    ): Double = (a + b) / 2

    /** A smooth field with interior peaks and valleys, sampled at the cell centers. */
    private fun smoothField(): HeatmapData =
        HeatmapData(
            columns = (0 until SMOOTH_COLUMNS).map { "C$it" },
            rows = (0 until SMOOTH_ROWS).map { "R$it" },
            values =
                (0 until SMOOTH_ROWS).map { r ->
                    (0 until SMOOTH_COLUMNS).map { c ->
                        SMOOTH_MEAN +
                            SMOOTH_AMPLITUDE * sin(2 * PI * c / SMOOTH_COLUMN_PERIOD) *
                            cos(2 * PI * r / SMOOTH_ROW_PERIOD)
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

    private fun assertColor(
        expected: Color,
        actual: Color,
        message: String? = null,
        tolerance: Float = ROUNDING_TOLERANCE,
    ) {
        val matches =
            listOf(
                expected.red - actual.red,
                expected.green - actual.green,
                expected.blue - actual.blue,
                expected.alpha - actual.alpha,
            ).all { abs(it) <= tolerance }
        assertTrue(matches, "${message.orEmpty()} expected $expected, actual $actual")
    }
}
