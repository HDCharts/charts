package io.github.hdcharts.core.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/**
 * Shared legend visibility configuration.
 *
 * @property visible Whether the legend is shown.
 */
@Immutable
data class LegendStyle(
    val visible: Boolean,
)

/**
 * Shared defaults for [LegendStyle].
 */
object LegendDefaults {
    /** Returns a [LegendStyle] with the supplied values or theme-aware defaults. */
    @Composable
    fun style(visible: Boolean = StyleDefaults.legendVisible): LegendStyle = LegendStyle(visible = visible)
}
