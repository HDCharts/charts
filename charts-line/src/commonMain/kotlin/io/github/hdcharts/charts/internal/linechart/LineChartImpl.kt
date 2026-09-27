package io.github.hdcharts.charts.internal.linechart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.common.composable.rememberDenseExpandedState
import io.github.hdcharts.charts.internal.common.composable.rememberZoomScaleState
import io.github.hdcharts.charts.internal.common.composable.zoomInScale
import io.github.hdcharts.charts.internal.common.composable.zoomOutScale
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import kotlinx.collections.immutable.ImmutableList

private const val LINE_ZOOM_MIN = 1f
private const val LINE_ZOOM_MAX = 4f
private const val LINE_ZOOM_STEP = 1.25f

/** Interactive line chart: dense fit/expand, zoom, scrolling and selection. */
@Composable
internal fun LineChartImpl(
    data: MultiChartData,
    modifier: Modifier = Modifier,
    style: LineChartInternalStyle,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedPointIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
    axisValueFormatter: io.github.hdcharts.charts.model.ChartValueFormatter,
    legendLabels: ImmutableList<String>,
    selectedTitle: String? = null,
) {
    val sourcePointsCount = remember(data) { data.getFirstPointsSize() }
    val isDenseData =
        remember(sourcePointsCount) {
            shouldUseScrollableDensity(sourcePointsCount)
        }
    // Without interaction the user cannot collapse, so fall back to the fit view.
    var denseExpanded by rememberDenseExpandedState(isDenseModeAvailable = interactionEnabled && isDenseData)
    val compactDenseMode = isDenseData && !denseExpanded
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
            selectedTitle ?: renderData.title
        } else {
            renderData.title
        }
    val isDenseMode = isDenseData && denseExpanded
    var zoomScale by
        rememberZoomScaleState(
            isZoomActive = isDenseMode,
            minZoom = LINE_ZOOM_MIN,
            maxZoom = LINE_ZOOM_MAX,
            initialZoom = LINE_ZOOM_MIN,
        )
    val lineColors = rememberLineColors(style = style, count = renderData.items.size)
    val showCompactToggle = interactionEnabled && isDenseData
    val showZoomControlsInHeader = interactionEnabled && isDenseMode && style.zoomControlsVisible
    val showHeader = title.isNotBlank() || showCompactToggle || showZoomControlsInHeader
    LineChartFrame(
        data = renderData,
        style = style,
        colors = lineColors,
        modifier = modifier,
        legendLabels = legendLabels,
        header = {
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
                        lineHeaderModifier(
                            style = style,
                            bottom =
                                if (showZoomControlsInHeader) {
                                    style.chartContainerStyle.contentPadding
                                } else {
                                    0.dp
                                },
                        ),
                )
            }
        },
    ) {
        LineChartContent(
            data = renderData,
            style = style,
            colors = lineColors,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            renderMode = LineChartRenderMode.Morph,
            isDenseMode = isDenseMode,
            zoomScale = zoomScale,
            selectedPointIndex = effectiveSelectedIndex,
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
}
