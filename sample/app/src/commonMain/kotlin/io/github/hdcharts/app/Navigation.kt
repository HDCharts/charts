package io.github.hdcharts.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.bar_chart
import hdcharts.app.generated.resources.bar_stacked_chart
import hdcharts.app.generated.resources.example_backlog_drain_summary
import hdcharts.app.generated.resources.example_backlog_drain_title
import hdcharts.app.generated.resources.example_defaults_summary
import hdcharts.app.generated.resources.example_defaults_title
import hdcharts.app.generated.resources.example_live_latency_summary
import hdcharts.app.generated.resources.example_live_latency_title
import hdcharts.app.generated.resources.example_live_percentiles_summary
import hdcharts.app.generated.resources.example_live_percentiles_title
import hdcharts.app.generated.resources.histogram_chart
import hdcharts.app.generated.resources.line_chart
import hdcharts.app.generated.resources.multi_line_chart
import hdcharts.app.generated.resources.pie_chart
import hdcharts.app.generated.resources.radar_chart
import hdcharts.app.generated.resources.ring_gauge_chart
import hdcharts.app.generated.resources.stacked_area_chart
import hdcharts.sample_shared.generated.resources.ic_bar_chart
import hdcharts.sample_shared.generated.resources.ic_histogram_chart
import hdcharts.sample_shared.generated.resources.ic_line_chart
import hdcharts.sample_shared.generated.resources.ic_multi_line_chart
import hdcharts.sample_shared.generated.resources.ic_pie_chart
import hdcharts.sample_shared.generated.resources.ic_radar_chart
import hdcharts.sample_shared.generated.resources.ic_ring_gauge_chart
import hdcharts.sample_shared.generated.resources.ic_stacked_bar_chart
import io.github.hdcharts.app.demo.bar.BarChartDemo
import io.github.hdcharts.app.demo.debug.DebugChartsScreen
import io.github.hdcharts.app.demo.gauge.RingGaugeChartDemo
import io.github.hdcharts.app.demo.histogram.HistogramChartDemo
import io.github.hdcharts.app.demo.line.LineChartDemo
import io.github.hdcharts.app.demo.line.LineScaleDropDemo
import io.github.hdcharts.app.demo.line.LiveLineChartDemo
import io.github.hdcharts.app.demo.multiline.LiveMultiLineChartDemo
import io.github.hdcharts.app.demo.multiline.MultiLineChartDemo
import io.github.hdcharts.app.demo.pie.PieChartDemo
import io.github.hdcharts.app.demo.radar.RadarChartDemo
import io.github.hdcharts.app.demo.stackedarea.StackedAreaChartDemo
import io.github.hdcharts.app.demo.stackedbar.StackedBarChartDemo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import hdcharts.sample_shared.generated.resources.Res as SharedRes

sealed class ChartDestination(
    val route: String,
    val icon: DrawableResource,
    val title: StringResource,
    val examples: List<ChartExample>,
) {
    fun exampleRoute(example: ChartExample): String = "$route/${example.id}"

    object MainScreen {
        const val ROUTE = "main"
    }

    object DebugChartsScreen {
        const val ROUTE = "debugCharts"
    }

    data object PieChartScreen :
        ChartDestination(
            route = "pieChart",
            icon = SharedRes.drawable.ic_pie_chart,
            title = Res.string.pie_chart,
            examples = listOf(defaultsExample { PieChartDemo() }),
        )

    data object LineChartScreen :
        ChartDestination(
            route = "lineChart",
            icon = SharedRes.drawable.ic_line_chart,
            title = Res.string.line_chart,
            examples =
                listOf(
                    defaultsExample { LineChartDemo() },
                    ChartExample(
                        id = "live-latency",
                        title = Res.string.example_live_latency_title,
                        summary = Res.string.example_live_latency_summary,
                    ) { LiveLineChartDemo() },
                    ChartExample(
                        id = "backlog-drain",
                        title = Res.string.example_backlog_drain_title,
                        summary = Res.string.example_backlog_drain_summary,
                    ) { LineScaleDropDemo() },
                ),
        )

    data object MultiLineChartScreen :
        ChartDestination(
            route = "multiLineChart",
            icon = SharedRes.drawable.ic_multi_line_chart,
            title = Res.string.multi_line_chart,
            examples =
                listOf(
                    defaultsExample { MultiLineChartDemo() },
                    ChartExample(
                        id = "live-percentiles",
                        title = Res.string.example_live_percentiles_title,
                        summary = Res.string.example_live_percentiles_summary,
                    ) { LiveMultiLineChartDemo() },
                ),
        )

    data object StackedAreaChartScreen :
        ChartDestination(
            route = "stackedAreaChart",
            icon = SharedRes.drawable.ic_stacked_bar_chart,
            title = Res.string.stacked_area_chart,
            examples = listOf(defaultsExample { StackedAreaChartDemo() }),
        )

    data object BarChartScreen :
        ChartDestination(
            route = "barChart",
            icon = SharedRes.drawable.ic_bar_chart,
            title = Res.string.bar_chart,
            examples = listOf(defaultsExample { BarChartDemo() }),
        )

    data object StackedBarChartScreen :
        ChartDestination(
            route = "stackedBarChart",
            icon = SharedRes.drawable.ic_stacked_bar_chart,
            title = Res.string.bar_stacked_chart,
            examples = listOf(defaultsExample { StackedBarChartDemo() }),
        )

    data object HistogramChartScreen :
        ChartDestination(
            route = "histogramChart",
            icon = SharedRes.drawable.ic_histogram_chart,
            title = Res.string.histogram_chart,
            examples = listOf(defaultsExample { HistogramChartDemo() }),
        )

    data object RadarChartScreen :
        ChartDestination(
            route = "radarChart",
            icon = SharedRes.drawable.ic_radar_chart,
            title = Res.string.radar_chart,
            examples = listOf(defaultsExample { RadarChartDemo() }),
        )

    data object RingGaugeChartScreen :
        ChartDestination(
            route = "ringGaugeChart",
            icon = SharedRes.drawable.ic_ring_gauge_chart,
            title = Res.string.ring_gauge_chart,
            examples = listOf(defaultsExample { RingGaugeChartDemo() }),
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

        menuState.menuItems.forEach { destination ->
            val singleExample = destination.examples.singleOrNull()
            if (singleExample != null) {
                composable(destination.route) {
                    singleExample.content()
                }
                return@forEach
            }

            composable(destination.route) {
                ChartExampleList(
                    examples = destination.examples,
                    onExampleSelected = { example ->
                        navController.navigate(destination.exampleRoute(example))
                    },
                )
            }

            destination.examples.forEach { example ->
                composable(destination.exampleRoute(example)) {
                    example.content()
                }
            }
        }

        composable(ChartDestination.DebugChartsScreen.ROUTE) {
            DebugChartsScreen()
        }
    }
}

private fun defaultsExample(content: @Composable () -> Unit): ChartExample =
    ChartExample(
        id = "defaults",
        title = Res.string.example_defaults_title,
        summary = Res.string.example_defaults_summary,
        content = content,
    )
