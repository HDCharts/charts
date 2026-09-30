package io.github.hdcharts.stackedbar.internal

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
import androidx.compose.runtime.rememberUpdatedState
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
import io.github.hdcharts.core.internal.AnimationSpec
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.axis.AxisXItems
import io.github.hdcharts.core.internal.axis.AxisXLabelsLayout
import io.github.hdcharts.core.internal.axis.AxisYLabelsLayout
import io.github.hdcharts.core.internal.axis.defaultAxisValueFormatter
import io.github.hdcharts.core.internal.axis.estimateXAxisLabelExtent
import io.github.hdcharts.core.internal.axis.rememberNumericYAxisLayout
import io.github.hdcharts.core.internal.axis.rememberXAxisLabelPlan
import io.github.hdcharts.core.internal.axis.xAxisLabelRowHeightPx
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.composable.ChartHeader
import io.github.hdcharts.core.internal.composable.ChartHeaderTestTags
import io.github.hdcharts.core.internal.composable.rememberDenseExpandedState
import io.github.hdcharts.core.internal.composable.rememberZoomScaleState
import io.github.hdcharts.core.internal.composable.zoomInScale
import io.github.hdcharts.core.internal.composable.zoomOutScale
import io.github.hdcharts.core.internal.layout.chartCanvasFits
import io.github.hdcharts.core.internal.layout.fillMaxSizeChartModifier
import io.github.hdcharts.core.internal.layout.placedHorizontalScrollPx
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.internal.model.normalizeStackedValues
import io.github.hdcharts.stackedbar.StackedBarChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 4f
private const val ZOOM_STEP = 1.25f
private val HEADER_TEST_TAGS =
    ChartHeaderTestTags(
        denseExpand = TestTags.STACKED_BAR_CHART_DENSE_EXPAND,
        denseCollapse = TestTags.STACKED_BAR_CHART_DENSE_COLLAPSE,
        zoomOut = TestTags.STACKED_BAR_CHART_ZOOM_OUT,
        zoomIn = TestTags.STACKED_BAR_CHART_ZOOM_IN,
    )

@Composable
internal fun StackedBarChart(
    data: MultiChartData,
    title: String,
    style: StackedBarChartStyle,
    colors: ImmutableList<Color>,
    showXAxisLabels: Boolean,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedBarIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
) {
    val isPreview = LocalInspectionMode.current
    val sourceDataSize = data.items.size
    BoxWithConstraints(modifier = fillMaxSizeChartModifier(style.chartContainerStyle)) {
        val density = LocalDensity.current
        val spacingPx = with(density) { style.layout.space.toPx() }
        val minBarWidthPx = with(density) { style.layout.minBarWidth.toPx() }

        val (sourceMinTotal, sourceMaxTotal) =
            remember(data) {
                resolveStackedTotalsRange(data)
            }
        val yAxisLayout =
            rememberNumericYAxisLayout(
                labels = style.axis.yLabels,
                minValue = sourceMinTotal,
                maxValue = sourceMaxTotal,
                // The plot height is not known yet. A short plot can show fewer ticks than this estimate.
                chartHeightPx = constraints.maxHeight.toFloat(),
                verticalInsetPx = 0f,
                formatter = defaultAxisValueFormatter,
                availableWidthPx = constraints.maxWidth,
            )
        val viewportWidthPx =
            (constraints.maxWidth.toFloat() - yAxisLayout.widthPx - yAxisLayout.gapPx).coerceAtLeast(1f)
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
        val forcedSelectedSourceIndex =
            selectedBarIndex.takeIf { it in 0 until sourceDataSize } ?: NO_SELECTION
        var selectedSourceIndexFromInteraction by
            remember(forcedSelectedSourceIndex) {
                mutableIntStateOf(forcedSelectedSourceIndex)
            }
        LaunchedEffect(selectedBarIndex) {
            selectedSourceIndexFromInteraction = selectedBarIndex
        }

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
                    in 0 until renderDataSize -> renderDataBundle.resolveSourceIndex(renderIndex)
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

        Column(modifier = Modifier.fillMaxSize()) {
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
                showXAxisLabels = showXAxisLabels,
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
                        .padding(top = if (showHeader) style.chartContainerStyle.contentPadding else 0.dp),
            )
        }
    }
}

