package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: stacked-area-basic-usage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.stackedarea.StackedAreaChart

@Composable
fun ShowMonthlySubscribers() {
    val data =
        listOf(
            "Free Plan" to listOf(240.0, 380.0, 520.0, 610.0, 740.0, 880.0),
            "Standard Plan" to listOf(100.0, 210.0, 330.0, 450.0, 580.0, 700.0),
            "Premium Plan" to listOf(60.0, 140.0, 260.0, 380.0, 520.0, 660.0),
        ).toChartData(categories = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"))

    StackedAreaChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(300.dp),
        title = "Monthly Active Subscribers by Plan",
    )
}
// END WIKI EXAMPLE: stacked-area-basic-usage
