package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.gauge.SegmentedRingGaugeChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Daily Nutrition"
private const val CAPACITY = 2_000.0
private const val CENTER_SUPPORTING_LABEL = "of 2000 kcal"

class SegmentedRingGaugeViewModel : ViewModel() {
    val data: StateFlow<ChartData> = MutableStateFlow(buildData()).asStateFlow()

    private fun buildData(): ChartData =
        listOf(240.0, 180.0, 320.0).toChartData(
            categories = listOf("Carbs", "Protein", "Fat"),
        )
}

@Composable
fun ShowSegmentedRingGauge(viewModel: SegmentedRingGaugeViewModel = viewModel()) {
    val data by viewModel.data.collectAsStateWithLifecycle()

    SegmentedRingGaugeChart(
        data = data,
        capacity = CAPACITY,
        title = TITLE,
        centerSupportingLabel = CENTER_SUPPORTING_LABEL,
    )
}
