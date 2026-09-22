package io.github.hdcharts.app.gif.docs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.model.PieSlice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TITLE = "Household Energy"

class PieViewModel : ViewModel() {
    val slices: StateFlow<List<PieSlice>> = MutableStateFlow(buildSlices()).asStateFlow()

    private fun buildSlices(): List<PieSlice> =
        listOf(
            PieSlice(label = "Heating", value = 32.0),
            PieSlice(label = "Cooling", value = 21.0),
            PieSlice(label = "Appliances", value = 24.0),
            PieSlice(label = "Water Heating", value = 14.0),
            PieSlice(label = "Lighting", value = 9.0),
        )
}

@Composable
fun ShowPie(viewModel: PieViewModel = viewModel()) {
    val slices by viewModel.slices.collectAsStateWithLifecycle()

    PieChart(
        data = slices,
        title = TITLE,
    )
}
