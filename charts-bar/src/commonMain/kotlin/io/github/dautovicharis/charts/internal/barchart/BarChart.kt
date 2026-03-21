package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.composable.rememberDenseExpandedState
import io.github.dautovicharis.charts.internal.common.composable.rememberZoomScaleState
import io.github.dautovicharis.charts.internal.common.composable.zoomInScale
import io.github.dautovicharis.charts.internal.common.composable.zoomOutScale
import io.github.dautovicharis.charts.internal.common.model.ChartData
import io.github.dautovicharis.charts.internal.common.model.normalizeBarValues
import io.github.dautovicharis.charts.internal.common.model.resolveBarRange
import io.github.dautovicharis.charts.style.BarChartStyle

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 4f
private const val ZOOM_STEP = 1.25f
private val Y_AXIS_CHART_GAP: Dp = 10.dp

@Composable
internal fun BarChart(
    chartData: ChartData,
    title: String,
    style: BarChartStyle,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedBarIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
) {
    val baseBarColor = style.barColor.copy(alpha = style.barAlpha)
    val sourceBarColors =
        remember(style.barColors, style.barAlpha) {
            style.barColors.map { color -> color.copy(alpha = style.barAlpha) }
        }
    val isPreview = LocalInspectionMode.current
    val sourceDataSize = chartData.points.size
    BoxWithConstraints(modifier = style.modifier) {
        val density = LocalDensity.current
        val spacingPx = with(density) { style.space.toPx() }
        val minBarWidthPx = with(density) { style.minBarWidth.toPx() }
        val (sourceFixedMin, sourceFixedMax) =
            remember(chartData, style.minValue, style.maxValue) {
                chartData.resolveBarRange(style.minValue, style.maxValue)
            }
        val yAxisTicks =
            remember(sourceFixedMin, sourceFixedMax, style.yAxisLabelCount) {
                buildYAxisTicks(
                    minValue = sourceFixedMin,
                    maxValue = sourceFixedMax,
                    labelCount = style.yAxisLabelCount,
                    chartHeightPx = 1f,
                )
            }
        val yAxisWidthPx =
            if (style.yAxisLabelsVisible) {
                estimateYAxisLabelWidthPx(
                    ticks = yAxisTicks,
                    fontSizePx = with(density) { style.yAxisLabelSize.toPx() },
                )
            } else {
                0f
            }
        val yAxisGapPx = if (style.yAxisLabelsVisible) with(density) { Y_AXIS_CHART_GAP.toPx() } else 0f
        val viewportWidthPx = (constraints.maxWidth.toFloat() - yAxisWidthPx - yAxisGapPx).coerceAtLeast(1f)
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
        val compactBarCenterIndices =
            remember(sourceDataSize, compactDenseMode, maxFitBars) {
                if (compactDenseMode) {
                    compactDensityCenterIndices(
                        sourcePointsCount = sourceDataSize,
                        targetPoints = maxFitBars,
                    )
                } else {
                    emptyList()
                }
            }
        val renderData =
            remember(chartData, compactDenseMode, maxFitBars) {
                if (compactDenseMode) {
                    aggregateForCompactDensity(
                        data = chartData,
                        targetPoints = maxFitBars,
                    )
                } else {
                    chartData
                }
            }
        val renderBarColors =
            remember(sourceBarColors, compactBarCenterIndices, compactDenseMode, baseBarColor) {
                when {
                    sourceBarColors.isEmpty() -> emptyList()
                    !compactDenseMode -> sourceBarColors
                    else ->
                        compactBarCenterIndices.map { centerIndex ->
                            sourceBarColors.getOrElse(centerIndex) { baseBarColor }
                        }
                }
            }
        val dataSize = renderData.points.size
        val hasFixedRange = style.minValue != null || style.maxValue != null
        val (fixedMin, fixedMax) =
            remember(renderData, style) {
                renderData.resolveBarRange(style.minValue, style.maxValue)
            }
        val targetNormalized =
            remember(renderData, fixedMin, fixedMax, hasFixedRange) {
                renderData.normalizeBarValues(fixedMin, fixedMax, hasFixedRange)
            }
        val animatedValues =
            rememberBarChartAnimatedValues(
                chartData = renderData,
                targetNormalized = targetNormalized,
                isPreview = isPreview,
                animateOnStart = animateOnStart,
            )

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
        var selectedIndexFromInteraction by remember { mutableIntStateOf(NO_SELECTION) }
        val forcedSelectedIndex =
            selectedBarIndex.takeIf { it in 0 until dataSize } ?: NO_SELECTION
        val hasForcedSelection = forcedSelectedIndex != NO_SELECTION

        LaunchedEffect(dataSize) {
            if (hasForcedSelection) return@LaunchedEffect
            if (selectedIndexFromInteraction !in 0 until dataSize) {
                selectedIndexFromInteraction = NO_SELECTION
                onValueChanged(NO_SELECTION)
            }
        }

        LaunchedEffect(renderData) {
            if (hasForcedSelection) return@LaunchedEffect
            if (selectedIndexFromInteraction != NO_SELECTION) {
                selectedIndexFromInteraction = NO_SELECTION
                onValueChanged(NO_SELECTION)
            }
        }

        val effectiveSelectedIndex =
            when (forcedSelectedIndex) {
                NO_SELECTION -> selectedIndexFromInteraction
                else -> forcedSelectedIndex
            }

        val resolvedTitle =
            remember(title, renderData, effectiveSelectedIndex) {
                when (effectiveSelectedIndex) {
                    NO_SELECTION -> title
                    else -> resolveSelectedBarTitle(chartData = renderData, index = effectiveSelectedIndex)
                }
            }

        val onSelectIndex: (Int) -> Unit = { index ->
            if (!hasForcedSelection) {
                val resolvedIndex = if (index in 0 until dataSize) index else NO_SELECTION
                if (selectedIndexFromInteraction != resolvedIndex) {
                    selectedIndexFromInteraction = resolvedIndex
                    onValueChanged(resolvedIndex)
                }
            }
        }

        val showZoomControlsInHeader = isScrollable && style.zoomControlsVisible
        val showCompactToggle = isDenseData
        val showHeader = resolvedTitle.isNotBlank() || showCompactToggle || showZoomControlsInHeader

        val onToggleSelection: (Int) -> Unit = { index ->
            val currentSelected = if (hasForcedSelection) forcedSelectedIndex else selectedIndexFromInteraction
            if (currentSelected == index && currentSelected != NO_SELECTION) {
                onSelectIndex(NO_SELECTION)
            } else {
                onSelectIndex(index)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (showHeader) {
                BarChartHeader(
                    title = resolvedTitle,
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

            BarChartContent(
                chartData = renderData,
                style = style,
                interactionEnabled = interactionEnabled,
                dragSelectionEnabled = !isScrollable,
                animatedValues = animatedValues,
                barColors = renderBarColors,
                defaultBarColor = baseBarColor,
                fixedMin = fixedMin,
                fixedMax = fixedMax,
                isScrollable = isScrollable,
                spacingPx = spacingPx,
                minBarWidthPx = minBarWidthPx,
                scrollState = scrollState,
                zoomScale = zoomScale,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                zoomStep = zoomStep,
                selectedIndex = effectiveSelectedIndex,
                onToggleSelection = onToggleSelection,
                onSelectIndex = onSelectIndex,
                onClearSelection = { onSelectIndex(NO_SELECTION) },
                onZoomScaleChange = { updatedScale ->
                    zoomScale = updatedScale
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = if (showHeader) style.chartViewStyle.innerPadding else 0.dp),
            )
        }
    }
}

private fun resolveSelectedBarTitle(
    chartData: ChartData,
    index: Int,
): String {
    val label =
        chartData.labels
            .getOrNull(index)
            .orEmpty()
            .ifBlank { (index + 1).toString() }
    val value = chartData.points.getOrNull(index) ?: return label
    return "$label: $value"
}
