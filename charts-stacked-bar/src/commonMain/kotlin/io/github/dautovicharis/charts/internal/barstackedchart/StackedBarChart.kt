package io.github.dautovicharis.charts.internal.barstackedchart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import io.github.dautovicharis.charts.internal.AXIS_LABEL_CHART_GAP
import io.github.dautovicharis.charts.internal.AnimationSpec
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.axis.AxisXPlanRequest
import io.github.dautovicharis.charts.internal.common.axis.estimateXAxisLabelFootprintPx
import io.github.dautovicharis.charts.internal.common.axis.estimateYAxisLabelWidthPx
import io.github.dautovicharis.charts.internal.common.axis.planAxisXLabels
import io.github.dautovicharis.charts.internal.common.composable.rememberDenseExpandedState
import io.github.dautovicharis.charts.internal.common.composable.rememberZoomScaleState
import io.github.dautovicharis.charts.internal.common.composable.zoomInScale
import io.github.dautovicharis.charts.internal.common.composable.zoomOutScale
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.model.normalizeStackedValues
import io.github.dautovicharis.charts.style.StackedBarChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 4f
private const val ZOOM_STEP = 1.25f
private const val FIXED_X_AXIS_LABEL_TILT_DEGREES = 34f

@Composable
internal fun StackedBarChart(
    data: MultiChartData,
    title: String,
    style: StackedBarChartStyle,
    colors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedBarIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
) {
    val isPreview = LocalInspectionMode.current
    val sourceDataSize = data.items.size
    BoxWithConstraints(modifier = style.modifier) {
        val density = LocalDensity.current
        val spacingPx = with(density) { style.space.toPx() }
        val minBarWidthPx = with(density) { style.minBarWidth.toPx() }

        val (sourceMinTotal, sourceMaxTotal) =
            remember(data) {
                resolveStackedTotalsRange(data)
            }
        val yAxisTicksForViewport =
            remember(sourceMinTotal, sourceMaxTotal, style.yAxisLabelCount) {
                buildStackedBarYAxisTicks(
                    minValue = sourceMinTotal,
                    maxValue = sourceMaxTotal,
                    labelCount = style.yAxisLabelCount,
                    chartHeightPx = 1f,
                )
            }
        val yAxisWidthPxForViewport =
            if (style.yAxisLabelsVisible) {
                estimateYAxisLabelWidthPx(
                    labels = yAxisTicksForViewport.map { tick -> tick.label },
                    fontSizePx = with(density) { style.yAxisLabelSize.toPx() },
                )
            } else {
                0f
            }
        val yAxisGapPxForViewport = if (style.yAxisLabelsVisible) with(density) { AXIS_LABEL_CHART_GAP.toPx() } else 0f
        val viewportWidthPx =
            (constraints.maxWidth.toFloat() - yAxisWidthPxForViewport - yAxisGapPxForViewport).coerceAtLeast(1f)
        val maxFitBars =
            remember(viewportWidthPx, spacingPx, minBarWidthPx) {
                maxBarsThatFit(
                    viewportWidthPx = viewportWidthPx,
                    spacingPx = spacingPx,
                    minBarWidthPx = minBarWidthPx,
                )
            }
        val isDenseData =
            remember(sourceDataSize, maxFitBars) {
                sourceDataSize > maxFitBars
            }
        var denseExpanded by rememberDenseExpandedState(isDenseModeAvailable = isDenseData)
        val compactDenseMode = isDenseData && !denseExpanded
        val renderDataBundle =
            remember(data, compactDenseMode, maxFitBars) {
                if (compactDenseMode) {
                    aggregateForCompactDensity(
                        data = data,
                        targetBars = maxFitBars,
                    )
                } else {
                    identityRenderData(data)
                }
            }
        val renderData = renderDataBundle.data
        val renderDataSize = renderData.items.size
        val targetNormalized =
            remember(renderData) {
                renderData.normalizeStackedValues()
            }
        val initialValues =
            remember(renderDataSize, isPreview, animateOnStart) {
                if (isPreview || !animateOnStart) targetNormalized else null
            }
        val animatedValues =
            remember(renderDataSize, isPreview, animateOnStart) {
                renderData.items.mapIndexed { index, _ ->
                    Animatable(initialValues?.getOrNull(index) ?: 0f)
                }
            }
        val hasInitialized = remember { mutableStateOf(false) }
        var selectedSourceIndexFromInteraction by remember { mutableIntStateOf(NO_SELECTION) }
        val forcedSelectedSourceIndex =
            selectedBarIndex.takeIf { it in 0 until sourceDataSize } ?: NO_SELECTION
        val hasForcedSelection = forcedSelectedSourceIndex != NO_SELECTION

        val isScrollable = isDenseData && denseExpanded
        val scrollState = rememberScrollState()
        val zoomMin = ZOOM_MIN
        val zoomMax = ZOOM_MAX
        val zoomStep = ZOOM_STEP
        var zoomScale by
            rememberZoomScaleState(
                isZoomActive = isScrollable,
                minZoom = zoomMin,
                maxZoom = zoomMax,
                initialZoom = zoomMin,
            )

        LaunchedEffect(targetNormalized) {
            if (renderData.items.isEmpty()) return@LaunchedEffect
            coroutineScope {
                animatedValues.forEachIndexed { index, animatable ->
                    val target = targetNormalized.getOrNull(index) ?: 0f
                    launch {
                        val shouldAnimate = !isPreview && (animateOnStart || hasInitialized.value)
                        if (!shouldAnimate) {
                            animatable.snapTo(target)
                        } else {
                            animatable.animateTo(
                                targetValue = target,
                                animationSpec = AnimationSpec.stackedBar(0),
                            )
                        }
                    }
                }
            }
            hasInitialized.value = true
        }

        LaunchedEffect(sourceDataSize) {
            if (hasForcedSelection) return@LaunchedEffect
            if (selectedSourceIndexFromInteraction !in 0 until sourceDataSize) {
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
                        in 0 until renderDataSize -> renderDataBundle.resolveSourceIndex(renderIndex)
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

        Column(modifier = Modifier.fillMaxSize()) {
            if (showHeader) {
                StackedBarChartHeader(
                    title = title,
                    style = style,
                    showDensityToggle = showCompactToggle,
                    denseExpanded = denseExpanded,
                    onToggleDensity = { denseExpanded = !denseExpanded },
                    showZoomControls = showZoomControlsInHeader,
                    zoomScale = zoomScale,
                    minZoom = zoomMin,
                    maxZoom = zoomMax,
                    onZoomOut = {
                        zoomScale = zoomOutScale(zoomScale, zoomStep, zoomMin, zoomMax)
                    },
                    onZoomIn = {
                        zoomScale = zoomInScale(zoomScale, zoomStep, zoomMin, zoomMax)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            StackedBarChartContent(
                data = renderData,
                style = style,
                colors = colors,
                interactionEnabled = interactionEnabled,
                dragSelectionEnabled = !isScrollable,
                animatedValues = animatedValues,
                fixedMinTotal = sourceMinTotal,
                fixedMaxTotal = sourceMaxTotal,
                isScrollable = isScrollable,
                spacingPx = spacingPx,
                minBarWidthPx = minBarWidthPx,
                scrollState = scrollState,
                zoomScale = zoomScale,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                zoomStep = zoomStep,
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
}

@Composable
private fun StackedBarChartContent(
    data: MultiChartData,
    style: StackedBarChartStyle,
    colors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    dragSelectionEnabled: Boolean,
    animatedValues: List<Animatable<Float, AnimationVector1D>>,
    fixedMinTotal: Double,
    fixedMaxTotal: Double,
    isScrollable: Boolean,
    spacingPx: Float,
    minBarWidthPx: Float,
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
    val dataSize = data.items.size
    val labels = remember(data) { data.items.map { item -> item.label } }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val xAxisTilt = FIXED_X_AXIS_LABEL_TILT_DEGREES
        val xAxisLabelSizePx = with(density) { style.xAxisLabelSize.toPx() }
        val xAxisLabelFootprintPx =
            remember(labels, dataSize, xAxisLabelSizePx, xAxisTilt) {
                estimateXAxisLabelFootprintPx(
                    labels = labels,
                    dataSize = dataSize,
                    fontSizePx = xAxisLabelSizePx,
                    tiltDegrees = xAxisTilt,
                )
            }
        val xAxisHeight =
            if (!style.xAxisLabelsVisible) {
                0.dp
            } else {
                with(density) {
                    (xAxisLabelFootprintPx.height + AXIS_LABEL_CHART_GAP.toPx()).toDp()
                }
            }
        val chartHeight = (maxHeight - xAxisHeight).coerceAtLeast(0.dp)
        val chartHeightPx = with(density) { chartHeight.toPx() }.coerceAtLeast(1f)
        val yAxisTicks =
            remember(fixedMinTotal, fixedMaxTotal, chartHeightPx, style.yAxisLabelCount) {
                buildStackedBarYAxisTicks(
                    minValue = fixedMinTotal,
                    maxValue = fixedMaxTotal,
                    labelCount = style.yAxisLabelCount,
                    chartHeightPx = chartHeightPx,
                )
            }
        val yAxisWidthPx =
            if (style.yAxisLabelsVisible) {
                estimateYAxisLabelWidthPx(
                    labels = yAxisTicks.map { tick -> tick.label },
                    fontSizePx = with(density) { style.yAxisLabelSize.toPx() },
                )
            } else {
                0f
            }
        val yAxisGapPx = if (style.yAxisLabelsVisible) with(density) { AXIS_LABEL_CHART_GAP.toPx() } else 0f
        val yAxisWidth = with(density) { yAxisWidthPx.toDp() }
        val plotStartPadding = with(density) { (yAxisWidthPx + yAxisGapPx).toDp() }
        val viewportWidthPx = (constraints.maxWidth.toFloat() - yAxisWidthPx - yAxisGapPx).coerceAtLeast(1f)

        val barWidthPx =
            when {
                isScrollable -> (minBarWidthPx * zoomScale).coerceAtLeast(1f)
                dataSize <= 0 -> viewportWidthPx
                else -> ((viewportWidthPx - spacingPx * (dataSize - 1)) / dataSize).coerceAtLeast(1f)
            }
        val unitWidthPx = unitWidth(barWidthPx, spacingPx)
        val contentWidthPx =
            if (isScrollable) {
                contentWidth(dataSize, unitWidthPx, spacingPx).coerceAtLeast(viewportWidthPx)
            } else {
                viewportWidthPx
            }
        val canvasWidth = with(density) { contentWidthPx.toDp() }
        val scrollOffsetPx = if (isScrollable) scrollState.value.toFloat() else 0f

        LaunchedEffect(contentWidthPx, viewportWidthPx, isScrollable) {
            val maxScroll = (contentWidthPx - viewportWidthPx).roundToInt().coerceAtLeast(0)
            if (!isScrollable && scrollState.value != 0) {
                scrollState.scrollTo(0)
            } else if (scrollState.value > maxScroll) {
                scrollState.scrollTo(maxScroll)
            }
        }

        val fitTapModifier =
            buildFitTapModifier(
                interactionEnabled = interactionEnabled,
                isScrollable = isScrollable,
                dataSize = dataSize,
                spacingPx = spacingPx,
                viewportWidthPx = viewportWidthPx,
                chartHeightPx = chartHeightPx,
                onTapIndex = onToggleSelection,
            )

        val fitDragModifier =
            buildFitDragModifier(
                interactionEnabled = interactionEnabled,
                dragSelectionEnabled = dragSelectionEnabled,
                isScrollable = isScrollable,
                dataSize = dataSize,
                spacingPx = spacingPx,
                viewportWidthPx = viewportWidthPx,
                chartHeightPx = chartHeightPx,
                onDragIndex = onSelectIndex,
                onDragFinished = onClearSelection,
            )

        val scrollTapModifier =
            buildScrollTapModifier(
                interactionEnabled = interactionEnabled,
                isScrollable = isScrollable,
                dataSize = dataSize,
                unitWidthPx = unitWidthPx,
                scrollState = scrollState,
                onTapIndex = onToggleSelection,
                onDoubleTap = {
                    onZoomScaleChange((zoomScale * zoomStep).coerceIn(zoomMin, zoomMax))
                },
            )

        val pinchModifier =
            buildPinchModifier(
                isScrollable = isScrollable,
                dataSize = dataSize,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                getZoomScale = { zoomScale },
                setZoomScale = onZoomScaleChange,
            )

        val xAxisPlan =
            remember(
                dataSize,
                style.xAxisLabelMaxCount,
                isScrollable,
                unitWidthPx,
                viewportWidthPx,
                scrollOffsetPx,
                barWidthPx,
                xAxisLabelFootprintPx.width,
            ) {
                planAxisXLabels(
                    request =
                        AxisXPlanRequest(
                            dataSize = dataSize,
                            requestedMaxLabelCount = style.xAxisLabelMaxCount,
                            isScrollable = isScrollable,
                            unitWidthPx = unitWidthPx,
                            viewportWidthPx = viewportWidthPx,
                            scrollOffsetPx = scrollOffsetPx,
                            firstCenterPx = barWidthPx / 2f,
                            labelWidthPx = xAxisLabelFootprintPx.width,
                        ),
                )
            }
        val visibleRange = xAxisPlan.visibleRange
        val selectedCenterXContent =
            if (selectedIndex in 0 until dataSize) {
                selectedIndex * unitWidthPx + barWidthPx / 2f
            } else {
                Float.NaN
            }
        val labelIndices = xAxisPlan.labelIndices
        val xAxisTicks =
            remember(labels, labelIndices, barWidthPx, unitWidthPx, scrollOffsetPx) {
                buildStackedBarXAxisTicks(
                    labels = labels,
                    labelIndices = labelIndices,
                    barWidthPx = barWidthPx,
                    unitWidthPx = unitWidthPx,
                    scrollOffsetPx = scrollOffsetPx,
                )
            }

        val interactionModifier =
            Modifier
                .then(fitTapModifier)
                .then(fitDragModifier)
                .then(scrollTapModifier)
                .then(pinchModifier)

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .testTag(TestTags.STACKED_BAR_CHART),
        ) {
            if (style.yAxisLabelsVisible) {
                StackedBarYAxisLabels(
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
                            .testTag(TestTags.STACKED_BAR_CHART_PLOT)
                            .then(interactionModifier)
                            .horizontalScroll(state = scrollState, enabled = isScrollable),
                ) {
                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .requiredWidth(canvasWidth),
                        onDraw = {
                            drawStackedBars(
                                data = data,
                                style = style,
                                progress = animatedValues,
                                selectedIndex = selectedIndex,
                                selectedCenterX = selectedCenterXContent,
                                colors = colors,
                                barWidthPx = barWidthPx,
                                spacingPx = spacingPx,
                                visibleRange = visibleRange,
                            )
                        },
                    )
                }
            }
        }

        if (style.xAxisLabelsVisible) {
            StackedBarXAxisLabels(
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

private fun DrawScope.drawStackedBars(
    data: MultiChartData,
    style: StackedBarChartStyle,
    progress: List<Animatable<Float, AnimationVector1D>>,
    selectedIndex: Int,
    selectedCenterX: Float,
    colors: ImmutableList<Color>,
    barWidthPx: Float,
    spacingPx: Float,
    visibleRange: IntRange,
) {
    if (barWidthPx <= 0f || data.items.isEmpty()) return
    val indices =
        when {
            visibleRange.isEmpty() -> 0 until data.items.size
            else -> visibleRange
        }
    for (index in indices) {
        val item = data.items.getOrNull(index) ?: continue
        var topOffset = size.height
        val left = index * (barWidthPx + spacingPx)
        val barTotal = item.item.points.sum()
        item.item.points.forEachIndexed { dataIndex, value ->
            val segmentShare =
                when {
                    barTotal == 0.0 -> 0f
                    else -> (value / barTotal).toFloat()
                }
            val height =
                stackedSegmentHeight(
                    segmentShare = segmentShare,
                    chartHeight = size.height,
                    progress = progress.getOrNull(index)?.value ?: 0f,
                )
            topOffset -= height

            drawRect(
                color = colors.getOrElse(dataIndex) { colors.lastOrNull() ?: Color.Transparent },
                topLeft =
                    androidx.compose.ui.geometry
                        .Offset(x = left, y = topOffset),
                size =
                    Size(
                        width = barWidthPx,
                        height = height,
                    ),
            )
        }
    }

    if (style.selectionLineVisible && selectedIndex != NO_SELECTION && selectedCenterX.isFinite()) {
        drawLine(
            color = style.selectionLineColor,
            start = Offset(selectedCenterX, 0f),
            end = Offset(selectedCenterX, size.height),
            strokeWidth = style.selectionLineWidth,
        )
        drawCircle(
            color = style.selectionLineColor,
            radius = 3.dp.toPx(),
            center = Offset(selectedCenterX, size.height),
            style = Stroke(width = style.selectionLineWidth),
        )
    }
}

internal fun stackedSegmentHeight(
    segmentShare: Float,
    chartHeight: Float,
    progress: Float,
): Float = lerp(0f, segmentShare * chartHeight, progress)
