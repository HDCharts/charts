package io.github.hdcharts.stackedbar.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.stackedbar.StackedBarChartStyle
import kotlinx.collections.immutable.ImmutableList

/** Plot and legend layout for [io.github.hdcharts.stackedbar.StackedBarChart], matching LineChartFrame. */
@Composable
internal fun StackedBarChartFrame(
    style: StackedBarChartStyle,
    colors: ImmutableList<Color>,
    segmentNames: ImmutableList<String>,
    selectedLabels: ImmutableList<String>,
    showLegend: Boolean,
    modifier: Modifier,
    plot: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val boundedHeight = maxHeight != Dp.Infinity
        Column {
            val plotModifier = if (boundedHeight) Modifier.weight(1f) else Modifier
            Box(modifier = plotModifier) {
                plot()
            }
            if (showLegend) {
                Legend(
                    chartContainerStyle = style.chartContainerStyle,
                    colors = colors,
                    legend = segmentNames,
                    labels = selectedLabels,
                )
            }
        }
    }
}
