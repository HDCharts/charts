package io.github.hdcharts.charts.internal.common.axis

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.InternalChartsApi
import kotlin.math.roundToInt

@InternalChartsApi
data class AxisXLayoutTick(
    val label: String,
    val centerX: Float,
)

@InternalChartsApi
data class AxisYLayoutTick(
    val label: String,
    val centerY: Float,
)

@Composable
@InternalChartsApi
fun AxisXLabelsLayout(
    ticks: List<AxisXLayoutTick>,
    color: Color,
    fontSize: TextUnit,
    tiltDegrees: Float,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            ticks.forEach { tick ->
                Text(
                    text = tick.label,
                    style = TextStyle(color = color, fontSize = fontSize),
                    maxLines = 1,
                    modifier =
                        if (tiltDegrees <= 0f) {
                            Modifier
                        } else {
                            Modifier.graphicsLayer {
                                rotationZ = -tiltDegrees
                                transformOrigin = TransformOrigin(0f, 1f)
                            }
                        },
                )
            }
        },
    ) { measurables, constraints ->
        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    Constraints(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val tick = ticks.getOrNull(index) ?: return@forEachIndexed
                if (!tick.centerX.isFinite()) return@forEachIndexed
                if (tick.centerX < 0f || tick.centerX > constraints.maxWidth.toFloat()) return@forEachIndexed

                val rawX = (tick.centerX - placeable.width / 2f).roundToInt()
                if (rawX + placeable.width <= 0 || rawX >= constraints.maxWidth) return@forEachIndexed
                val y = (constraints.maxHeight - placeable.height).coerceAtLeast(0)
                placeable.place(rawX, y)
            }
        }
    }
}

@Composable
@InternalChartsApi
fun AxisYLabelsLayout(
    ticks: List<AxisYLayoutTick>,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            ticks.forEach { tick ->
                Text(
                    text = tick.label,
                    style = TextStyle(color = color, fontSize = fontSize),
                    maxLines = 1,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    Constraints(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            }
        val edgePadding = 4.dp.roundToPx()

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val tick = ticks.getOrNull(index) ?: return@forEachIndexed
                if (!tick.centerY.isFinite()) return@forEachIndexed
                if (tick.centerY < 0f || tick.centerY > constraints.maxHeight.toFloat()) return@forEachIndexed

                val rawY = (tick.centerY - placeable.height / 2f).roundToInt()
                val maxY = (constraints.maxHeight - placeable.height).coerceAtLeast(0)
                val y = rawY.coerceIn(0, maxY)
                val x = (constraints.maxWidth - placeable.width - edgePadding).coerceAtLeast(0)
                placeable.place(x, y)
            }
        }
    }
}
