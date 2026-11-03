package io.github.hdcharts.scatter.internal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import io.github.hdcharts.core.internal.AnimationSpec
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.axis.AxisXItems
import io.github.hdcharts.core.internal.axis.AxisXLabelsLayout
import io.github.hdcharts.core.internal.axis.AxisYLabelsLayout
import io.github.hdcharts.core.internal.axis.estimateXAxisLabelExtent
import io.github.hdcharts.core.internal.axis.rememberNumericYAxisLayout
import io.github.hdcharts.core.internal.axis.rememberXAxisLabelPlan
import io.github.hdcharts.core.internal.axis.xAxisLabelEdgeInsetPx
import io.github.hdcharts.core.internal.axis.xAxisLabelRowHeightPx
import io.github.hdcharts.core.internal.composable.ChartHeading
import io.github.hdcharts.core.internal.composable.ChartPlotLayout
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.composable.rememberShowState
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.scatter.ScatterChartStyle
import io.github.hdcharts.scatter.ScatterData
import kotlinx.collections.immutable.toImmutableList

internal const val SCATTER_CHART_TAG = "ScatterChart"
internal const val SCATTER_CHART_X_AXIS_LABELS_TAG = "ScatterChartXAxisLabels"
internal const val SCATTER_CHART_Y_AXIS_LABELS_TAG = "ScatterChartYAxisLabels"

