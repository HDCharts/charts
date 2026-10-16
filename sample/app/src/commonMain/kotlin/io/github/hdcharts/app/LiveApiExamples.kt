package io.github.hdcharts.app

import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.example_earthquakes_summary
import hdcharts.app.generated.resources.example_earthquakes_title
import io.github.hdcharts.app.demo.earthquake.EarthquakeDashboard

internal fun liveApiRoute(example: ChartExample): String = "liveApi/${example.id}"

internal val EarthquakeDashboardExample =
    ChartExample(
        id = "earthquakes",
        title = Res.string.example_earthquakes_title,
        summary = Res.string.example_earthquakes_summary,
    ) { EarthquakeDashboard() }

internal val LiveApiExamples = listOf(EarthquakeDashboardExample)
