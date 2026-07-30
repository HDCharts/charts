package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.LineChartRenderMode
import io.github.dautovicharis.charts.internal.ANIMATION_TARGET
import io.github.dautovicharis.charts.internal.AXIS_LABEL_CHART_GAP
import io.github.dautovicharis.charts.internal.AnimationSpec
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.axis.AxisXPlanRequest
import io.github.dautovicharis.charts.internal.common.axis.baselineYForRange
import io.github.dautovicharis.charts.internal.common.axis.estimateXAxisLabelFootprintPx
import io.github.dautovicharis.charts.internal.common.axis.estimateYAxisLabelWidthPx
import io.github.dautovicharis.charts.internal.common.axis.planAxisXLabels
import io.github.dautovicharis.charts.internal.common.bezier.DEFAULT_BEZIER_TENSION
import io.github.dautovicharis.charts.internal.common.composable.rememberShowState
import io.github.dautovicharis.charts.internal.common.interaction.buildHorizontalDragGestureModifier
import io.github.dautovicharis.charts.internal.common.interaction.buildTapGestureModifier
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.model.minMax
import io.github.dautovicharis.charts.internal.common.model.normalizeByMinMax
import io.github.dautovicharis.charts.style.LineChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

internal const val LINE_STROKE_WIDTH = 5f
internal const val MARKER_REVEAL_DURATION_MS = 260
internal const val MARKER_REVEAL_THRESHOLD = 0.999f
internal const val MARKER_REVEAL_START_SCALE = 0.7f
internal const val MIN_TIMELINE_DURATION_MS = 1
internal const val LINE_VERTICAL_SAFE_INSET = (LINE_STROKE_WIDTH / 2f) + 1f
internal const val LINE_DENSE_MIN_STEP_PX = 12f
internal const val FIXED_X_AXIS_LABEL_TILT_DEGREES = 34f

internal data class TimelineTransitionData(
    val previousSeries: List<List<Double>>,
    val currentSeries: List<List<Double>>,
    val minMax: Pair<Double, Double>,
)

internal sealed interface LineChartTransitionMode {
    data object Morph : LineChartTransitionMode

    data class TimelineShift(
        val transitionData: TimelineTransitionData,
        val animationDurationMillis: Int,
    ) : LineChartTransitionMode
}

internal data class LineChartUpdateDecision(
    val normalizationMinMax: Pair<Double, Double>?,
    val nextTimelineRenderMinMax: Pair<Double, Double>?,
    val mode: LineChartTransitionMode,
)

