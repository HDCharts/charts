package io.github.hdcharts.charts.internal.stackedareachart

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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.ANIMATION_TARGET
import io.github.hdcharts.charts.internal.AnimationSpec
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.internal.common.axis.AxisXItems
import io.github.hdcharts.charts.internal.common.axis.AxisXLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.AxisYLabelsLayout
import io.github.hdcharts.charts.internal.common.axis.defaultAxisValueFormatter
import io.github.hdcharts.charts.internal.common.axis.estimateXAxisLabelExtent
import io.github.hdcharts.charts.internal.common.axis.rememberNumericYAxisLayout
import io.github.hdcharts.charts.internal.common.axis.rememberXAxisLabelPlan
import io.github.hdcharts.charts.internal.common.axis.xAxisLabelEdgeInsetPx
import io.github.hdcharts.charts.internal.common.axis.xAxisLabelRowHeightPx
import io.github.hdcharts.charts.internal.common.bezier.cubicControlPointsForSegment
import io.github.hdcharts.charts.internal.common.composable.ChartErrors
import io.github.hdcharts.charts.internal.common.composable.ChartHeader
import io.github.hdcharts.charts.internal.common.composable.ChartHeaderTestTags
import io.github.hdcharts.charts.internal.common.composable.rememberDenseExpandedState
import io.github.hdcharts.charts.internal.common.composable.rememberShowState
import io.github.hdcharts.charts.internal.common.composable.rememberZoomScaleState
import io.github.hdcharts.charts.internal.common.composable.zoomInScale
import io.github.hdcharts.charts.internal.common.composable.zoomOutScale
import io.github.hdcharts.charts.internal.common.density.denseStepForViewport
import io.github.hdcharts.charts.internal.common.interaction.buildHorizontalDragGestureModifier
import io.github.hdcharts.charts.internal.common.interaction.buildPinchZoomModifier
import io.github.hdcharts.charts.internal.common.interaction.buildTapGestureModifier
import io.github.hdcharts.charts.internal.common.interaction.horizontalScrollGestures
import io.github.hdcharts.charts.internal.common.interaction.nearestPointIndexForContentX
import io.github.hdcharts.charts.internal.common.interaction.selectedIndexForTouchX
import io.github.hdcharts.charts.internal.common.layout.chartCanvasFits
import io.github.hdcharts.charts.internal.common.layout.fillMaxSizeChartModifier
import io.github.hdcharts.charts.internal.common.layout.placedHorizontalScrollPx
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.common.model.normalizeStackedAreaValues
import io.github.hdcharts.charts.style.StackedAreaChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 4f
private const val ZOOM_STEP = 1.25f
private val HEADER_TEST_TAGS =
    ChartHeaderTestTags(
        denseExpand = TestTags.STACKED_AREA_CHART_DENSE_EXPAND,
        denseCollapse = TestTags.STACKED_AREA_CHART_DENSE_COLLAPSE,
        zoomOut = TestTags.STACKED_AREA_CHART_ZOOM_OUT,
        zoomIn = TestTags.STACKED_AREA_CHART_ZOOM_IN,
    )

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
    val forcedSelectedSourceIndex =
        selectedPointIndex.takeIf { it in 0 until sourcePointsCount } ?: NO_SELECTION
    var selectedSourceIndexFromInteraction by
        remember(forcedSelectedSourceIndex) {
            mutableIntStateOf(forcedSelectedSourceIndex)
        }
    LaunchedEffect(selectedPointIndex) {
        selectedSourceIndexFromInteraction = selectedPointIndex
    }
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

    val onToggleSelection: (Int) -> Unit = { renderIndex ->
        val currentRenderIndex = effectiveSelectedRenderIndex
        if (currentRenderIndex == renderIndex && currentRenderIndex != NO_SELECTION) {
            onSelectRenderIndex(NO_SELECTION)
        } else {
            onSelectRenderIndex(renderIndex)
        }
    }

    val showZoomControlsInHeader = interactionEnabled && isScrollable && style.zoomControlsVisible
    val showCompactToggle = interactionEnabled && isDenseData
    val showHeader = title.isNotBlank() || showCompactToggle || showZoomControlsInHeader

    Column(
        modifier =
            fillMaxSizeChartModifier(style.chartContainerStyle)
                .onGloballyPositioned { show = true },
    ) {
        if (showHeader) {
            ChartHeader(
                title = title,
                titleTextStyle = style.chartContainerStyle.styleTitle,
                testTags = HEADER_TEST_TAGS,
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
                    .padding(top = if (showHeader) style.chartContainerStyle.contentPadding else 0.dp),
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
    val xLabels = style.axis.xLabels
    val yLabels = style.axis.yLabels
    val xAxisLabels =
        remember(data) {
            data.categories
                .toList()
                .takeUnless { it.all(String::isBlank) }
                .orEmpty()
        }
    val showYAxisLabels = yLabels.visible
    val showXAxisLabels = xLabels.visible && xAxisLabels.isNotEmpty()
    val dragInteractionEnabled = interactionEnabled && !isScrollable
    val tapInteractionEnabled = interactionEnabled && isScrollable
    val currentToggleSelection by rememberUpdatedState(onToggleSelection)
    val currentSelectIndex by rememberUpdatedState(onSelectIndex)
    val currentClearSelection by rememberUpdatedState(onClearSelection)
    val currentZoomScale by rememberUpdatedState(zoomScale)
    val currentZoomChange by rememberUpdatedState(onZoomScaleChange)

    BoxWithConstraints(modifier = modifier) {
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
        val (minTotal, maxTotal) =
            remember(data) {
                resolveStackedAreaTotalsRange(data)
            }
        val yAxisLayout =
            rememberNumericYAxisLayout(
                labels = yLabels,
                minValue = minTotal,
                maxValue = maxTotal,
                chartHeightPx = chartHeightPx,
                verticalInsetPx = 0f,
                formatter = defaultAxisValueFormatter,
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
            if (isScrollable && pointsCount > 1) {
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
            if (isScrollable) {
                with(density) { placedHorizontalScrollPx(scrollState.value, plotContentWidth, plotViewportWidthPx) }
                    .toFloat()
            } else {
                0f
            }

        LaunchedEffect(plotContentWidthPx, plotViewportWidthPx, isScrollable) {
            val maxScroll = (plotContentWidthPx - plotViewportWidthPx).roundToInt().coerceAtLeast(0)
            if (!isScrollable && scrollState.value != 0) {
                scrollState.scrollTo(0)
            } else if (scrollState.value > maxScroll) {
                scrollState.scrollTo(maxScroll)
            }
        }

        val xAxisLabelPlan =
            rememberXAxisLabelPlan(
                labels = xAxisLabels,
                dataSize = pointsCount,
                maxLabelCount = xLabels.maxCount,
                isScrollable = isScrollable,
                unitWidthPx = if (isScrollable) denseStepX else fitStepX,
                viewportWidthPx = plotViewportWidthPx,
                fontSizePx = xAxisLabelSizePx,
                items = AxisXItems.Points,
                scrollOffsetPx = scrollOffsetPx,
            )
        val visibleRange = xAxisLabelPlan.visibleRange
        val xAxisTicks = xAxisLabelPlan.ticks

        val dragModifier =
            buildHorizontalDragGestureModifier(
                enabled = dragInteractionEnabled,
                pointsCount,
                plotViewportWidthPx,
                onDragStart = { offset ->
                    val selected =
                        selectedIndexForTouchX(
                            touchX = offset.x,
                            widthPx = plotViewportWidthPx,
                            pointsCount = pointsCount,
                        )
                    currentSelectIndex(selected)
                },
                onHorizontalDrag = { position ->
                    val selected =
                        selectedIndexForTouchX(
                            touchX = position.x,
                            widthPx = plotViewportWidthPx,
                            pointsCount = pointsCount,
                        )
                    currentSelectIndex(selected)
                },
                onDragEnd = { currentClearSelection() },
                onDragCancel = { currentClearSelection() },
            )
        val denseTapModifier =
            buildTapGestureModifier(
                enabled = tapInteractionEnabled,
                pointsCount,
                denseStepX,
                onTap = { offset ->
                    val selected =
                        nearestPointIndexForContentX(
                            contentX = offset.x + scrollState.value.toFloat(),
                            pointsCount = pointsCount,
                            stepPx = denseStepX,
                        )
                    if (selected != NO_SELECTION) {
                        currentToggleSelection(selected)
                    }
                },
                onDoubleTap = {
                    currentZoomChange((currentZoomScale * zoomStep).coerceIn(zoomMin, zoomMax))
                },
            )
        val pinchModifier =
            buildPinchZoomModifier(
                enabled = isScrollable,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                getZoomScale = { currentZoomScale },
                setZoomScale = { currentZoomChange(it) },
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
                AxisYLabelsLayout(
                    ticks = yAxisLayout.ticks,
                    color = yLabels.color,
                    fontSize = yLabels.size,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .fillMaxHeight()
                            .width(yAxisWidth)
                            .testTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS),
                )
            }

            // Gestures cover the end inset too, so a touch right of the last point still selects it.
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = plotStartPadding)
                        .then(denseTapModifier)
                        .then(dragModifier)
                        .then(pinchModifier)
                        .then(
                            if (isScrollable) {
                                Modifier.horizontalScrollGestures(
                                    state = scrollState,
                                    enabled = true,
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
                            .testTag(TestTags.STACKED_AREA_CHART_PLOT)
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
                        val lineWidthPx = style.boundary.width.toPx()
                        val selectionLineWidthPx = style.selection.width.toPx()

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
                                        .copy(alpha = style.fill.alpha),
                                bezier = style.boundary.bezier,
                                revealProgress = revealProgress,
                                visibleStart = rangeStart,
                                visibleEnd = rangeEnd,
                            )
                            if (style.boundary.visible && lineWidthPx > 0f) {
                                drawStackedAreaLine(
                                    upperSeries = upperSeries,
                                    lineColor =
                                        lineColors.getOrElse(index) {
                                            lineColors.lastOrNull() ?: Color.Transparent
                                        },
                                    lineWidth = lineWidthPx,
                                    bezier = style.boundary.bezier,
                                    revealProgress = revealProgress,
                                    visibleStart = rangeStart,
                                    visibleEnd = rangeEnd,
                                )
                            }
                        }

                        if (style.selection.visible && selectedIndex != NO_SELECTION && pointsCount > 1) {
                            val safeIndex = selectedIndex.coerceIn(0, pointsCount - 1)
                            val stepX = if (isScrollable) denseStepX else fitStepX
                            if (stepX > 0f) {
                                val selectedX = safeIndex * stepX
                                drawLine(
                                    color = style.selection.color,
                                    start = Offset(selectedX, 0f),
                                    end = Offset(selectedX, size.height),
                                    strokeWidth = selectionLineWidthPx,
                                )
                            }
                        }
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
                        .testTag(TestTags.STACKED_AREA_CHART_X_AXIS_LABELS),
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
