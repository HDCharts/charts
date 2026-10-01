package io.github.hdcharts.app.demo.debug

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.pie.PieChart
import io.github.hdcharts.pie.PieChartDefaults
import io.github.hdcharts.pie.PieSlice
import io.github.hdcharts.radar.RadarChart
import io.github.hdcharts.sampleshared.theme.Dimens

/**
 * Scratch screen for checking chart edge cases by hand. Add a section per case.
 */
@Composable
fun DebugChartsScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        HeightOnlyPieCase()
        HeightOnlyRadarCase()
    }
}

/** Height set, no width, legend hidden: a selected label wider than the plot widens the chart. */
@Composable
private fun HeightOnlyPieCase() {
    val selection = rememberChartSelection()
    CaseTitle("Pie: height only, legend hidden")
    ToggleSelectionButton(selection)
    PieChart(
        data =
            listOf(
                PieSlice(label = "A slice label wider than the plot", value = 60.0),
                PieSlice(label = "B", value = 40.0),
            ),
        modifier = Modifier.height(160.dp).debugBounds(),
        title = "Pie",
        selection = selection,
        style = PieChartDefaults.style(legend = PieChartDefaults.legend(visible = false)),
    )
}

/** Height set, no width, legend hidden: selecting an axis shows the category legend. */
@Composable
private fun HeightOnlyRadarCase() {
    val selection = rememberChartSelection()
    CaseTitle("Radar: height only, legend hidden")
    ToggleSelectionButton(selection)
    RadarChart(
        data =
            chartDataOf(
                categories = listOf("First long category", "Second long category", "Third long category"),
                ChartSeries(name = "Series", values = listOf(1.0, 2.0, 3.0)),
            ),
        modifier = Modifier.height(300.dp).debugBounds(),
        title = "Radar",
        selection = selection,
    )
}

@Composable
private fun CaseTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ToggleSelectionButton(selection: ChartSelection) {
    Button(onClick = { if (selection.selectedIndex == null) selection.select(0) else selection.clear() }) {
        Text("Toggle selection")
    }
}

/** Outlines the chart's bounds so width changes are easy to see. */
@Composable
private fun Modifier.debugBounds(): Modifier = border(1.dp, MaterialTheme.colorScheme.outline)
