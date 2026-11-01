package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: pie-basic-usage
import androidx.compose.runtime.Composable
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.pie.PieChart

internal data class EnergyUsage(
    val category: String,
    val consumption: Double,
) {
    companion object {
        val sample =
            listOf(
                EnergyUsage("Heating", 32.0),
                EnergyUsage("Cooling", 21.0),
                EnergyUsage("Appliances", 24.0),
                EnergyUsage("Water Heating", 14.0),
                EnergyUsage("Lighting", 9.0),
            )
    }
}

@Composable
fun ShowPie() {
    val energyUsage = EnergyUsage.sample
    val data =
        energyUsage.toChartData(
            value = { it.consumption },
            category = { it.category },
        )

    PieChart(data = data, title = "Household Energy")
}
// END WIKI EXAMPLE: pie-basic-usage
