package io.github.hdcharts.app.demo.heatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.heatmap_interaction_enabled
import hdcharts.app.generated.resources.heatmap_render_cells
import hdcharts.app.generated.resources.heatmap_render_interpolated
import hdcharts.app.generated.resources.heatmap_select_busiest
import hdcharts.app.generated.resources.heatmap_selected_cell
import hdcharts.app.generated.resources.heatmap_selected_none
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.heatmap.HeatmapChart
import io.github.hdcharts.heatmap.HeatmapChartDefaults
import io.github.hdcharts.heatmap.HeatmapData
import io.github.hdcharts.heatmap.HeatmapRenderMode
import io.github.hdcharts.sampleshared.data.HeatmapSampleColorScales
import io.github.hdcharts.sampleshared.data.heatmapSampleUseCase
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Weekly sessions as discrete cells, with every style option left at its default. */
@Composable
fun HeatmapChartDemo() {
    val sample = remember { heatmapSampleUseCase().weeklyActivity() }
    ChartDemo(onRefresh = {}, refreshVisible = false) {
        HeatmapChart(data = sample.data, modifier = Modifier.fillMaxSize(), title = sample.title)
    }
}

/**
 * Regional CPU load on a fixed green-to-red scale, as cells or as a field interpolated from the same
 * values. Busy hours move across the day from region to region, so the field changes in both directions.
 */
@Composable
fun HeatmapRenderModesDemo() {
    val sample = remember { heatmapSampleUseCase().regionalCpuLoad() }
    var renderMode by rememberSaveable { mutableStateOf(HeatmapRenderMode.Interpolated) }
    ChartDemo(
        onRefresh = {},
        refreshVisible = false,
        controlsContent = {
            RenderModePicker(selected = renderMode, onSelected = { renderMode = it })
        },
    ) {
        HeatmapChart(
            data = sample.data,
            modifier = Modifier.fillMaxSize(),
            title = sample.title,
            style =
                HeatmapChartDefaults.style(
                    renderMode = renderMode,
                    colorScale = HeatmapSampleColorScales.cpuLoad,
                ),
            valueFormatter = { value -> "${value.roundToInt()}%" },
        )
    }
}

/** Tap or drag selection, a programmatic selection, and a switch to turn gestures off. */
@Composable
fun HeatmapSelectionDemo() {
    val sample = remember { heatmapSampleUseCase().weeklyActivity() }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var interactionEnabled by rememberSaveable { mutableStateOf(true) }
    val selection = rememberChartSelection(onSelectionChanged = { selectedIndex = it })
    val busiestIndex = remember(sample.data) { busiestCellIndex(sample.data) }

    ChartDemo(
        onRefresh = {},
        refreshVisible = false,
        controlsContent = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.sm),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs),
            ) {
                Text(
                    text = selectionSummary(sample.data, selectedIndex),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    FilterChip(
                        selected = selectedIndex == busiestIndex,
                        onClick = { selection.select(busiestIndex) },
                        label = { Text(stringResource(Res.string.heatmap_select_busiest)) },
                    )
                    FilterChip(
                        selected = interactionEnabled,
                        onClick = { interactionEnabled = !interactionEnabled },
                        label = { Text(stringResource(Res.string.heatmap_interaction_enabled)) },
                    )
                }
            }
        },
    ) {
        HeatmapChart(
            data = sample.data,
            modifier = Modifier.fillMaxSize(),
            title = sample.title,
            selection = selection,
            interactionEnabled = interactionEnabled,
        )
    }
}

/** Values below and above zero on a three-color scale centered on zero. */
@Composable
fun HeatmapDivergingDemo() {
    val sample = remember { heatmapSampleUseCase().temperatureAnomalies() }
    ChartDemo(onRefresh = {}, refreshVisible = false) {
        HeatmapChart(
            data = sample.data,
            modifier = Modifier.fillMaxSize(),
            title = sample.title,
            style =
                HeatmapChartDefaults.style(
                    colorScale = HeatmapSampleColorScales.temperatureAnomaly,
                    cellSpacing = 4.dp,
                ),
            valueFormatter = { value ->
                val rounded = kotlin.math.round(value * 10) / 10
                if (rounded > 0) "+$rounded °C" else "$rounded °C"
            },
        )
    }
}

@Composable
private fun RenderModePicker(
    selected: HeatmapRenderMode,
    onSelected: (HeatmapRenderMode) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = Dimens.sm).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
    ) {
        HeatmapRenderMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                label = { Text(stringResource(mode.label)) },
                modifier = Modifier.semantics { role = Role.RadioButton },
            )
        }
    }
}

private val HeatmapRenderMode.label: StringResource
    get() =
        when (this) {
            HeatmapRenderMode.Cells -> Res.string.heatmap_render_cells
            HeatmapRenderMode.Interpolated -> Res.string.heatmap_render_interpolated
        }

@Composable
private fun selectionSummary(
    data: HeatmapData,
    index: Int?,
): String {
    val columnCount = data.columns.size
    val value = index?.let { data.values.getOrNull(it / columnCount)?.getOrNull(it % columnCount) }
    if (index == null || value == null) return stringResource(Res.string.heatmap_selected_none)
    return stringResource(
        Res.string.heatmap_selected_cell,
        data.rows[index / columnCount],
        data.columns[index % columnCount],
        value.toInt(),
    )
}

private fun busiestCellIndex(data: HeatmapData): Int {
    var busiest = 0
    var busiestValue = Double.NEGATIVE_INFINITY
    data.values.forEachIndexed { row, values ->
        values.forEachIndexed { column, value ->
            if (value != null && value > busiestValue) {
                busiest = data.cellIndex(row, column)
                busiestValue = value
            }
        }
    }
    return busiest
}
