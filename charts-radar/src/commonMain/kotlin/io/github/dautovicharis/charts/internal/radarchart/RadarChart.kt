package io.github.dautovicharis.charts.internal.radarchart

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.internal.AnimationSpec
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.composable.rememberShowState
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.model.minMax
import io.github.dautovicharis.charts.internal.common.model.normalizeByMinMax
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun RadarChart(
    data: MultiChartData,
    style: RadarInternalStyle,
    colors: ImmutableList<Color>,
    categoryColors: ImmutableList<Color>,
    axisLabels: ImmutableList<String> = persistentListOf(),
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedAxisIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
) {
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    var dragging by remember { mutableStateOf(false) }
    val selectedIndex = remember { mutableIntStateOf(NO_SELECTION) }
    val radarAnimationSpec = remember { AnimationSpec.radarChart() }
    val hasInitialized = remember { mutableStateOf(false) }

    val axisCount = remember(data) { data.getFirstPointsSize() }
    val forcedSelectedIndex =
        selectedAxisIndex.takeIf { it in 0 until axisCount } ?: NO_SELECTION
    val hasForcedSelection = forcedSelectedIndex != NO_SELECTION
    val effectiveSelectedIndex =
        when (forcedSelectedIndex) {
            NO_SELECTION -> selectedIndex.intValue
            else -> forcedSelectedIndex
        }

    BoxWithConstraints(modifier = style.chartContainerStyle.fillMaxSizeChartModifier()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val minMax = remember(data) { data.minMax() }
        val targetNormalized = remember(data, minMax) { data.normalizeByMinMax(minMax, 1f) }
        val initialValues =
            remember(axisCount, data.items.size, isPreview, animateOnStart) {
                if (isPreview || !animateOnStart) targetNormalized else null
            }
        val animatedValues =
            remember(axisCount, data.items.size, isPreview, animateOnStart) {
                List(data.items.size) { seriesIndex ->
                    List(axisCount) { pointIndex ->
                        Animatable(initialValues?.getOrNull(seriesIndex)?.getOrNull(pointIndex) ?: 0f)
                    }
                }
            }

        LaunchedEffect(show, targetNormalized) {
            if (axisCount <= 0 || data.items.isEmpty()) return@LaunchedEffect
            if (!show && !isPreview) {
                animatedValues.forEach { series ->
                    series.forEach { animatable ->
                        animatable.snapTo(0f)
                    }
                }
                hasInitialized.value = false
                return@LaunchedEffect
            }

            coroutineScope {
                animatedValues.forEachIndexed { seriesIndex, series ->
                    val targetSeries = targetNormalized.getOrNull(seriesIndex) ?: emptyList()
                    series.forEachIndexed { pointIndex, animatable ->
                        val target = targetSeries.getOrNull(pointIndex) ?: 0f
                        launch {
                            val shouldAnimate = !isPreview && (animateOnStart || hasInitialized.value)
                            if (!shouldAnimate) {
                                animatable.snapTo(target)
                            } else {
                                animatable.animateTo(
                                    targetValue = target,
                                    animationSpec = radarAnimationSpec,
                                )
                            }
                        }
                    }
                }
            }
            hasInitialized.value = true
        }

        val center =
            remember(widthPx, heightPx) {
                Offset(x = widthPx / 2f, y = heightPx / 2f)
            }
        val radius =
            remember(widthPx, heightPx) {
                min(widthPx, heightPx) / 2f
            }

        val labelRadius =
            remember(radius, style.axisLabelPadding) {
                radius + style.axisLabelPadding
            }

        val labelPositions =
            remember(axisCount, center, labelRadius) {
                buildAxisLabelPositions(
                    axisCount = axisCount,
                    center = center,
                    radius = labelRadius,
                )
            }

        Box(modifier = Modifier.fillMaxSize()) {
            val interactionModifier =
                if (interactionEnabled && !hasForcedSelection) {
                    Modifier.pointerInput(axisCount) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragging = true
                                selectedIndex.intValue =
                                    axisIndexForOffset(offset, size, axisCount)
                                onValueChanged(selectedIndex.intValue)
                            },
                            onDrag = { change, _ ->
                                selectedIndex.intValue =
                                    axisIndexForOffset(change.position, size, axisCount)
                                onValueChanged(selectedIndex.intValue)
                                change.consume()
                            },
                            onDragEnd = {
                                dragging = false
                                selectedIndex.intValue = NO_SELECTION
                                onValueChanged(NO_SELECTION)
                            },
                            onDragCancel = {
                                dragging = false
                                selectedIndex.intValue = NO_SELECTION
                                onValueChanged(NO_SELECTION)
                            },
                        )
                    }
                } else {
                    Modifier
                }

            Canvas(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .testTag(TestTags.RADAR_CHART)
                        .onGloballyPositioned { show = true }
                        .then(interactionModifier),
            ) {
                drawRadar(
                    data = data,
                    style = style,
                    colors = colors,
                    categoryColors = categoryColors,
                    axisCount = axisCount,
                    center = center,
                    radius = radius,
                    normalizedValues =
                        animatedValues.map { series ->
                            series.map { it.value }
                        },
                    dragging = dragging || hasForcedSelection,
                    selectedIndex = effectiveSelectedIndex,
                )
            }

            if (style.axisLabelVisible && axisLabels.isNotEmpty()) {
                RadarAxisLabels(
                    labels = axisLabels,
                    labelPositions = labelPositions,
                    color = style.axisLabelColor,
                    fontSize = style.axisLabelSize,
                )
            }
        }
    }
}

