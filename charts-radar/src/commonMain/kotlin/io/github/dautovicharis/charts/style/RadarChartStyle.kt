package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class RadarGridStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Float,
    val steps: Int,
)

@Immutable
data class RadarAxesStyle(
    val visible: Boolean,
    val lineColor: Color,
    val lineWidth: Float,
    val labelColor: Color,
    val labelSize: TextUnit,
    val labelPadding: Float,
    val labelVisible: Boolean,
)

@Immutable
data class RadarPolygonStyle(
    val fillVisible: Boolean,
    val fillAlpha: Float,
    val lineColor: Color,
    val lineColors: ImmutableList<Color>,
    val lineWidth: Float,
) {
    constructor(
        fillVisible: Boolean,
        fillAlpha: Float,
        lineColor: Color,
        lineColors: List<Color>,
        lineWidth: Float,
    ) : this(
        fillVisible = fillVisible,
        fillAlpha = fillAlpha.coerceIn(0f, 1f),
        lineColor = lineColor,
        lineColors = lineColors.toImmutableList(),
        lineWidth = lineWidth,
    )
}

@Immutable
data class RadarPointStyle(
    val visible: Boolean,
    val color: Color,
    val colorSameAsLine: Boolean,
    val size: Float,
)

@Immutable
data class RadarCategoryStyle(
    val legendVisible: Boolean,
    val pinsVisible: Boolean,
    val colors: ImmutableList<Color>,
    val pinSize: Float,
)

@Immutable
class RadarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val grid: RadarGridStyle,
    val axes: RadarAxesStyle,
    val polygon: RadarPolygonStyle,
    val points: RadarPointStyle,
    val categories: RadarCategoryStyle,
)

object RadarChartDefaults {
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        grid: RadarGridStyle = grid(),
        axes: RadarAxesStyle = axes(),
        polygon: RadarPolygonStyle = polygon(),
        points: RadarPointStyle = points(),
        categories: RadarCategoryStyle = categories(),
    ): RadarChartStyle =
        RadarChartStyle(
            chartContainerStyle = chartContainerStyle,
            grid = grid,
            axes = axes,
            polygon = polygon,
            points = points,
            categories = categories,
        )

    @Composable
    fun grid(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
        lineWidth: Float = 1f,
        steps: Int = 5,
    ): RadarGridStyle = RadarGridStyle(visible, color, lineWidth, steps)

    @Composable
    fun axes(
        visible: Boolean = true,
        lineColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        lineWidth: Float = 1f,
        labelColor: Color = MaterialTheme.colorScheme.onSurface,
        labelSize: TextUnit = 12.sp,
        labelPadding: Float = 8f,
        labelVisible: Boolean = false,
    ): RadarAxesStyle =
        RadarAxesStyle(
            visible = visible,
            lineColor = lineColor,
            lineWidth = lineWidth,
            labelColor = labelColor,
            labelSize = labelSize,
            labelPadding = labelPadding,
            labelVisible = labelVisible,
        )

    @Composable
    fun polygon(
        fillVisible: Boolean = true,
        fillAlpha: Float = defaultChartAlpha(light = 0.25f, dark = 0.2f),
        lineColor: Color = MaterialTheme.colorScheme.primary,
        lineColors: List<Color> = emptyList(),
        lineWidth: Float = 3f,
    ): RadarPolygonStyle = RadarPolygonStyle(fillVisible, fillAlpha, lineColor, lineColors, lineWidth)

    @Composable
    fun points(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.tertiary,
        colorSameAsLine: Boolean = true,
        size: Float = 9f,
    ): RadarPointStyle = RadarPointStyle(visible, color, colorSameAsLine, size)

    @Composable
    fun categories(
        legendVisible: Boolean = true,
        pinsVisible: Boolean = true,
        colors: List<Color> = emptyList(),
        pinSize: Float = 6.3f,
    ): RadarCategoryStyle = RadarCategoryStyle(legendVisible, pinsVisible, colors.toImmutableList(), pinSize)
}
