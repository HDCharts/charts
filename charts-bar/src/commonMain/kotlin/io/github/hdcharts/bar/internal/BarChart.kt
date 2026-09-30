package io.github.hdcharts.bar.internal

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.axis.rememberNumericYAxisLayout
import io.github.hdcharts.core.internal.composable.ChartHeader
import io.github.hdcharts.core.internal.composable.ChartHeaderTestTags
import io.github.hdcharts.core.internal.composable.rememberDenseExpandedState
import io.github.hdcharts.core.internal.composable.rememberZoomScaleState
import io.github.hdcharts.core.internal.composable.zoomInScale
import io.github.hdcharts.core.internal.composable.zoomOutScale
import io.github.hdcharts.core.internal.layout.fillMaxSizeChartModifier
import io.github.hdcharts.core.internal.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.BarChartStyle

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 4f
private const val ZOOM_STEP = 1.25f
private val HEADER_TEST_TAGS =
    ChartHeaderTestTags(
        denseExpand = TestTags.BAR_CHART_DENSE_EXPAND,
        denseCollapse = TestTags.BAR_CHART_DENSE_COLLAPSE,
        zoomOut = TestTags.BAR_CHART_ZOOM_OUT,
        zoomIn = TestTags.BAR_CHART_ZOOM_IN,
    )

@Composable
internal fun BarChart(
    chartData: ChartData,
    title: String,
    style: BarChartStyle,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedBarIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
    aggregate: Boolean,
    valueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
) {
    val baseBarColor = style.bars.color.copy(alpha = style.bars.alpha)
    val sourceBarColors =
        remember(style.bars.colors, style.bars.alpha) {
            style.bars.colors.map { color -> color.copy(alpha = style.bars.alpha) }
        }
    val isPreview = LocalInspectionMode.current
    val sourceDataSize = chartData.points.size
    BoxWithConstraints(modifier = fillMaxSizeChartModifier(style.chartContainerStyle)) {
        val density = LocalDensity.current
        val spacingPx = with(density) { style.bars.space.toPx() }
        val minBarWidthPx = with(density) { style.bars.minBarWidth.toPx() }
        val (fixedMin, fixedMax) =
            remember(chartData, style.range.min, style.range.max) {
                chartData.resolveBarRange(style.range.min, style.range.max)
            }
        val yAxisLayout =
            rememberNumericYAxisLayout(
                labels = style.axis.yLabels,
                minValue = fixedMin,
                maxValue = fixedMax,
                // The plot height is not known yet. A short plot can show fewer ticks than this estimate.
                chartHeightPx = constraints.maxHeight.toFloat(),
                verticalInsetPx = 0f,
                formatter = axisValueFormatter,
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
        val compactDenseMode = aggregate && isDenseData && !denseExpanded
        val sourceRanges =
            remember(sourceDataSize, compactDenseMode, maxFitBars) {
                if (compactDenseMode) {
                    compactDensityRanges(
                        sourcePointsCount = sourceDataSize,
                        targetPoints = maxFitBars,
                    )
                } else {
                    List(sourceDataSize) { it..it }
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
            remember(sourceBarColors, sourceRanges, compactDenseMode, baseBarColor) {
                when {
                    sourceBarColors.isEmpty() -> emptyList()
                    !compactDenseMode -> sourceBarColors
                    else ->
                        sourceRanges.map { range ->
                            val centerIndex = range.first + (range.last - range.first) / 2
                            sourceBarColors.getOrElse(centerIndex) { baseBarColor }
                        }
                }
            }
        val dataSize = renderData.points.size
        val targetNormalized =
            remember(renderData, fixedMin, fixedMax) {
                val baseline = barValueYFraction(0.0, fixedMin, fixedMax)
                renderData.points.map { (baseline - barValueYFraction(it, fixedMin, fixedMax)).toFloat() }
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
        val effectiveSelectedIndex =
            sourceRanges.indexOfFirst { selectedBarIndex in it }

        val resolvedTitle =
            remember(title, chartData, selectedBarIndex, valueFormatter) {
                when {
                    selectedBarIndex !in chartData.points.indices -> title
                    else -> resolveSelectedBarTitle(chartData, selectedBarIndex, valueFormatter)
                }
            }

        val onSelectIndex: (Int) -> Unit = { index ->
            val range = sourceRanges.getOrNull(index)
            onValueChanged(range?.let { it.first + (it.last - it.first) / 2 } ?: NO_SELECTION)
        }

        val showZoomControlsInHeader = interactionEnabled && isScrollable && style.zoomControlsVisible
        val showCompactToggle = interactionEnabled && isDenseData
        val showHeader = resolvedTitle.isNotBlank() || showCompactToggle || showZoomControlsInHeader

        val onToggleSelection: (Int) -> Unit = { index ->
            if (effectiveSelectedIndex == index && effectiveSelectedIndex != NO_SELECTION) {
                onValueChanged(NO_SELECTION)
            } else {
                onSelectIndex(index)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (showHeader) {
                ChartHeader(
                    title = resolvedTitle,
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
                axisValueFormatter = axisValueFormatter,
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
                        .padding(top = if (showHeader) style.chartContainerStyle.contentPadding else 0.dp),
            )
        }
    }
}

private fun resolveSelectedBarTitle(
    chartData: ChartData,
    index: Int,
    formatter: ChartValueFormatter,
): String {
    val label = chartData.labels.getOrNull(index).orEmpty()
    val value = chartData.points.getOrNull(index) ?: return label
    val formatted = formatter.format(value)
    return if (label.isBlank()) formatted else "$label: $formatted"
}
