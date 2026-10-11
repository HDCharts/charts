package io.github.hdcharts.app.gif.docs

import androidx.compose.ui.graphics.Color

/** Gradient stops shared by the bar, histogram, and live line gradient GIFs. */
internal val GradientColors = listOf(Color(0xFF1E89E6), Color(0xFF9B4EDD), Color(0xFFEA7A2E))

/** Series colors shared by the stacked bar and stacked area gradient GIFs. */
internal val StackedSeriesColors = listOf(Color(0xFF1E89E6), Color(0xFF5C4EE5), Color(0xFF9B4EDD))

/** Stops of the line gradient GIF, from high values to low ones. */
internal val LineValueColors = listOf(Color(0xFFE5484D), Color(0xFFF5A524), Color(0xFF1E89E6))
