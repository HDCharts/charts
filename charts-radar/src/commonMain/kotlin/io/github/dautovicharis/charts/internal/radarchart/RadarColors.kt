package io.github.dautovicharis.charts.internal.radarchart

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val DefaultCategoryColors =
    listOf(
        // Blue
        Color(0xFF4C78A8),
        // Orange
        Color(0xFFF58518),
        // Green
        Color(0xFF54A24B),
        // Red
        Color(0xFFE45756),
        // Purple
        Color(0xFFB279A2),
        // Teal
        Color(0xFF72B7B2),
        // Pink
        Color(0xFFFF9DA6),
        // Yellow
        Color(0xFFEDC949),
    )

internal fun categoryColors(
    style: RadarInternalStyle,
    count: Int,
): ImmutableList<Color> {
    if (count <= 0) return emptyList<Color>().toImmutableList()
    val baseColors =
        if (style.categoryColors.isNotEmpty()) {
            style.categoryColors
        } else {
            DefaultCategoryColors
        }
    val colors = mutableListOf<Color>()
    while (colors.size < count) {
        colors.addAll(baseColors)
    }
    return colors.take(count).toImmutableList()
}
