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
import hdcharts.app.generated.resources.example_bar_gradients_summary
import hdcharts.app.generated.resources.example_defaults_summary
import hdcharts.app.generated.resources.example_defaults_title
import hdcharts.app.generated.resources.example_gradients_title
import hdcharts.app.generated.resources.example_histogram_gradients_summary
import hdcharts.app.generated.resources.example_line_gradients_summary
import hdcharts.app.generated.resources.example_live_gradients_summary
import hdcharts.app.generated.resources.example_live_gradients_title
import hdcharts.app.generated.resources.example_live_latency_summary
import hdcharts.app.generated.resources.example_live_latency_title
import hdcharts.app.generated.resources.example_live_percentiles_summary
import hdcharts.app.generated.resources.example_live_percentiles_title
import hdcharts.app.generated.resources.example_stacked_gradients_summary
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
import io.github.hdcharts.app.demo.bar.BarGradientDemo
import io.github.hdcharts.app.demo.debug.DebugChartsScreen
import io.github.hdcharts.app.demo.gauge.RingGaugeChartDemo
import io.github.hdcharts.app.demo.histogram.HistogramChartDemo
import io.github.hdcharts.app.demo.histogram.HistogramGradientDemo
import io.github.hdcharts.app.demo.line.LineChartDemo
import io.github.hdcharts.app.demo.line.LineGradientDemo
import io.github.hdcharts.app.demo.line.LineScaleDropDemo
import io.github.hdcharts.app.demo.line.LiveLineChartDemo
import io.github.hdcharts.app.demo.line.LiveLineGradientDemo
import io.github.hdcharts.app.demo.multiline.LiveMultiLineChartDemo
import io.github.hdcharts.app.demo.multiline.MultiLineChartDemo
import io.github.hdcharts.app.demo.pie.PieChartDemo
import io.github.hdcharts.app.demo.radar.RadarChartDemo
import io.github.hdcharts.app.demo.stackedarea.StackedAreaChartDemo
import io.github.hdcharts.app.demo.stackedarea.StackedAreaGradientDemo
import io.github.hdcharts.app.demo.stackedbar.StackedBarChartDemo
import io.github.hdcharts.app.demo.stackedbar.StackedBarGradientDemo
import io.github.hdcharts.app.library.BuildConfig
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
                    gradientsExample(summary = Res.string.example_line_gradients_summary) { LineGradientDemo() },
                    ChartExample(
                        id = "live-latency",
                        title = Res.string.example_live_latency_title,
                        summary = Res.string.example_live_latency_summary,
                    ) { LiveLineChartDemo() },
                    ChartExample(
                        id = "live-gradients",
                        title = Res.string.example_live_gradients_title,
                        summary = Res.string.example_live_gradients_summary,
                    ) { LiveLineGradientDemo() },
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
            examples =
                listOf(
                    defaultsExample { StackedAreaChartDemo() },
                    gradientsExample(
                        summary = Res.string.example_stacked_gradients_summary,
                    ) { StackedAreaGradientDemo() },
                ),
        )

    data object BarChartScreen :
        ChartDestination(
            route = "barChart",
            icon = SharedRes.drawable.ic_bar_chart,
            title = Res.string.bar_chart,
            examples =
                listOf(
                    defaultsExample { BarChartDemo() },
                    gradientsExample(summary = Res.string.example_bar_gradients_summary) { BarGradientDemo() },
                ),
        )

    data object StackedBarChartScreen :
        ChartDestination(
            route = "stackedBarChart",
            icon = SharedRes.drawable.ic_stacked_bar_chart,
            title = Res.string.bar_stacked_chart,
            examples =
                listOf(
                    defaultsExample { StackedBarChartDemo() },
                    gradientsExample(
                        summary = Res.string.example_stacked_gradients_summary,
                    ) { StackedBarGradientDemo() },
                ),
        )

    data object HistogramChartScreen :
        ChartDestination(
            route = "histogramChart",
            icon = SharedRes.drawable.ic_histogram_chart,
            title = Res.string.histogram_chart,
            examples =
                listOf(
                    defaultsExample { HistogramChartDemo() },
                    gradientsExample(
                        summary = Res.string.example_histogram_gradients_summary,
                    ) { HistogramGradientDemo() },
                ),
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
                liveApiExamples = LiveApiExamples,
                onLiveApiExampleSelected = { example -> navController.navigate(liveApiRoute(example)) },
            )
        }

        LiveApiExamples.forEach { example ->
            composable(liveApiRoute(example)) {
                example.content()
            }
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

        if (BuildConfig.SHOW_DEBUG_CHARTS) {
            composable(ChartDestination.DebugChartsScreen.ROUTE) {
                DebugChartsScreen()
            }
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

private fun gradientsExample(
    summary: StringResource,
    content: @Composable () -> Unit,
): ChartExample =
    ChartExample(
        id = "gradients",
        title = Res.string.example_gradients_title,
        summary = summary,
        content = content,
    )
