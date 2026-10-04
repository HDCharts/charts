package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.pie.PieChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Household Energy"

class PieViewModel : ViewModel() {
    val title: String = TITLE

    val data: StateFlow<ChartData> = MutableStateFlow(buildData()).asStateFlow()

    private fun buildData(): ChartData =
        listOf(32.0, 21.0, 24.0, 14.0, 9.0).toChartData(
            categories = listOf("Heating", "Cooling", "Appliances", "Water Heating", "Lighting"),
        )
}

@Composable
fun ShowPie(viewModel: PieViewModel = viewModel()) {
    val data by viewModel.data.collectAsStateWithLifecycle()

    PieChart(
        data = data,
        title = TITLE,
    )
}
