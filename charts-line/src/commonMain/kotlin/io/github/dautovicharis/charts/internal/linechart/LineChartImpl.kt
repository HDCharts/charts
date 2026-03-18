package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.LineChartRenderMode
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.composable.Legend
import io.github.dautovicharis.charts.internal.common.composable.rememberDenseExpandedState
import io.github.dautovicharis.charts.internal.common.composable.rememberZoomScaleState
import io.github.dautovicharis.charts.internal.common.composable.zoomInScale
import io.github.dautovicharis.charts.internal.common.composable.zoomOutScale
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.validateLineData
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.LineChartStyle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val LINE_ZOOM_MIN = 1f
private const val LINE_ZOOM_MAX = 4f
private const val LINE_ZOOM_STEP = 1.25f

@Composable
internal fun LineChartImpl(
    data: MultiChartData,
    style: LineChartStyle = LineChartDefaults.style(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    renderMode: LineChartRenderMode = LineChartRenderMode.Morph,
    animationDurationMillis: Int = 420,
    selectedPointIndex: Int = NO_SELECTION,
) {
    val errors =
        remember(data, style) {
            validateLineData(
                data = data,
                style = style,
            )
        }

    if (errors.isEmpty()) {
        val isTimelineMode = renderMode == LineChartRenderMode.Timeline
        var selectedIndexFromInteraction by remember(data) { mutableIntStateOf(NO_SELECTION) }
        val sourcePointsCount = remember(data) { data.getFirstPointsSize() }
        val isDenseMorphData =
            remember(renderMode, sourcePointsCount) {
                renderMode == LineChartRenderMode.Morph && shouldUseScrollableDensity(sourcePointsCount)
            }
        var denseExpanded by rememberDenseExpandedState(isDenseModeAvailable = isDenseMorphData)
        val compactDenseMode = isDenseMorphData && !denseExpanded
        val renderData =
            remember(data, compactDenseMode) {
                if (compactDenseMode) {
                    aggregateForCompactDensity(data)
                } else {
                    data
                }
            }
        val renderDataPointsCount = renderData.getFirstPointsSize()
        val forcedSelectedIndex =
            if (isTimelineMode) {
                NO_SELECTION
            } else {
                selectedPointIndex.takeIf { it in 0 until renderDataPointsCount }
                    ?: NO_SELECTION
            }
        val selectedInteractionIndex =
            selectedIndexFromInteraction.takeIf { it in 0 until renderDataPointsCount } ?: NO_SELECTION
        val effectiveSelectedIndex =
            when (forcedSelectedIndex) {
                NO_SELECTION -> selectedInteractionIndex
                else -> forcedSelectedIndex
            }
        val title =
            remember(renderData, effectiveSelectedIndex) {
                renderData.getLabel(effectiveSelectedIndex)
            }
        val labels =
            remember(renderData, effectiveSelectedIndex, isTimelineMode) {
                if (!isTimelineMode && renderData.hasCategories() && effectiveSelectedIndex != NO_SELECTION) {
                    renderData.items.map { it.item.labels[effectiveSelectedIndex] }.toImmutableList()
                } else {
                    persistentListOf()
                }
            }
        val isDenseMorphMode = isDenseMorphData && denseExpanded
        val scrollState = rememberScrollState()
        var zoomScale by
            rememberZoomScaleState(
                isZoomActive = isDenseMorphMode,
                minZoom = LINE_ZOOM_MIN,
                maxZoom = LINE_ZOOM_MAX,
                initialZoom = LINE_ZOOM_MIN,
            )
        val lineColors =
            remember(renderData, style.lineColors, style.lineColor, style.lineAlpha) {
                if (renderData.hasSingleItem()) {
                    persistentListOf(style.lineColor.copy(alpha = style.lineAlpha))
                } else if (style.lineColors.isEmpty()) {
                    generateColorShades(
                        baseColor = style.lineColor.copy(alpha = style.lineAlpha),
                        numberOfShades = renderData.items.size,
                    )
                } else {
                    style.lineColors
                        .map { color -> color.copy(alpha = style.lineAlpha) }
                        .toImmutableList()
                }
            }
        val showCompactToggle = isDenseMorphData
        val showZoomControlsInHeader = isDenseMorphMode && style.zoomControlsVisible
        val showHeader = title.isNotBlank() || showCompactToggle || showZoomControlsInHeader
        Chart(chartViewsStyle = style.chartViewStyle) {
            if (showHeader) {
                LineChartHeader(
                    title = title,
                    style = style,
                    showDensityToggle = showCompactToggle,
                    denseExpanded = denseExpanded,
                    onToggleDensity = { denseExpanded = !denseExpanded },
                    showZoomControls = showZoomControlsInHeader,
                    zoomScale = zoomScale,
                    minZoom = LINE_ZOOM_MIN,
                    maxZoom = LINE_ZOOM_MAX,
                    onZoomOut = {
                        zoomScale = zoomOutScale(zoomScale, LINE_ZOOM_STEP, LINE_ZOOM_MIN, LINE_ZOOM_MAX)
                    },
                    onZoomIn = {
                        zoomScale = zoomInScale(zoomScale, LINE_ZOOM_STEP, LINE_ZOOM_MIN, LINE_ZOOM_MAX)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = style.chartViewStyle.innerPadding,
                                start = style.chartViewStyle.innerPadding,
                                end = style.chartViewStyle.innerPadding,
                                bottom = if (showZoomControlsInHeader) style.chartViewStyle.innerPadding else 0.dp,
                            ),
                )
            }

            LineChart(
                data = renderData,
                style = style,
                colors = lineColors,
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                renderMode = renderMode,
                animationDurationMillis = animationDurationMillis,
                isDenseMorphMode = isDenseMorphMode,
                scrollState = scrollState,
                zoomScale = zoomScale,
                selectedPointIndex = forcedSelectedIndex,
            ) { selectedIndex ->
                if (forcedSelectedIndex == NO_SELECTION && selectedIndexFromInteraction != selectedIndex) {
                    selectedIndexFromInteraction = selectedIndex
                }
            }

            if (renderData.hasCategories() || isTimelineMode) {
                Legend(
                    chartViewsStyle = style.chartViewStyle,
                    legend = renderData.items.map { it.label }.toImmutableList(),
                    colors = lineColors,
                    labels = labels,
                )
            }
        }
    } else {
        ChartErrors(style = style.chartViewStyle, errors = errors.toImmutableList())
    }
}
