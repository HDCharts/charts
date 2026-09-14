package io.github.hdcharts.charts.internal.common.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.InternalChartsApi

@Composable
@InternalChartsApi
fun DenseToggleControl(
    expanded: Boolean,
    onToggle: () -> Unit,
    expandTag: String,
    collapseTag: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = if (expanded) "[-]" else "[+]",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onToggle)
                .testTag(if (expanded) collapseTag else expandTag)
                .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
@InternalChartsApi
fun ZoomControls(
    zoomScale: Float,
    minZoom: Float,
    maxZoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    zoomOutTag: String,
    zoomInTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val enabledColor = MaterialTheme.colorScheme.onSurface
        val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

        Text(
            text = "-",
            style = MaterialTheme.typography.titleMedium,
            color = if (zoomScale > minZoom) enabledColor else disabledColor,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = zoomScale > minZoom, onClick = onZoomOut)
                    .testTag(zoomOutTag)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
        )

        Text(
            text = "+",
            style = MaterialTheme.typography.titleMedium,
            color = if (zoomScale < maxZoom) enabledColor else disabledColor,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = zoomScale < maxZoom, onClick = onZoomIn)
                    .testTag(zoomInTag)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
