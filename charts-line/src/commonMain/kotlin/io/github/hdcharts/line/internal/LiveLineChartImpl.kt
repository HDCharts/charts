package io.github.hdcharts.line.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.composable.ChartHeaderLayout
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.line.LineChartStyle
import kotlin.time.Duration

/** Display-only line chart: no dense mode, zoom, scrolling or selection. */
@Composable
internal fun LiveLineChartImpl(
    data: MultiChartData,
    modifier: Modifier,
    style: LineChartStyle,
    shiftDuration: Duration,
    animateOnStart: Boolean,
    axisValueFormatter: ChartValueFormatter,
) {
    val renderMode = remember(shiftDuration) { LineChartRenderMode.Timeline(shiftDuration = shiftDuration) }
    val colors = rememberLineColors(style = style, count = data.items.size)
    LineChartFrame(
        data = data,
        style = style,
        colors = colors,
        modifier = modifier,
        header = {
            if (data.title.isNotBlank()) {
                ChartHeaderLayout(
                    title = data.title,
                    titleTextStyle = style.chartContainerStyle.styleTitle,
                    showControls = false,
                    modifier = lineHeaderModifier(contentPadding = style.chartContainerStyle.contentPadding),
                )
            }
        },
    ) {
        LineChartContent(
            data = data,
            style = style,
            colors = colors,
            interactionEnabled = false,
            animateOnStart = animateOnStart,
            renderMode = renderMode,
            axisValueFormatter = axisValueFormatter,
        )
    }
}
