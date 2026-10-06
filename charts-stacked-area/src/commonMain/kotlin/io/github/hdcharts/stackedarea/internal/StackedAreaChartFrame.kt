package io.github.hdcharts.stackedarea.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.stackedarea.StackedAreaChartStyle
import kotlinx.collections.immutable.ImmutableList

/** Plot and legend layout for [io.github.hdcharts.stackedarea.StackedAreaChart], matching LineChartFrame. */
@Composable
internal fun StackedAreaChartFrame(
    style: StackedAreaChartStyle,
    colors: ImmutableList<Color>,
    seriesNames: ImmutableList<String>,
    selectedLabels: ImmutableList<String>,
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
            Legend(
                chartContainerStyle = style.chartContainerStyle,
                style = style.legend,
                colors = colors,
                legend = seriesNames,
                labels = selectedLabels,
            )
        }
    }
}
