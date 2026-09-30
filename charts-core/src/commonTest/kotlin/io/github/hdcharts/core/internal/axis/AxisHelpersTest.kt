package io.github.hdcharts.core.internal.axis

import androidx.compose.ui.unit.IntOffset
import io.github.hdcharts.core.model.ChartValueFormatter
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AxisHelpersTest {
    @Test
    fun yAxisTickCount_nullMaxCount_usesFiveTicksWhenTheyFit() {
        assertEquals(expected = 5, actual = yAxisTickCount(maxCount = null, spanPx = 400f, fontSizePx = 10f))
    }

    @Test
    fun yAxisTickCount_setMaxCount_isUsedWhenItFits() {
        assertEquals(expected = 8, actual = yAxisTickCount(maxCount = 8, spanPx = 400f, fontSizePx = 10f))
    }

    @Test
    fun yAxisTickCount_shortSpan_dropsTicksThatWouldOverlap() {
        // Ticks need 10 * 1.2 * 1.5 = 18 px between them, so 72 px holds five and 71 px only four.
        assertEquals(expected = 5, actual = yAxisTickCount(maxCount = 1000, spanPx = 72f, fontSizePx = 10f))
        assertEquals(expected = 4, actual = yAxisTickCount(maxCount = 1000, spanPx = 71f, fontSizePx = 10f))
        assertEquals(expected = 4, actual = yAxisTickCount(maxCount = null, spanPx = 71f, fontSizePx = 10f))
    }

    @Test
    fun yAxisTickCount_noRoom_keepsBothEnds() {
        listOf(0f, -5f, Float.NaN, Float.NEGATIVE_INFINITY).forEach { spanPx ->
            assertEquals(
                expected = 2,
                actual = yAxisTickCount(maxCount = 8, spanPx = spanPx, fontSizePx = 10f),
                message = "spanPx = $spanPx",
            )
        }
    }

    @Test
    fun yAxisTickCount_hugeSpanOrZeroFont_usesMaxCount() {
        assertEquals(
            expected = Int.MAX_VALUE,
            actual = yAxisTickCount(maxCount = Int.MAX_VALUE, spanPx = Float.MAX_VALUE, fontSizePx = 10f),
        )
        assertEquals(expected = 7, actual = yAxisTickCount(maxCount = 7, spanPx = 10f, fontSizePx = 0f))
    }

    @Test
    fun yAxisLabelMinSpacingPx_isOneLinePlusHalfALineGap() {
        assertEquals(expected = 18f, actual = yAxisLabelMinSpacingPx(fontSizePx = 10f), absoluteTolerance = 0.001f)
        assertEquals(expected = 0f, actual = yAxisLabelMinSpacingPx(fontSizePx = -3f))
    }

    @Test
    fun estimateYAxisLabelWidthPx_usesLongestLabelWithoutPadding() {
        val width = estimateYAxisLabelWidthPx(labels = listOf("5", "-123.45"), fontSizePx = 10f)

        assertTrue(kotlin.math.abs(width - 40.6f) < 0.001f)
    }

    @Test
    fun estimateYAxisLabelWidthPx_noLabels_returnsZero() {
        assertEquals(expected = 0f, actual = estimateYAxisLabelWidthPx(labels = emptyList(), fontSizePx = 10f))
    }

    @Test
    fun buildNumericYAxisTicks_appliesInsetToFirstAndLastTick() {
        val ticks =
            buildNumericYAxisTicks(
                minValue = 0.0,
                maxValue = 100.0,
                labelCount = 5,
                plotHeightPx = 200f,
                verticalInsetPx = 10f,
                formatter = defaultAxisValueFormatter,
            )

        assertEquals(expected = 5, actual = ticks.size)
        assertEquals(expected = "100", actual = ticks.first().label)
        assertEquals(expected = "0", actual = ticks.last().label)
        assertEquals(expected = 10f, actual = ticks.first().centerY)
        assertEquals(expected = 190f, actual = ticks.last().centerY)
    }

    @Test
    fun buildNumericYAxisTicks_withoutInset_usesPlotBoundaries() {
        val ticks =
            buildNumericYAxisTicks(
                minValue = -10.0,
                maxValue = 30.0,
                labelCount = 5,
                plotHeightPx = 200f,
                verticalInsetPx = 0f,
                formatter = defaultAxisValueFormatter,
            )

        assertEquals(expected = 5, actual = ticks.size)
        assertEquals(expected = "30", actual = ticks.first().label)
        assertEquals(expected = "-10", actual = ticks.last().label)
        assertEquals(expected = 0f, actual = ticks.first().centerY)
        assertEquals(expected = 200f, actual = ticks.last().centerY)
    }

    @Test
    fun buildNumericYAxisTicks_nonPositiveHeight_returnsEmpty() {
        val zeroHeight =
            buildNumericYAxisTicks(
                minValue = 0.0,
                maxValue = 10.0,
                labelCount = 3,
                plotHeightPx = 0f,
                verticalInsetPx = 0f,
                formatter = defaultAxisValueFormatter,
            )
        val negativeHeight =
            buildNumericYAxisTicks(
                minValue = 0.0,
                maxValue = 10.0,
                labelCount = 3,
                plotHeightPx = -1f,
                verticalInsetPx = 0f,
                formatter = defaultAxisValueFormatter,
            )

        assertTrue(zeroHeight.isEmpty())
        assertTrue(negativeHeight.isEmpty())
    }

    @Test
    fun buildNumericYAxisTicks_negativeRange_runsFromMaxAtTopToMinAtBottom() {
        val ticks = numericTicks(minValue = -50.0, maxValue = -10.0, labelCount = 5)

        assertEquals(expected = listOf("-10", "-20", "-30", "-40", "-50"), actual = ticks.map { tick -> tick.label })
        assertEquals(expected = listOf(0f, 50f, 100f, 150f, 200f), actual = ticks.map { tick -> tick.centerY })
    }

    @Test
    fun buildNumericYAxisTicks_rangeCrossingZero_hasExactZeroTick() {
        val values = mutableListOf<Double>()
        numericTicks(minValue = -10.0, maxValue = 30.0, labelCount = 5, formatter = recordingFormatter(values))

        assertEquals(expected = listOf(30.0, 20.0, 10.0, 0.0, -10.0), actual = values)
    }

    @Test
    fun buildNumericYAxisTicks_equalMinAndMax_repeatsValueAtEvenSpacing() {
        val ticks = numericTicks(minValue = 5.0, maxValue = 5.0, labelCount = 3)

        assertEquals(expected = listOf("5", "5", "5"), actual = ticks.map { tick -> tick.label })
        assertEquals(expected = listOf(0f, 100f, 200f), actual = ticks.map { tick -> tick.centerY })
    }

    @Test
    fun buildNumericYAxisTicks_equalMinAndMax_keepsExactValueForEveryCount() {
        // 0.375 prints as 0.38, but a tick one double below it prints as 0.37.
        for (labelCount in 2..12) {
            val values = mutableListOf<Double>()
            numericTicks(
                minValue = 0.375,
                maxValue = 0.375,
                labelCount = labelCount,
                formatter = recordingFormatter(values),
            )

            assertEquals(expected = List(labelCount) { 0.375 }, actual = values, message = "count $labelCount")
        }
    }

    @Test
    fun buildNumericYAxisTicks_extremeSignedRange_staysFinite() {
        val values = mutableListOf<Double>()
        numericTicks(
            minValue = -Double.MAX_VALUE,
            maxValue = Double.MAX_VALUE,
            labelCount = 5,
            formatter = recordingFormatter(values),
        )

        // max - min overflows here; the ticks must not.
        assertTrue(values.all { value -> value.isFinite() }, "$values")
        assertEquals(expected = Double.MAX_VALUE, actual = values.first())
        assertEquals(expected = 0.0, actual = values[2])
        assertEquals(expected = -Double.MAX_VALUE, actual = values.last())
        assertTrue(values.zipWithNext { upper, lower -> upper > lower }.all { it }, "$values")
    }

    @Test
    fun buildNumericYAxisTicks_infiniteMax_formatsWithoutThrowing() {
        // Stacked totals of valid finite values can overflow to infinity.
        val ticks = numericTicks(minValue = 0.0, maxValue = Double.POSITIVE_INFINITY, labelCount = 5)

        assertEquals(expected = 5, actual = ticks.size)
        assertTrue(ticks.all { tick -> tick.centerY.isFinite() })
    }

    @Test
    fun buildNumericYAxisTicks_labelCountBelowTwo_usesBothEnds() {
        listOf(Int.MIN_VALUE, 0, 1).forEach { labelCount ->
            val ticks = numericTicks(minValue = 0.0, maxValue = 10.0, labelCount = labelCount)

            assertEquals(
                expected = listOf("10", "0"),
                actual = ticks.map { tick -> tick.label },
                message = "count $labelCount",
            )
        }
    }

    @Test
    fun buildNumericYAxisTicks_insetOverHalfHeight_centersEveryTick() {
        val ticks = numericTicks(minValue = 0.0, maxValue = 10.0, labelCount = 3, verticalInsetPx = 500f)

        assertEquals(expected = listOf(100f, 100f, 100f), actual = ticks.map { tick -> tick.centerY })
    }

    @Test
    fun baselineYForRange_positiveRange_putsZeroAtBottom() {
        assertEquals(expected = 200f, actual = baselineYForRange(minValue = 0.0, maxValue = 100.0, heightPx = 200f))
        assertEquals(expected = 200f, actual = baselineYForRange(minValue = 20.0, maxValue = 100.0, heightPx = 200f))
    }

    @Test
    fun baselineYForRange_negativeRange_putsZeroAtTop() {
        assertEquals(expected = 0f, actual = baselineYForRange(minValue = -100.0, maxValue = 0.0, heightPx = 200f))
        assertEquals(expected = 0f, actual = baselineYForRange(minValue = -100.0, maxValue = -20.0, heightPx = 200f))
    }

    @Test
    fun baselineYForRange_rangeCrossingZero_putsZeroInside() {
        assertEquals(expected = 150f, actual = baselineYForRange(minValue = -10.0, maxValue = 30.0, heightPx = 200f))
        assertEquals(expected = 100f, actual = baselineYForRange(minValue = -1e300, maxValue = 1e300, heightPx = 200f))
    }

    @Test
    fun baselineYForRange_emptyRange_putsZeroAtBottomUnlessNegative() {
        assertEquals(expected = 200f, actual = baselineYForRange(minValue = 5.0, maxValue = 5.0, heightPx = 200f))
        assertEquals(expected = 200f, actual = baselineYForRange(minValue = 0.0, maxValue = 0.0, heightPx = 200f))
        assertEquals(expected = 0f, actual = baselineYForRange(minValue = -5.0, maxValue = -5.0, heightPx = 200f))
    }

    @Test
    fun baselineYForRange_nonPositiveHeight_returnsZero() {
        assertEquals(expected = 0f, actual = baselineYForRange(minValue = -10.0, maxValue = 30.0, heightPx = 0f))
        assertEquals(expected = 0f, actual = baselineYForRange(minValue = -10.0, maxValue = 30.0, heightPx = -5f))
    }

    @Test
    fun placeYAxisLabel_centersLabelOnTickAndRightAlignsIt() {
        assertEquals(expected = IntOffset(x = 16, y = 90), actual = placeY(tickY = 100f))
    }

    @Test
    fun placeYAxisLabel_tickOnColumnEdges_keepsLabelInside() {
        assertEquals(expected = 0, actual = placeY(tickY = 0f)?.y)
        assertEquals(expected = COLUMN_HEIGHT - Y_LABEL_HEIGHT, actual = placeY(tickY = COLUMN_HEIGHT.toFloat())?.y)
    }

    @Test
    fun placeYAxisLabel_tickPastColumnByRounding_isDrawnAtEdge() {
        // A 200.4 px plot drawn in a 200 px column puts the bottom tick 0.4 px past the column.
        assertEquals(expected = COLUMN_HEIGHT - Y_LABEL_HEIGHT, actual = placeY(tickY = COLUMN_HEIGHT + 0.4f)?.y)
        assertEquals(expected = COLUMN_HEIGHT - Y_LABEL_HEIGHT, actual = placeY(tickY = COLUMN_HEIGHT + 1f)?.y)
        assertEquals(expected = 0, actual = placeY(tickY = -1f)?.y)
    }

    @Test
    fun placeYAxisLabel_tickOutsideColumn_hidesLabel() {
        val outsideTicks =
            listOf(
                -1.01f,
                -20f,
                COLUMN_HEIGHT + 1.01f,
                COLUMN_HEIGHT + 20f,
                Float.NaN,
                Float.NEGATIVE_INFINITY,
                Float.POSITIVE_INFINITY,
            )

        outsideTicks.forEach { tickY ->
            assertNull(placeY(tickY = tickY), "tick $tickY")
        }
    }

    @Test
    fun placeYAxisLabel_labelFillingColumn_startsAtColumnLeftEdge() {
        val position = placeY(tickY = 100f, labelWidthPx = COLUMN_WIDTH)

        assertEquals(expected = 0, actual = position?.x)
    }

    @Test
    fun placeYAxisLabel_labelTallerThanColumn_startsAtColumnTop() {
        val position = placeY(tickY = 100f, labelHeightPx = COLUMN_HEIGHT + 50)

        assertEquals(expected = 0, actual = position?.y)
    }

    @Test
    fun placeYAxisLabel_columnShorterThanLabel_drawsTicksInsideColumnAtTop() {
        listOf(-1f, 0f, 2.5f, 5f, 6f).forEach { tickY ->
            assertEquals(expected = 0, actual = placeY(tickY = tickY, columnHeightPx = 5)?.y, message = "tick $tickY")
        }
        assertNull(placeY(tickY = 6.01f, columnHeightPx = 5))
    }

    @Test
    fun buildXAxisLayoutTicks_placesItemsFromFirstTickMinusScroll() {
        val ticks =
            buildXAxisLayoutTicks(
                labels = listOf("A", "B", "C", "D"),
                labelIndices = listOf(0, 2, 3),
                unitWidthPx = 100f,
                firstTickPx = 20f,
                scrollOffsetPx = 50f,
            )

        assertEquals(expected = listOf("A", "C", "D"), actual = ticks.map { tick -> tick.label })
        assertEquals(expected = listOf(-30f, 170f, 270f), actual = ticks.map { tick -> tick.centerX })
    }

    @Test
    fun buildXAxisLayoutTicks_blankLabel_usesItemNumber() {
        val ticks =
            buildXAxisLayoutTicks(
                labels = listOf("", "B"),
                labelIndices = listOf(0),
                unitWidthPx = 100f,
                firstTickPx = 0f,
                scrollOffsetPx = 0f,
            )

        assertEquals(expected = "1", actual = ticks.single().label)
    }

    @Test
    fun estimateXAxisLabelExtent_usesLongestResolvedLabel() {
        val height =
            labelHeightPx(
                labels = listOf("Jan", "", "September"),
                dataSize = 3,
                fontSizePx = 10f,
            )

        // "September": 9 characters at 0.58 of the font size, one 1.2 line high, tilted.
        assertEquals(
            expected = 52.2f * sin(TILT_RADIANS) + 12f * cos(TILT_RADIANS),
            actual = height,
            absoluteTolerance = 0.01f,
        )
    }

    @Test
    fun estimateXAxisLabelExtent_ignoresLabelsPastDataSize() {
        assertEquals(
            expected = labelHeightPx(labels = listOf("Jan"), dataSize = 1, fontSizePx = 10f),
            actual = labelHeightPx(labels = listOf("Jan", "September"), dataSize = 1, fontSizePx = 10f),
        )
    }

    @Test
    fun estimateXAxisLabelExtent_missingLabels_useItemNumberLength() {
        // Item 120 has no label, so it shows "120".
        assertEquals(
            expected = labelHeightPx(labels = listOf("abc"), dataSize = 1, fontSizePx = 10f),
            actual = labelHeightPx(labels = emptyList(), dataSize = 120, fontSizePx = 10f),
        )
    }

    @Test
    fun estimateXAxisLabelExtent_blankLabel_usesItsOwnItemNumberLength() {
        // Item 5 of 1200 is blank and prints "5", so the longest label stays "Jan".
        val labels = List(1_200) { index -> if (index == 4) " " else "Jan" }
        assertEquals(
            expected = labelHeightPx(labels = listOf("Jan"), dataSize = 1, fontSizePx = 10f),
            actual = labelHeightPx(labels = labels, dataSize = 1_200, fontSizePx = 10f),
        )
        // Item 1200 is blank and prints "1200", which is longer.
        val lastBlank = List(1_200) { index -> if (index == 1_199) "" else "Jan" }
        assertEquals(
            expected = labelHeightPx(labels = listOf("1200"), dataSize = 1, fontSizePx = 10f),
            actual = labelHeightPx(labels = lastBlank, dataSize = 1_200, fontSizePx = 10f),
        )
    }

    @Test
    fun estimateXAxisLabelExtent_matchesLongestResolvedLabel() {
        val random = Random(7)
        repeat(2_000) {
            val dataSize = random.nextInt(1, 300)
            val labels =
                List(random.nextInt(0, dataSize + 5)) {
                    when (random.nextInt(4)) {
                        0 -> ""
                        1 -> " "
                        else -> "x".repeat(random.nextInt(1, 6))
                    }
                }
            val longest = (0 until dataSize).maxOf { index -> resolveAxisLabel(labels, index).length }
            assertEquals(
                expected =
                    labelHeightPx(
                        labels = listOf("x".repeat(longest)),
                        dataSize = 1,
                        fontSizePx = 10f,
                    ),
                actual = labelHeightPx(labels = labels, dataSize = dataSize, fontSizePx = 10f),
                message = "dataSize = $dataSize, labels = $labels",
            )
        }
    }

    @Test
    fun estimateXAxisLabelExtent_noItemsOrFontSize_returnsFallbackHeight() {
        assertEquals(
            expected = 10f,
            actual = labelHeightPx(labels = listOf("A"), dataSize = 0, fontSizePx = 10f),
        )
        assertEquals(
            expected = 1f,
            actual = labelHeightPx(labels = listOf("A"), dataSize = 3, fontSizePx = 0f),
        )
    }

    @Test
    fun placeXAxisLabel_centersTiltedLabelOnItsTickAndCenterLine() {
        val position = place(tickX = 200f)

        // The label turns around its center, so the tilted label keeps that center.
        assertNotNull(position)
        assertEquals(expected = 200f, actual = position.x + LABEL_WIDTH / 2f, absoluteTolerance = 0.5f)
        assertEquals(expected = CENTER_Y, actual = position.y + LABEL_HEIGHT / 2f, absoluteTolerance = 0.5f)
    }

    @Test
    fun placeXAxisLabel_tickAtRowEnd_staysCenteredOnTick() {
        val position = place(tickX = ROW_WIDTH.toFloat())

        assertNotNull(position)
        assertEquals(expected = ROW_WIDTH.toFloat(), actual = position.x + LABEL_WIDTH / 2f, absoluteTolerance = 0.5f)
    }

    @Test
    fun placeXAxisLabel_tickOutsideRow_hidesLabel() {
        listOf(
            -5f,
            -1.01f,
            ROW_WIDTH + 1.01f,
            ROW_WIDTH + 5f,
            Float.NaN,
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY,
        ).forEach { tickX ->
            assertNull(place(tickX = tickX), "tick $tickX")
        }
    }

    @Test
    fun placeXAxisLabel_tickWithinOnePixelOfRow_isDrawnForRounding() {
        listOf(-1f, -0.5f, ROW_WIDTH + 0.5f, ROW_WIDTH + 1f).forEach { tickX ->
            val position = place(tickX = tickX)

            assertNotNull(position, "tick $tickX")
            assertEquals(expected = tickX, actual = position.x + LABEL_WIDTH / 2f, absoluteTolerance = 0.5f)
        }
    }

    @Test
    fun placeXAxisLabel_tickAtRowStart_letsLabelExtendPastRow() {
        val position = place(tickX = 0f)

        // Edge labels stay centered on their tick instead of shifting into the row.
        assertNotNull(position)
        assertEquals(expected = -LABEL_WIDTH / 2, actual = position.x)
    }

    @Test
    fun placeXAxisLabel_oddLabelSize_staysWithinHalfPixelOfTick() {
        val position =
            placeXAxisLabel(
                tickX = 200.3f,
                labelWidthPx = 61,
                labelHeightPx = 21,
                rowWidthPx = ROW_WIDTH,
                centerY = CENTER_Y,
            )

        assertNotNull(position)
        assertEquals(expected = 200.3f, actual = position.x + 61 / 2f, absoluteTolerance = 0.5f)
        assertEquals(expected = CENTER_Y, actual = position.y + 21 / 2f, absoluteTolerance = 0.5f)
    }

    @Test
    fun xAxisLabelMinSpacingPx_keepsParallelLabelLinesApart() {
        val fontSizePx = 20f
        val spacing = xAxisLabelMinSpacingPx(fontSizePx)

        // Two tilted labels one spacing apart are this far apart across their text lines.
        val distanceAcrossLines = spacing * sin(TILT_RADIANS)

        assertTrue(distanceAcrossLines > fontSizePx * 1.2f)
    }

    @Test
    fun xAxisLabelMinSpacingPx_isAboutThreeFontSizes() {
        // 1.2 line heights plus half a line of gap, across a 34 degree tilt.
        assertEquals(expected = 32.19f, actual = xAxisLabelMinSpacingPx(10f), absoluteTolerance = 0.01f)
        assertEquals(
            expected = 2f * xAxisLabelMinSpacingPx(10f),
            actual = xAxisLabelMinSpacingPx(20f),
            absoluteTolerance = 0.001f,
        )
    }

    @Test
    fun xAxisLabelMinSpacingPx_nonPositiveFontSize_returnsZero() {
        assertEquals(expected = 0f, actual = xAxisLabelMinSpacingPx(0f))
        assertEquals(expected = 0f, actual = xAxisLabelMinSpacingPx(-12f))
    }

    @Test
    fun resolveAxisLabel_presentLabel_isKeptAsIs() {
        assertEquals(expected = " Jan ", actual = resolveAxisLabel(labels = listOf("Dec", " Jan "), index = 1))
    }

    @Test
    fun resolveAxisLabel_blankOrMissingLabel_usesOneBasedItemNumber() {
        val labels = listOf("", " ", "Mar")

        assertEquals(expected = "1", actual = resolveAxisLabel(labels = labels, index = 0))
        assertEquals(expected = "2", actual = resolveAxisLabel(labels = labels, index = 1))
        assertEquals(expected = "4", actual = resolveAxisLabel(labels = labels, index = 3))
        assertEquals(expected = "1", actual = resolveAxisLabel(labels = emptyList(), index = 0))
    }

    @Test
    fun defaultAxisValueFormatter_roundsToTwoDecimalsWithoutTrailingZero() {
        assertEquals(expected = "5", actual = defaultAxisValueFormatter.format(5.0))
        assertEquals(expected = "-10", actual = defaultAxisValueFormatter.format(-10.0))
        assertEquals(expected = "2.5", actual = defaultAxisValueFormatter.format(2.5))
        assertEquals(expected = "1.23", actual = defaultAxisValueFormatter.format(1.234))
        assertEquals(expected = "0", actual = defaultAxisValueFormatter.format(-0.001))
        assertEquals(expected = "NaN", actual = defaultAxisValueFormatter.format(Double.NaN))
        assertEquals(expected = "Infinity", actual = defaultAxisValueFormatter.format(Double.POSITIVE_INFINITY))
    }

    @Test
    fun defaultAxisValueFormatter_largeValues_printPlainDigits() {
        // Double.toString switches to scientific notation from 10^7 on the JVM.
        assertEquals(expected = "9000000", actual = defaultAxisValueFormatter.format(9_000_000.0))
        assertEquals(expected = "10000000", actual = defaultAxisValueFormatter.format(1e7))
        assertEquals(expected = "12500000", actual = defaultAxisValueFormatter.format(12_500_000.0))
        assertEquals(expected = "-22500000.5", actual = defaultAxisValueFormatter.format(-22_500_000.5))
    }

    @Test
    fun visibleIndexRange_viewportOnItemBoundaries_includesItemStartingAtFarEdge() {
        // 10 px items and a viewport over 100..150 px. Item 15 starts on the far edge, where a
        // line chart point is still on screen.
        val range = visibleIndexRange(dataSize = 100, viewportWidthPx = 50f, scrollOffsetPx = 100f, unitWidthPx = 10f)

        assertEquals(expected = 10..15, actual = range)
    }

    @Test
    fun visibleIndexRange_viewportBetweenBoundaries_includesPartlyVisibleItems() {
        val range = visibleIndexRange(dataSize = 100, viewportWidthPx = 50f, scrollOffsetPx = 95f, unitWidthPx = 10f)

        assertEquals(expected = 9..14, actual = range)
    }

    @Test
    fun visibleIndexRange_viewportWiderThanContent_coversEveryItem() {
        assertEquals(
            expected = 0..4,
            actual = visibleIndexRange(dataSize = 5, viewportWidthPx = 1_000f, scrollOffsetPx = 0f, unitWidthPx = 10f),
        )
        assertEquals(
            expected = 0..0,
            actual = visibleIndexRange(dataSize = 1, viewportWidthPx = 1_000f, scrollOffsetPx = 0f, unitWidthPx = 10f),
        )
    }

    @Test
    fun visibleIndexRange_viewportNarrowerThanOneItem_coversItemsItTouches() {
        assertEquals(
            expected = 0..0,
            actual = visibleIndexRange(dataSize = 10, viewportWidthPx = 3f, scrollOffsetPx = 5f, unitWidthPx = 10f),
        )
        assertEquals(
            expected = 0..1,
            actual = visibleIndexRange(dataSize = 10, viewportWidthPx = 3f, scrollOffsetPx = 8f, unitWidthPx = 10f),
        )
    }

    @Test
    fun visibleIndexRange_scrolledPastLastItem_clampsToLastItem() {
        assertEquals(
            expected = 9..9,
            actual = visibleIndexRange(dataSize = 10, viewportWidthPx = 50f, scrollOffsetPx = 500f, unitWidthPx = 10f),
        )
    }

    @Test
    fun visibleIndexRange_negativeSizes_returnEmpty() {
        assertEquals(
            expected = IntRange.EMPTY,
            actual = visibleIndexRange(dataSize = -1, viewportWidthPx = 50f, scrollOffsetPx = 0f, unitWidthPx = 10f),
        )
        assertEquals(
            expected = IntRange.EMPTY,
            actual = visibleIndexRange(dataSize = 10, viewportWidthPx = -50f, scrollOffsetPx = 0f, unitWidthPx = 10f),
        )
        assertEquals(
            expected = IntRange.EMPTY,
            actual = visibleIndexRange(dataSize = 10, viewportWidthPx = 50f, scrollOffsetPx = 0f, unitWidthPx = -10f),
        )
    }

    @Test
    fun visibleIndexRange_smallGrid_coversSlotsTouchingViewport() {
        // Every whole-pixel width and offset up to one item past the content, with whole and fractional slots.
        for (dataSize in 1..12) {
            for (unitWidthPx in listOf(0.75f, 1f, 2.5f, 3f, 10f)) {
                val limitPx = ceil((dataSize + 1) * unitWidthPx).toInt()
                for (widthPx in 1..limitPx) {
                    for (offsetPx in 0..limitPx) {
                        assertEquals(
                            expected =
                                slotsTouchingViewport(
                                    dataSize = dataSize,
                                    viewportWidthPx = widthPx.toFloat(),
                                    scrollOffsetPx = offsetPx.toFloat(),
                                    unitWidthPx = unitWidthPx,
                                ),
                            actual =
                                visibleIndexRange(
                                    dataSize = dataSize,
                                    viewportWidthPx = widthPx.toFloat(),
                                    scrollOffsetPx = offsetPx.toFloat(),
                                    unitWidthPx = unitWidthPx,
                                ),
                            message = "$dataSize items of $unitWidthPx px, width $widthPx, offset $offsetPx",
                        )
                    }
                }
            }
        }
    }

    private fun place(tickX: Float) =
        placeXAxisLabel(
            tickX = tickX,
            labelWidthPx = LABEL_WIDTH,
            labelHeightPx = LABEL_HEIGHT,
            rowWidthPx = ROW_WIDTH,
            centerY = CENTER_Y,
        )

    private fun placeY(
        tickY: Float,
        labelWidthPx: Int = Y_LABEL_WIDTH,
        labelHeightPx: Int = Y_LABEL_HEIGHT,
        columnHeightPx: Int = COLUMN_HEIGHT,
    ) = placeYAxisLabel(
        tickY = tickY,
        labelWidthPx = labelWidthPx,
        labelHeightPx = labelHeightPx,
        columnWidthPx = COLUMN_WIDTH,
        columnHeightPx = columnHeightPx,
        edgePaddingPx = Y_EDGE_PADDING,
    )

    private fun numericTicks(
        minValue: Double,
        maxValue: Double,
        labelCount: Int,
        verticalInsetPx: Float = 0f,
        formatter: ChartValueFormatter = defaultAxisValueFormatter,
    ) = buildNumericYAxisTicks(
        minValue = minValue,
        maxValue = maxValue,
        labelCount = labelCount,
        plotHeightPx = 200f,
        verticalInsetPx = verticalInsetPx,
        formatter = formatter,
    )

    private fun recordingFormatter(values: MutableList<Double>) =
        ChartValueFormatter { value ->
            values += value
            value.toString()
        }

    // Items whose slot [i × unit, (i + 1) × unit) touches [offset, offset + width]; past the content, the last item.
    private fun slotsTouchingViewport(
        dataSize: Int,
        viewportWidthPx: Float,
        scrollOffsetPx: Float,
        unitWidthPx: Float,
    ): IntRange {
        val touching =
            (0 until dataSize).filter { index ->
                index * unitWidthPx <= scrollOffsetPx + viewportWidthPx && (index + 1) * unitWidthPx > scrollOffsetPx
            }
        return if (touching.isEmpty()) dataSize - 1..dataSize - 1 else touching.first()..touching.last()
    }

    @Test
    fun xAxisLabelEdgeInsetPx_keepsThePartOfTheLabelPastTheSlack() {
        val extent = AxisXLabelExtent(heightPx = 20f, halfWidthPx = 30.2f)

        assertEquals(expected = 11f, actual = xAxisLabelEdgeInsetPx(extent, edgeSlackPx = 20f, availableWidthPx = 600))
        assertEquals(expected = 0f, actual = xAxisLabelEdgeInsetPx(extent, edgeSlackPx = 40f, availableWidthPx = 600))
    }

    @Test
    fun xAxisLabelEdgeInsetPx_longLabels_takeAtMostAFifthOfTheWidth() {
        val extent = AxisXLabelExtent(heightPx = 200f, halfWidthPx = 300f)

        assertEquals(expected = 40f, actual = xAxisLabelEdgeInsetPx(extent, edgeSlackPx = 0f, availableWidthPx = 200))
        assertEquals(expected = 0f, actual = xAxisLabelEdgeInsetPx(extent, edgeSlackPx = 0f, availableWidthPx = 0))
    }

    private fun labelHeightPx(
        labels: List<String>,
        dataSize: Int,
        fontSizePx: Float,
    ): Float = estimateXAxisLabelExtent(labels = labels, dataSize = dataSize, fontSizePx = fontSizePx).heightPx

    private companion object {
        const val LABEL_WIDTH = 60
        const val LABEL_HEIGHT = 20
        const val ROW_WIDTH = 400
        const val CENTER_Y = 40f
        const val Y_LABEL_WIDTH = 30
        const val Y_LABEL_HEIGHT = 20
        const val COLUMN_WIDTH = 50
        const val COLUMN_HEIGHT = 200
        const val Y_EDGE_PADDING = 4
        val TILT_RADIANS = X_AXIS_LABEL_TILT_DEGREES * PI.toFloat() / 180f
    }
}
