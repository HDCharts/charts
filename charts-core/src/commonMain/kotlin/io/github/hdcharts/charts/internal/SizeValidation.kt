package io.github.hdcharts.charts.internal

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/** Returns an error for each named style size that does not resolve to 0..[MAX_SIZE_PX] pixels at [density]. */
@InternalChartsApi
fun validateSizes(
    density: Density,
    vararg sizes: Pair<String, Dp>,
): List<String> =
    sizes.mapNotNull { (name, size) ->
        val pixels = size.value * density.density
        if (pixels.isFinite() &&
            pixels in 0f..MAX_SIZE_PX
        ) {
            null
        } else {
            "$name must resolve to 0..${MAX_SIZE_PX.toInt()} pixels."
        }
    }
