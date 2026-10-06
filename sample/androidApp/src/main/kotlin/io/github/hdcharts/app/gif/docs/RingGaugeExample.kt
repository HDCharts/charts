package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.gauge.RingGaugeChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Quarterly Targets"

class RingGaugeViewModel : ViewModel() {
    val title: String = TITLE

    val valueFormatter: ChartValueFormatter = ChartValueFormatters.suffix("%")

    val data: StateFlow<ChartData> = MutableStateFlow(buildData()).asStateFlow()

    private fun buildData(): ChartData =
        listOf(62.0, 39.0, 25.0).toChartData(
            categories = listOf("Revenue", "Signups", "Retention"),
        )
}

@Composable
fun ShowRingGauge(viewModel: RingGaugeViewModel = viewModel()) {
    val data by viewModel.data.collectAsStateWithLifecycle()

    RingGaugeChart(
        data = data,
        title = TITLE,
        valueFormatter = viewModel.valueFormatter,
    )
}
