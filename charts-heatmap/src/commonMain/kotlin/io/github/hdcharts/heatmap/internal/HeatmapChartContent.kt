package io.github.hdcharts.heatmap.internal

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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.AnimationSpec
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.axis.AxisXItems
import io.github.hdcharts.core.internal.axis.AxisXLabelsLayout
import io.github.hdcharts.core.internal.axis.AxisYLabelsLayout
import io.github.hdcharts.core.internal.axis.AxisYLayoutTick
import io.github.hdcharts.core.internal.axis.estimateXAxisLabelExtent
import io.github.hdcharts.core.internal.axis.rememberXAxisLabelPlan
import io.github.hdcharts.core.internal.axis.xAxisLabelEdgeInsetPx
import io.github.hdcharts.core.internal.axis.xAxisLabelRowHeightPx
import io.github.hdcharts.core.internal.axis.yAxisLabelMinSpacingPx
import io.github.hdcharts.core.internal.composable.ChartHeading
import io.github.hdcharts.core.internal.composable.ChartPlotLayout
import io.github.hdcharts.core.internal.composable.rememberShowState
import io.github.hdcharts.core.internal.layout.modifierLegend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.heatmap.HeatmapChartStyle
import io.github.hdcharts.heatmap.HeatmapData
import io.github.hdcharts.heatmap.HeatmapRenderMode

internal const val HEATMAP_CHART_TAG = "HeatmapChart"
internal const val HEATMAP_CHART_X_AXIS_LABELS_TAG = "HeatmapChartXAxisLabels"
internal const val HEATMAP_CHART_Y_AXIS_LABELS_TAG = "HeatmapChartYAxisLabels"

// The shared Y-axis label layout keeps labels this far from the plot side of their column.
private val ROW_LABEL_EDGE_PADDING = 4.dp