@Composable
internal fun LineChartContent(
    data: MultiChartData,
    style: LineChartStyle,
    colors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    renderMode: LineChartRenderMode = LineChartRenderMode.Morph,
    animationDurationMillis: Int = 420,
    isDenseMorphMode: Boolean = false,
    scrollState: ScrollState,
    zoomScale: Float = 1f,
    selectedPointIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
) {
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    val touchX = remember { mutableFloatStateOf(0f) }
    val dragging = remember { mutableStateOf(false) }
    val valueAnimationSpec = remember { AnimationSpec.lineChart() }

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

    val rawSeries = remember(data) { data.items.map { item -> item.item.points.toList() } }
    val xAxisLabels = remember(data) { resolveLineXAxisLabels(data) }
    val minMax = remember(data) { data.minMax() }
    val targetNormalized = remember(rawSeries, minMax) { data.normalizeByMinMax(minMax, 0f) }
    val pointsCount = rawSeries.firstOrNull()?.size ?: 0
    val forcedSelectionIndex = selectedPointIndex.takeIf { it in 0 until pointsCount } ?: NO_SELECTION
    val hasForcedSelection = forcedSelectionIndex != NO_SELECTION
    val reportedSelection = remember(forcedSelectionIndex) { mutableIntStateOf(forcedSelectionIndex) }
    val seriesCount = rawSeries.size
    val bezierTension = DEFAULT_BEZIER_TENSION
    val animatedValues =
        remember(seriesCount, pointsCount) {
            List(seriesCount) { seriesIndex ->
                List(pointsCount) { pointIndex ->
                    val initialValue =
                        when {
                            isPreview || !animateOnStart ->
                                targetNormalized.getOrNull(seriesIndex)?.getOrNull(pointIndex) ?: 0f
                            else -> 0f
                        }
                    Animatable(initialValue)
                }
            }
        }
    val hasInitialized = remember { mutableStateOf(false) }
    val previousRawSeries = remember { mutableStateOf<List<List<Double>>?>(null) }
    val timelineTransitionData = remember { mutableStateOf<TimelineTransitionData?>(null) }
    val timelineProgress = remember { Animatable(ANIMATION_TARGET) }
    val timelineRenderMinMax = remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val isTimelineMode = renderMode == LineChartRenderMode.Timeline
    val denseMorphEnabled = isDenseMorphMode && !isTimelineMode
    val dragInteractionEnabled = interactionEnabled && !isTimelineMode && !denseMorphEnabled && !hasForcedSelection
    val tapInteractionEnabled = interactionEnabled && denseMorphEnabled && !hasForcedSelection

    LaunchedEffect(dragInteractionEnabled, tapInteractionEnabled, hasForcedSelection) {
        dragging.value = false
        if (hasForcedSelection) return@LaunchedEffect
        if (!tapInteractionEnabled) {
            dragging.value = false
            if (reportedSelection.intValue != NO_SELECTION) {
                reportedSelection.intValue = NO_SELECTION
                onValueChanged(NO_SELECTION)
            }
        }
    }

    LaunchedEffect(pointsCount, tapInteractionEnabled, hasForcedSelection) {
        if (hasForcedSelection) return@LaunchedEffect
        if (tapInteractionEnabled && reportedSelection.intValue >= pointsCount) {
            reportedSelection.intValue = NO_SELECTION
            onValueChanged(NO_SELECTION)
        }
    }

    LaunchedEffect(show, rawSeries, renderMode, animationDurationMillis) {
        if (pointsCount <= 0 || seriesCount == 0) return@LaunchedEffect

        if (!show && !isPreview) {
            animatedValues.forEach { series ->
                series.forEach { animatable -> animatable.snapTo(0f) }
            }
            hasInitialized.value = false
            previousRawSeries.value = null
            timelineTransitionData.value = null
            timelineProgress.snapTo(ANIMATION_TARGET)
            timelineRenderMinMax.value = null
            return@LaunchedEffect
        }

        val previousRawSnapshot = previousRawSeries.value
        previousRawSeries.value = rawSeries

        val updateDecision =
            decideLineChartUpdate(
                previousRawSeries = previousRawSnapshot,
                currentRawSeries = rawSeries,
                currentMinMax = minMax,
                previousTimelineRenderMinMax = timelineRenderMinMax.value,
                renderMode = renderMode,
                animationDurationMillis = animationDurationMillis,
            )
        timelineRenderMinMax.value = updateDecision.nextTimelineRenderMinMax
        val normalizedForCurrent =
            when (val normalizationMinMax = updateDecision.normalizationMinMax) {
                null -> targetNormalized
                else -> normalizeSeriesByMinMax(series = rawSeries, minMax = normalizationMinMax)
            }
        val hasStructureChanged =
            previousRawSnapshot != null &&
                !hasSameSeriesStructure(
                    previous = previousRawSnapshot,
                    current = rawSeries,
                )

        if (hasStructureChanged) {
            animatedValues.forEachIndexed { seriesIndex, series ->
                val targetSeries = normalizedForCurrent.getOrNull(seriesIndex) ?: emptyList()
                series.forEachIndexed { pointIndex, animatable ->
                    val target = targetSeries.getOrNull(pointIndex) ?: 0f
                    animatable.snapTo(target)
                }
            }
            hasInitialized.value = true
            timelineTransitionData.value = null
            timelineProgress.snapTo(ANIMATION_TARGET)
            return@LaunchedEffect
        }

        if (isPreview || !hasInitialized.value) {
            animatedValues.forEachIndexed { seriesIndex, series ->
                val targetSeries = normalizedForCurrent.getOrNull(seriesIndex) ?: emptyList()
                series.forEachIndexed { pointIndex, animatable ->
                    val target = targetSeries.getOrNull(pointIndex) ?: 0f
                    animatable.snapTo(target)
                }
            }
            hasInitialized.value = true
            timelineTransitionData.value = null
            timelineProgress.snapTo(ANIMATION_TARGET)
            return@LaunchedEffect
        }

        when (val mode = updateDecision.mode) {
            is LineChartTransitionMode.TimelineShift -> {
                animatedValues.forEachIndexed { seriesIndex, series ->
                    val targetSeries = normalizedForCurrent.getOrNull(seriesIndex) ?: emptyList()
                    series.forEachIndexed { pointIndex, animatable ->
                        val target = targetSeries.getOrNull(pointIndex) ?: 0f
                        animatable.snapTo(target)
                    }
                }

                timelineTransitionData.value = mode.transitionData

                timelineProgress.snapTo(0f)
                timelineProgress.animateTo(
                    targetValue = ANIMATION_TARGET,
                    animationSpec =
                        tween(
                            durationMillis = mode.animationDurationMillis,
                            easing = LinearEasing,
                        ),
                )
                timelineTransitionData.value = null
                return@LaunchedEffect
            }

            LineChartTransitionMode.Morph -> Unit
        }

        timelineTransitionData.value = null
        timelineProgress.snapTo(ANIMATION_TARGET)

        coroutineScope {
            animatedValues.forEachIndexed { seriesIndex, series ->
                val targetSeries = normalizedForCurrent.getOrNull(seriesIndex) ?: emptyList()
                series.forEachIndexed { pointIndex, animatable ->
                    val target = targetSeries.getOrNull(pointIndex) ?: 0f
                    launch {
                        val shouldAnimate = !isPreview && (animateOnStart || hasInitialized.value)
                        if (!shouldAnimate) {
                            animatable.snapTo(target)
                        } else {
                            animatable.animateTo(
                                targetValue = target,
                                animationSpec = valueAnimationSpec,
                            )
                        }
                    }
                }
            }
        }
    }

    val dragInteractionModifier =
        buildHorizontalDragGestureModifier(
            dragInteractionEnabled,
            pointsCount,
            onDragStart = { offset ->
                dragging.value = true
                touchX.floatValue = offset.x
                val selectedIndex =
                    selectedIndexForTouch(
                        touchX = offset.x,
                        width = size.width.toFloat(),
                        pointsCount = pointsCount,
                    )
                if (reportedSelection.intValue != selectedIndex) {
                    reportedSelection.intValue = selectedIndex
                    onValueChanged(selectedIndex)
                }
            },
            onHorizontalDrag = { position ->
                touchX.floatValue = position.x
                val selectedIndex =
                    selectedIndexForTouch(
                        touchX = position.x,
                        width = size.width.toFloat(),
                        pointsCount = pointsCount,
                    )
                if (reportedSelection.intValue != selectedIndex) {
                    reportedSelection.intValue = selectedIndex
                    onValueChanged(selectedIndex)
                }
            },
            onDragEnd = {
                dragging.value = false
                if (reportedSelection.intValue != NO_SELECTION) {
                    reportedSelection.intValue = NO_SELECTION
                    onValueChanged(NO_SELECTION)
                }
            },
            onDragCancel = {
                dragging.value = false
                if (reportedSelection.intValue != NO_SELECTION) {
                    reportedSelection.intValue = NO_SELECTION
                    onValueChanged(NO_SELECTION)
                }
            },
        )

    val denseTapInteractionModifier =
        buildTapGestureModifier(
            tapInteractionEnabled,
            pointsCount,
            zoomScale,
            onTap = { offset ->
                val stepX =
                    denseStepForViewport(
                        viewportWidth = size.width.toFloat(),
                        pointsCount = pointsCount,
                        zoomScale = zoomScale,
                    )
                val selectedIndex =
                    selectedIndexForContentX(
                        contentX = offset.x + scrollState.value.toFloat(),
                        pointsCount = pointsCount,
                        stepX = stepX,
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
                        onValueChanged(toggledSelection)
                    }
                }
            },
        )

    val showYAxisLabels = style.yAxisLabelsVisible
    val showXAxisLabelsCandidate =
        style.xAxisLabelsVisible &&
            xAxisLabels.isNotEmpty()
    val showAxisLines = style.axisVisible

    BoxWithConstraints(
        modifier =
            style.modifier
                .onGloballyPositioned {
                    show = true
                },
    ) {
        val density = LocalDensity.current
        val xAxisTilt = FIXED_X_AXIS_LABEL_TILT_DEGREES
        val xAxisLabelSizePx = with(density) { style.xAxisLabelSize.toPx() }
        val xAxisLabelFootprintPx =
            remember(xAxisLabels, pointsCount, xAxisLabelSizePx, xAxisTilt) {
                estimateXAxisLabelFootprintPx(
                    labels = xAxisLabels,
                    dataSize = pointsCount,
                    fontSizePx = xAxisLabelSizePx,
                    tiltDegrees = xAxisTilt,
                )
            }
        val xAxisHeight =
            if (!showXAxisLabelsCandidate) {
                0.dp
            } else {
                with(density) {
                    (xAxisLabelFootprintPx.height + AXIS_LABEL_CHART_GAP.toPx()).toDp()
                }
            }
        val chartHeight = (maxHeight - xAxisHeight).coerceAtLeast(0.dp)
        val chartHeightPx = with(density) { chartHeight.toPx() }.coerceAtLeast(1f)
        val lineVerticalInsetPx = LINE_VERTICAL_SAFE_INSET.coerceAtMost(chartHeightPx / 2f)
        val yAxisTicks =
            remember(minMax, chartHeightPx, style.yAxisLabelCount, showYAxisLabels, lineVerticalInsetPx) {
                if (!showYAxisLabels) {
                    emptyList()
                } else {
                    buildLineYAxisTicks(
                        minValue = minMax.first,
                        maxValue = minMax.second,
                        labelCount = style.yAxisLabelCount,
                        plotHeightPx = chartHeightPx,
                        verticalInsetPx = lineVerticalInsetPx,
                    )
                }
            }
        val yAxisLabelSizePx = with(density) { style.yAxisLabelSize.toPx() }
        val yAxisWidthPx =
            if (showYAxisLabels) {
                estimateYAxisLabelWidthPx(
                    labels = yAxisTicks.map { tick -> tick.label },
                    fontSizePx = yAxisLabelSizePx,
                )
            } else {
                0f
            }
        val yAxisGapPx = if (showYAxisLabels) with(density) { AXIS_LABEL_CHART_GAP.toPx() } else 0f
        val yAxisWidth = with(density) { yAxisWidthPx.toDp() }
        val plotStartPadding = with(density) { (yAxisWidthPx + yAxisGapPx).toDp() }
        val plotViewportWidthPx =
            (constraints.maxWidth.toFloat() - yAxisWidthPx - yAxisGapPx).coerceAtLeast(1f)
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
            if (denseMorphEnabled && pointsCount > 1) {
                max(plotViewportWidthPx, denseStepX * (pointsCount - 1))
            } else {
                plotViewportWidthPx
            }
        val plotContentWidth = with(density) { plotContentWidthPx.toDp() }
        val scrollOffsetPx = if (denseMorphEnabled) scrollState.value.toFloat() else 0f
        LaunchedEffect(plotContentWidthPx, plotViewportWidthPx, denseMorphEnabled) {
            val maxScroll = (plotContentWidthPx - plotViewportWidthPx).roundToInt().coerceAtLeast(0)
            if (!denseMorphEnabled && scrollState.value != 0) {
                scrollState.scrollTo(0)
            } else if (scrollState.value > maxScroll) {
                scrollState.scrollTo(maxScroll)
            }
        }

        val xAxisPlan =
            remember(
                pointsCount,
                style.xAxisLabelMaxCount,
                denseMorphEnabled,
                fitStepX,
                denseStepX,
                plotViewportWidthPx,
                scrollOffsetPx,
                xAxisLabelFootprintPx.width,
            ) {
                planAxisXLabels(
                    request =
                        AxisXPlanRequest(
                            dataSize = pointsCount,
                            requestedMaxLabelCount = style.xAxisLabelMaxCount,
                            isScrollable = denseMorphEnabled,
                            unitWidthPx =
                                if (denseMorphEnabled) {
                                    denseStepX.coerceAtLeast(1f)
                                } else {
                                    fitStepX.coerceAtLeast(1f)
                                },
                            viewportWidthPx = plotViewportWidthPx,
                            scrollOffsetPx = scrollOffsetPx,
                            firstCenterPx = 0f,
                            labelWidthPx = xAxisLabelFootprintPx.width,
                        ),
                )
            }
        val xAxisLabelIndices =
            if (showXAxisLabelsCandidate) {
                xAxisPlan.labelIndices
            } else {
                emptyList()
            }
        val showXAxisLabels = showXAxisLabelsCandidate && xAxisLabelIndices.isNotEmpty()
        val xAxisTicks =
            remember(
                xAxisLabels,
                xAxisLabelIndices,
                pointsCount,
                fitStepX,
                denseStepX,
                denseMorphEnabled,
                scrollOffsetPx,
                showXAxisLabels,
            ) {
                if (!showXAxisLabels) {
                    emptyList()
                } else {
                    buildLineXAxisTicks(
                        labels = xAxisLabels,
                        labelIndices = xAxisLabelIndices,
                        pointsCount = pointsCount,
                        stepX = if (denseMorphEnabled) denseStepX else fitStepX,
                        scrollOffsetPx = scrollOffsetPx,
                    )
                }
            }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .testTag(TestTags.LINE_CHART),
        ) {
            if (showYAxisLabels) {
                LineYAxisLabels(
                    ticks = yAxisTicks,
                    color = style.yAxisLabelColor,
                    fontSize = style.yAxisLabelSize,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .fillMaxHeight()
                            .width(yAxisWidth),
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = plotStartPadding),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag(TestTags.LINE_CHART_PLOT)
                            .then(denseTapInteractionModifier)
                            .then(
                                if (denseMorphEnabled) {
                                    Modifier.horizontalScroll(state = scrollState, enabled = true)
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
                                val verticalInset = LINE_VERTICAL_SAFE_INSET.coerceAtMost(size.height / 2f)
                                val drawableHeight = (size.height - (verticalInset * 2f)).coerceAtLeast(0f)
                                val baselineY =
                                    verticalInset +
                                        baselineYForRange(
                                            minValue = minMax.first,
                                            maxValue = minMax.second,
                                            heightPx = drawableHeight,
                                        )

                                drawLine(
                                    color = style.axisColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = style.axisLineWidth,
                                )
                                drawLine(
                                    color = style.axisColor,
                                    start = Offset(0f, baselineY),
                                    end = Offset(size.width, baselineY),
                                    strokeWidth = style.axisLineWidth,
                                )
                            }

                            val transitionData = timelineTransitionData.value
                            val progress = timelineProgress.value.coerceIn(0f, ANIMATION_TARGET)
                            val useTimeline =
                                transitionData != null &&
                                    renderMode == LineChartRenderMode.Timeline &&
                                    progress < ANIMATION_TARGET

                            if (useTimeline) {
                                val normalizedPrevious =
                                    normalizeSeriesByMinMax(
                                        series = transitionData.previousSeries,
                                        minMax = transitionData.minMax,
                                    )
                                val normalizedCurrent =
                                    normalizeSeriesByMinMax(
                                        series = transitionData.currentSeries,
                                        minMax = transitionData.minMax,
                                    )
                                val shiftPx = -(progress * timelineStep(size.width, pointsCount))

                                data.items.forEachIndexed { index, _ ->
                                    val previousValues = normalizedPrevious.getOrNull(index).orEmpty()
                                    val currentValues = normalizedCurrent.getOrNull(index).orEmpty()
                                    if (previousValues.isEmpty() || currentValues.isEmpty()) return@forEachIndexed

                                    val timelineValues = previousValues + currentValues.last()
                                    val scaledValues = timelineValues.map { value -> value * size.height }

                                    drawChartPath(
                                        values = scaledValues,
                                        style = style,
                                        lineAnimationProgress = lineAnimation,
                                        markerRevealProgress = markerRevealProgress,
                                        bezierTension = bezierTension,
                                        lineColor = colors[index],
                                        timelineWindowPoints = pointsCount,
                                        horizontalOffsetPx = shiftPx,
                                    )
                                }
                                return@Canvas
                            }

                            data.items.forEachIndexed { index, _ ->
                                val seriesValues = animatedValues.getOrNull(index).orEmpty()
                                val scaledValues = seriesValues.map { value -> value.value * size.height }
                                drawChartPath(
                                    values = scaledValues,
                                    style = style,
                                    lineAnimationProgress = lineAnimation,
                                    markerRevealProgress = markerRevealProgress,
                                    bezierTension = bezierTension,
                                    lineColor = colors[index],
                                    stepXOverride = if (denseMorphEnabled) denseStepX else null,
                                )
                            }

                            val selectedIndex = reportedSelection.intValue
                            if (!isTimelineMode && selectedIndex != NO_SELECTION && pointsCount > 1) {
                                val safeSelectedIndex = selectedIndex.coerceIn(0, pointsCount - 1)
                                val stepX = if (denseMorphEnabled) denseStepX else fitStepX
                                if (stepX > 0f) {
                                    val selectedX = safeSelectedIndex * stepX
                                    val selectionLineColor = resolveSelectionLineColor(style = style, colors = colors)
                                    val selectionStrokeWidth = max(style.axisLineWidth, 1f)
                                    drawLine(
                                        color = selectionLineColor,
                                        start = Offset(selectedX, 0f),
                                        end = Offset(selectedX, size.height),
                                        strokeWidth = selectionStrokeWidth,
                                    )

                                    if (!dragging.value && (style.dragPointVisible || style.pointVisible)) {
                                        val verticalInset = LINE_VERTICAL_SAFE_INSET.coerceAtMost(size.height / 2f)
                                        val markerRadius = style.dragActivePointSize.coerceAtLeast(1f)
                                        data.items.forEachIndexed { seriesIndex, _ ->
                                            val normalized =
                                                animatedValues
                                                    .getOrNull(seriesIndex)
                                                    ?.getOrNull(safeSelectedIndex)
                                                    ?.value
                                                    ?: return@forEachIndexed
                                            val y =
                                                mapScaledValueToCanvasY(
                                                    scaledValue = normalized * size.height,
                                                    canvasHeight = size.height,
                                                    verticalInset = verticalInset,
                                                )
                                            val markerColor =
                                                when (style.dragPointColorSameAsLine) {
                                                    true -> colors.getOrNull(seriesIndex) ?: selectionLineColor
                                                    else -> style.dragPointColor
                                                }
                                            drawCircle(
                                                color = markerColor,
                                                radius = markerRadius,
                                                center = Offset(selectedX, y),
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    )

                    if (dragInteractionEnabled) {
                        Canvas(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .then(dragInteractionModifier),
                            onDraw = {
                                if (!dragging.value) return@Canvas
                                data.items.forEachIndexed { index, _ ->
                                    val seriesValues = animatedValues.getOrNull(index).orEmpty()
                                    val scaledValues = seriesValues.map { value -> value.value * size.height }
                                    drawDragMarker(
                                        touchX = touchX.floatValue,
                                        values = scaledValues,
                                        style = style,
                                        lineColor = colors[index],
                                        bezierTension = bezierTension,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }

        if (showXAxisLabels) {
            LineXAxisLabels(
                ticks = xAxisTicks,
                color = style.xAxisLabelColor,
                fontSize = style.xAxisLabelSize,
                tiltDegrees = xAxisTilt,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = plotStartPadding)
                        .height(xAxisHeight),
            )
        }
    }
}
