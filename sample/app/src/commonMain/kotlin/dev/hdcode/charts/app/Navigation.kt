package dev.hdcode.charts.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.bar_chart
import chartsproject.app.generated.resources.bar_stacked_chart
import chartsproject.app.generated.resources.histogram_chart
import chartsproject.app.generated.resources.line_chart
import chartsproject.app.generated.resources.multi_line_chart
import chartsproject.app.generated.resources.pie_chart
import chartsproject.app.generated.resources.radar_chart
import chartsproject.app.generated.resources.stacked_area_chart
import chartsproject.sample_shared.generated.resources.ic_bar_chart
import chartsproject.sample_shared.generated.resources.ic_histogram_chart
import chartsproject.sample_shared.generated.resources.ic_line_chart
import chartsproject.sample_shared.generated.resources.ic_multi_line_chart
import chartsproject.sample_shared.generated.resources.ic_pie_chart
import chartsproject.sample_shared.generated.resources.ic_radar_chart
import chartsproject.sample_shared.generated.resources.ic_stacked_bar_chart
import dev.hdcode.charts.app.demo.bar.BarChartDemo
import dev.hdcode.charts.app.demo.histogram.HistogramChartDemo
import dev.hdcode.charts.app.demo.line.LineChartDemo
import dev.hdcode.charts.app.demo.multiline.MultiLineChartDemo
import dev.hdcode.charts.app.demo.pie.PieChartDemo
import dev.hdcode.charts.app.demo.radar.RadarChartDemo
import dev.hdcode.charts.app.demo.stackedarea.StackedAreaChartDemo
import dev.hdcode.charts.app.demo.stackedbar.StackedBarChartDemo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import chartsproject.sample_shared.generated.resources.Res as SharedRes

sealed class ChartDestination(
    val route: String,
    val icon: DrawableResource,
    val title: StringResource,
) {
    object MainScreen {
        const val ROUTE = "main"
    }

    data object PieChartScreen :
        ChartDestination(
            route = "pieChart",
            icon = SharedRes.drawable.ic_pie_chart,
            title = Res.string.pie_chart,
        )

    data object LineChartScreen :
        ChartDestination(
            route = "lineChart",
            icon = SharedRes.drawable.ic_line_chart,
            title = Res.string.line_chart,
        )

    data object MultiLineChartScreen :
        ChartDestination(
            route = "multiLineChart",
            icon = SharedRes.drawable.ic_multi_line_chart,
            title = Res.string.multi_line_chart,
        )

    data object StackedAreaChartScreen :
        ChartDestination(
            route = "stackedAreaChart",
            icon = SharedRes.drawable.ic_stacked_bar_chart,
            title = Res.string.stacked_area_chart,
        )

    data object BarChartScreen :
        ChartDestination(
            route = "barChart",
            icon = SharedRes.drawable.ic_bar_chart,
            title = Res.string.bar_chart,
        )

    data object StackedBarChartScreen :
        ChartDestination(
            route = "stackedBarChart",
            icon = SharedRes.drawable.ic_stacked_bar_chart,
            title = Res.string.bar_stacked_chart,
        )

    data object HistogramChartScreen :
        ChartDestination(
            route = "histogramChart",
            icon = SharedRes.drawable.ic_histogram_chart,
            title = Res.string.histogram_chart,
        )

    data object RadarChartScreen :
        ChartDestination(
            route = "radarChart",
            icon = SharedRes.drawable.ic_radar_chart,
            title = Res.string.radar_chart,
        )
}

@Composable
fun Navigation(
    navController: NavHostController,
    menuState: MenuState,
    onChartSelected: (selected: ChartDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ChartDestination.MainScreen.ROUTE,
        modifier = modifier,
    ) {
        composable(ChartDestination.MainScreen.ROUTE) {
            MainScreenContent(
                menuState = menuState,
                onChartSelected = onChartSelected,
            )
        }
        composable(ChartDestination.PieChartScreen.route) {
            PieChartDemo()
        }

        composable(ChartDestination.LineChartScreen.route) {
            LineChartDemo()
        }

        composable(ChartDestination.MultiLineChartScreen.route) {
            MultiLineChartDemo()
        }

        composable(ChartDestination.StackedAreaChartScreen.route) {
            StackedAreaChartDemo()
        }

        composable(ChartDestination.BarChartScreen.route) {
            BarChartDemo()
        }

        composable(ChartDestination.StackedBarChartScreen.route) {
            StackedBarChartDemo()
        }

        composable(ChartDestination.HistogramChartScreen.route) {
            HistogramChartDemo()
        }

        composable(ChartDestination.RadarChartScreen.route) {
            RadarChartDemo()
        }
    }
}
