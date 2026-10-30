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

private data class EnergyUsage(
    val category: String,
    val consumption: Double,
)

class PieViewModel : ViewModel() {
    val title: String = TITLE

    val data: StateFlow<ChartData> = MutableStateFlow(buildData()).asStateFlow()

    private fun buildData(): ChartData {
        val energyUsage =
            listOf(
                EnergyUsage("Heating", 32.0),
                EnergyUsage("Cooling", 21.0),
                EnergyUsage("Appliances", 24.0),
                EnergyUsage("Water Heating", 14.0),
                EnergyUsage("Lighting", 9.0),
            )

        return energyUsage.toChartData(
            value = { it.consumption },
            category = { it.category },
        )
    }
}

@Composable
fun ShowPie(viewModel: PieViewModel = viewModel()) {
    val data by viewModel.data.collectAsStateWithLifecycle()

    PieChart(
        data = data,
        title = TITLE,
    )
}
