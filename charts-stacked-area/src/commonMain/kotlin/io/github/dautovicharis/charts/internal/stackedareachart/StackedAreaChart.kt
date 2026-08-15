package io.github.dautovicharis.charts.internal.stackedareachart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.internal.ANIMATION_TARGET
import io.github.dautovicharis.charts.internal.AXIS_LABEL_CHART_GAP
import io.github.dautovicharis.charts.internal.AnimationSpec
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.axis.AxisXPlanRequest
import io.github.dautovicharis.charts.internal.common.axis.estimateXAxisLabelFootprintPx
import io.github.dautovicharis.charts.internal.common.axis.estimateYAxisLabelWidthPx
import io.github.dautovicharis.charts.internal.common.axis.planAxisXLabels
import io.github.dautovicharis.charts.internal.common.bezier.cubicControlPointsForSegment
import io.github.dautovicharis.charts.internal.common.composable.rememberDenseExpandedState
import io.github.dautovicharis.charts.internal.common.composable.rememberShowState
import io.github.dautovicharis.charts.internal.common.composable.rememberZoomScaleState
import io.github.dautovicharis.charts.internal.common.composable.zoomInScale
import io.github.dautovicharis.charts.internal.common.composable.zoomOutScale
import io.github.dautovicharis.charts.internal.common.interaction.buildHorizontalDragGestureModifier
import io.github.dautovicharis.charts.internal.common.interaction.buildPinchZoomModifier
import io.github.dautovicharis.charts.internal.common.interaction.buildTapGestureModifier
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.model.normalizeStackedAreaValues
import io.github.dautovicharis.charts.style.StackedAreaChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 4f
private const val ZOOM_STEP = 1.25f
private const val FIXED_X_AXIS_LABEL_TILT_DEGREES = 34f

