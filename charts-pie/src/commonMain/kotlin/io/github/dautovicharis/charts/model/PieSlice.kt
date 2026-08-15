package io.github.dautovicharis.charts.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A single slice of a pie chart.
 *
 * Each [PieSlice] is a self-contained entity carrying its own label, value, and optional
 * color. Providing the color on the slice (rather than as a separate style list) makes it
 * impossible to supply a color count that does not match the slice count.
 *
 * @param label The slice label, shown in the legend and while the slice is selected.
 * @param value The slice value. The rendered arc is proportional to this value.
 * @param color The slice color. When `null`, a shade is generated from the style's
 * base color.
 */
@Immutable
data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color? = null,
)
