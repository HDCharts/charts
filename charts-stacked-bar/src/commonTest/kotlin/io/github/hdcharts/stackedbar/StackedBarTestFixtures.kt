package io.github.hdcharts.stackedbar

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf

private val DENSE_SEGMENT_NAMES = listOf("S1", "S2", "S3", "S4")

internal fun denseStackedBarDataSet(bars: Int = 120): ChartData =
    transposeStackedBars(
        rows =
            List(bars) { index ->
                "Bar ${index + 1}" to
                    listOf(
                        50f + (index % 9),
                        30f + (index % 7),
                        20f + (index % 5),
                        10f + (index % 3),
                    )
            },
        segmentNames = DENSE_SEGMENT_NAMES,
    )

internal fun transposeStackedBars(
    rows: List<Pair<String, List<Float>>>,
    segmentNames: List<String>,
): ChartData =
    chartDataOf(
        categories = rows.map { (barLabel, _) -> barLabel },
        *segmentNames
            .mapIndexed { segmentIndex, segmentName ->
                ChartSeries(
                    name = segmentName,
                    values =
                        rows.map { (_, values) ->
                            values.getOrNull(segmentIndex)?.toDouble() ?: Double.NaN
                        },
                )
            }.toTypedArray(),
    )
