package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: waterfall-basic-usage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.waterfall.WaterfallChart
import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry.Change
import io.github.hdcharts.waterfall.WaterfallEntry.Total

val monthlyCashBalance =
    WaterfallData(
        listOf(
            Change("Opening", 50.0),
            Change("Sales", 120.0),
            Change("Supplies", -65.0),
            Total("Subtotal"),
            Change("Rent", -40.0),
            Total("Closing"),
        ),
    )

@Composable
fun ShowMonthlyCashBalance(modifier: Modifier = Modifier) {
    WaterfallChart(data = monthlyCashBalance, modifier = modifier, title = "Monthly cash balance")
}
// END WIKI EXAMPLE: waterfall-basic-usage