private fun DrawScope.drawRadar(
    data: MultiChartData,
    style: RadarInternalStyle,
    colors: ImmutableList<Color>,
    categoryColors: ImmutableList<Color>,
    axisCount: Int,
    center: Offset,
    radius: Float,
    normalizedValues: List<List<Float>>,
    dragging: Boolean,
    selectedIndex: Int,
) {
    if (axisCount <= 0) return

    val startAngle = (-PI / 2f).toFloat()
    val angleStep = (2f * PI / axisCount).toFloat()
    if (style.gridVisible && style.gridSteps > 0) {
        drawGrid(
            axisCount = axisCount,
            center = center,
            radius = radius,
            steps = style.gridSteps,
            startAngle = startAngle,
            angleStep = angleStep,
            color = style.gridColor,
            strokeWidth = style.gridLineWidth,
        )
    }

    if (style.axisVisible) {
        drawAxes(
            axisCount = axisCount,
            center = center,
            radius = radius,
            startAngle = startAngle,
            angleStep = angleStep,
            color = style.axisLineColor,
            strokeWidth = style.axisLineWidth,
        )
    }

    val seriesValues =
        data.items.mapIndexed { index, _ ->
            val seriesNormalized = normalizedValues.getOrNull(index)
            val scaledValues =
                List(axisCount) { pointIndex ->
                    val normalized = seriesNormalized?.getOrNull(pointIndex) ?: 0f
                    normalized * radius
                }
            val lineColor = colors.getOrNull(index) ?: style.lineColor
            scaledValues to lineColor
        }

    seriesValues.forEach { (values, lineColor) ->
        val path =
            buildPolygonPath(
                values = values,
                center = center,
                startAngle = startAngle,
                angleStep = angleStep,
            )

        if (style.fillVisible) {
            drawPath(
                path = path,
                color = lineColor.copy(alpha = style.fillAlpha),
            )
        }

        if (style.lineWidth > 0f) {
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = style.lineWidth),
            )
        }
    }

    if (style.pointVisible) {
        seriesValues.forEach { (values, lineColor) ->
            drawPoints(
                values = values,
                center = center,
                startAngle = startAngle,
                angleStep = angleStep,
                pointSize = style.pointSize,
                pointColor =
                    when (style.pointColorSameAsLine) {
                        true -> lineColor
                        else -> style.pointColor
                    },
                dragging = dragging,
                selectedIndex = selectedIndex,
            )
        }
    }

    if (style.categoryPinsVisible && categoryColors.isNotEmpty()) {
        drawCategoryPins(
            axisCount = axisCount,
            center = center,
            radius = radius,
            startAngle = startAngle,
            angleStep = angleStep,
            colors = categoryColors,
            pinSize = style.categoryPinSize,
            dragging = dragging,
            selectedIndex = selectedIndex,
        )
    }
}

