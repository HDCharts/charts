package io.github.hdcharts.core.internal.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import io.github.hdcharts.core.internal.InternalChartsApi

/** Test tags for one chart's header controls. */
@Immutable
@InternalChartsApi
class ChartHeaderTestTags(
    val denseExpand: String,
    val denseCollapse: String,
    val zoomOut: String,
    val zoomIn: String,
)

/** Chart title with the optional dense toggle and zoom controls. */
@Composable
@InternalChartsApi
fun ChartHeader(
    title: String,
    titleTextStyle: TextStyle,
    testTags: ChartHeaderTestTags,
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
        titleTextStyle = titleTextStyle,
        showControls = showDensityToggle || showZoomControls,
        modifier = modifier,
    ) {
        if (showDensityToggle) {
            DenseToggleControl(
                expanded = denseExpanded,
                onToggle = onToggleDensity,
                expandTag = testTags.denseExpand,
                collapseTag = testTags.denseCollapse,
            )
        }

        if (showZoomControls) {
            ZoomControls(
                zoomScale = zoomScale,
                minZoom = minZoom,
                maxZoom = maxZoom,
                onZoomOut = onZoomOut,
                onZoomIn = onZoomIn,
                zoomOutTag = testTags.zoomOut,
                zoomInTag = testTags.zoomIn,
            )
        }
    }
}
