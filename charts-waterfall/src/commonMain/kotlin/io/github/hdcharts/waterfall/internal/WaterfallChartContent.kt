package io.github.hdcharts.waterfall.internal

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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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
import io.github.hdcharts.waterfall.WaterfallChartStyle
import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry
import kotlinx.collections.immutable.persistentListOf

internal const val WATERFALL_CHART_TAG = "WaterfallChart"
internal const val WATERFALL_X_LABELS_TAG = "WaterfallChartXAxisLabels"
internal const val WATERFALL_Y_LABELS_TAG = "WaterfallChartYAxisLabels"

@Composable
internal fun WaterfallChartContent(
    data: WaterfallData,
    bars: List<WaterfallBar>,
    modifier: Modifier,
    style: WaterfallChartStyle,
    title: String?,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    valueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
) {
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    val progress by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = AnimationSpec.barChartSmooth(),
        label = "waterfallReveal",
    )
    val selectedIndex = selection.selectedIndex?.takeIf { it in bars.indices }
    val chartTitle =
        buildString {
            if (!title.isNullOrBlank()) append(title)
            if (selectedIndex != null) {
                if (isNotEmpty()) append(" · ")
                val entry = data.entries[selectedIndex]
                append(entry.category)
                if (entry is WaterfallEntry.Change) append(" · Change: ${valueFormatter.format(entry.amount)}")
                append(" · Total: ${valueFormatter.format(bars[selectedIndex].end)}")
            }
        }
    val range = remember(bars, style.range) { waterfallRange(bars, style.range) }
    val labels = remember(data) { data.entries.map { it.category } }
    val colors =
        remember(data, style.bars) {
            data.entries.map { entry ->
                when (entry) {
                    is WaterfallEntry.Total -> style.bars.totalColor
                    is WaterfallEntry.Change ->
                        if (entry.amount <
                            0.0
                        ) {
                            style.bars.negativeColor
                        } else {
                            style.bars.positiveColor
                        }
                }
            }
        }
    val density = LocalDensity.current
    ChartPlotLayout(
        plotAspectRatio = 1.5f,
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
                legend = persistentListOf("Increase", "Decrease", "Total"),
                colors = persistentListOf(style.bars.positiveColor, style.bars.negativeColor, style.bars.totalColor),
                labels = persistentListOf("", "", ""),
            )
        },
        plot = {
            BoxWithConstraints(Modifier.fillMaxSize().padding(bottom = style.chartContainerStyle.contentPadding)) {
                val padding = with(density) { style.chartContainerStyle.contentPadding.toPx() }
                val showXLabels = style.axes.visible && style.axes.xLabels.visible
                val showYLabels = style.axes.visible && style.axes.yLabels.visible
                val xExtent =
                    if (showXLabels) {
                        estimateXAxisLabelExtent(
                            labels,
                            labels.size,
                            with(density) {
                                style.axes.xLabels.size
                                    .toPx()
                            },
                        )
                    } else {
                        null
                    }
                val xAxisHeight = xExtent?.let { with(density) { xAxisLabelRowHeightPx(it) } } ?: 0f
                val edgeSpace =
                    xExtent?.let {
                        xAxisLabelEdgeInsetPx(it, edgeSlackPx = 0f, availableWidthPx = constraints.maxWidth)
                    } ?: 0f
                val plotHeight = (constraints.maxHeight - padding - maxOf(padding, xAxisHeight)).coerceAtLeast(1f)
                val yAxis =
                    rememberNumericYAxisLayout(
                        labels = style.axes.yLabels.copy(visible = showYLabels),
                        minValue = range.first,
                        maxValue = range.second,
                        chartHeightPx = plotHeight,
                        verticalInsetPx = 0f,
                        formatter = axisValueFormatter,
                        availableWidthPx = constraints.maxWidth,
                    )
                val left = maxOf(padding, yAxis.widthPx + yAxis.gapPx, edgeSpace)
                val layout =
                    WaterfallPlotLayout(
                        left = left,
                        top = padding,
                        width = (constraints.maxWidth - left - maxOf(padding, edgeSpace)).coerceAtLeast(1f),
                        height = plotHeight,
                        count = bars.size,
                        space = with(density) { style.bars.space.toPx() },
                        maxBarWidth = with(density) { style.bars.maxBarWidth.toPx() },
                    )
                val xPlan =
                    rememberXAxisLabelPlan(
                        labels = labels,
                        dataSize = labels.size,
                        maxLabelCount = style.axes.xLabels.maxCount,
                        isScrollable = false,
                        unitWidthPx = layout.slotWidth,
                        viewportWidthPx = layout.width,
                        fontSizePx =
                            with(density) {
                                style.axes.xLabels.size
                                    .toPx()
                            },
                        items = AxisXItems.Bars(layout.slotWidth),
                        scrollOffsetPx = 0f,
                    )
                val bounds =
                    remember(layout, bars, range, progress) {
                        bars.mapIndexed { index, bar -> layout.bounds(index, bar, range, progress) }
                    }
                val currentBounds by rememberUpdatedState(bounds)
                val touchSlop = with(density) { 8.dp.toPx() }
                val selectAt: (Offset) -> Unit = { position ->
                    val index =
                        if (position.x in layout.left..layout.left + layout.width &&
                            position.y in layout.top..layout.top + layout.height
                        ) {
                            waterfallHitIndex(position, currentBounds, touchSlop)
                        } else {
                            NO_SELECTION
                        }
                    if (index == NO_SELECTION) selection.clear() else selection.select(index)
                }
                val currentSelectAt by rememberUpdatedState(selectAt)
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { show = true }
                        .testTag(WATERFALL_CHART_TAG)
                        .semantics { contentDescription = chartTitle.ifBlank { "Waterfall chart" } }
                        .then(
                            if (interactionEnabled) {
                                Modifier
                                    .pointerInput(data, layout, selection) {
                                        detectTapGestures { currentSelectAt(it) }
                                    }.pointerInput(data, layout, selection) {
                                        detectDragGestures { change, _ -> currentSelectAt(change.position) }
                                    }
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    val right = layout.left + layout.width
                    val bottom = layout.top + layout.height
                    clipRect(layout.left, layout.top, right, bottom) {
                        if (style.axes.visible && style.axes.grid.visible && style.axes.grid.steps > 0) {
                            val positions =
                                if (showYLabels) {
                                    yAxis.ticks.map { it.centerY }
                                } else {
                                    (0..style.axes.grid.steps).map { layout.height * it / style.axes.grid.steps }
                                }
                            positions.forEach { y ->
                                drawLine(
                                    style.axes.grid.color,
                                    Offset(left, layout.top + y),
                                    Offset(
                                        right,
                                        layout.top + y,
                                    ),
                                    style.axes.grid.lineWidth
                                        .toPx(),
                                )
                            }
                        }
                        if (style.axes.visible && style.axes.lines.visible) {
                            val zeroY = layout.y(0.0, range)
                            drawLine(
                                style.axes.lines.color,
                                Offset(left, layout.top),
                                Offset(left, bottom),
                                style.axes.lines.lineWidth
                                    .toPx(),
                            )
                            drawLine(
                                style.axes.lines.color,
                                Offset(left, zeroY),
                                Offset(right, zeroY),
                                style.axes.lines.lineWidth
                                    .toPx(),
                            )
                        }
                        if (style.connectors.visible) {
                            for (index in 0 until bars.lastIndex) {
                                val y = layout.y(bars[index].end * progress, range)
                                drawLine(
                                    style.connectors.color,
                                    Offset(bounds[index].right, y),
                                    Offset(bounds[index + 1].left, y),
                                    style.connectors.lineWidth.toPx(),
                                )
                            }
                        }
                        bounds.forEachIndexed { index, rect ->
                            val alpha =
                                if (selectedIndex != null &&
                                    selectedIndex != index &&
                                    style.selection.visible
                                ) {
                                    style.selection.unselectedAlpha
                                } else {
                                    1f
                                }
                            drawRect(colors[index], rect.topLeft, rect.size, alpha = alpha)
                            if (selectedIndex == index && style.selection.visible) {
                                drawRect(
                                    style.selection.color,
                                    rect.topLeft,
                                    rect.size,
                                    style = Stroke(style.selection.width.toPx()),
                                )
                            }
                        }
                    }
                }
                if (showYLabels) {
                    AxisYLabelsLayout(
                        ticks = yAxis.ticks,
                        color = style.axes.yLabels.color,
                        fontSize = style.axes.yLabels.size,
                        modifier =
                            Modifier
                                .offset(y = with(density) { layout.top.toDp() })
                                .width(with(density) { yAxis.widthPx.toDp() })
                                .height(with(density) { layout.height.toDp() })
                                .testTag(WATERFALL_Y_LABELS_TAG),
                    )
                }
                if (showXLabels) {
                    AxisXLabelsLayout(
                        ticks = xPlan.ticks,
                        color = style.axes.xLabels.color,
                        fontSize = style.axes.xLabels.size,
                        modifier =
                            Modifier
                                .offset(
                                    x = with(density) { layout.left.toDp() },
                                    y = with(density) { (layout.top + layout.height).toDp() },
                                ).width(with(density) { layout.width.toDp() })
                                .height(with(density) { xAxisHeight.toDp() })
                                .testTag(WATERFALL_X_LABELS_TAG),
                    )
                }
            }
        },
    )
}