@Composable
internal fun HeatmapChartContent(
    data: HeatmapData,
    modifier: Modifier,
    style: HeatmapChartStyle,
    title: String?,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    valueFormatter: ChartValueFormatter,
) {
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    val reveal by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = AnimationSpec.barChartSmooth(),
        label = "heatmapReveal",
    )
    val columnCount = data.columns.size
    val rowCount = data.rows.size
    val selectedIndex = selection.selectedIndex ?: NO_SELECTION
    val selectedValue = data.valueAtIndex(selectedIndex)
    val selectedRow = if (selectedValue != null) selectedIndex / columnCount else NO_SELECTION
    val selectedColumn = if (selectedValue != null) selectedIndex % columnCount else NO_SELECTION
    val chartTitle =
        buildString {
            if (!title.isNullOrBlank()) append(title)
            if (selectedValue != null) {
                if (isNotEmpty()) append(" · ")
                append(heatmapLabel(data.rows, selectedRow))
                append(", ")
                append(heatmapLabel(data.columns, selectedColumn))
                append(": ")
                append(valueFormatter.format(selectedValue))
            }
        }
    val mapping = remember(data, style.colorScale) { heatmapColorMapping(data, style.colorScale) }
    val selectionColor = StyleDefaults.selectionColor
    val selectionHaloColor = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    // The shared row label layout draws Material Text, which merges the ambient text style.
    val ambientTextStyle = LocalTextStyle.current

    ChartPlotLayout(
        plotAspectRatio =
            heatmapPlotAspectRatio(
                columnCount = columnCount,
                rowCount = rowCount,
                minRatio = StyleDefaults.heatmapMinPlotAspectRatio,
                maxRatio = StyleDefaults.heatmapMaxPlotAspectRatio,
            ),
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
            if (style.legend.visible) {
                HeatmapColorScaleLegend(
                    mapping = mapping,
                    valueFormatter = valueFormatter,
                    labelStyle = style.yLabels,
                    selectedValue = selectedValue,
                    markerColor = selectionColor,
                    modifier = style.chartContainerStyle.modifierLegend,
                )
            }
        },
        plot = {
            BoxWithConstraints(
                Modifier.fillMaxSize().padding(bottom = style.chartContainerStyle.contentPadding),
            ) {
                val padding = with(density) { style.chartContainerStyle.contentPadding.toPx() }
                val showXLabels = style.xLabels.visible
                val showYLabels = style.yLabels.visible
                val xFontPx = with(density) { style.xLabels.size.toPx() }
                val yFontPx = with(density) { style.yLabels.size.toPx() }
                val availableWidth = constraints.maxWidth

                // Row labels are words, not digits, so their width is measured rather than estimated.
                val yLabelWidth =
                    remember(
                        data.rows,
                        showYLabels,
                        style.yLabels.size,
                        availableWidth,
                        textMeasurer,
                        ambientTextStyle,
                        density,
                    ) {
                        if (showYLabels) {
                            val widest =
                                data.rows.indices.maxOfOrNull { row ->
                                    textMeasurer
                                        .measure(
                                            text = heatmapLabel(data.rows, row),
                                            style = ambientTextStyle.merge(TextStyle(fontSize = style.yLabels.size)),
                                            maxLines = 1,
                                        ).size.width
                                } ?: 0
                            heatmapRowLabelColumnWidth(
                                textWidthPx = widest.toFloat(),
                                edgePaddingPx = with(density) { ROW_LABEL_EDGE_PADDING.toPx() },
                                availableWidthPx = availableWidth,
                            )
                        } else {
                            0f
                        }
                    }
                val yLabelGap = if (showYLabels) with(density) { StyleDefaults.axisLabelPadding.toPx() } else 0f
                val xLabelExtent =
                    if (showXLabels) {
                        estimateXAxisLabelExtent(
                            labels = data.columns,
                            dataSize = columnCount,
                            fontSizePx = xFontPx,
                        )
                    } else {
                        null
                    }
                val xAxisHeight = xLabelExtent?.let { with(density) { xAxisLabelRowHeightPx(it) } } ?: 0f
                val leftSpace = maxOf(padding, yLabelWidth + yLabelGap)
                // The first and last labels center on half a cell, so that half cell is already slack.
                val provisionalCellWidth = (availableWidth - leftSpace - padding).coerceAtLeast(1f) / columnCount
                val xEdgeInset =
                    xLabelExtent?.let {
                        xAxisLabelEdgeInsetPx(
                            extent = it,
                            edgeSlackPx = provisionalCellWidth / 2f,
                            availableWidthPx = availableWidth,
                        )
                    } ?: 0f
                val layout =
                    heatmapPlotLayout(
                        width = availableWidth.toFloat(),
                        height = constraints.maxHeight.toFloat(),
                        leftSpace = maxOf(leftSpace, xEdgeInset),
                        topSpace = padding,
                        rightSpace = maxOf(padding, xEdgeInset),
                        bottomSpace = maxOf(padding, xAxisHeight),
                        columnCount = columnCount,
                        rowCount = rowCount,
                    )
                val xAxisPlan =
                    rememberXAxisLabelPlan(
                        labels = data.columns,
                        dataSize = columnCount,
                        maxLabelCount = style.xLabels.maxCount,
                        isScrollable = false,
                        unitWidthPx = layout.cellWidth,
                        viewportWidthPx = layout.width,
                        fontSizePx = xFontPx,
                        items = AxisXItems.Bars(layout.cellWidth),
                        scrollOffsetPx = 0f,
                    )
                val yTicks =
                    remember(data.rows, layout.cellHeight, yFontPx, style.yLabels.maxCount) {
                        val stride =
                            heatmapRowLabelStride(
                                rowCount = rowCount,
                                cellHeightPx = layout.cellHeight,
                                minSpacingPx = yAxisLabelMinSpacingPx(yFontPx),
                                maxCount = style.yLabels.maxCount,
                            )
                        (0 until rowCount step stride).map { row ->
                            AxisYLayoutTick(
                                label = heatmapLabel(data.rows, row),
                                centerY = (row + 0.5f) * layout.cellHeight,
                            )
                        }
                    }
                val strips =
                    remember(data, mapping, layout, style.renderMode) {
                        if (style.renderMode == HeatmapRenderMode.Interpolated) {
                            heatmapInterpolatedStrips(data, mapping, layout).map { strip ->
                                HeatmapStripBrush(
                                    top = strip.top,
                                    height = strip.height,
                                    brush =
                                        Brush.horizontalGradient(
                                            colorStops = strip.stops.toTypedArray(),
                                            startX = layout.left,
                                            endX = layout.right,
                                        ),
                                )
                            }
                        } else {
                            emptyList()
                        }
                    }
                val cellColors =
                    remember(data, mapping) {
                        data.values.map { row -> row.map { value -> value?.let(mapping::color) } }
                    }
                val spacingPx = with(density) { style.cellSpacing.toPx() }
                val selectionWidthPx = with(density) { StyleDefaults.heatmapSelectionWidth.toPx() }

                Canvas(
                    Modifier.fillMaxSize().onGloballyPositioned { show = true }.testTag(HEATMAP_CHART_TAG).then(
                        if (interactionEnabled) {
                            Modifier
                                .pointerInput(data, layout, selection) {
                                    detectTapGestures { position -> selectCellAt(data, layout, selection, position) }
                                }.pointerInput(data, layout, selection) {
                                    detectDragGestures { change, _ ->
                                        selectCellAt(data, layout, selection, change.position)
                                    }
                                }
                        } else {
                            Modifier
                        },
                    ),
                ) {
                    when (style.renderMode) {
                        HeatmapRenderMode.Cells ->
                            cellColors.forEachIndexed { row, colors ->
                                colors.forEachIndexed { column, color ->
                                    if (color != null) {
                                        val rect = heatmapCellRect(layout.cellBounds(row, column), spacingPx)
                                        drawRect(
                                            color = color,
                                            topLeft = rect.topLeft,
                                            size = rect.size,
                                            alpha = reveal,
                                        )
                                    }
                                }
                            }
                        HeatmapRenderMode.Interpolated ->
                            strips.forEach { strip ->
                                drawRect(
                                    brush = strip.brush,
                                    topLeft = Offset(layout.left, strip.top),
                                    size = Size(layout.width, strip.height),
                                    alpha = reveal,
                                )
                            }
                    }
                    if (selectedValue != null && reveal > 0f) {
                        val slot = layout.cellBounds(selectedRow, selectedColumn)
                        val cell =
                            if (style.renderMode == HeatmapRenderMode.Cells) heatmapCellRect(slot, spacingPx) else slot
                        val inset = selectionWidthPx / 2f
                        // A light halo inside the outline keeps it visible on dark and light cells.
                        drawRect(
                            color = selectionHaloColor,
                            topLeft = Offset(cell.left + inset * 2f, cell.top + inset * 2f),
                            size =
                                Size(
                                    (cell.width - inset * 4f).coerceAtLeast(0f),
                                    (cell.height - inset * 4f).coerceAtLeast(0f),
                                ),
                            alpha = reveal,
                            style = Stroke(width = selectionWidthPx / 2f),
                        )
                        drawRect(
                            color = selectionColor,
                            topLeft = Offset(cell.left + inset, cell.top + inset),
                            size =
                                Size(
                                    (cell.width - inset * 2f).coerceAtLeast(0f),
                                    (cell.height - inset * 2f).coerceAtLeast(0f),
                                ),
                            alpha = reveal,
                            style = Stroke(width = selectionWidthPx),
                        )
                    }
                }
                if (showYLabels) {
                    AxisYLabelsLayout(
                        ticks = yTicks,
                        color = style.yLabels.color,
                        fontSize = style.yLabels.size,
                        modifier =
                            Modifier
                                .offset(
                                    x =
                                        with(
                                            density,
                                        ) { (layout.left - yLabelGap - yLabelWidth).coerceAtLeast(0f).toDp() },
                                    y = with(density) { layout.top.toDp() },
                                ).width(with(density) { yLabelWidth.toDp() })
                                .height(with(density) { layout.height.toDp() })
                                .testTag(HEATMAP_CHART_Y_AXIS_LABELS_TAG),
                    )
                }
                if (showXLabels) {
                    AxisXLabelsLayout(
                        ticks = xAxisPlan.ticks,
                        color = style.xLabels.color,
                        fontSize = style.xLabels.size,
                        modifier =
                            Modifier
                                .offset(
                                    x = with(density) { layout.left.toDp() },
                                    y = with(density) { layout.bottom.toDp() },
                                ).width(with(density) { layout.width.toDp() })
                                .height(with(density) { xAxisHeight.toDp() })
                                .testTag(HEATMAP_CHART_X_AXIS_LABELS_TAG),
                    )
                }
            }
        },
    )
}

private class HeatmapStripBrush(
    val top: Float,
    val height: Float,
    val brush: Brush,
)

/** Selects the present cell under [position], or clears selection outside the grid or on a missing cell. */
private fun selectCellAt(
    data: HeatmapData,
    layout: HeatmapPlotLayout,
    selection: ChartSelection,
    position: Offset,
) {
    val index = layout.cellIndexAt(position)
    if (index == NO_SELECTION || data.valueAtIndex(index) == null) selection.clear() else selection.select(index)
}
