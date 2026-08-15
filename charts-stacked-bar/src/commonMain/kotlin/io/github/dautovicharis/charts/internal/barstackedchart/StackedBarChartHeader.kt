package io.github.dautovicharis.charts.internal.barstackedchart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.composable.ChartHeaderLayout
import io.github.dautovicharis.charts.internal.common.composable.DenseToggleControl
import io.github.dautovicharis.charts.internal.common.composable.ZoomControls
import io.github.dautovicharis.charts.style.StackedBarChartStyle

@Composable
internal fun StackedBarChartHeader(
    title: String,
    style: StackedBarChartStyle,
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
                expandTag = TestTags.STACKED_BAR_CHART_DENSE_EXPAND,
                collapseTag = TestTags.STACKED_BAR_CHART_DENSE_COLLAPSE,
            )
        }

        if (showZoomControls) {
            ZoomControls(
                zoomScale = zoomScale,
                minZoom = minZoom,
                maxZoom = maxZoom,
                onZoomOut = onZoomOut,
                onZoomIn = onZoomIn,
                zoomOutTag = TestTags.STACKED_BAR_CHART_ZOOM_OUT,
                zoomInTag = TestTags.STACKED_BAR_CHART_ZOOM_IN,
            )
        }
    }
}
