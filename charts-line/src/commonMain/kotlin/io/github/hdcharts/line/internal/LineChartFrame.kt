package io.github.hdcharts.line.internal

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
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.line.LineChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Header, plot and legend layout shared by [LineChartImpl] and [LiveLineChartImpl]. */
@Composable
internal fun LineChartFrame(
    data: ChartRenderData,
    style: LineChartStyle,
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

            Legend(
                chartContainerStyle = style.chartContainerStyle,
                style = style.legend,
                legend = data.series.map { it.name.orEmpty() }.toImmutableList(),
                colors = colors,
                labels = legendLabels,
            )
        }
    }
}

@Composable
internal fun rememberLineColors(
    style: LineChartStyle,
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
    contentPadding: Dp,
    bottom: Dp = 0.dp,
): Modifier =
    Modifier
        .fillMaxWidth()
        .padding(
            top = contentPadding,
            start = contentPadding,
            end = contentPadding,
            bottom = bottom,
        )
