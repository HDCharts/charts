package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.sampleshared.data.ScatterSampleData
import io.github.hdcharts.sampleshared.data.ScatterSampleUseCase
import io.github.hdcharts.scatter.ScatterData
import io.github.hdcharts.scatter.ScatterPoint
import io.github.hdcharts.scatter.ScatterSeries

internal class DefaultScatterSampleUseCase : ScatterSampleUseCase {
    override fun deterministic(bubbles: Boolean): ScatterSampleData {
        val series =
            listOf(
                ScatterSeries(
                    name = "North",
                    points =
                        listOf(
                            ScatterPoint(1.0, 2.4, size = 0.0),
                            ScatterPoint(2.5, 3.8, size = 24.0),
                            ScatterPoint(4.0, 3.1, size = 18.0),
                            ScatterPoint(3.0, 2.1, size = 12.0),
                        ),
                ),
                ScatterSeries(
                    name = "South",
                    points =
                        listOf(
                            ScatterPoint(1.5, 1.8, size = 20.0),
                            ScatterPoint(3.0, 2.7, size = 10.0),
                            ScatterPoint(4.5, 4.2, size = 28.0),
                        ),
                ),
            )
        val data =
            if (bubbles) {
                series
            } else {
                series.map { item ->
                    ScatterSeries(name = item.name, points = item.points.map { it.copy(size = null) })
                }
            }
        return ScatterSampleData(data = ScatterData(data), title = "Regional growth")
    }
}
