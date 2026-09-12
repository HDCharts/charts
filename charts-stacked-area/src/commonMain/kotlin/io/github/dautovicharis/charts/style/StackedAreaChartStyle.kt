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
data class StackedAreaFillStyle(
    val color: Color,
    val colors: ImmutableList<Color>,
    val alpha: Float,
) {
    constructor(color: Color, colors: List<Color>, alpha: Float) : this(color, colors.toImmutableList(), alpha)
}

@Immutable
data class StackedAreaBoundaryStyle(
    val visible: Boolean,
    val color: Color,
    val colors: ImmutableList<Color>,
    val width: Dp,
    val bezier: Boolean,
) {
    constructor(
        visible: Boolean,
        color: Color,
        colors: List<Color>,
        width: Dp,
        bezier: Boolean,
    ) : this(visible, color, colors.toImmutableList(), width, bezier)
}

@Immutable
data class StackedAreaAxisStyle(
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

@Immutable
data class StackedAreaSelectionStyle(
    val visible: Boolean,
    val color: Color,
    val width: Dp,
)

@Immutable
class StackedAreaChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val fill: StackedAreaFillStyle,
    val boundary: StackedAreaBoundaryStyle,
    val axis: StackedAreaAxisStyle,
    val selection: StackedAreaSelectionStyle,
    val zoomControlsVisible: Boolean,
)

object StackedAreaChartDefaults {
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        fill: StackedAreaFillStyle = fill(),
        boundary: StackedAreaBoundaryStyle = boundary(),
        axis: StackedAreaAxisStyle = axis(),
        selection: StackedAreaSelectionStyle = selection(),
        zoomControlsVisible: Boolean = true,
    ): StackedAreaChartStyle =
        StackedAreaChartStyle(
            chartContainerStyle = chartContainerStyle,
            fill = fill,
            boundary = boundary,
            axis = axis,
            selection = selection,
            zoomControlsVisible = zoomControlsVisible,
        )

    @Composable
    fun fill(
        color: Color = MaterialTheme.colorScheme.primary,
        colors: List<Color> = emptyList(),
        alpha: Float = defaultChartAlpha(),
    ): StackedAreaFillStyle = StackedAreaFillStyle(color, colors, alpha.coerceIn(0f, 1f))

    @Composable
    fun boundary(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.primary,
        colors: List<Color> = emptyList(),
        width: Dp = 1.dp,
        bezier: Boolean = false,
    ): StackedAreaBoundaryStyle = StackedAreaBoundaryStyle(visible, color, colors, width.coerceAtLeast(0.dp), bezier)

    @Composable
    fun axis(
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ): StackedAreaAxisStyle = StackedAreaAxisStyle(xLabels, yLabels)

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
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        width: Dp = 1.dp,
    ): StackedAreaSelectionStyle = StackedAreaSelectionStyle(visible, color, width.coerceAtLeast(0.dp))
}
