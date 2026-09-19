package io.github.hdcharts.charts.unit.helpers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.internal.ANIMATION_DURATION_LINE_CHART
import io.github.hdcharts.charts.internal.common.bezier.cubicControlPointsForSegment
import io.github.hdcharts.charts.internal.common.model.ChartDataItem
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.common.model.toChartData
import io.github.hdcharts.charts.internal.linechart.LineChartTransitionMode
import io.github.hdcharts.charts.internal.linechart.MIN_TIMELINE_DURATION_MS
import io.github.hdcharts.charts.internal.linechart.aggregateForCompactDensity
import io.github.hdcharts.charts.internal.linechart.buildLineXAxisTicks
import io.github.hdcharts.charts.internal.linechart.buildLineYAxisTicks
import io.github.hdcharts.charts.internal.linechart.compactDensityRanges
import io.github.hdcharts.charts.internal.linechart.decideLineChartUpdate
import io.github.hdcharts.charts.internal.linechart.findNearestPoint
import io.github.hdcharts.charts.internal.linechart.lineChartValueAnimationSpec
import io.github.hdcharts.charts.internal.linechart.normalizeSeriesByMinMax
import io.github.hdcharts.charts.internal.linechart.renderIndexForSourceIndex
import io.github.hdcharts.charts.internal.linechart.resolveLineXAxisLabels
import io.github.hdcharts.charts.internal.linechart.scaleValues
import io.github.hdcharts.charts.internal.linechart.shouldUseScrollableDensity
import io.github.hdcharts.charts.internal.linechart.sourceIndexForRenderIndex
import io.github.hdcharts.charts.internal.linechart.timelineShiftValues
import io.github.hdcharts.charts.internal.linechart.toTimelineDurationMillis
import kotlin.test.Test
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
                    scaledValues = entry.key.second,
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
    fun buildLineXAxisTicks_spreadsCentersAcrossPlotWidth() {
        val ticks =
            buildLineXAxisTicks(
                labels = listOf("A", "B", "C", "D"),
                labelIndices = listOf(0, 2, 3),
                pointsCount = 4,
                stepX = 100f,
            )

        assertEquals(expected = 3, actual = ticks.size)
        assertEquals(expected = "A", actual = ticks[0].label)
        assertEquals(expected = 0f, actual = ticks[0].centerX)
        assertEquals(expected = "C", actual = ticks[1].label)
        assertEquals(expected = 200f, actual = ticks[1].centerX)
        assertEquals(expected = "D", actual = ticks[2].label)
        assertEquals(expected = 300f, actual = ticks[2].centerX)
    }

    @Test
    fun buildLineXAxisTicks_appliesScrollOffsetForViewportPositions() {
        val ticks =
            buildLineXAxisTicks(
                labels = listOf("A", "B", "C", "D"),
                labelIndices = listOf(1, 3),
                pointsCount = 4,
                stepX = 100f,
                scrollOffsetPx = 50f,
            )

        assertEquals(expected = 2, actual = ticks.size)
        assertEquals(expected = "B", actual = ticks[0].label)
        assertEquals(expected = 50f, actual = ticks[0].centerX)
        assertEquals(expected = "D", actual = ticks[1].label)
        assertEquals(expected = 250f, actual = ticks[1].centerX)
    }

    @Test
    fun buildLineYAxisTicks_appliesInsetToFirstAndLastTick() {
        val ticks =
            buildLineYAxisTicks(
                minValue = 0.0,
                maxValue = 100.0,
                labelCount = 5,
                plotHeightPx = 200f,
                verticalInsetPx = 10f,
            )

        assertEquals(expected = 5, actual = ticks.size)
        assertEquals(expected = "100", actual = ticks.first().label)
        assertEquals(expected = "0", actual = ticks.last().label)
        assertEquals(expected = 10f, actual = ticks.first().centerY)
        assertEquals(expected = 190f, actual = ticks.last().centerY)
    }

    @Test
    fun decideLineChartUpdate_inMorphMode_keepsMorphTransition() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = listOf(listOf(1.0, 2.0)),
                currentRawSeries = listOf(listOf(2.0, 3.0)),
                currentMinMax = 2.0 to 3.0,
                renderMode = LineChartRenderMode.Morph,
                animationDuration = 500.milliseconds,
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
                renderMode = LineChartRenderMode.Timeline,
                animationDuration = 500.milliseconds,
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
                renderMode = LineChartRenderMode.Timeline,
                animationDuration = 500.milliseconds,
            )

        val shift = assertIs<LineChartTransitionMode.TimelineShift>(mode)
        assertEquals(expected = 20.0 to 60.0, actual = shift.transitionData.minMax)
        assertEquals(expected = previousSeries, actual = shift.transitionData.previousSeries)
        assertEquals(expected = currentSeries, actual = shift.transitionData.currentSeries)
        assertEquals(expected = 500.milliseconds, actual = shift.animationDuration)
    }

    @Test
    fun normalizeSeriesByMinMax_spreadsSmallValuesOverFullRange() {
        val normalized =
            normalizeSeriesByMinMax(
                series = listOf(listOf(20.0, 40.0, 60.0)),
                minMax = 20.0 to 60.0,
            )

        assertEquals(expected = listOf(listOf(0f, 0.5f, 1f)), actual = normalized)
    }

    @Test
    fun decideLineChartUpdate_whenWindowIsRebuiltInPlace_keepsMorphTransition() {
        val mode =
            decideLineChartUpdate(
                previousRawSeries = listOf(listOf(10.0, 20.0, 30.0)),
                currentRawSeries = listOf(listOf(90.0, 80.0, 70.0)),
                currentMinMax = 70.0 to 90.0,
                renderMode = LineChartRenderMode.Timeline,
                animationDuration = 500.milliseconds,
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
                renderMode = LineChartRenderMode.Timeline,
                animationDuration = 500.milliseconds,
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
                renderMode = LineChartRenderMode.Timeline,
                animationDuration = 500.milliseconds,
            )

        assertIs<LineChartTransitionMode.TimelineShift>(mode)
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
                renderMode = LineChartRenderMode.Timeline,
                animationDuration = 160.milliseconds,
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
                renderMode = LineChartRenderMode.Timeline,
                animationDuration = Duration.ZERO,
            )

        assertEquals(expected = MIN_TIMELINE_DURATION_MS, actual = spec.durationMillis)
    }

    @Test
    fun lineChartValueAnimationSpec_inMorphMode_keepsTheDefaultDuration() {
        val spec =
            lineChartValueAnimationSpec(
                renderMode = LineChartRenderMode.Morph,
                animationDuration = 160.milliseconds,
            )

        assertEquals(expected = ANIMATION_DURATION_LINE_CHART, actual = spec.durationMillis)
    }
}