@Composable
internal fun StackedAreaChart(
    data: MultiChartData,
    title: String,
    style: StackedAreaChartStyle,
    areaColors: ImmutableList<Color>,
    lineColors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedPointIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
) {
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    val sourcePointsCount =
        data.items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: 0
    val isDenseData =
        remember(sourcePointsCount) {
            shouldUseScrollableDensity(sourcePointsCount)
        }
    var denseExpanded by rememberDenseExpandedState(isDenseModeAvailable = isDenseData)
    val compactDenseMode = isDenseData && !denseExpanded
    val renderDataBundle =
        remember(data, compactDenseMode) {
            if (compactDenseMode) {
                aggregateForCompactDensity(data)
            } else {
                identityRenderData(data)
            }
        }
    val renderData = renderDataBundle.data
    val pointsCount =
        renderData.items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: 0
    val seriesCount = renderData.items.size
    val targetNormalized = remember(renderData) { renderData.normalizeStackedAreaValues() }
    val valueAnimationSpec = remember { AnimationSpec.lineChart() }
    val revealProgress by animateFloatAsState(
        targetValue = if (show) ANIMATION_TARGET else 0f,
        animationSpec = AnimationSpec.lineChart(),
        label = "stackedAreaReveal",
    )
    val animatedValues =
        remember(seriesCount, pointsCount, isPreview, animateOnStart) {
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
    var selectedSourceIndexFromInteraction by remember { mutableIntStateOf(NO_SELECTION) }
    val forcedSelectedSourceIndex =
        selectedPointIndex.takeIf { it in 0 until sourcePointsCount } ?: NO_SELECTION
    val hasForcedSelection = forcedSelectedSourceIndex != NO_SELECTION
    val isScrollable = isDenseData && denseExpanded
    val scrollState = rememberScrollState()
    var zoomScale by
        rememberZoomScaleState(
            isZoomActive = isScrollable,
            minZoom = ZOOM_MIN,
            maxZoom = ZOOM_MAX,
            initialZoom = ZOOM_MIN,
        )

    LaunchedEffect(show, targetNormalized) {
        if (pointsCount <= 0 || seriesCount == 0) return@LaunchedEffect
        if (!show && !isPreview) {
            animatedValues.forEach { series ->
                series.forEach { animatable -> animatable.snapTo(0f) }
            }
            hasInitialized.value = false
            return@LaunchedEffect
        }

        if (isPreview || !hasInitialized.value) {
            animatedValues.forEachIndexed { seriesIndex, series ->
                val targetSeries = targetNormalized.getOrNull(seriesIndex).orEmpty()
                series.forEachIndexed { pointIndex, animatable ->
                    val target = targetSeries.getOrNull(pointIndex) ?: 0f
                    animatable.snapTo(target)
                }
            }
            hasInitialized.value = true
            return@LaunchedEffect
        }

        coroutineScope {
            animatedValues.forEachIndexed { seriesIndex, series ->
                val targetSeries = targetNormalized.getOrNull(seriesIndex).orEmpty()
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
        hasInitialized.value = true
    }

    LaunchedEffect(sourcePointsCount) {
        if (hasForcedSelection) return@LaunchedEffect
        if (selectedSourceIndexFromInteraction !in 0 until sourcePointsCount) {
            selectedSourceIndexFromInteraction = NO_SELECTION
            onValueChanged(NO_SELECTION)
        }
    }

    LaunchedEffect(renderData) {
        if (hasForcedSelection) return@LaunchedEffect
        if (selectedSourceIndexFromInteraction != NO_SELECTION) {
            selectedSourceIndexFromInteraction = NO_SELECTION
            onValueChanged(NO_SELECTION)
        }
    }

    val effectiveSelectedSourceIndex =
        when (forcedSelectedSourceIndex) {
            NO_SELECTION -> selectedSourceIndexFromInteraction
            else -> forcedSelectedSourceIndex
        }
    val effectiveSelectedRenderIndex =
        when (effectiveSelectedSourceIndex) {
            NO_SELECTION -> NO_SELECTION
            else -> renderDataBundle.resolveRenderIndex(effectiveSelectedSourceIndex)
        }

    val onSelectRenderIndex: (Int) -> Unit = { renderIndex ->
        if (!hasForcedSelection) {
            val resolvedSourceIndex =
                when (renderIndex) {
                    in 0 until pointsCount -> renderDataBundle.resolveSourceIndex(renderIndex)
                    else -> NO_SELECTION
                }
            if (selectedSourceIndexFromInteraction != resolvedSourceIndex) {
                selectedSourceIndexFromInteraction = resolvedSourceIndex
                onValueChanged(resolvedSourceIndex)
            }
        }
    }

    val onToggleSelection: (Int) -> Unit = { renderIndex ->
        val currentRenderIndex =
            when (hasForcedSelection) {
                true -> renderDataBundle.resolveRenderIndex(forcedSelectedSourceIndex)
                false -> renderDataBundle.resolveRenderIndex(selectedSourceIndexFromInteraction)
            }
        if (currentRenderIndex == renderIndex && currentRenderIndex != NO_SELECTION) {
            onSelectRenderIndex(NO_SELECTION)
        } else {
            onSelectRenderIndex(renderIndex)
        }
    }

    val showZoomControlsInHeader = isScrollable && style.zoomControlsVisible
    val showCompactToggle = isDenseData
    val showHeader = title.isNotBlank() || showCompactToggle || showZoomControlsInHeader

    Column(
        modifier =
            style.modifier
                .onGloballyPositioned { show = true },
    ) {
        if (showHeader) {
            StackedAreaChartHeader(
                title = title,
                style = style,
                showDensityToggle = showCompactToggle,
                denseExpanded = denseExpanded,
                onToggleDensity = { denseExpanded = !denseExpanded },
                showZoomControls = showZoomControlsInHeader,
                zoomScale = zoomScale,
                minZoom = ZOOM_MIN,
                maxZoom = ZOOM_MAX,
                onZoomOut = {
                    zoomScale = zoomOutScale(zoomScale, ZOOM_STEP, ZOOM_MIN, ZOOM_MAX)
                },
                onZoomIn = {
                    zoomScale = zoomInScale(zoomScale, ZOOM_STEP, ZOOM_MIN, ZOOM_MAX)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        StackedAreaChartContent(
            data = renderData,
            style = style,
            areaColors = areaColors,
            lineColors = lineColors,
            interactionEnabled = interactionEnabled,
            isScrollable = isScrollable,
            hasForcedSelection = hasForcedSelection,
            animatedValues = animatedValues,
            revealProgress = revealProgress,
            pointsCount = pointsCount,
            scrollState = scrollState,
            zoomScale = zoomScale,
            zoomMin = ZOOM_MIN,
            zoomMax = ZOOM_MAX,
            zoomStep = ZOOM_STEP,
            selectedIndex = effectiveSelectedRenderIndex,
            onToggleSelection = onToggleSelection,
            onSelectIndex = onSelectRenderIndex,
            onClearSelection = { onSelectRenderIndex(NO_SELECTION) },
            onZoomScaleChange = { updatedScale ->
                zoomScale = updatedScale
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = if (showHeader) style.chartContainerStyle.innerPadding else 0.dp),
        )
    }
}

@Composable
private fun StackedAreaChartContent(
    data: MultiChartData,
    style: StackedAreaChartStyle,
    areaColors: ImmutableList<Color>,
    lineColors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    isScrollable: Boolean,
    hasForcedSelection: Boolean,
    animatedValues: List<List<Animatable<Float, *>>>,
    revealProgress: Float,
    pointsCount: Int,
    scrollState: ScrollState,
    zoomScale: Float,
    zoomMin: Float,
    zoomMax: Float,
    zoomStep: Float,
    selectedIndex: Int,
    onToggleSelection: (Int) -> Unit,
    onSelectIndex: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onZoomScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val xAxisLabels = remember(data) { if (data.hasCategories()) data.categories.toList() else emptyList() }
    val showYAxisLabels = style.yAxisLabelsVisible
    val showXAxisLabelsCandidate = style.xAxisLabelsVisible && xAxisLabels.isNotEmpty()
    val dragInteractionEnabled = interactionEnabled && !isScrollable && !hasForcedSelection
    val tapInteractionEnabled = interactionEnabled && isScrollable && !hasForcedSelection

    BoxWithConstraints(modifier = modifier) {
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
        val (minTotal, maxTotal) =
            remember(data) {
                resolveStackedAreaTotalsRange(data)
            }
        val yAxisTicks =
            remember(minTotal, maxTotal, chartHeightPx, style.yAxisLabelCount, showYAxisLabels) {
                if (!showYAxisLabels) {
                    emptyList()
                } else {
                    buildStackedAreaYAxisTicks(
                        minValue = minTotal,
                        maxValue = maxTotal,
                        labelCount = style.yAxisLabelCount,
                        plotHeightPx = chartHeightPx,
                    )
                }
            }
        val yAxisWidthPx =
            if (showYAxisLabels) {
                estimateYAxisLabelWidthPx(
                    labels = yAxisTicks.map { tick -> tick.label },
                    fontSizePx = with(density) { style.yAxisLabelSize.toPx() },
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
            if (isScrollable && pointsCount > 1) {
                max(plotViewportWidthPx, denseStepX * (pointsCount - 1))
            } else {
                plotViewportWidthPx
            }
        val plotContentWidth = with(density) { plotContentWidthPx.toDp() }
        val scrollOffsetPx = if (isScrollable) scrollState.value.toFloat() else 0f

        LaunchedEffect(plotContentWidthPx, plotViewportWidthPx, isScrollable) {
            val maxScroll = (plotContentWidthPx - plotViewportWidthPx).roundToInt().coerceAtLeast(0)
            if (!isScrollable && scrollState.value != 0) {
                scrollState.scrollTo(0)
            } else if (scrollState.value > maxScroll) {
                scrollState.scrollTo(maxScroll)
            }
        }

        val xAxisPlan =
            remember(
                pointsCount,
                style.xAxisLabelMaxCount,
                isScrollable,
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
                            isScrollable = isScrollable,
                            unitWidthPx =
                                if (isScrollable) {
                                    denseStepX.coerceAtLeast(
                                        1f,
                                    )
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
        val visibleRange = xAxisPlan.visibleRange
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
                isScrollable,
                scrollOffsetPx,
                showXAxisLabels,
            ) {
                if (!showXAxisLabels) {
                    emptyList()
                } else {
                    buildStackedAreaXAxisTicks(
                        labels = xAxisLabels,
                        labelIndices = xAxisLabelIndices,
                        pointsCount = pointsCount,
                        stepX = if (isScrollable) denseStepX else fitStepX,
                        scrollOffsetPx = scrollOffsetPx,
                    )
                }
            }

        val dragModifier =
            buildHorizontalDragGestureModifier(
                enabled = dragInteractionEnabled,
                pointsCount,
                onDragStart = { offset ->
                    val selected =
                        selectedIndexForTouch(
                            touchX = offset.x,
                            width = size.width.toFloat(),
                            pointsCount = pointsCount,
                        )
                    onSelectIndex(selected)
                },
                onHorizontalDrag = { position ->
                    val selected =
                        selectedIndexForTouch(
                            touchX = position.x,
                            width = size.width.toFloat(),
                            pointsCount = pointsCount,
                        )
                    onSelectIndex(selected)
                },
                onDragEnd = { onClearSelection() },
                onDragCancel = { onClearSelection() },
            )
        val denseTapModifier =
            buildTapGestureModifier(
                enabled = tapInteractionEnabled,
                pointsCount,
                zoomScale,
                onTap = { offset ->
                    val stepX =
                        denseStepForViewport(
                            viewportWidth = size.width.toFloat(),
                            pointsCount = pointsCount,
                            zoomScale = zoomScale,
                        )
                    val selected =
                        selectedIndexForContentX(
                            contentX = offset.x + scrollState.value.toFloat(),
                            pointsCount = pointsCount,
                            stepX = stepX,
                        )
                    if (selected != NO_SELECTION) {
                        onToggleSelection(selected)
                    }
                },
                onDoubleTap = {
                    onZoomScaleChange((zoomScale * zoomStep).coerceIn(zoomMin, zoomMax))
                },
            )
        val pinchModifier =
            buildPinchZoomModifier(
                enabled = isScrollable,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                getZoomScale = { zoomScale },
                setZoomScale = onZoomScaleChange,
                pointsCount,
                zoomMin,
                zoomMax,
            )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .testTag(TestTags.STACKED_AREA_CHART),
        ) {
            if (showYAxisLabels) {
                StackedAreaYAxisLabels(
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
                            .testTag(TestTags.STACKED_AREA_CHART_PLOT)
                            .then(denseTapModifier)
                            .then(dragModifier)
                            .then(pinchModifier)
                            .then(
                                if (isScrollable) {
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
                    ) {
                        val stackedUpperBounds =
                            animatedValues.map { series ->
                                series.map { value -> value.value * size.height }
                            }
                        val emptyLower = List(pointsCount) { 0f }

                        val clampedVisibleRange =
                            when {
                                visibleRange.isEmpty() -> 0 until pointsCount
                                else -> visibleRange
                            }
                        // Area segments connect adjacent points, so include one-point overscan
                        // to avoid visible truncation/gaps at viewport edges while scrolling/zooming.
                        val rangeStart =
                            (clampedVisibleRange.first - 1)
                                .coerceAtLeast(0)
                        val rangeEnd =
                            (clampedVisibleRange.last + 1)
                                .coerceAtMost(pointsCount - 1)

                        stackedUpperBounds.forEachIndexed { index, upperSeries ->
                            val lowerSeries = stackedUpperBounds.getOrNull(index - 1) ?: emptyLower
                            drawStackedAreaSeries(
                                upperSeries = upperSeries,
                                lowerSeries = lowerSeries,
                                fillColor =
                                    areaColors
                                        .getOrElse(index) { areaColors.lastOrNull() ?: Color.Transparent }
                                        .copy(alpha = style.fillAlpha),
                                bezier = style.bezier,
                                revealProgress = revealProgress,
                                visibleStart = rangeStart,
                                visibleEnd = rangeEnd,
                            )
                            if (style.lineVisible && style.lineWidth > 0f) {
                                drawStackedAreaLine(
                                    upperSeries = upperSeries,
                                    lineColor =
                                        lineColors.getOrElse(index) {
                                            lineColors.lastOrNull() ?: Color.Transparent
                                        },
                                    lineWidth = style.lineWidth,
                                    bezier = style.bezier,
                                    revealProgress = revealProgress,
                                    visibleStart = rangeStart,
                                    visibleEnd = rangeEnd,
                                )
                            }
                        }

                        if (selectedIndex != NO_SELECTION && pointsCount > 1) {
                            val safeIndex = selectedIndex.coerceIn(0, pointsCount - 1)
                            val stepX = if (isScrollable) denseStepX else fitStepX
                            if (stepX > 0f) {
                                val selectedX = safeIndex * stepX
                                drawLine(
                                    color = style.yAxisLabelColor.copy(alpha = 0.4f),
                                    start = Offset(selectedX, 0f),
                                    end = Offset(selectedX, size.height),
                                    strokeWidth = 1f,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showXAxisLabels) {
            StackedAreaXAxisLabels(
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

private fun DrawScope.drawStackedAreaSeries(
    upperSeries: List<Float>,
    lowerSeries: List<Float>,
    fillColor: Color,
    bezier: Boolean,
    revealProgress: Float,
    visibleStart: Int,
    visibleEnd: Int,
) {
    if (upperSeries.size <= 1 || lowerSeries.size <= 1 || size.width <= 0f) return
    if (visibleEnd <= visibleStart || visibleEnd >= upperSeries.size || visibleStart < 0) return

    val upperPoints = buildSeriesPoints(upperSeries).subList(visibleStart, visibleEnd + 1)
    val lowerPoints = buildSeriesPoints(lowerSeries).subList(visibleStart, visibleEnd + 1)

    val areaPath =
        Path().apply {
            moveTo(upperPoints.first().x, upperPoints.first().y)
            appendSeriesPath(points = upperPoints, bezier = bezier)
            if (bezier) {
                lineTo(lowerPoints.last().x, lowerPoints.last().y)
                appendSeriesPath(points = lowerPoints.asReversed(), bezier = true)
            } else {
                for (index in lowerPoints.lastIndex downTo 0) {
                    val point = lowerPoints[index]
                    lineTo(point.x, point.y)
                }
            }
            close()
        }

    val revealX = (size.width * revealProgress).coerceIn(0f, size.width)
    if (revealProgress >= ANIMATION_TARGET) {
        drawPath(
            path = areaPath,
            color = fillColor,
        )
    } else {
        clipRect(left = 0f, top = 0f, right = revealX, bottom = size.height) {
            drawPath(
                path = areaPath,
                color = fillColor,
            )
        }
    }
}

private fun DrawScope.drawStackedAreaLine(
    upperSeries: List<Float>,
    lineColor: Color,
    lineWidth: Float,
    bezier: Boolean,
    revealProgress: Float,
    visibleStart: Int,
    visibleEnd: Int,
) {
    if (upperSeries.size <= 1 || size.width <= 0f) return
    if (visibleEnd <= visibleStart || visibleEnd >= upperSeries.size || visibleStart < 0) return

    val linePoints = buildSeriesPoints(upperSeries).subList(visibleStart, visibleEnd + 1)
    val linePath =
        Path().apply {
            moveTo(linePoints.first().x, linePoints.first().y)
            appendSeriesPath(points = linePoints, bezier = bezier)
        }

    val revealX = (size.width * revealProgress).coerceIn(0f, size.width)
    if (revealProgress >= ANIMATION_TARGET) {
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = lineWidth),
        )
    } else {
        clipRect(left = 0f, top = 0f, right = revealX + lineWidth, bottom = size.height) {
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = lineWidth),
            )
        }
    }
}

private fun DrawScope.buildSeriesPoints(values: List<Float>): List<Offset> {
    val stepX = size.width / (values.size - 1)
    return values.mapIndexed { index, value ->
        Offset(
            x = index * stepX,
            y = size.height - value.coerceIn(0f, size.height),
        )
    }
}

private fun Path.appendSeriesPath(
    points: List<Offset>,
    bezier: Boolean,
) {
    if (points.size <= 1) return

    if (!bezier) {
        for (index in 1 until points.size) {
            val point = points[index]
            lineTo(point.x, point.y)
        }
    } else {
        for (segmentStart in 0 until points.lastIndex) {
            val controls =
                cubicControlPointsForSegment(
                    points = points,
                    segmentStartIndex = segmentStart,
                )
            val segmentEnd = points[segmentStart + 1]
            cubicTo(
                controls.first.x,
                controls.first.y,
                controls.second.x,
                controls.second.y,
                segmentEnd.x,
                segmentEnd.y,
            )
        }
    }
}
