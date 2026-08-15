package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.composable.ChartHeaderLayout
import io.github.dautovicharis.charts.internal.common.composable.DenseToggleControl
import io.github.dautovicharis.charts.internal.common.composable.ZoomControls
import io.github.dautovicharis.charts.style.LineChartStyle

@Composable
internal fun LineChartHeader(
    title: String,
    style: LineChartStyle,
    showDensityToggle: Boolean,
    denseExpanded: Boolean,
    onToggleDensity: () -> Unit,
    showZoomControls: Boolean,
    zoomScale: Float,
    minZoom: Float,
    maxZoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChartHeaderLayout(
        title = title,
        titleTextStyle = style.chartContainerStyle.styleTitle,
        showControls = showDensityToggle || showZoomControls,
        modifier = modifier,
    ) {
        if (showDensityToggle) {
            DenseToggleControl(
                expanded = denseExpanded,
                onToggle = onToggleDensity,
                expandTag = TestTags.LINE_CHART_DENSE_EXPAND,
                collapseTag = TestTags.LINE_CHART_DENSE_COLLAPSE,
            )
        }
        if (showZoomControls) {
            ZoomControls(
                zoomScale = zoomScale,
                minZoom = minZoom,
                maxZoom = maxZoom,
                onZoomOut = onZoomOut,
                onZoomIn = onZoomIn,
                zoomOutTag = TestTags.LINE_CHART_ZOOM_OUT,
                zoomInTag = TestTags.LINE_CHART_ZOOM_IN,
            )
        }
    }
}
