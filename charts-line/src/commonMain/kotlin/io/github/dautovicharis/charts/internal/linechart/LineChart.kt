package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.LineChartRenderMode
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.linechart.LineChartInternalStyle
import io.github.dautovicharis.charts.model.ChartValueFormatter
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LineChart(
    data: MultiChartData,
    style: LineChartInternalStyle,
    colors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    renderMode: LineChartRenderMode = LineChartRenderMode.Morph,
    animationDurationMillis: Int = 420,
    isDenseMorphMode: Boolean = false,
    scrollState: ScrollState,
    zoomScale: Float = 1f,
    selectedPointIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
    valueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
) {
    LineChartContent(
        data = data,
        style = style,
        colors = colors,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        renderMode = renderMode,
        animationDurationMillis = animationDurationMillis,
        isDenseMorphMode = isDenseMorphMode,
        scrollState = scrollState,
        zoomScale = zoomScale,
        selectedPointIndex = selectedPointIndex,
        onValueChanged = onValueChanged,
        valueFormatter = valueFormatter,
        axisValueFormatter = axisValueFormatter,
    )
}
