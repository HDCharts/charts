package io.github.hdcharts.line.internal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.ANIMATION_TARGET
import io.github.hdcharts.core.internal.AnimationSpec
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.axis.AxisXItems
import io.github.hdcharts.core.internal.axis.AxisXLabelsLayout
import io.github.hdcharts.core.internal.axis.AxisYLabelsLayout
import io.github.hdcharts.core.internal.axis.baselineYForRange
import io.github.hdcharts.core.internal.axis.estimateXAxisLabelExtent
import io.github.hdcharts.core.internal.axis.rememberNumericYAxisLayout
import io.github.hdcharts.core.internal.axis.rememberXAxisLabelPlan
import io.github.hdcharts.core.internal.axis.xAxisLabelEdgeInsetPx
import io.github.hdcharts.core.internal.axis.xAxisLabelRowHeightPx
import io.github.hdcharts.core.internal.bezier.DEFAULT_BEZIER_TENSION
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.composable.rememberShowState
import io.github.hdcharts.core.internal.density.denseStepForViewport
import io.github.hdcharts.core.internal.interaction.buildHorizontalDragGestureModifier
import io.github.hdcharts.core.internal.interaction.buildTapGestureModifier
import io.github.hdcharts.core.internal.interaction.horizontalScrollGestures
import io.github.hdcharts.core.internal.interaction.nearestPointIndexForContentX
import io.github.hdcharts.core.internal.interaction.selectedIndexForTouchX
import io.github.hdcharts.core.internal.layout.chartCanvasFits
import io.github.hdcharts.core.internal.layout.placedHorizontalScrollPx
import io.github.hdcharts.core.internal.layout.wrapContentChartModifier
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.internal.model.normalizeByMinMax
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.line.LineChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.max
import kotlin.math.roundToInt

internal const val MARKER_REVEAL_DURATION_MS = 260
internal const val MARKER_REVEAL_THRESHOLD = 0.999f
internal const val MARKER_REVEAL_START_SCALE = 0.7f

/** Space kept above and below the plot so the line, points, and selection markers fit at the min and max values. */
internal fun Density.lineVerticalSafeInset(style: LineChartStyle): Float {
    val pointRadius = if (style.points.visible) style.points.size.toPx() else 0f
    val markerRadius =
        if (style.points.visible || style.selection.visible) {
            max(style.selection.markerSize.toPx(), style.selection.pointSize.toPx())
        } else {
            0f
        }
    return max(style.line.strokeWidth.toPx() / 2f, max(pointRadius, markerRadius)) + 1f
}

