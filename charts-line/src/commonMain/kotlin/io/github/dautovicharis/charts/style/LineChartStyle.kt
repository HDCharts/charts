package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.ChartValueFormatters
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class LineVisualStyle(
    val color: Color,
    val alpha: Float,
    val colors: ImmutableList<Color>,
    val strokeWidth: Dp,
    val bezier: Boolean,
) {
    constructor(
        color: Color,
        alpha: Float,
        colors: List<Color>,
        strokeWidth: Dp,
        bezier: Boolean,
    ) : this(color, alpha, colors.toImmutableList(), strokeWidth, bezier)
}

@Immutable
data class LinePointStyle(
    val color: Color,
    val size: Dp,
    val visible: Boolean,
)

@Immutable
data class LineSelectionStyle(
    val color: Color,
    val size: Dp,
    val activeSize: Dp,
    val visible: Boolean,
)

@Immutable
data class LineAxisStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Dp,
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

/** Grouped, Compose-friendly v3 style for single- and multi-line charts. */
@Immutable
class LineChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val line: LineVisualStyle,
    val points: LinePointStyle,
    val selection: LineSelectionStyle,
    val axis: LineAxisStyle,
    val zoomControlsVisible: Boolean,
)

object LineChartDefaults {
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        line: LineVisualStyle = line(),
        points: LinePointStyle = points(),
        selection: LineSelectionStyle = selection(),
        axis: LineAxisStyle = axis(),
        zoomControlsVisible: Boolean = true,
    ): LineChartStyle = LineChartStyle(chartContainerStyle, line, points, selection, axis, zoomControlsVisible)

    @Composable
    fun line(
        color: Color = MaterialTheme.colorScheme.primary,
        alpha: Float = defaultChartAlpha(),
        colors: List<Color> = emptyList(),
        strokeWidth: Dp = 5.dp,
        bezier: Boolean = true,
    ): LineVisualStyle = LineVisualStyle(color, alpha.coerceIn(0f, 1f), colors, strokeWidth, bezier)

    @Composable
    fun points(
        color: Color = MaterialTheme.colorScheme.tertiary,
        size: Dp = 9.dp,
        visible: Boolean = false,
    ): LinePointStyle = LinePointStyle(color, size, visible)

    @Composable
    fun selection(
        color: Color = MaterialTheme.colorScheme.tertiary,
        size: Dp = 7.dp,
        activeSize: Dp = 12.dp,
        visible: Boolean = true,
    ): LineSelectionStyle = LineSelectionStyle(color, size, activeSize, visible)

    @Composable
    fun axis(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        lineWidth: Dp = 1.dp,
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ): LineAxisStyle = LineAxisStyle(visible, color, lineWidth, xLabels, yLabels)

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

    val valueFormatter: ChartValueFormatter = ChartValueFormatters.Default
    val axisValueFormatter: ChartValueFormatter =
        ChartValueFormatter { value ->
            ChartValueFormatters.Default.format(value).removeSuffix(".0")
        }
}
