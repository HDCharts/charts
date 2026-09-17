package io.github.hdcharts.charts.internal.linechart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.LineChartRenderMode
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.common.composable.ChartErrors
import io.github.hdcharts.charts.internal.common.composable.Legend
import io.github.hdcharts.charts.internal.common.composable.rememberDenseExpandedState
import io.github.hdcharts.charts.internal.common.composable.rememberZoomScaleState
import io.github.hdcharts.charts.internal.common.composable.zoomInScale
import io.github.hdcharts.charts.internal.common.composable.zoomOutScale
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.common.palette.generateColorShades
import io.github.hdcharts.charts.internal.linechart.LineChartInternalStyle
import io.github.hdcharts.charts.internal.validateLineData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration

private const val LINE_ZOOM_MIN = 1f
private const val LINE_ZOOM_MAX = 4f
private const val LINE_ZOOM_STEP = 1.25f

@Composable
internal fun LineChartImpl(
    data: MultiChartData,
    modifier: Modifier = Modifier,
    style: LineChartInternalStyle,
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    renderMode: LineChartRenderMode = LineChartRenderMode.Morph,
    animationDuration: Duration,
    selectedPointIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
    valueFormatter: io.github.hdcharts.charts.model.ChartValueFormatter,
    axisValueFormatter: io.github.hdcharts.charts.model.ChartValueFormatter,
    selectedTitle: String? = null,
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
        val sourcePointsCount = remember(data) { data.getFirstPointsSize() }
        val isDenseMorphData =
            remember(renderMode, sourcePointsCount) {
                renderMode == LineChartRenderMode.Morph && shouldUseScrollableDensity(sourcePointsCount)
            }
        var denseExpanded by rememberDenseExpandedState(isDenseModeAvailable = isDenseMorphData)
        val compactDenseMode = isDenseMorphData && !denseExpanded
        val sourceRanges =
            remember(sourcePointsCount, compactDenseMode) {
                if (compactDenseMode) {
                    compactDensityRanges(sourcePointsCount)
                } else {
                    emptyList()
                }
            }
        val renderData =
            remember(data, compactDenseMode) {
                if (compactDenseMode) {
                    aggregateForCompactDensity(data)
                } else {
                    data
                }
            }
        val effectiveSelectedIndex =
            remember(selectedPointIndex, sourceRanges, compactDenseMode, sourcePointsCount) {
                if (compactDenseMode) {
                    renderIndexForSourceIndex(selectedPointIndex, sourceRanges)
                } else {
                    selectedPointIndex.takeIf { it in 0 until sourcePointsCount } ?: NO_SELECTION
                }
            }
        val title =
            if (effectiveSelectedIndex != NO_SELECTION) {
                selectedTitle ?: renderData.getLabel(effectiveSelectedIndex)
            } else {
                renderData.title
            }
        val labels =
            remember(data, selectedPointIndex, isTimelineMode) {
                if (!isTimelineMode && data.hasCategories() && selectedPointIndex in 0 until sourcePointsCount) {
                    data.items.map { it.item.labels[selectedPointIndex] }.toImmutableList()
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
        BoxWithConstraints(modifier = modifier) {
            val boundedHeight = maxHeight != Dp.Infinity
            Column {
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
                                    top = style.chartContainerStyle.contentPadding,
                                    start = style.chartContainerStyle.contentPadding,
                                    end = style.chartContainerStyle.contentPadding,
                                    bottom =
                                        if (showZoomControlsInHeader) {
                                            style.chartContainerStyle.contentPadding
                                        } else {
                                            0.dp
                                        },
                                ),
                    )
                }

                val plotModifier = if (boundedHeight) Modifier.weight(1f) else Modifier
                Box(modifier = plotModifier) {
                    LineChart(
                        data = renderData,
                        style = style,
                        colors = lineColors,
                        interactionEnabled = interactionEnabled,
                        animateOnStart = animateOnStart,
                        renderMode = renderMode,
                        animationDuration = animationDuration,
                        isDenseMorphMode = isDenseMorphMode,
                        scrollState = scrollState,
                        zoomScale = zoomScale,
                        selectedPointIndex = effectiveSelectedIndex,
                        valueFormatter = valueFormatter,
                        axisValueFormatter = axisValueFormatter,
                        onValueChanged = { renderIndex ->
                            onValueChanged(
                                if (compactDenseMode) {
                                    sourceIndexForRenderIndex(renderIndex, sourceRanges)
                                } else {
                                    renderIndex
                                },
                            )
                        },
                    )
                }

                if (style.legendVisible && renderData.items.size > 1) {
                    Legend(
                        chartContainerStyle = style.chartContainerStyle,
                        legend = renderData.items.map { it.label }.toImmutableList(),
                        colors = lineColors,
                        labels = labels,
                    )
                }
            }
        }
    } else {
        ChartErrors(
            style = style.chartContainerStyle,
            errors = errors.toImmutableList(),
            modifier = modifier,
        )
    }
}
