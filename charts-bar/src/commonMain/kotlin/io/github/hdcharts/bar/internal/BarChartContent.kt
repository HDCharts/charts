package io.github.hdcharts.bar.internal

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
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.axis.AxisXItems
import io.github.hdcharts.core.internal.axis.AxisXLabelsLayout
import io.github.hdcharts.core.internal.axis.AxisYLabelsLayout
import io.github.hdcharts.core.internal.axis.estimateXAxisLabelExtent
import io.github.hdcharts.core.internal.axis.rememberNumericYAxisLayout
import io.github.hdcharts.core.internal.axis.rememberXAxisLabelPlan
import io.github.hdcharts.core.internal.axis.xAxisLabelRowHeightPx
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.layout.chartCanvasFits
import io.github.hdcharts.core.internal.layout.placedHorizontalScrollPx
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.BarChartStyle
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.roundToInt

@Composable
internal fun BarChartContent(
    chartData: ChartData,
    style: BarChartStyle,
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
    val xLabels = style.axis.xLabels
    val yLabels = style.axis.yLabels
    val dataSize = chartData.barValues.size
    val showXLabels = xLabels.visible && chartData.categories.any { it.isNotBlank() }
    val currentToggleSelection by rememberUpdatedState(onToggleSelection)
    val currentSelectIndex by rememberUpdatedState(onSelectIndex)
    val currentClearSelection by rememberUpdatedState(onClearSelection)
    val currentZoomScale by rememberUpdatedState(zoomScale)
    val currentZoomChange by rememberUpdatedState(onZoomScaleChange)

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val xAxisLabelSizePx = with(density) { xLabels.size.toPx() }
        val xAxisRowHeightPx =
            remember(showXLabels, chartData.categories, dataSize, xAxisLabelSizePx, density) {
                if (showXLabels) {
                    with(density) {
                        xAxisLabelRowHeightPx(
                            estimateXAxisLabelExtent(
                                labels = chartData.categories,
                                dataSize = dataSize,
                                fontSizePx = xAxisLabelSizePx,
                            ),
                        )
                    }
                } else {
                    0f
                }
            }
        val xAxisHeightPx = xAxisRowHeightPx.roundToInt().coerceIn(0, constraints.maxHeight)
        val xAxisHeight = with(density) { xAxisHeightPx.toDp() }
        val chartHeightPx = (constraints.maxHeight - xAxisHeightPx).coerceAtLeast(0).toFloat()
        val chartHeight = with(density) { chartHeightPx.toDp() }
        val yAxisLayout =
            rememberNumericYAxisLayout(
                labels = yLabels,
                minValue = fixedMin,
                maxValue = fixedMax,
                chartHeightPx = chartHeightPx,
                verticalInsetPx = 0f,
                formatter = axisValueFormatter,
                availableWidthPx = constraints.maxWidth,
            )
        val yAxisWidth = with(density) { yAxisLayout.widthPx.toDp() }
        val plotStartPadding = with(density) { (yAxisLayout.widthPx + yAxisLayout.gapPx).toDp() }
        val viewportWidthPx =
            (constraints.maxWidth.toFloat() - yAxisLayout.widthPx - yAxisLayout.gapPx).coerceAtLeast(1f)

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

        val xAxisLabelPlan =
            rememberXAxisLabelPlan(
                labels = chartData.categories,
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
        val ticks = xAxisLabelPlan.ticks
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
                            .testTag(TestTags.BAR_CHART_Y_AXIS_LABELS),
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
            AxisXLabelsLayout(
                ticks = ticks,
                color = xLabels.color,
                fontSize = xLabels.size,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = plotStartPadding)
                        .height(xAxisHeight)
                        .testTag(TestTags.BAR_CHART_X_AXIS_LABELS),
            )
        }
    }
}
