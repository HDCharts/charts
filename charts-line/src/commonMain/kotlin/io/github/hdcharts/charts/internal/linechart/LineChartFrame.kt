package io.github.hdcharts.charts.internal.linechart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.common.composable.Legend
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Header, plot and legend layout shared by [LineChartImpl] and [LiveLineChartImpl]. */
@Composable
internal fun LineChartFrame(
    data: MultiChartData,
    style: LineChartInternalStyle,
    colors: ImmutableList<Color>,
    modifier: Modifier,
    legendLabels: ImmutableList<String> = persistentListOf(),
    header: @Composable () -> Unit,
    plot: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val boundedHeight = maxHeight != Dp.Infinity
        Column {
            header()

            val plotModifier = if (boundedHeight) Modifier.weight(1f) else Modifier
            Box(modifier = plotModifier) {
                plot()
            }

            if (style.legendVisible && data.items.size > 1) {
                Legend(
                    chartContainerStyle = style.chartContainerStyle,
                    legend = data.items.map { it.label }.toImmutableList(),
                    colors = colors,
                    labels = legendLabels,
                )
            }
        }
    }
}

@Composable
internal fun rememberLineColors(
    style: LineChartInternalStyle,
    count: Int,
): ImmutableList<Color> =
    remember(count, style.line) {
        style.line
            .resolveColors(seriesCount = count)
            .map { color -> color.copy(alpha = style.line.alpha) }
            .toImmutableList()
    }

/** Header padding shared by both line charts, so their titles line up. */
internal fun lineHeaderModifier(
    style: LineChartInternalStyle,
    bottom: Dp = 0.dp,
): Modifier =
    Modifier
        .fillMaxWidth()
        .padding(
            top = style.chartContainerStyle.contentPadding,
            start = style.chartContainerStyle.contentPadding,
            end = style.chartContainerStyle.contentPadding,
            bottom = bottom,
        )