private fun DrawScope.drawCategoryPins(
    axisCount: Int,
    center: Offset,
    radius: Float,
    startAngle: Float,
    angleStep: Float,
    colors: ImmutableList<Color>,
    pinSize: Float,
    dragging: Boolean,
    selectedIndex: Int,
) {
    repeat(axisCount) { index ->
        val angle = startAngle + angleStep * index
        val point =
            Offset(
                x = center.x + cos(angle) * radius,
                y = center.y + sin(angle) * radius,
            )
        val color = colors.getOrNull(index) ?: colors.firstOrNull() ?: Color.Unspecified
        val radiusSize =
            if (dragging && selectedIndex == index) {
                pinSize * 1.5f
            } else {
                pinSize
            }
        drawCircle(
            color = color,
            radius = radiusSize,
            center = point,
        )
    }
}

private fun DrawScope.drawGrid(
    axisCount: Int,
    center: Offset,
    radius: Float,
    steps: Int,
    startAngle: Float,
    angleStep: Float,
    color: Color,
    strokeWidth: Float,
) {
    for (step in 1..steps) {
        val r = radius * (step / steps.toFloat())
        val path =
            buildPolygonPath(
                values = List(axisCount) { r },
                center = center,
                startAngle = startAngle,
                angleStep = angleStep,
            )
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth),
        )
    }
}

private fun DrawScope.drawAxes(
    axisCount: Int,
    center: Offset,
    radius: Float,
    startAngle: Float,
    angleStep: Float,
    color: Color,
    strokeWidth: Float,
) {
    repeat(axisCount) { index ->
        val angle = startAngle + angleStep * index
        val end =
            Offset(
                x = center.x + cos(angle) * radius,
                y = center.y + sin(angle) * radius,
            )
        drawLine(
            color = color,
            start = center,
            end = end,
            strokeWidth = strokeWidth,
        )
    }
}

private fun DrawScope.drawPoints(
    values: List<Float>,
    center: Offset,
    startAngle: Float,
    angleStep: Float,
    pointSize: Float,
    pointColor: Color,
    dragging: Boolean,
    selectedIndex: Int,
) {
    values.forEachIndexed { index, value ->
        val angle = startAngle + angleStep * index
        val point =
            Offset(
                x = center.x + cos(angle) * value,
                y = center.y + sin(angle) * value,
            )
        val radius =
            if (dragging && selectedIndex == index) {
                pointSize * 1.6f
            } else {
                pointSize
            }
        drawCircle(
            color = pointColor,
            radius = radius,
            center = point,
        )
    }
}

private fun buildPolygonPath(
    values: List<Float>,
    center: Offset,
    startAngle: Float,
    angleStep: Float,
): Path {
    val path = Path()
    values.forEachIndexed { index, value ->
        val angle = startAngle + angleStep * index
        val point =
            Offset(
                x = center.x + cos(angle) * value,
                y = center.y + sin(angle) * value,
            )
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }
    path.close()
    return path
}

@Composable
private fun RadarAxisLabels(
    labels: ImmutableList<String>,
    labelPositions: List<Offset>,
    color: Color,
    fontSize: TextUnit,
    edgePadding: Dp = 6.dp,
) {
    val edgePaddingPx = with(LocalDensity.current) { edgePadding.toPx() }.roundToInt()
    androidx.compose.ui.layout.Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            labels.forEach { label ->
                androidx.compose.material3.Text(
                    text = label,
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

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val position = labelPositions.getOrNull(index) ?: return@forEachIndexed
                val rawX = (position.x - placeable.width / 2f).roundToInt()
                val rawY = (position.y - placeable.height / 2f).roundToInt()

                val maxX =
                    (constraints.maxWidth - placeable.width - edgePaddingPx)
                        .coerceAtLeast(edgePaddingPx)
                val maxY =
                    (constraints.maxHeight - placeable.height - edgePaddingPx)
                        .coerceAtLeast(edgePaddingPx)

                val clampedX = rawX.coerceIn(edgePaddingPx, maxX)
                val clampedY = rawY.coerceIn(edgePaddingPx, maxY)
                placeable.place(clampedX, clampedY)
            }
        }
    }
}
