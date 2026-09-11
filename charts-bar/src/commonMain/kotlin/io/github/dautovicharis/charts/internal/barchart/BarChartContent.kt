package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import io.github.dautovicharis.charts.internal.AXIS_LABEL_CHART_GAP
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.axis.AxisXPlanRequest
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.model.ChartData
import io.github.dautovicharis.charts.model.ChartValueFormatter
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.roundToInt

// X Axis label layout constants
private const val FIXED_X_AXIS_LABEL_TILT_DEGREES = 34f

@Composable
internal fun BarChartContent(
    chartData: ChartData,
    style: BarChartInternalStyle,
    interactionEnabled: Boolean,
    dragSelectionEnabled: Boolean,
    animatedValues: List<Animatable<Float, AnimationVector1D>>,
    barColors: List<Color>,
    defaultBarColor: Color,
    fixedMin: Double,
    fixedMax: Double,
    axisValueFormatter: ChartValueFormatter,
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
    val dataSize = chartData.points.size
    val showXLabels = style.xAxisLabelsVisible && chartData.labels.any { it.isNotBlank() }
    val currentToggleSelection by rememberUpdatedState(onToggleSelection)
    val currentSelectIndex by rememberUpdatedState(onSelectIndex)
    val currentClearSelection by rememberUpdatedState(onClearSelection)
    val currentZoomScale by rememberUpdatedState(zoomScale)
    val currentZoomChange by rememberUpdatedState(onZoomScaleChange)

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val xAxisTilt = FIXED_X_AXIS_LABEL_TILT_DEGREES
        val xAxisLabelSizePx = with(density) { style.xAxisLabelSize.toPx() }
        val xAxisLabelFootprintPx =
            remember(chartData.labels, dataSize, xAxisLabelSizePx, xAxisTilt) {
                estimateXAxisLabelFootprintPx(
                    labels = chartData.labels,
                    dataSize = dataSize,
                    fontSizePx = xAxisLabelSizePx,
                    tiltDegrees = xAxisTilt,
                )
            }
        val xAxisHeightPx =
            if (!showXLabels) {
                0
            } else {
                (xAxisLabelFootprintPx.height + with(density) { AXIS_LABEL_CHART_GAP.toPx() })
                    .roundToInt()
                    .coerceIn(0, constraints.maxHeight)
            }
        val xAxisHeight = with(density) { xAxisHeightPx.toDp() }
        val chartHeightPx = (constraints.maxHeight - xAxisHeightPx).coerceAtLeast(0).toFloat()
        val chartHeight = with(density) { chartHeightPx.toDp() }
        val yAxisTicks =
            remember(fixedMin, fixedMax, chartHeightPx, style.yAxisLabelCount, axisValueFormatter) {
                buildYAxisTicks(
                    minValue = fixedMin,
                    maxValue = fixedMax,
                    labelCount = style.yAxisLabelCount,
                    chartHeightPx = chartHeightPx,
                    formatter = axisValueFormatter,
                )
            }
        val yAxisWidthPx =
            if (style.yAxisLabelsVisible) {
                barYAxisWidthPx(
                    ticks = yAxisTicks,
                    fontSizePx = with(density) { style.yAxisLabelSize.toPx() },
                    availableWidthPx = constraints.maxWidth,
                )
            } else {
                0f
            }
        val yAxisGapPx =
            if (style.yAxisLabelsVisible) {
                with(density) { AXIS_LABEL_CHART_GAP.roundToPx().toFloat() }.coerceAtMost(
                    (
                        constraints.maxWidth -
                            yAxisWidthPx -
                            1f
                    ).coerceAtLeast(0f),
                )
            } else {
                0f
            }
        val yAxisWidth = with(density) { yAxisWidthPx.toDp() }
        val plotStartPadding = with(density) { (yAxisWidthPx + yAxisGapPx).toDp() }
        val viewportWidthPx =
            (constraints.maxWidth.toFloat() - yAxisWidthPx - yAxisGapPx).coerceAtLeast(1f)

        // Preserve subpixel bins in fit mode, and reduce excessive spacing in narrow parents.
        val effectiveSpacingPx =
            if (isScrollable) {
                spacingPx
            } else {
                spacingPx.coerceAtMost(
                    viewportWidthPx / (dataSize.coerceAtLeast(1) * 2f),
                )
            }
        val barWidthPx =
            when {
                isScrollable -> (minBarWidthPx * zoomScale).coerceAtLeast(1f)
                dataSize <= 0 -> viewportWidthPx
                else ->
                    ((viewportWidthPx - effectiveSpacingPx * (dataSize - 1)) / dataSize).coerceAtLeast(
                        Float.MIN_VALUE,
                    )
            }
        val unitWidthPx = unitWidth(barWidthPx, effectiveSpacingPx)
        val contentWidthPx =
            if (isScrollable) {
                contentWidth(dataSize, unitWidthPx, effectiveSpacingPx).coerceAtLeast(viewportWidthPx)
            } else {
                viewportWidthPx
            }
        if (!barCanvasFits(contentWidthPx, chartHeightPx)) {
            ChartErrors(
                style = style.chartContainerStyle,
                errors = persistentListOf("Chart exceeds layout limits. Reduce zoom, minimum bar width, or spacing."),
                modifier = Modifier.fillMaxWidth(),
            )
            return@BoxWithConstraints
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
                spacingPx = effectiveSpacingPx,
                viewportWidthPx = viewportWidthPx,
                chartHeightPx = chartHeightPx,
                onTapIndex = { currentToggleSelection(it) },
            )

        val fitDragModifier =
            buildFitDragModifier(
                interactionEnabled = interactionEnabled,
                dragSelectionEnabled = dragSelectionEnabled,
                isScrollable = isScrollable,
                dataSize = dataSize,
                spacingPx = effectiveSpacingPx,
                viewportWidthPx = viewportWidthPx,
                chartHeightPx = chartHeightPx,
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
                isScrollable = isScrollable && interactionEnabled,
                dataSize = dataSize,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                getZoomScale = { currentZoomScale },
                setZoomScale = { currentZoomChange(it) },
            )

        val selectedCenterXContent =
            if (selectedIndex in 0 until dataSize) {
                selectedIndex * unitWidthPx + barWidthPx / 2f
            } else {
                Float.NaN
            }

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
        val labelIndices = xAxisPlan.labelIndices

        val ticks =
            remember(
                chartData.labels,
                labelIndices,
                barWidthPx,
                unitWidthPx,
                scrollOffsetPx,
            ) {
                buildAxisTicks(
                    chartData = chartData,
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
                    .testTag(TestTags.BAR_CHART),
        ) {
            if (style.yAxisLabelsVisible) {
                BarYAxisLabels(
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
                            .testTag(TestTags.BAR_CHART_PLOT)
                            .then(interactionModifier)
                            .horizontalScroll(state = scrollState, enabled = isScrollable && interactionEnabled),
                ) {
                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .requiredWidth(canvasWidth),
                        onDraw = {
                            drawBars(
                                style = style,
                                animatedValues = animatedValues,
                                visibleRange = visibleRange,
                                selectedIndex = selectedIndex,
                                barColors = barColors,
                                defaultBarColor = defaultBarColor,
                                maxValue = fixedMax,
                                minValue = fixedMin,
                                barWidthPx = barWidthPx,
                                spacingPx = effectiveSpacingPx,
                                selectedCenterX = selectedCenterXContent,
                            )
                        },
                    )
                }
            }
        }

        if (showXLabels) {
            BarXAxisLabels(
                ticks = ticks,
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
