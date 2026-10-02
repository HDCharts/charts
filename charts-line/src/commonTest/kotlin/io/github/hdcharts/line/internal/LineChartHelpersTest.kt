package io.github.hdcharts.line.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.ANIMATION_DURATION_LINE_CHART
import io.github.hdcharts.core.internal.bezier.cubicControlPointsForSegment
import io.github.hdcharts.core.internal.model.ChartDataItem
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.internal.model.toChartData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LineChartHelpersTest {
    @Test
    fun toTimelineDurationMillis_boundsValuesToTweenRange() {
        assertEquals(expected = 1, actual = (-1).milliseconds.toTimelineDurationMillis())
        assertEquals(expected = Int.MAX_VALUE, actual = Duration.INFINITE.toTimelineDurationMillis())
        assertEquals(
            expected = Int.MAX_VALUE,
            actual = (Int.MAX_VALUE.toLong() + 1L).milliseconds.toTimelineDurationMillis(),
        )
    }

    @Test
    fun shouldUseScrollableDensity_resolvesFromThreshold() {
        assertEquals(expected = false, actual = shouldUseScrollableDensity(pointsCount = 49))
        assertEquals(expected = true, actual = shouldUseScrollableDensity(pointsCount = 50))
    }

    @Test
    fun aggregateForCompactDensity_aboveThreshold_reducesPointCount() {
        val sourcePoints = List(120) { index -> (index + 1).toDouble() }
        val sourceLabels = List(120) { index -> "P${index + 1}" }
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem(
                            label = "Series",
                            item = sourcePoints.toChartData(labels = sourceLabels),
                        ),
                    ),
                title = "Dense",
            )

        val aggregated = aggregateForCompactDensity(data)
        val aggregatedPoints =
            aggregated.items
                .first()
                .item.points
        val aggregatedLabels =
            aggregated.items
                .first()
                .item.labels

        assertTrue(aggregatedPoints.size < sourcePoints.size)
        assertEquals(expected = 40, actual = aggregatedPoints.size)
        assertEquals(expected = "P2", actual = aggregatedLabels.first())
        assertEquals(expected = "P119", actual = aggregatedLabels.last())
        assertEquals(expected = 2.0, actual = aggregatedPoints.first())
    }

    @Test
    fun aggregateForCompactDensity_belowThreshold_returnsOriginalData() {
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem(
                            label = "Series",
                            item = List(20) { index -> index.toDouble() }.toChartData(labels = List(20) { "L$it" }),
                        ),
                    ),
                title = "Small",
            )

        val aggregated = aggregateForCompactDensity(data)

        assertSame(data, aggregated)
    }

    @Test
    fun compactDensitySelection_mapsBetweenRenderBucketsAndSourceIndices() {
        val ranges = compactDensityRanges(sourcePointsCount = 120, targetPoints = 50)

        assertEquals(expected = 26, actual = renderIndexForSourceIndex(sourceIndex = 80, sourceRanges = ranges))
        assertEquals(expected = 79, actual = sourceIndexForRenderIndex(renderIndex = 26, sourceRanges = ranges))
        assertEquals(expected = -1, actual = renderIndexForSourceIndex(sourceIndex = 120, sourceRanges = ranges))
        assertEquals(expected = -1, actual = sourceIndexForRenderIndex(renderIndex = 40, sourceRanges = ranges))
    }

    @Test
    fun cubicControlPointsForSegment_middleSegment_correctControlPointsReturned() {
        // Arrange
        val points =
            listOf(
                Offset(0f, 0f),
                Offset(10f, 10f),
                Offset(20f, 0f),
                Offset(30f, 10f),
            )
        val segmentStartIndex = 1

        // Act
        val controls =
            cubicControlPointsForSegment(
                points = points,
                segmentStartIndex = segmentStartIndex,
            )

        // Assert
        val tolerance = 0.0001f
        assertEquals(13.166667f, controls.first.x, tolerance)
        assertEquals(10f, controls.first.y, tolerance)
        assertEquals(16.833334f, controls.second.x, tolerance)
        assertEquals(0f, controls.second.y, tolerance)
    }

    @Test
    fun cubicControlPointsForSegment_zeroTension_controlPointsMatchSegmentEnds() {
        // Arrange
        val points =
            listOf(
                Offset(0f, 0f),
                Offset(10f, 10f),
                Offset(20f, 0f),
            )
        val segmentStartIndex = 1

        // Act
        val controls =
            cubicControlPointsForSegment(
                points = points,
                segmentStartIndex = segmentStartIndex,
                tension = 0f,
            )

        // Assert
        assertEquals(points[segmentStartIndex], controls.first)
        assertEquals(points[segmentStartIndex + 1], controls.second)
    }

    @Test
    fun cubicControlPointsForSegment_yBoundsProvided_controlPointsAreClamped() {
        // Arrange
        val points =
            listOf(
                Offset(0f, 100f),
                Offset(10f, 20f),
                Offset(20f, 10f),
                Offset(30f, -100f),
            )
        val segmentStartIndex = 1

        // Act
        val controls =
            cubicControlPointsForSegment(
                points = points,
                segmentStartIndex = segmentStartIndex,
                minY = 10f,
                maxY = 25f,
            )

        // Assert
        val tolerance = 0.0001f
        assertEquals(13.166667f, controls.first.x, tolerance)
        assertEquals(10f, controls.first.y, tolerance)
        assertEquals(16.833334f, controls.second.x, tolerance)
        assertEquals(25f, controls.second.y, tolerance)
    }

    @Test
    fun findNearestPoint_validInput_correctOffsetReturned() {
        // Arrange
        val testCases =
            hashMapOf(
                Triple(500f, listOf(100f, 200f, 300f, 400f, 500f), Size(1000f, 2500f))
                    to Offset(500f, 2200f),
                Triple(900f, listOf(180f, 360f, 540f, 720f, 900f), Size(1000f, 2500f))
                    to Offset(900f, 1672f),
                Triple(400f, listOf(-40f, 20f, 50f, -100f, 300f), Size(1000f, 2500f))
                    to Offset(400f, 2462f),
            )

        testCases.forEach { entry: Map.Entry<Triple<Float, List<Float>, Size>, Offset> ->
            // Act
            val nearestPoint =
                findNearestPoint(
                    touchX = entry.key.first,
                    scaledValues = entry.key.second.toFloatArray(),
                    scaledValuesCount = entry.key.second.size,
                    size = entry.key.third,
                    bezier = false,
                )

            // Assert
            assertTrue { nearestPoint == entry.value }
        }
    }

    @Test
    fun scaleValues_validInput_correctlyScaledValuesReturned() {
        // Arrange
        val testCases =
            hashMapOf(
                Pair(listOf(10.0, 20.0, 30.0, 40.0, 50.0), Size(40f, 40f))
                    to listOf(0.0f, 10.0f, 20.0f, 30.0f, 40.0f),
                Pair(listOf(-5.0, 0.0, 5.0, 10.0, 15.0), Size(30f, 30f))
                    to listOf(0.0f, 7.5f, 15.0f, 22.5f, 30.0f),
                Pair(listOf(100.0, 200.0, 300.0, 400.0, 500.0), Size(100f, 100f))
                    to listOf(0.0f, 25.0f, 50.0f, 75.0f, 100.0f),
            )

        testCases.forEach { entry: Map.Entry<Pair<List<Double>, Size>, List<Float>> ->
            // Act
            val scaledValues = scaleValues(entry.key.first, entry.key.second)

            // Assert
            assertTrue { scaledValues == entry.value }
        }
    }

    @Test
    fun resolveLineRange_noOverrides_returnsDataMinMax() {
        val data = singleSeriesData(listOf(10.0, 20.0, 30.0))

        assertEquals(10.0 to 30.0, data.resolveLineRange(null, null))
    }

    @Test
    fun resolveLineRange_independentOverrides_applyEachBoundSeparately() {
        val data = singleSeriesData(listOf(10.0, 20.0, 30.0))

        assertEquals(0.0 to 30.0, data.resolveLineRange(minValue = 0.0, maxValue = null))
        assertEquals(10.0 to 100.0, data.resolveLineRange(minValue = null, maxValue = 100.0))
        assertEquals(0.0 to 100.0, data.resolveLineRange(minValue = 0.0, maxValue = 100.0))
    }

    @Test
    fun resolveLineRange_invalidOverride_fallsBackToDataMinMax() {
        val data = singleSeriesData(listOf(10.0, 20.0, 30.0))

        assertEquals(10.0 to 30.0, data.resolveLineRange(minValue = 50.0, maxValue = 20.0))
        assertEquals(10.0 to 30.0, data.resolveLineRange(minValue = 20.0, maxValue = 20.0))
    }

    @Test
    fun resolveLineRange_singleSidedOverride_crossingDataBound_keepsOverrideInstead_ofFallingBackToDataRange() {
        val data = singleSeriesData(listOf(-30.0, -20.0, -10.0))

        assertEquals(0.0 to 0.0, data.resolveLineRange(minValue = 0.0, maxValue = null))

        val positiveData = singleSeriesData(listOf(10.0, 20.0, 30.0))
        assertEquals(0.0 to 0.0, positiveData.resolveLineRange(minValue = null, maxValue = 0.0))
    }

    @Test
    fun resolveLineRange_timelineFixedMin_staysPinnedAcrossLiveTicksEvenWhenDataDipsBelowIt() {
        // LineChartContent recomputes minMax via remember(data, style.range.min, style.range.max) on every tick.
        val tickBeforeDip = singleSeriesData(listOf(5.0, 8.0, 12.0))
        val tickDuringDip = singleSeriesData(listOf(8.0, 12.0, -3.0))
        val tickAfterDip = singleSeriesData(listOf(12.0, -3.0, 6.0))

        val minMaxBeforeDip = tickBeforeDip.resolveLineRange(minValue = 0.0, maxValue = null)
        val minMaxDuringDip = tickDuringDip.resolveLineRange(minValue = 0.0, maxValue = null)
        val minMaxAfterDip = tickAfterDip.resolveLineRange(minValue = 0.0, maxValue = null)

        assertEquals(0.0, minMaxBeforeDip.first)
        assertEquals(0.0, minMaxDuringDip.first)
        assertEquals(0.0, minMaxAfterDip.first)
        assertEquals(0.0 to 12.0, minMaxBeforeDip)
        assertEquals(0.0 to 12.0, minMaxDuringDip)
        assertEquals(0.0 to 12.0, minMaxAfterDip)
    }

    @Test
    fun resolveLineRange_timelineFixedMin_wholeWindowBelowIt_clampsInsteadOfShowingNegativeRange() {
        val allBelowFloor = singleSeriesData(listOf(-8.0, -5.0, -2.0))

        val minMax = allBelowFloor.resolveLineRange(minValue = 0.0, maxValue = null)

        assertEquals(0.0 to 0.0, minMax)
        val drawValues =
            timelineShiftValues(
                previousSeries = listOf(allBelowFloor.items[0].item.points),
                currentSeries = listOf(allBelowFloor.items[0].item.points),
                minMax = minMax,
            )
        assertEquals(listOf(listOf(0f, 0f, 0f, 0f)), drawValues)
    }

    @Test
    fun timelineShiftValues_fixedRangeNarrowerThanLiveWindow_pinsMoreThanTheOldestPointFlat() {
        val previousSeries = listOf(listOf(120.0, 130.0, 140.0))
        val currentSeries = listOf(listOf(130.0, 140.0, 150.0))
        val fixedMinMax = 0.0 to 100.0

        val drawValues =
            timelineShiftValues(previousSeries = previousSeries, currentSeries = currentSeries, minMax = fixedMinMax)

        assertEquals(listOf(listOf(1f, 1f, 1f, 1f)), drawValues)
    }

    @Test
    fun resolveLineXAxisLabels_singleSeries_returnsItemLabels() {
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem(
                            label = "Series",
                            item = listOf(10f, 20f, 30f).toChartData(labels = listOf("A", "B", "C")),
                        ),
                    ),
                title = "Single",
            )

        val labels = resolveLineXAxisLabels(data)

        assertEquals(listOf("A", "B", "C"), labels)
    }

    @Test
    fun resolveLineXAxisLabels_multiSeriesWithCategories_prefersCategories() {
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem(
                            label = "Series 1",
                            item = listOf(1f, 2f, 3f).toChartData(labels = listOf("v1", "v2", "v3")),
                        ),
                        ChartDataItem(
                            label = "Series 2",
                            item = listOf(4f, 5f, 6f).toChartData(labels = listOf("w1", "w2", "w3")),
                        ),
                    ),
                categories = listOf("Jan", "Feb", "Mar"),
                title = "Multi",
            )

        val labels = resolveLineXAxisLabels(data)

        assertEquals(listOf("Jan", "Feb", "Mar"), labels)
    }

    @Test
    fun resolveLineXAxisLabels_multiSeriesWithoutCategories_returnsEmpty() {
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem(
                            label = "Series 1",
                            item = listOf(1f, 2f, 3f).toChartData(labels = listOf("v1", "v2", "v3")),
                        ),
                        ChartDataItem(
                            label = "Series 2",
                            item = listOf(4f, 5f, 6f).toChartData(labels = listOf("w1", "w2", "w3")),
                        ),
                    ),
                title = "Multi",
            )

        val labels = resolveLineXAxisLabels(data)

        assertTrue(labels.isEmpty())
    }

    @Test
    fun decideLineChartUpdate_inMorphMode_keepsMorphTransition() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = listOf(listOf(1.0, 2.0)),
                currentRawSeries = listOf(listOf(2.0, 3.0)),
                currentMinMax = 2.0 to 3.0,
                renderMode = LineChartRenderMode.Morph,
            )

        assertEquals(expected = LineChartTransitionMode.Morph, actual = mode)
    }

    @Test
    fun decideLineChartUpdate_withoutPreviousSeries_keepsMorphTransition() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = null,
                currentRawSeries = listOf(listOf(2.0, 3.0)),
                currentMinMax = 2.0 to 3.0,
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 500.milliseconds),
            )

        assertEquals(expected = LineChartTransitionMode.Morph, actual = mode)
    }

    @Test
    fun decideLineChartUpdate_whenTimelineRangeShrinks_usesCurrentRange() {
        val previousSeries = listOf(listOf(1_500_000.0, 20.0, 40.0))
        val currentSeries = listOf(listOf(20.0, 40.0, 60.0))

        val mode =
            decideLineChartUpdate(
                previousRawSeries = previousSeries,
                currentRawSeries = currentSeries,
                currentMinMax = 20.0 to 60.0,
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 500.milliseconds),
            )

        val shift = assertIs<LineChartTransitionMode.Timeline>(mode)
        assertEquals(expected = 20.0 to 60.0, actual = shift.shiftData.minMax)
        assertEquals(expected = previousSeries, actual = shift.shiftData.previousSeries)
        assertEquals(expected = currentSeries, actual = shift.shiftData.currentSeries)
    }

    @Test
    fun decideLineChartUpdate_whenWindowIsRebuiltInPlace_keepsMorphTransition() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = listOf(listOf(10.0, 20.0, 30.0)),
                currentRawSeries = listOf(listOf(90.0, 80.0, 70.0)),
                currentMinMax = 70.0 to 90.0,
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 500.milliseconds),
            )

        assertEquals(expected = LineChartTransitionMode.Morph, actual = mode)
    }

    @Test
    fun decideLineChartUpdate_whenOneSeriesIsNotAdvanced_keepsMorphTransition() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = listOf(listOf(10.0, 20.0, 30.0), listOf(1.0, 2.0, 3.0)),
                currentRawSeries = listOf(listOf(20.0, 30.0, 40.0), listOf(7.0, 8.0, 9.0)),
                currentMinMax = 1.0 to 40.0,
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 500.milliseconds),
            )

        assertEquals(expected = LineChartTransitionMode.Morph, actual = mode)
    }

    @Test
    fun decideLineChartUpdate_whenWindowAdvancesByOnePoint_shiftsTimeline() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = listOf(listOf(10.0, 20.0, 30.0)),
                currentRawSeries = listOf(listOf(20.0, 30.0, 40.0)),
                currentMinMax = 20.0 to 40.0,
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 500.milliseconds),
            )

        assertIs<LineChartTransitionMode.Timeline>(mode)
    }

    @Test
    fun decideLineChartUpdate_whenDrawnWindowIsTwoStepsBehind_morphsInsteadOfShifting() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = listOf(listOf(10.0, 20.0, 30.0)),
                currentRawSeries = listOf(listOf(30.0, 40.0, 50.0)),
                currentMinMax = 30.0 to 50.0,
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 500.milliseconds),
            )

        assertEquals(
            expected = LineChartTransitionMode.Morph,
            actual = mode,
            message = "A shift is built from the window the chart drew, so it can only cover one step.",
        )
    }

    @Test
    fun decideLineChartUpdate_windowAlreadyDrawn_morphs() {
        val window = listOf(listOf(10.0, 20.0, 30.0))
        val mode =
            decideLineChartUpdate(
                previousRawSeries = window,
                currentRawSeries = window,
                currentMinMax = 10.0 to 30.0,
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 500.milliseconds),
            )

        assertEquals(
            expected = LineChartTransitionMode.Morph,
            actual = mode,
            message = "A range or duration change re-runs the update for a window already drawn.",
        )
    }

    @Test
    fun timelineShiftValues_drawsPreviousWindowFollowedByNewestPoint() {
        val values =
            timelineShiftValues(
                previousSeries = listOf(listOf(20.0, 40.0, 60.0)),
                currentSeries = listOf(listOf(40.0, 60.0, 80.0)),
                minMax = 20.0 to 80.0,
            )

        assertEquals(expected = listOf(listOf(0f, 1f / 3f, 2f / 3f, 1f)), actual = values)
    }

    @Test
    fun lineChartValueAnimationSpec_inTimelineMode_usesTheRequestedDuration() {
        val spec =
            lineChartValueAnimationSpec(
                renderMode = LineChartRenderMode.Timeline(shiftDuration = 160.milliseconds),
            )

        assertEquals(
            expected = 160,
            actual = spec.durationMillis,
            message =
                "A timeline update that cannot be shifted still has to settle within the update " +
                    "interval the caller asked for, or the next update cancels it half finished.",
        )
    }

    @Test
    fun lineChartValueAnimationSpec_inTimelineMode_clampsANonPositiveDuration() {
        val spec =
            lineChartValueAnimationSpec(
                renderMode = LineChartRenderMode.Timeline(shiftDuration = Duration.ZERO),
            )

        assertEquals(expected = MIN_TIMELINE_DURATION_MS, actual = spec.durationMillis)
    }

    @Test
    fun lineChartValueAnimationSpec_inMorphMode_keepsTheDefaultDuration() {
        val spec =
            lineChartValueAnimationSpec(
                renderMode = LineChartRenderMode.Morph,
            )

        assertEquals(expected = ANIMATION_DURATION_LINE_CHART, actual = spec.durationMillis)
    }

    @Test
    fun lineChartRevealWindow_scrollingChart_revealsAcrossTheViewportNotTheCanvas() {
        val window =
            lineChartRevealWindow(
                viewportStartPx = 4_000f,
                viewportWidthPx = 800f,
                canvasWidthPx = 6_000f,
                progress = 0.5f,
            )

        assertEquals(expected = 4_000f, actual = window.leftPx)
        assertEquals(
            expected = 4_400f,
            actual = window.rightPx,
            message = "A canvas far wider than the screen must still reveal across the visible plot.",
        )
    }

    @Test
    fun lineChartRevealWindow_noProgress_revealsNothingBeyondTheViewportStart() {
        val window =
            lineChartRevealWindow(
                viewportStartPx = 100f,
                viewportWidthPx = 400f,
                canvasWidthPx = 500f,
                progress = 0f,
            )

        assertEquals(expected = 100f, actual = window.leftPx)
        assertEquals(expected = 100f, actual = window.rightPx)
    }

    @Test
    fun lineChartRevealWindow_fullProgress_coversTheViewport() {
        val window =
            lineChartRevealWindow(
                viewportStartPx = 1_200f,
                viewportWidthPx = 500f,
                canvasWidthPx = 5_000f,
                progress = 1f,
            )

        assertEquals(expected = 1_200f, actual = window.leftPx)
        assertEquals(expected = 1_700f, actual = window.rightPx)
    }

    @Test
    fun lineChartRevealWindow_viewportWiderThanCanvas_clampsToTheCanvas() {
        val window =
            lineChartRevealWindow(
                viewportStartPx = 0f,
                viewportWidthPx = 900f,
                canvasWidthPx = 600f,
                progress = 1f,
            )

        assertEquals(expected = 0f, actual = window.leftPx)
        assertEquals(expected = 600f, actual = window.rightPx)
    }

    @Test
    fun blendInto_morphHalfway_halvesTheDistanceFromStartToTarget() {
        val buffer = FloatArray(4)

        val count =
            blendInto(
                into = buffer,
                from = listOf(0f, 0.5f, 1f),
                to = listOf(1f, 1f, 0f),
                progress = 0.5f,
            )

        assertEquals(expected = 3, actual = count)
        assertEquals(expected = 0.5f, actual = buffer[0])
        assertEquals(expected = 0.75f, actual = buffer[1])
        assertEquals(expected = 0.5f, actual = buffer[2])
    }

    @Test
    fun blendInto_atTarget_writesTheTargetValues() {
        val buffer = FloatArray(3)

        val count =
            blendInto(
                into = buffer,
                from = listOf(0f, 0f, 0f),
                to = listOf(0.25f, 0.5f, 1f),
                progress = 1f,
            )

        assertEquals(expected = 3, actual = count)
        assertContentEquals(expected = floatArrayOf(0.25f, 0.5f, 1f), actual = buffer)
    }

    @Test
    fun blendInto_smallerBuffer_writesOnlyWhatFits() {
        val buffer = FloatArray(2)

        val count =
            blendInto(
                into = buffer,
                from = emptyList(),
                to = listOf(1f, 2f, 3f, 4f),
                progress = 1f,
            )

        assertEquals(expected = 2, actual = count)
        assertContentEquals(expected = floatArrayOf(1f, 2f), actual = buffer)
    }

    @Test
    fun copyInto_scalesIntoTheBuffer() {
        val buffer = FloatArray(3)

        val count = copyInto(source = listOf(0.5f, 1f), into = buffer, scaleBy = 200f)

        assertEquals(expected = 2, actual = count)
        assertContentEquals(expected = floatArrayOf(100f, 200f, 0f), actual = buffer)
    }

    @Test
    fun timelineWindowCounter_sameWindowRebuiltByRemember_reportsTheSameStep() {
        val counter = TimelineWindowCounter()
        val first = listOf(listOf(1.0, 2.0, 3.0))
        val second = listOf(listOf(2.0, 3.0, 4.0))

        counter.next(first)
        val step = counter.next(second)

        assertEquals(expected = 1L, actual = step.droppedPoints)
        // remember rebuilds the window list, so composition running again brings an equal copy.
        assertEquals(expected = step, actual = counter.next(second.map { it.toList() }))
    }

    @Test
    fun timelineWindowCounter_windowReplacedInPlace_resetsTheDroppedCount() {
        val counter = TimelineWindowCounter()

        counter.next(listOf(listOf(1.0, 2.0, 3.0)))
        val step = counter.next(listOf(listOf(9.0, 8.0, 7.0)))

        assertEquals(expected = 0L, actual = step.droppedPoints)
    }

    @Test
    fun timelineWindowCounter_advancingWindow_countsEveryDroppedPoint() {
        val counter = TimelineWindowCounter()
        val firstWindow = listOf(listOf(1.0, 2.0, 3.0))
        var window = firstWindow

        counter.next(firstWindow)
        val steps =
            List(3) { index ->
                window = listOf(window[0].drop(1) + (10.0 + index))
                counter.next(window)
            }

        assertEquals(expected = listOf(1L, 2L, 3L), actual = steps.map { it.droppedPoints })
    }

    private fun singleSeriesData(points: List<Double>): MultiChartData =
        MultiChartData(
            items = listOf(ChartDataItem(label = "Series", item = points.toChartData())),
            title = "Single",
        )
}
