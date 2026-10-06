package io.github.hdcharts.core.internal.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.InternalChartsApi

/**
 * Lays out a plot of fixed shape between an optional title above it and an optional legend below
 * it. [plotAspectRatio] is the plot's width divided by its height: `1f` for a full circle, `2f` for
 * a half circle.
 *
 * Charts whose plot has a fixed shape own this scaffold so they cannot drift apart on the details
 * that are easy to get wrong:
 *
 * - The column matches the width the caller asks for, so the plot centers in a wide chart instead
 *   of sticking to the start edge. The title and legend stay start-aligned with the chart.
 * - The plot box keeps its shape and is centered. When the caller bounds the height, the box takes
 *   the leftover height without filling it, so the title and legend keep their space.
 *
 * The slots are plain composables so each chart keeps its own title content and its own condition
 * for showing a legend.
 */
@Composable
@InternalChartsApi
fun ChartPlotLayout(
    plotAspectRatio: Float,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    legend: @Composable () -> Unit = {},
    plot: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val boundedHeight = maxHeight != Dp.Infinity
        Column(modifier = Modifier.widthIn(min = minWidth)) {
            title()

            val plotModifier =
                if (boundedHeight) Modifier.weight(1f, fill = false) else Modifier
            Box(
                modifier =
                    plotModifier
                        .aspectRatio(plotAspectRatio)
                        .align(Alignment.CenterHorizontally),
            ) {
                plot()
            }

            legend()
        }
    }
}
