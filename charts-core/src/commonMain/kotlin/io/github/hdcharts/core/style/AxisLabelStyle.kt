package io.github.hdcharts.core.style

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

/**
 * Axis label configuration shared by Cartesian charts.
 *
 * @property visible Whether the labels are visible.
 * @property color The label color.
 * @property size The label text size.
 * @property maxCount The most labels to show, or null to let the chart choose. Labels are evenly
 * spaced and never overlap, so a chart shows fewer labels when [maxCount] would not fit. X-axis labels
 * start at the first item and count per screen while the chart scrolls; null shows as many as fit.
 * Y-axis labels run from the lowest to the highest value; null shows up to five. A set count must be
 * in 2..1000.
 */
@Immutable
data class AxisLabelStyle(
    val visible: Boolean,
    val color: Color,
    val size: TextUnit,
    val maxCount: Int?,
)
