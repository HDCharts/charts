package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class StackedBarSegmentStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val alpha: Float,
) {
    constructor(color: Color, colors: List<Color>, alpha: Float) : this(color, colors.toImmutableList(), alpha)
}

@Immutable
data class StackedBarLayoutStyle(
    val space: Dp,
    val minBarWidth: Dp,
)

@Immutable
data class StackedBarAxisStyle(
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

@Immutable
data class StackedBarSelectionStyle(
    val visible: Boolean,
    val color: Color,
    val width: Dp,
)

@Immutable
class StackedBarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val segments: StackedBarSegmentStyle,
    val layout: StackedBarLayoutStyle,
    val axis: StackedBarAxisStyle,
    val selection: StackedBarSelectionStyle,
    val zoomControlsVisible: Boolean,
)

object StackedBarChartDefaults {
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        segments: StackedBarSegmentStyle = segments(),
        layout: StackedBarLayoutStyle = layout(),
        axis: StackedBarAxisStyle = axis(),
        selection: StackedBarSelectionStyle = selection(),
        zoomControlsVisible: Boolean = true,
    ): StackedBarChartStyle =
        StackedBarChartStyle(chartContainerStyle, segments, layout, axis, selection, zoomControlsVisible)

    @Composable
    fun segments(
        color: Color = MaterialTheme.colorScheme.primary,
        colors: List<Color> = emptyList(),
        alpha: Float = defaultChartAlpha(),
    ): StackedBarSegmentStyle = StackedBarSegmentStyle(color, colors, alpha.coerceIn(0f, 1f))

    @Composable
    fun layout(
        space: Dp = 10.dp,
        minBarWidth: Dp = 10.dp,
    ): StackedBarLayoutStyle = StackedBarLayoutStyle(space, minBarWidth)

    @Composable
    fun axis(
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ): StackedBarAxisStyle = StackedBarAxisStyle(xLabels, yLabels)

    @Composable
    fun xLabels(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        size: TextUnit = 11.sp,
        count: Int = 6,
    ): AxisLabelStyle = AxisLabelStyle(visible, color, size, count)

    @Composable
    fun yLabels(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        size: TextUnit = 11.sp,
        count: Int = 5,
    ): AxisLabelStyle = AxisLabelStyle(visible, color, size, count)

    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        width: Dp = 1.dp,
    ): StackedBarSelectionStyle = StackedBarSelectionStyle(visible, color, width)
}