@Composable
internal fun LineChartContent(
    data: MultiChartData,
    style: LineChartStyle,
    colors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    renderMode: LineChartRenderMode,
    isDenseMode: Boolean = false,
    zoomScale: Float = 1f,
    selectedPointIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
    axisValueFormatter: ChartValueFormatter,
) {
    val xLabels = style.axis.xLabels
    val yLabels = style.axis.yLabels
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    val touchX = remember { mutableFloatStateOf(0f) }
    val dragging = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val valueAnimationSpec =
        remember(renderMode) {
            lineChartValueAnimationSpec(renderMode = renderMode)
        }
    val currentOnValueChanged by rememberUpdatedState(onValueChanged)

    val lineAnimation by animateFloatAsState(
        targetValue = if (show) ANIMATION_TARGET else 0f,
        animationSpec = AnimationSpec.lineChart(),
        label = "lineAnimation",
    )
    val markerRevealProgress by animateFloatAsState(
        targetValue =
            if (show && lineAnimation >= MARKER_REVEAL_THRESHOLD) {
                ANIMATION_TARGET
            } else {
                0f
            },
        animationSpec =
            tween(
                durationMillis = MARKER_REVEAL_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "lineMarkerReveal",
    )

    val rawSeries = remember(data) { data.items.map { item -> item.item.points } }
    val xAxisLabels = remember(data) { resolveLineXAxisLabels(data) }
    // X labels of a live window count from the points it has dropped, so each stays on its point.
    val timelineWindowCounter = remember { TimelineWindowCounter() }
    // Keyed on the mode type, not the instance: a new shiftDuration must not count the same window twice.
    val isTimeline = renderMode is LineChartRenderMode.Timeline
    val droppedTimelinePoints: Long? =
        remember(rawSeries, isTimeline) {
            if (isTimeline) timelineWindowCounter.next(rawSeries) else null
        }
    val minMax =
        remember(data, style.range.min, style.range.max) {
            data.resolveLineRange(style.range.min, style.range.max)
        }
    val targetNormalized = remember(rawSeries, minMax) { data.normalizeByMinMax(minMax, 0f) }
    val pointsCount = rawSeries.firstOrNull()?.size ?: 0
    val forcedSelectionIndex = selectedPointIndex.takeIf { it in 0 until pointsCount } ?: NO_SELECTION
    // A preset is authoritative for initial rendering, but does not disable later interaction.
    val hasForcedSelection = forcedSelectionIndex != NO_SELECTION
    val reportedSelection = remember(forcedSelectionIndex) { mutableIntStateOf(forcedSelectionIndex) }
    val seriesCount = rawSeries.size
    val bezierTension = DEFAULT_BEZIER_TENSION
    // One animated scalar morphs every point, so the animation costs the same whether the chart
    // draws ten points or ten thousand.
    val initialMorphValues =
        remember(seriesCount, pointsCount) {
            val startAtTarget = isPreview || !animateOnStart
            List(seriesCount) { seriesIndex ->
                val targetSeries = targetNormalized.getOrNull(seriesIndex)
                List(pointsCount) { pointIndex ->
                    if (startAtTarget) targetSeries?.getOrNull(pointIndex) ?: 0f else 0f
                }
            }
        }
    val transition = remember(seriesCount, pointsCount) { LineChartTransitionState(initialMorphValues) }
    val dragInteractionEnabled = interactionEnabled && !isDenseMode
    val tapInteractionEnabled = interactionEnabled && isDenseMode
    // Every frame scales its values into this buffer. A timeline window holds one point more than
    // it draws, so the buffer covers the widest frame the chart can produce.
    val drawValuesBuffer = remember(pointsCount) { FloatArray(pointsCount + 1) }
    val dragValuesBuffer = remember(pointsCount) { FloatArray(pointsCount) }
    // The path, the canvas heights, and the control points are reused for every series of every
    // frame, so an expanded chart of a million points draws without allocating per point or
    // per segment.
    val drawScratch = remember(pointsCount) { LineChartDrawScratch(valuesCapacity = pointsCount + 1) }

    LaunchedEffect(dragInteractionEnabled, tapInteractionEnabled, hasForcedSelection) {
        dragging.value = false
        if (hasForcedSelection) return@LaunchedEffect
        if (!tapInteractionEnabled) {
            if (reportedSelection.intValue != NO_SELECTION) {
                reportedSelection.intValue = NO_SELECTION
                currentOnValueChanged(NO_SELECTION)
            }
        }
    }

    LaunchedEffect(pointsCount, tapInteractionEnabled, hasForcedSelection) {
        if (hasForcedSelection) return@LaunchedEffect
        if (tapInteractionEnabled && reportedSelection.intValue >= pointsCount) {
            reportedSelection.intValue = NO_SELECTION
            currentOnValueChanged(NO_SELECTION)
        }
    }

    LaunchedEffect(show, rawSeries, minMax, renderMode) {
        if (pointsCount <= 0 || seriesCount == 0) return@LaunchedEffect

        if (!show && !isPreview) {
            transition.resetForHidden(seriesCount = seriesCount, pointsCount = pointsCount)
            return@LaunchedEffect
        }

        transition.update(
            currentRawSeries = rawSeries,
            currentMinMax = minMax,
            targetNormalized = targetNormalized,
            renderMode = renderMode,
            animationSpec = valueAnimationSpec,
            droppedTimelinePoints = droppedTimelinePoints ?: 0L,
            isPreview = isPreview,
        )
    }

    val showYAxisLabels = yLabels.visible
    val showXAxisLabels =
        xLabels.visible &&
            xAxisLabels.isNotEmpty()
    val showAxisLines = style.axis.visible

    BoxWithConstraints(
        modifier =
            wrapContentChartModifier(style.chartContainerStyle)
                .onGloballyPositioned {
                    show = true
                },
    ) {
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val xAxisLabelSizePx = with(density) { xLabels.size.toPx() }
        val xAxisLabelExtent =
            remember(showXAxisLabels, xAxisLabels, pointsCount, xAxisLabelSizePx) {
                if (showXAxisLabels) {
                    estimateXAxisLabelExtent(
                        labels = xAxisLabels,
                        dataSize = pointsCount,
                        fontSizePx = xAxisLabelSizePx,
                    )
                } else {
                    null
                }
            }
        val xAxisRowHeightPx = xAxisLabelExtent?.let { with(density) { xAxisLabelRowHeightPx(it) } } ?: 0f
        val xAxisHeight = with(density) { xAxisRowHeightPx.toDp() }
        val chartHeight = (maxHeight - xAxisHeight).coerceAtLeast(0.dp)
        val chartHeightPx = with(density) { chartHeight.toPx() }.coerceAtLeast(1f)
        val lineVerticalInsetPx = with(density) { lineVerticalSafeInset(style) }.coerceAtMost(chartHeightPx / 2f)
        val yAxisLayout =
            rememberNumericYAxisLayout(
                labels = yLabels,
                minValue = minMax.first,
                maxValue = minMax.second,
                chartHeightPx = chartHeightPx,
                verticalInsetPx = lineVerticalInsetPx,
                formatter = axisValueFormatter,
                availableWidthPx = constraints.maxWidth,
            )
        val yAxisWidthPx = yAxisLayout.widthPx
        val yAxisGapPx = yAxisLayout.gapPx
        val yAxisWidth = with(density) { yAxisWidthPx.toDp() }
        // The first and last points sit on the plot edges, so their centered labels reach past them. The
        // plot moves in by the part that the chart padding, and on the left the Y-axis gutter, cannot hold.
        val xAxisLabelEdgeInsetPx =
            xAxisLabelExtent?.let {
                xAxisLabelEdgeInsetPx(
                    it,
                    edgeSlackPx = with(density) { style.chartContainerStyle.contentPadding.toPx() },
                    availableWidthPx = constraints.maxWidth,
                )
            } ?: 0f
        val plotStartInsetPx = (xAxisLabelEdgeInsetPx - yAxisWidthPx - yAxisGapPx).coerceAtLeast(0f)
        val plotStartPadding = with(density) { (yAxisWidthPx + yAxisGapPx + plotStartInsetPx).toDp() }
        val plotEndPadding = with(density) { xAxisLabelEdgeInsetPx.toDp() }
        val plotViewportWidthPx =
            (constraints.maxWidth.toFloat() - yAxisWidthPx - yAxisGapPx - plotStartInsetPx - xAxisLabelEdgeInsetPx)
                .coerceAtLeast(1f)
        val fitStepX =
            when {
                pointsCount <= 1 -> plotViewportWidthPx
                else -> plotViewportWidthPx / (pointsCount - 1)
            }
        val denseStepX =
            denseStepForViewport(
                viewportWidth = plotViewportWidthPx,
                pointsCount = pointsCount,
                zoomScale = zoomScale,
            )
        val plotContentWidthPx =
            if (isDenseMode && pointsCount > 1) {
                max(plotViewportWidthPx, denseStepX * (pointsCount - 1))
            } else {
                plotViewportWidthPx
            }
        if (!chartCanvasFits(plotContentWidthPx, chartHeightPx)) {
            ChartErrors(
                style = style.chartContainerStyle,
                errors = persistentListOf("Chart exceeds layout limits. Reduce zoom or collapse the chart."),
                modifier = Modifier.fillMaxWidth(),
            )
            return@BoxWithConstraints
        }
        val plotContentWidth = with(density) { plotContentWidthPx.toDp() }
        val scrollOffsetPx =
            if (isDenseMode) {
                with(density) { placedHorizontalScrollPx(scrollState.value, plotContentWidth, plotViewportWidthPx) }
                    .toFloat()
            } else {
                0f
            }
        LaunchedEffect(plotContentWidthPx, plotViewportWidthPx, isDenseMode) {
            val maxScroll = (plotContentWidthPx - plotViewportWidthPx).roundToInt().coerceAtLeast(0)
            if (!isDenseMode && scrollState.value != 0) {
                scrollState.scrollTo(0)
            } else if (scrollState.value > maxScroll) {
                scrollState.scrollTo(maxScroll)
            }
        }

        val dragInteractionModifier =
            buildHorizontalDragGestureModifier(
                dragInteractionEnabled,
                pointsCount,
                plotViewportWidthPx,
                onDragStart = { offset ->
                    dragging.value = true
                    touchX.floatValue = offset.x
                    val selectedIndex =
                        selectedIndexForTouchX(
                            touchX = offset.x,
                            widthPx = plotViewportWidthPx,
                            pointsCount = pointsCount,
                        )
                    if (reportedSelection.intValue != selectedIndex) {
                        reportedSelection.intValue = selectedIndex
                        currentOnValueChanged(selectedIndex)
                    }
                },
                onHorizontalDrag = { position ->
                    touchX.floatValue = position.x
                    val selectedIndex =
                        selectedIndexForTouchX(
                            touchX = position.x,
                            widthPx = plotViewportWidthPx,
                            pointsCount = pointsCount,
                        )
                    if (reportedSelection.intValue != selectedIndex) {
                        reportedSelection.intValue = selectedIndex
                        currentOnValueChanged(selectedIndex)
                    }
                },
                onDragEnd = {
                    dragging.value = false
                    if (reportedSelection.intValue != NO_SELECTION) {
                        reportedSelection.intValue = NO_SELECTION
                        currentOnValueChanged(NO_SELECTION)
                    }
                },
                onDragCancel = {
                    dragging.value = false
                    if (reportedSelection.intValue != NO_SELECTION) {
                        reportedSelection.intValue = NO_SELECTION
                        currentOnValueChanged(NO_SELECTION)
                    }
                },
            )

        val denseTapInteractionModifier =
            buildTapGestureModifier(
                tapInteractionEnabled,
                pointsCount,
                denseStepX,
                onTap = { offset ->
                    val selectedIndex =
                        nearestPointIndexForContentX(
                            contentX = offset.x + scrollState.value.toFloat(),
                            pointsCount = pointsCount,
                            stepPx = denseStepX,
                        )
                    if (selectedIndex != NO_SELECTION) {
                        val toggledSelection =
                            if (reportedSelection.intValue == selectedIndex) {
                                NO_SELECTION
                            } else {
                                selectedIndex
                            }
                        if (reportedSelection.intValue != toggledSelection) {
                            reportedSelection.intValue = toggledSelection
                            currentOnValueChanged(toggledSelection)
                        }
                    }
                },
            )

        val xAxisLabelPlan =
            rememberXAxisLabelPlan(
                labels = xAxisLabels,
                dataSize = pointsCount,
                maxLabelCount = xLabels.maxCount,
                isScrollable = isDenseMode,
                unitWidthPx = if (isDenseMode) denseStepX else fitStepX,
                viewportWidthPx = plotViewportWidthPx,
                fontSizePx = xAxisLabelSizePx,
                items = AxisXItems.Points,
                scrollOffsetPx = scrollOffsetPx,
                firstItemIndex = droppedTimelinePoints,
            )
        val xAxisTicks = xAxisLabelPlan.ticks

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .testTag(TestTags.LINE_CHART),
        ) {
            if (showYAxisLabels) {
                AxisYLabelsLayout(
                    ticks = yAxisLayout.ticks,
                    color = yLabels.color,
                    fontSize = yLabels.size,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .fillMaxHeight()
                            .width(yAxisWidth)
                            .testTag(TestTags.LINE_CHART_Y_AXIS_LABELS),
                )
            }

            // Gestures cover the end inset too, so a touch right of the last point still selects it.
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = plotStartPadding)
                        .then(denseTapInteractionModifier)
                        .then(dragInteractionModifier)
                        .then(
                            if (isDenseMode) {
                                Modifier.horizontalScrollGestures(
                                    state = scrollState,
                                    enabled = interactionEnabled,
                                    layoutDirection = layoutDirection,
                                )
                            } else {
                                Modifier
                            },
                        ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(end = plotEndPadding)
                            .testTag(TestTags.LINE_CHART_PLOT)
                            .then(
                                if (isDenseMode) {
                                    Modifier.horizontalScroll(state = scrollState, enabled = interactionEnabled)
                                } else {
                                    Modifier
                                },
                            ),
                ) {
                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .requiredWidth(plotContentWidth),
                        onDraw = {
                            if (!show) return@Canvas

                            if (showAxisLines) {
                                val drawableHeight = (size.height - (lineVerticalInsetPx * 2f)).coerceAtLeast(0f)
                                val baselineY =
                                    lineVerticalInsetPx +
                                        baselineYForRange(
                                            minValue = minMax.first,
                                            maxValue = minMax.second,
                                            heightPx = drawableHeight,
                                        )

                                drawLine(
                                    color = style.axis.color,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = style.axis.lineWidth.toPx(),
                                )
                                drawLine(
                                    color = style.axis.color,
                                    start = Offset(0f, baselineY),
                                    end = Offset(size.width, baselineY),
                                    strokeWidth = style.axis.lineWidth.toPx(),
                                )
                            }

                            drawLineChartSeries(
                                transition = transition,
                                seriesCount = seriesCount,
                                pointsCount = pointsCount,
                                valuesBuffer = drawValuesBuffer,
                                scratch = drawScratch,
                                style = style,
                                colors = colors,
                                bezierTension = bezierTension,
                                lineAnimationProgress = lineAnimation,
                                markerRevealProgress = markerRevealProgress,
                                isDenseMode = isDenseMode,
                                denseStepX = denseStepX,
                                verticalInset = lineVerticalInsetPx,
                                revealViewportStartPx = scrollOffsetPx,
                                revealViewportWidthPx = plotViewportWidthPx,
                            )

                            drawLineChartSelection(
                                transition = transition,
                                selectedIndex = reportedSelection.intValue,
                                seriesCount = seriesCount,
                                pointsCount = pointsCount,
                                isDragging = dragging.value,
                                stepX = if (isDenseMode) denseStepX else fitStepX,
                                style = style,
                                verticalInset = lineVerticalInsetPx,
                            )
                        },
                    )

                    if (dragInteractionEnabled) {
                        Canvas(
                            modifier = Modifier.fillMaxSize(),
                            onDraw = {
                                if (!dragging.value) return@Canvas
                                val morphProgress = transition.morph.progress.value
                                data.items.forEachIndexed { index, _ ->
                                    val from =
                                        transition.morph.from
                                            .getOrNull(index)
                                            .orEmpty()
                                    val to =
                                        transition.morph.to
                                            .getOrNull(index)
                                            .orEmpty()
                                    val valuesCount =
                                        blendInto(
                                            into = dragValuesBuffer,
                                            from = from,
                                            to = to,
                                            progress = morphProgress,
                                            scaleBy = size.height,
                                        )
                                    drawDragMarker(
                                        touchX = touchX.floatValue,
                                        values = dragValuesBuffer,
                                        valuesCount = valuesCount,
                                        style = style,
                                        bezierTension = bezierTension,
                                        verticalInset = lineVerticalInsetPx,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }

        if (showXAxisLabels) {
            AxisXLabelsLayout(
                ticks = xAxisTicks,
                color = xLabels.color,
                fontSize = xLabels.size,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = plotStartPadding, end = plotEndPadding)
                        .height(xAxisHeight)
                        .testTag(TestTags.LINE_CHART_X_AXIS_LABELS),
                tickOffsetPx = {
                    // The labels are planned for the newest window, but the line draws the window it
                    // last shifted to until the update effect starts the next shift. Each label sits
                    // one step right per point the drawn line lags behind, less the part of the shift
                    // that has run, so it stays on its point.
                    val pendingPoints = (droppedTimelinePoints ?: 0L) - transition.drawnTimelinePoints
                    val progress = if (transition.isShifting) transition.shiftProgress else ANIMATION_TARGET
                    if (pendingPoints in 0L..1L) {
                        (pendingPoints + ANIMATION_TARGET - progress) * fitStepX
                    } else {
                        0f
                    }
                },
            )
        }
    }
}
