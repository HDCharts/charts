package io.github.hdcharts.radar.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.AnimationSpec
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.composable.rememberShowState
import io.github.hdcharts.core.internal.layout.fillMaxSizeChartModifier
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.internal.model.minMax
import io.github.hdcharts.core.internal.model.normalizeByMinMax
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.radar.RadarChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val SERIES_TOUCH_RADIUS = 24.dp

@Composable
internal fun RadarChartContent(
    data: MultiChartData,
    style: RadarChartStyle,
    colors: ImmutableList<Color>,
    axisLabels: ImmutableList<String> = persistentListOf(),
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedAxisIndex: Int = NO_SELECTION,
    focusedSeriesIndex: Int = NO_SELECTION,
    onValueChanged: (Int) -> Unit = {},
    onFocusedSeriesChanged: ((Int) -> Unit)? = null,
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

    LaunchedEffect(forcedSelectedIndex) {
        selectedIndex.intValue = forcedSelectedIndex
    }

    BoxWithConstraints(modifier = fillMaxSizeChartModifier(style.chartContainerStyle)) {
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
        val showAxisLabels = style.axes.labelVisible && axisLabels.isNotEmpty()
        val textMeasurer = rememberTextMeasurer()
        // The web shrinks so the labels fit outside it, so it needs their measured size.
        val labelSizePx =
            remember(showAxisLabels, axisLabels, style.axes.labelSize, textMeasurer, density) {
                if (!showAxisLabels) {
                    IntSize.Zero
                } else {
                    val sizes =
                        axisLabels.map { label ->
                            textMeasurer.measure(text = label, style = TextStyle(fontSize = style.axes.labelSize)).size
                        }
                    IntSize(width = sizes.maxOf { it.width }, height = sizes.maxOf { it.height })
                }
            }
        val radius =
            remember(widthPx, heightPx, labelSizePx, style.axes.labelPadding, density) {
                radarPlotRadius(
                    axisCount = axisCount,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    labelWidthPx = labelSizePx.width.toFloat(),
                    labelHeightPx = labelSizePx.height.toFloat(),
                    labelPaddingPx = with(density) { style.axes.labelPadding.toPx() },
                )
            }

        val labelRadius =
            remember(radius, style.axes.labelPadding, density) {
                radius + with(density) { style.axes.labelPadding.toPx() }
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
            val touchRadiusPx = with(density) { SERIES_TOUCH_RADIUS.toPx() }
            val currentFocusedSeriesIndex by rememberUpdatedState(focusedSeriesIndex)
            // A tap never selects: it clears the axis a drag left behind, then focuses a series when
            // the chart has several. That way touch alone can always return the chart to rest.
            val tapModifier =
                if (interactionEnabled) {
                    Modifier.pointerInput(targetNormalized, center, radius, touchRadiusPx) {
                        detectTapGestures { offset ->
                            onValueChanged(NO_SELECTION)
                            if (onFocusedSeriesChanged != null && data.items.size > 1) {
                                val polygons =
                                    radarPolygons(
                                        normalizedValues = targetNormalized,
                                        axisCount = axisCount,
                                        center = center,
                                        radius = radius,
                                    )
                                val candidates =
                                    seriesCandidatesAt(
                                        tap = offset,
                                        polygons = polygons,
                                        touchRadius = touchRadiusPx,
                                    )
                                onFocusedSeriesChanged(
                                    nextFocusedSeries(
                                        candidates = candidates,
                                        focusedIndex = currentFocusedSeriesIndex,
                                    ),
                                )
                            }
                        }
                    }
                } else {
                    Modifier
                }
            val interactionModifier =
                if (interactionEnabled) {
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
                            },
                            onDragCancel = {
                                dragging = false
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
                        .then(tapModifier)
                        .then(interactionModifier),
            ) {
                drawRadar(
                    data = data,
                    style = style,
                    colors = colors,
                    axisCount = axisCount,
                    center = center,
                    radius = radius,
                    normalizedValues =
                        animatedValues.map { series ->
                            series.map { it.value }
                        },
                    dragging = dragging || hasForcedSelection,
                    selectedIndex = effectiveSelectedIndex,
                    focusedSeriesIndex = focusedSeriesIndex,
                )
            }

            if (showAxisLabels) {
                val selectionActive = (dragging || hasForcedSelection) && style.selection.visible
                RadarAxisLabels(
                    labels = axisLabels,
                    labelPositions = labelPositions,
                    center = center,
                    color = style.axes.labelColor,
                    fontSize = style.axes.labelSize,
                    edgePadding = StyleDefaults.radarLabelEdgePadding,
                    selectedIndex = if (selectionActive) effectiveSelectedIndex else NO_SELECTION,
                    unselectedAlpha = style.selection.unselectedAlpha,
                )
            }
        }
    }
}

