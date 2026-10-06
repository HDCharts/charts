package io.github.hdcharts.core.internal.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.InternalChartsApi

/**
 * Lays out a square plot between an optional title above it and an optional legend below it, for
 * charts whose plot is a circle. [ChartPlotLayout] with a `1f` aspect ratio; see it for the rules.
 */
@Composable
@InternalChartsApi
fun ChartSquarePlotLayout(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    legend: @Composable () -> Unit = {},
    plot: @Composable () -> Unit,
) {
    ChartPlotLayout(
        plotAspectRatio = 1f,
        modifier = modifier,
        title = title,
        legend = legend,
        plot = plot,
    )
}