@Composable
private fun StackedBarChartContent(
    data: MultiChartData,
    style: StackedBarChartStyle,
    colors: ImmutableList<Color>,
    showXAxisLabels: Boolean,
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
    val xLabels = style.axis.xLabels
    val yLabels = style.axis.yLabels
    val dataSize = data.items.size
    val labels = remember(data) { data.items.map { item -> item.label } }
    val currentToggleSelection by rememberUpdatedState(onToggleSelection)
    val currentSelectIndex by rememberUpdatedState(onSelectIndex)
    val currentClearSelection by rememberUpdatedState(onClearSelection)
    val currentZoomScale by rememberUpdatedState(zoomScale)
    val currentZoomChange by rememberUpdatedState(onZoomScaleChange)

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val xAxisLabelSizePx = with(density) { xLabels.size.toPx() }
        val xAxisRowHeightPx =
            remember(showXAxisLabels, labels, dataSize, xAxisLabelSizePx, density) {
                if (showXAxisLabels) {
                    with(density) {
                        xAxisLabelRowHeightPx(
                            estimateXAxisLabelExtent(
                                labels = labels,
                                dataSize = dataSize,
                                fontSizePx = xAxisLabelSizePx,
                            ),
                        )
                    }
                } else {
                    0f
                }
            }
        val xAxisHeight = with(density) { xAxisRowHeightPx.toDp() }
        val chartHeight = (maxHeight - xAxisHeight).coerceAtLeast(0.dp)
        val chartHeightPx = with(density) { chartHeight.toPx() }.coerceAtLeast(1f)
        val yAxisLayout =
            rememberNumericYAxisLayout(
                labels = yLabels,
                minValue = fixedMinTotal,
                maxValue = fixedMaxTotal,
                chartHeightPx = chartHeightPx,
                verticalInsetPx = 0f,
                formatter = defaultAxisValueFormatter,
                availableWidthPx = constraints.maxWidth,
            )
        val yAxisWidth = with(density) { yAxisLayout.widthPx.toDp() }
        val plotStartPadding = with(density) { (yAxisLayout.widthPx + yAxisLayout.gapPx).toDp() }
        val viewportWidthPx =
            (constraints.maxWidth.toFloat() - yAxisLayout.widthPx - yAxisLayout.gapPx).coerceAtLeast(1f)

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
        if (!chartCanvasFits(contentWidthPx, chartHeightPx)) {
            ChartErrors(
                style = style.chartContainerStyle,
                errors = persistentListOf("Chart exceeds layout limits. Reduce zoom, minimum bar width, or spacing."),
                modifier = Modifier.fillMaxWidth(),
            )
            return@BoxWithConstraints
        }
        val canvasWidth = with(density) { contentWidthPx.toDp() }
        val scrollOffsetPx =
            if (isScrollable) {
                with(density) { placedHorizontalScrollPx(scrollState.value, canvasWidth, viewportWidthPx) }.toFloat()
            } else {
                0f
            }

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
                onTapIndex = { currentToggleSelection(it) },
            )

        val fitDragModifier =
            buildFitDragModifier(
                interactionEnabled = interactionEnabled,
                dragSelectionEnabled = dragSelectionEnabled,
                isScrollable = isScrollable,
                dataSize = dataSize,
                spacingPx = spacingPx,
                viewportWidthPx = viewportWidthPx,
                onDragIndex = { currentSelectIndex(it) },
                onDragFinished = { currentClearSelection() },
            )

        val scrollTapModifier =
            buildScrollTapModifier(
                interactionEnabled = interactionEnabled,
                isScrollable = isScrollable,
                dataSize = dataSize,
                unitWidthPx = unitWidthPx,
                scrollState = scrollState,
                onTapIndex = { currentToggleSelection(it) },
                onDoubleTap = {
                    currentZoomChange((currentZoomScale * zoomStep).coerceIn(zoomMin, zoomMax))
                },
            )

        val pinchModifier =
            buildPinchModifier(
                isScrollable = isScrollable,
                dataSize = dataSize,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                getZoomScale = { currentZoomScale },
                setZoomScale = { currentZoomChange(it) },
            )

        val xAxisLabelPlan =
            rememberXAxisLabelPlan(
                labels = labels,
                dataSize = dataSize,
                maxLabelCount = xLabels.maxCount,
                isScrollable = isScrollable,
                unitWidthPx = unitWidthPx,
                viewportWidthPx = viewportWidthPx,
                fontSizePx = xAxisLabelSizePx,
                items = AxisXItems.Bars(barWidthPx),
                scrollOffsetPx = scrollOffsetPx,
            )
        val visibleRange = xAxisLabelPlan.visibleRange
        val xAxisTicks = xAxisLabelPlan.ticks
        val selectedCenterXContent =
            if (selectedIndex in 0 until dataSize) {
                selectedIndex * unitWidthPx + barWidthPx / 2f
            } else {
                Float.NaN
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
            if (yLabels.visible) {
                AxisYLabelsLayout(
                    ticks = yAxisLayout.ticks,
                    color = yLabels.color,
                    fontSize = yLabels.size,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .fillMaxHeight()
                            .width(yAxisWidth)
                            .testTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS),
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

        if (showXAxisLabels) {
            AxisXLabelsLayout(
                ticks = xAxisTicks,
                color = xLabels.color,
                fontSize = xLabels.size,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = plotStartPadding)
                        .height(xAxisHeight)
                        .testTag(TestTags.STACKED_BAR_CHART_X_AXIS_LABELS),
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

    if (style.selection.visible && selectedIndex != NO_SELECTION && selectedCenterX.isFinite()) {
        drawLine(
            color = style.selection.color,
            start = Offset(selectedCenterX, 0f),
            end = Offset(selectedCenterX, size.height),
            strokeWidth = style.selection.width.toPx(),
        )
        drawCircle(
            color = style.selection.color,
            radius = 3.dp.toPx(),
            center = Offset(selectedCenterX, size.height),
            style = Stroke(width = style.selection.width.toPx()),
        )
    }
}

internal fun stackedSegmentHeight(
    segmentShare: Float,
    chartHeight: Float,
    progress: Float,
): Float = lerp(0f, segmentShare * chartHeight, progress)