private fun DrawScope.drawRadar(
    data: MultiChartData,
    style: RadarChartStyle,
    colors: ImmutableList<Color>,
    axisCount: Int,
    center: Offset,
    radius: Float,
    normalizedValues: List<List<Float>>,
    dragging: Boolean,
    selectedIndex: Int,
    focusedSeriesIndex: Int,
) {
    if (axisCount <= 0) return

    val frame = RadarFrame.of(axisCount)
    if (style.grid.visible && style.grid.steps > 0) {
        drawGrid(
            axisCount = axisCount,
            center = center,
            radius = radius,
            steps = style.grid.steps,
            frame = frame,
            color = style.grid.color,
            strokeWidth = style.grid.lineWidth.toPx(),
        )
    }

    if (style.axes.visible) {
        drawAxes(
            axisCount = axisCount,
            center = center,
            radius = radius,
            frame = frame,
            color = style.axes.lineColor,
            strokeWidth = style.axes.lineWidth.toPx(),
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
            val lineColor = colors.getOrNull(index) ?: style.polygon.lineColor
            scaledValues to lineColor
        }

    val showSelection = style.selection.visible && dragging && selectedIndex in 0 until axisCount
    val shownSelectedIndex = if (showSelection) selectedIndex else NO_SELECTION
    val unselectedAlpha = if (showSelection) style.selection.unselectedAlpha else 1f

    // The focused series draws last, so it sits on top of the dimmed ones.
    val drawOrder = seriesValues.indices.sortedBy { it == focusedSeriesIndex }
    val seriesAlphas =
        seriesValues.indices.map { index ->
            val unfocused =
                style.selection.visible && focusedSeriesIndex != NO_SELECTION && index != focusedSeriesIndex
            if (unfocused) style.selection.unfocusedSeriesAlpha else 1f
        }

    drawOrder.forEach { index ->
        val (values, lineColor) = seriesValues[index]
        val seriesAlpha = seriesAlphas[index]
        val path =
            buildPolygonPath(
                values = values,
                center = center,
                frame = frame,
            )

        if (style.polygon.fillVisible) {
            drawPath(
                path = path,
                color = lineColor.copy(alpha = style.polygon.fillAlpha * seriesAlpha),
            )
        }

        if (style.polygon.lineWidth > 0.dp) {
            drawPath(
                path = path,
                color = lineColor.copy(alpha = lineColor.alpha * seriesAlpha),
                style = Stroke(width = style.polygon.lineWidth.toPx()),
            )
        }
    }

    // Hidden points still draw the selected axis, like the selected point on a line chart.
    if (style.points.visible || shownSelectedIndex != NO_SELECTION) {
        drawOrder.forEach { index ->
            val (values, lineColor) = seriesValues[index]
            val pointColor = if (style.points.colorSameAsLine) lineColor else style.points.color
            drawPoints(
                values = values,
                center = center,
                frame = frame,
                pointSize = style.points.size.toPx(),
                selectedPointSize = style.selection.pointSize.toPx(),
                pointColor = pointColor.copy(alpha = pointColor.alpha * seriesAlphas[index]),
                selectedIndex = shownSelectedIndex,
                unselectedAlpha = unselectedAlpha,
                onlySelected = !style.points.visible,
            )
        }
    }
}

private fun DrawScope.drawGrid(
    axisCount: Int,
    center: Offset,
    radius: Float,
    steps: Int,
    frame: RadarFrame,
    color: Color,
    strokeWidth: Float,
) {
    for (step in 1..steps) {
        val r = radius * (step / steps.toFloat())
        val path =
            buildPolygonPath(
                values = List(axisCount) { r },
                center = center,
                frame = frame,
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
    frame: RadarFrame,
    color: Color,
    strokeWidth: Float,
) {
    repeat(axisCount) { index ->
        val angle = frame.angleAt(index)
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
    frame: RadarFrame,
    pointSize: Float,
    selectedPointSize: Float,
    pointColor: Color,
    selectedIndex: Int,
    unselectedAlpha: Float,
    onlySelected: Boolean,
) {
    values.forEachIndexed { index, value ->
        if (onlySelected && index != selectedIndex) return@forEachIndexed
        val angle = frame.angleAt(index)
        val point =
            Offset(
                x = center.x + cos(angle) * value,
                y = center.y + sin(angle) * value,
            )
        val isSelected = selectedIndex == index
        val dimmed = selectedIndex != NO_SELECTION && !isSelected
        val radius = if (isSelected) selectedPointSize else pointSize
        drawCircle(
            color = if (dimmed) pointColor.copy(alpha = pointColor.alpha * unselectedAlpha) else pointColor,
            radius = radius,
            center = point,
        )
    }
}

private fun buildPolygonPath(
    values: List<Float>,
    center: Offset,
    frame: RadarFrame,
): Path {
    val path = Path()
    values.forEachIndexed { index, value ->
        val angle = frame.angleAt(index)
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
    center: Offset,
    selectedIndex: Int,
    unselectedAlpha: Float,
    edgePadding: Dp,
) {
    val edgePaddingPx = with(LocalDensity.current) { edgePadding.toPx() }.roundToInt()
    androidx.compose.ui.layout.Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            labels.forEachIndexed { index, label ->
                val dimmed = selectedIndex != NO_SELECTION && index != selectedIndex
                androidx.compose.material3.Text(
                    text = label,
                    style =
                        TextStyle(
                            color = if (dimmed) color.copy(alpha = color.alpha * unselectedAlpha) else color,
                            fontSize = fontSize,
                        ),
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
                val topLeft =
                    axisLabelTopLeft(
                        anchor = position,
                        center = center,
                        widthPx = placeable.width,
                        heightPx = placeable.height,
                    )
                val rawX = topLeft.x.roundToInt()
                val rawY = topLeft.y.roundToInt()

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