@Composable
internal fun ScatterChartContent(
    data: ScatterData,
    modifier: Modifier,
    style: ScatterChartStyle,
    title: String?,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    xValueFormatter: ChartValueFormatter,
    yValueFormatter: ChartValueFormatter,
    sizeValueFormatter: ChartValueFormatter,
) {
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    val pointAnimation by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = AnimationSpec.barChartSmooth(),
        label = "scatterPointReveal",
    )
    val points = remember(data) { data.series.flatMap { it.points } }
    val selectedIndex = selection.selectedIndex?.takeIf { it in points.indices } ?: NO_SELECTION
    val selected = points.getOrNull(selectedIndex)
    val selectedSeriesIndex =
        remember(data, selectedIndex) {
            scatterSeriesIndexForPointIndex(data.series.map { it.points.size }, selectedIndex)
        }
    val chartTitle =
        buildString {
            if (!title.isNullOrBlank()) append(title)
            if (selected != null) {
                if (isNotEmpty()) append(" · ")
                append("X: ${xValueFormatter.format(selected.x)}, Y: ${yValueFormatter.format(selected.y)}")
                selected.size?.let { append(", Size: ${sizeValueFormatter.format(it)}") }
            }
        }
    val colors = remember(style.points, data.series.size) { style.points.resolveColors(data.series.size) }
    val domain = remember(points, style.range) { scatterDomain(points, style.range) }
    val positions = remember(points, domain, style.range) { normalizedScatterPositions(points, domain, style.range) }
    val density = LocalDensity.current
    val radii =
        remember(points, style.points, density) {
            with(density) {
                scatterPointRadii(
                    points = points,
                    pointRadius = style.points.size.toPx(),
                    minBubbleRadius = style.points.minSize.toPx(),
                    maxBubbleRadius = style.points.maxSize.toPx(),
                )
            }
        }
    ChartPlotLayout(
        plotAspectRatio = StyleDefaults.scatterPlotAspectRatio,
        modifier = modifier,
        title = {
            ChartHeading(
                chartTitle,
                style.chartContainerStyle.styleTitle,
                null,
                style.chartContainerStyle.styleSubtitle,
                modifier = style.chartContainerStyle.modifierTopTitle,
            )
        },
        legend = {
            Legend(
                chartContainerStyle = style.chartContainerStyle,
                style = style.legend,
                legend = data.series.map { it.name.orEmpty() }.toImmutableList(),
                colors = colors,
                labels =
                    data.series
                        .mapIndexed { seriesIndex, series ->
                            val selectedSize = selected?.takeIf { selectedSeriesIndex == seriesIndex }?.size
                            selectedSize?.let(sizeValueFormatter::format).orEmpty()
                        }.toImmutableList(),
            )
        },
        plot = {
            BoxWithConstraints(
                Modifier.fillMaxSize().padding(bottom = style.chartContainerStyle.contentPadding),
            ) {
                val padding =
                    with(density) {
                        scatterPlotPadding(
                            contentPadding = style.chartContainerStyle.contentPadding.toPx(),
                            pointRadii = radii,
                            selectionPadding = StyleDefaults.scatterSelectionPadding.toPx(),
                            selectionWidth = StyleDefaults.scatterSelectionWidth.toPx(),
                        )
                    }
                val showXLabels = style.axes.visible && style.axes.xLabels.visible
                val showYLabels = style.axes.visible && style.axes.yLabels.visible
                val xLabelCount = style.axes.xLabels.maxCount ?: StyleDefaults.yAxisMaxLabelCount
                val xLabels =
                    remember(domain, xLabelCount, xValueFormatter) {
                        scatterXAxisValues(domain, xLabelCount).map(xValueFormatter::format)
                    }
                val xLabelExtent =
                    if (showXLabels) {
                        estimateXAxisLabelExtent(
                            labels = xLabels,
                            dataSize = xLabels.size,
                            fontSizePx =
                                with(density) {
                                    style.axes.xLabels.size
                                        .toPx()
                                },
                        )
                    } else {
                        null
                    }
                val xAxisHeight = xLabelExtent?.let { with(density) { xAxisLabelRowHeightPx(it) } } ?: 0f
                val xAxisEdgeSpace =
                    xLabelExtent?.let {
                        xAxisLabelEdgeInsetPx(it, edgeSlackPx = 0f, availableWidthPx = constraints.maxWidth)
                    } ?: 0f
                val plotHeight = (constraints.maxHeight - padding - maxOf(padding, xAxisHeight)).coerceAtLeast(1f)
                val yAxisLayout =
                    rememberNumericYAxisLayout(
                        labels = style.axes.yLabels.copy(visible = showYLabels),
                        minValue = domain.minY,
                        maxValue = domain.maxY,
                        chartHeightPx = plotHeight,
                        verticalInsetPx = 0f,
                        formatter = yValueFormatter,
                        availableWidthPx = constraints.maxWidth,
                    )
                val layout =
                    scatterPlotLayout(
                        size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat()),
                        padding = padding,
                        yAxisSpace = yAxisLayout.widthPx + yAxisLayout.gapPx,
                        xAxisSpace = xAxisHeight,
                        xAxisEdgeSpace = xAxisEdgeSpace,
                    )
                val xAxisPlan =
                    rememberXAxisLabelPlan(
                        labels = xLabels,
                        dataSize = xLabels.size,
                        maxLabelCount = style.axes.xLabels.maxCount,
                        isScrollable = false,
                        unitWidthPx = layout.width / (xLabels.size - 1),
                        viewportWidthPx = layout.width,
                        fontSizePx =
                            with(density) {
                                style.axes.xLabels.size
                                    .toPx()
                            },
                        items = AxisXItems.Points,
                        scrollOffsetPx = 0f,
                    )
                val touchRadius = with(density) { StyleDefaults.scatterSelectionTouchRadius.toPx() }
                Canvas(
                    Modifier.fillMaxSize().onGloballyPositioned { show = true }.testTag(SCATTER_CHART_TAG).then(
                        if (interactionEnabled) {
                            Modifier
                                .pointerInput(positions, radii, layout, touchRadius, selection) {
                                    detectTapGestures { position ->
                                        val index =
                                            nearestScatterPointIndex(position, positions, radii, layout, touchRadius)
                                        if (index == NO_SELECTION) selection.clear() else selection.select(index)
                                    }
                                }.pointerInput(positions, radii, layout, touchRadius, selection) {
                                    detectDragGestures { change, _ ->
                                        val index =
                                            nearestScatterPointIndex(
                                                change.position,
                                                positions,
                                                radii,
                                                layout,
                                                touchRadius,
                                            )
                                        if (index == NO_SELECTION) selection.clear() else selection.select(index)
                                    }
                                }
                        } else {
                            Modifier
                        },
                    ),
                ) {
                    val right = layout.left + layout.width
                    val bottom = layout.top + layout.height
                    if (style.axes.visible && style.axes.grid.visible && style.axes.grid.steps > 0) {
                        val gridColor = style.axes.grid.color
                        val gridWidth =
                            style.axes.grid.lineWidth
                                .toPx()
                        xAxisPlan.ticks.forEach { tick ->
                            val x = layout.left + tick.centerX
                            drawLine(gridColor, Offset(x, layout.top), Offset(x, bottom), strokeWidth = gridWidth)
                        }
                        val gridPositions =
                            if (showYLabels) {
                                yAxisLayout.ticks.map { it.centerY }
                            } else {
                                val steps = style.axes.grid.steps
                                (0..steps).map { layout.height * it / steps }
                            }
                        gridPositions.forEach { position ->
                            val y = layout.top + position
                            drawLine(gridColor, Offset(layout.left, y), Offset(right, y), strokeWidth = gridWidth)
                        }
                    }
                    if (style.axes.visible && style.axes.lines.visible) {
                        val color = style.axes.lines.color
                        val lineWidth =
                            style.axes.lines.lineWidth
                                .toPx()
                        drawLine(
                            color,
                            Offset(layout.left, layout.top),
                            Offset(layout.left, bottom),
                            strokeWidth = lineWidth,
                        )
                        drawLine(color, Offset(layout.left, bottom), Offset(right, bottom), strokeWidth = lineWidth)
                    }
                    var pointIndex = 0
                    data.series.forEachIndexed { seriesIndex, series ->
                        series.points.forEach { _ ->
                            val radius = radii[pointIndex] * pointAnimation
                            val center = layout.position(positions[pointIndex])
                            drawCircle(colors[seriesIndex].copy(alpha = style.points.alpha), radius, center)
                            if (pointIndex == selectedIndex && pointAnimation > 0f) {
                                drawCircle(
                                    style.axes.lines.color,
                                    radius + StyleDefaults.scatterSelectionPadding.toPx() * pointAnimation,
                                    center,
                                    style.points.alpha,
                                    style = Stroke(StyleDefaults.scatterSelectionWidth.toPx() * pointAnimation),
                                )
                            }
                            pointIndex++
                        }
                    }
                }
                if (showYLabels) {
                    AxisYLabelsLayout(
                        ticks = yAxisLayout.ticks,
                        color = style.axes.yLabels.color,
                        fontSize = style.axes.yLabels.size,
                        modifier =
                            Modifier
                                .offset(y = with(density) { layout.top.toDp() })
                                .width(with(density) { yAxisLayout.widthPx.toDp() })
                                .height(with(density) { layout.height.toDp() })
                                .testTag(SCATTER_CHART_Y_AXIS_LABELS_TAG),
                    )
                }
                if (showXLabels) {
                    AxisXLabelsLayout(
                        ticks = xAxisPlan.ticks,
                        color = style.axes.xLabels.color,
                        fontSize = style.axes.xLabels.size,
                        modifier =
                            Modifier
                                .offset(
                                    x = with(density) { layout.left.toDp() },
                                    y = with(density) { (layout.top + layout.height).toDp() },
                                ).width(with(density) { layout.width.toDp() })
                                .height(with(density) { xAxisHeight.toDp() })
                                .testTag(SCATTER_CHART_X_AXIS_LABELS_TAG),
                    )
                }
            }
        },
    )
}
